package dev.steshko.playground.spring.webflux.notes

import kotlinx.coroutines.flow.toList
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/notes")
class NotesController(
    private val notesRepository: NotesRepository,
) {
    @PostMapping
    suspend fun saveNote(
        @RequestBody note: Note
    ) = notesRepository.save(note)

    @GetMapping
    suspend fun getNotes() = notesRepository.findAll().toList()
}