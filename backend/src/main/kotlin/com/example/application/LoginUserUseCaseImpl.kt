package com.example.application

import com.example.domain.InvalidCredentialsException
import com.example.domain.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class LoginUserUseCaseImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenProvider: TokenProvider,
) : LoginUserUseCase {
    override fun execute(input: LoginUserInput): LoginUserOutput {
        val user = userRepository.findByEmail(input.email) ?: throw InvalidCredentialsException()
        if (!passwordEncoder.matches(input.password, user.passwordHash)) throw InvalidCredentialsException()
        val token = tokenProvider.generateToken(user.id.value.toString())
        return LoginUserOutput(userId = user.id.value.toString(), email = user.email, name = user.name, token = token)
    }
}
