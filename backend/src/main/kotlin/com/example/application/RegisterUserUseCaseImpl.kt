package com.example.application

import com.example.domain.DuplicateUserException
import com.example.domain.User
import com.example.domain.UserId
import com.example.domain.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class RegisterUserUseCaseImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenProvider: TokenProvider,
) : RegisterUserUseCase {
    override fun execute(input: RegisterUserInput): RegisterUserOutput {
        if (userRepository.findByEmail(input.email) != null) throw DuplicateUserException(input.email)
        val user = User(
            id = UserId.generate(),
            email = input.email,
            passwordHash = passwordEncoder.encode(input.password),
            name = input.name,
        )
        val saved = userRepository.save(user)
        val token = tokenProvider.generateToken(saved.id.value.toString())
        return RegisterUserOutput(userId = saved.id.value.toString(), email = saved.email, name = saved.name, token = token)
    }
}
