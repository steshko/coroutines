package dev.steshko.playground.spring.webflux.notes

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("notes")
data class Note(
    @Id val id: Long? = null,
    @Column("user_id") val userId: Long,
    val title: String,
    @CreatedDate
    @Column("created_time") val createdTime: LocalDateTime? = null,
    val content: String
)
