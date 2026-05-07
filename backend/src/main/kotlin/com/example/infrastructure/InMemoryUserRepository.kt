package com.example.infrastructure

import com.example.domain.User
import com.example.domain.UserId
import com.example.domain.UserRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
@ConditionalOnProperty(name = ["storage.type"], havingValue = "memory", matchIfMissing = true)
class InMemoryUserRepository : UserRepository {

    private val byId = ConcurrentHashMap<UserId, User>()
    private val byEmail = ConcurrentHashMap<String, User>()

    override fun save(user: User): User {
        byId[user.id] = user
        byEmail[user.email] = user
        return user
    }

    override fun findByEmail(email: String): User? = byEmail[email]

    override fun findById(id: UserId): User? = byId[id]
}
