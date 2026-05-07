package com.example.infrastructure.persistence

import com.example.domain.User
import com.example.domain.UserId
import com.example.domain.UserRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository

@Repository
@ConditionalOnProperty(name = ["storage.type"], havingValue = "supabase")
class JpaUserRepository(
    private val springRepo: UserSpringDataRepository,
) : UserRepository {

    override fun save(user: User): User {
        springRepo.save(user.toEntity())
        return user
    }

    override fun findByEmail(email: String): User? =
        springRepo.findByEmail(email)?.toDomain()

    override fun findById(id: UserId): User? =
        springRepo.findById(id.value).orElse(null)?.toDomain()

    private fun User.toEntity() = UserJpaEntity(
        id = id.value,
        email = email,
        passwordHash = passwordHash,
        name = name,
    )

    private fun UserJpaEntity.toDomain() = User(
        id = UserId(id),
        email = email,
        passwordHash = passwordHash,
        name = name,
    )
}
