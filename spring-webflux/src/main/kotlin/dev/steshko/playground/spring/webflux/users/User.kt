package dev.steshko.playground.spring.webflux.users

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("users")
data class User(
    @Id
    @Column("user_id") val userId: Long? = null,
    @Column("user_name") val userName: String
)
