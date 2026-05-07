package com.example.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "users")
class UserJpaEntity(
    @Id
    var id: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true, length = 255)
    var email: String = "",

    @Column(name = "password_hash", nullable = false, length = 255)
    var passwordHash: String = "",

    @Column(nullable = false, length = 255)
    var name: String = "",
)
