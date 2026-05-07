package com.example.presentation

import com.example.application.LoginUserInput
import com.example.application.LoginUserUseCase
import com.example.application.RegisterUserInput
import com.example.application.RegisterUserUseCase
import com.example.presentation.dto.AuthResponse
import com.example.presentation.dto.LoginRequest
import com.example.presentation.dto.RegisterRequest
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val registerUseCase: RegisterUserUseCase,
    private val loginUseCase: LoginUserUseCase,
) {
    @PostMapping("/register")
    fun register(@RequestBody @Validated req: RegisterRequest): ResponseEntity<AuthResponse> {
        val output = registerUseCase.execute(RegisterUserInput(email = req.email, password = req.password, name = req.name))
        return ResponseEntity.ok(AuthResponse(token = output.token, userId = output.userId, email = output.email, name = output.name))
    }

    @PostMapping("/login")
    fun login(@RequestBody @Validated req: LoginRequest): ResponseEntity<AuthResponse> {
        val output = loginUseCase.execute(LoginUserInput(email = req.email, password = req.password))
        return ResponseEntity.ok(AuthResponse(token = output.token, userId = output.userId, email = output.email, name = output.name))
    }
}
