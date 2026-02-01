package dev.steshko.playground.spring.webflux.notes

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface NotesRepository : CoroutineCrudRepository<Note, Long>