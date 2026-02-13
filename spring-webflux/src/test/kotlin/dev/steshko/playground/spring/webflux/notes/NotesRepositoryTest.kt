package dev.steshko.playground.spring.webflux.notes

import dev.steshko.playground.spring.webflux.AbstractIntegrationTest
import dev.steshko.playground.spring.webflux.users.User
import dev.steshko.playground.spring.webflux.users.UserRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Order
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NotesRepositoryTest(
    private val userRepository: UserRepository,
    private val notesRepository: NotesRepository
) : AbstractIntegrationTest() {
    @Test
    @Order(1)
    fun `should get notes when there is a test db`() = runTest {
        val notes = notesRepository.findAll().toList()
        assertTrue { notes.isEmpty() }
    }

    @Test
    @Order(2)
    fun `should insert notes into db`() = runTest {
        val user = userRepository.save(User(userName = "test"))
        (0..3).map {
            Note(userId = user.userId!!, title = "title $it", content = "content$it")
        }.run(notesRepository::saveAll).collect()

        assertEquals(
            expected = 4,
            actual = notesRepository.findAll().toList().size
        )
    }
}