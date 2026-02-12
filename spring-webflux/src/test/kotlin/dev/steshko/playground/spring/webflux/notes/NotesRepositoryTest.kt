package dev.steshko.playground.spring.webflux.notes

import dev.steshko.playground.spring.webflux.users.User
import dev.steshko.playground.spring.webflux.users.UserRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.TestConstructor.AutowireMode.ALL
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
@Testcontainers
@TestConstructor(autowireMode = ALL)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class NotesRepositoryTest(
    private val userRepository: UserRepository,
    private val notesRepository: NotesRepository,
) {
    companion object {
        @Container
        @ServiceConnection
        val postgres = PostgreSQLContainer("postgres:18")
    }

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