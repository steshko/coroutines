package dev.steshko.playground.reactive

import kotlin.math.min

typealias OnNext<T> = (T) -> Unit
typealias OnError = (Throwable) -> Unit
typealias OnComplete = () -> Unit
typealias OnSubscribe<T> = (
    onNext: OnNext<T>,
    onError: OnError?,
    onComplete: OnComplete?,
    onSubscribe: ((MySubscription) -> Unit)?
) -> Unit

interface MyProducer<T> {
    var onSubscribe: OnSubscribe<T>

    fun subscribe(
        onNext: OnNext<T>
    ) {
        onSubscribe(onNext, null, null, null)
    }
    fun subscribe(
        onNext: OnNext<T>,
        onError: OnError? = null,
        onComplete: OnComplete? = null,
        onSubscribe: ((MySubscription) -> Unit)? = null
    ) {
        onSubscribe(onNext, onError, onComplete, onSubscribe)
    }

    fun <S> map(block: (T) -> S): MyProducer<S>
    fun filter(block: (T) -> Boolean): MyProducer<T>
    fun take(num: Int): MyProducer<T>
    fun skip(num: Int): MyProducer<T>

    fun <S> flatMap(block: (T) -> MyProducer<S>) = MyFlux<S> { onNext, onError, onComplete, onSubscribe ->
        subscribe(onNext = {
            block(it).subscribe(onNext, onError)
        }, onError = onError, onComplete = onComplete)
    }
}

interface MySubscription {
    fun request(n: Long)
    fun cancel()
}

class MyMono<T>(
    override var onSubscribe: OnSubscribe<T>
) : MyProducer<T> {
    override fun <S> map(block: (T) -> S) = MyMono<S> { onNext, onError, onComplete, onSubscribe ->
        subscribe(onNext = {
            onNext(block(it))
        }, onError = onError, onComplete = onComplete, onSubscribe = { originalSubscription ->
            val newSubscription = object : MySubscription {
                override fun request(n: Long) = originalSubscription.request(n)
                override fun cancel() = originalSubscription.cancel()
            }
            onSubscribe?.invoke(newSubscription) ?: newSubscription.request(Long.MAX_VALUE)
        })
    }

    override fun filter(block: (T) -> Boolean) = MyMono { onNext, onError, onComplete, onSubscribe ->
        subscribe(onNext = {
            if (block(it)) onNext(it)
        }, onError = onError, onComplete = onComplete, onSubscribe = onSubscribe)
    }

    override fun take(num: Int) = MyMono { onNext, onError, onComplete, _ ->
        var processed = 0
        subscribe(onNext = {
            if (processed++ < num) { onNext(it) }
        }, onError = onError, onComplete = onComplete)
    }

    override fun skip(num: Int) = MyMono { onNext, onError, onComplete, _ ->
        var processed = 0
        subscribe(onNext = {
            if (processed++ >= num) { onNext(it) }
        }, onError = onError, onComplete = onComplete)
    }
    fun <S> flatMap(block: (T) -> MyMono<S>) = MyMono { onNext, onError, onComplete, _ ->
        subscribe(onNext = {
            block(it).subscribe(onNext, onError, null)
        }, onError = onError, onComplete = onComplete)
    }

    companion object {
        fun <T> just(result: T): MyMono<T> {
            return MyMono { onNext, onError, onComplete, onSubscribe ->
                val subscription = object : MySubscription {
                    var emitted = false

                    override fun request(n: Long) {
                        if (n > 0 && !emitted) {
                            emitted = true
                            try {
                                onNext(result)
                            } catch (t: Throwable) {
                                if (onError != null) {
                                    onError(t)
                                    return
                                } else throw t
                            }
                            onComplete?.invoke()
                        }
                    }

                    override fun cancel() { TODO("Not yet implemented") }
                }
                onSubscribe?.invoke(subscription) ?: subscription.request(Long.MAX_VALUE)
            }
        }
    }
}

class MyFlux<T>(
    override var onSubscribe: OnSubscribe<T>
) : MyProducer<T> {
    override fun <S> map(block: (T) -> S) = MyFlux<S> { onNext, onError, onComplete, onSubscribe ->
        subscribe(onNext = {
            onNext(block(it))
        }, onError = onError, onComplete = onComplete, onSubscribe = { originalSubscription ->
            val newSubscription = object : MySubscription {
                override fun request(n: Long) = originalSubscription.request(n)
                override fun cancel() = originalSubscription.cancel()
            }
            onSubscribe?.invoke(newSubscription) ?: newSubscription.request(Long.MAX_VALUE)
        })
    }

    override fun filter(block: (T) -> Boolean) = MyFlux { onNext, onError, onComplete, onSubscribe ->
        var totalRequested = 0L
        var totalDelivered = 0L
        var requestedFromSource = 0L
        var triedDeliver = 0L
        lateinit var originalSubscription: MySubscription

        subscribe(
            onNext = { value ->
                triedDeliver++
                if (block(value)) {
                    onNext(value)
                    totalDelivered++
                }

                val stillNeeded = totalRequested - totalDelivered
                val alreadyRequested = requestedFromSource - triedDeliver

                if (stillNeeded > 0 && alreadyRequested == 0L) {
                    originalSubscription.request(stillNeeded)
                    requestedFromSource += stillNeeded
                }
            },
            onError = onError,
            onComplete = onComplete,
            onSubscribe = { sub ->
                originalSubscription = sub

                val filterSub = object : MySubscription {
                    override fun request(n: Long) {
                        totalRequested += n
                        requestedFromSource += n
                        originalSubscription.request(n)
                    }
                    override fun cancel() = originalSubscription.cancel()
                }
                onSubscribe?.invoke(filterSub) ?: filterSub.request(Long.MAX_VALUE)
            }
        )
    }

    override fun take(num: Int) = MyFlux { onNext, onError, onComplete, _ ->
        var processed = 0
        subscribe(onNext = {
            if (processed++ < num) { onNext(it) }
        }, onError = onError, onComplete = onComplete)
    }

    override fun skip(num: Int) = MyFlux { onNext, onError, onComplete, _ ->
        var processed = 0
        subscribe(onNext = {
            if (processed++ >= num) { onNext(it) }
        }, onError = onError, onComplete = onComplete)
    }

    companion object {
        fun <T> just(vararg values: T): MyFlux<T> {
            return MyFlux { onNext, onError, onComplete, onSubscribe ->
                val subscription = object : MySubscription {
                    var curIndex = 0
                    override fun request(n: Long) {
                        try {
                            val steps = if (n == Long.MAX_VALUE) {
                                (values.size - curIndex).toLong()
                            } else {
                                min(n, (values.size - curIndex).toLong())
                            }
                            val currentIndex = curIndex
                            curIndex += steps.toInt()
                            for (i in currentIndex until currentIndex + steps) {
                                onNext(values[i.toInt()])
                            }

                        } catch (t: Throwable) {
                            if (onError != null) {
                                onError(t)
                                return
                            } else throw t
                        }

                        if (curIndex >= values.size) {
                            onComplete?.invoke()
                        }
                    }

                    override fun cancel() { TODO("Not yet implemented") }
                }
                onSubscribe?.invoke(subscription) ?: subscription.request(Long.MAX_VALUE)
            }
        }
    }
}
