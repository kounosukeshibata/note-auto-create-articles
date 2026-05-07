package com.example.presentation

import com.example.domain.UserRepository
import com.example.domain.UserId
import com.example.presentation.dto.MeResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class UserController(private val userRepository: UserRepository) {

    @GetMapping("/me")
    fun me(authentication: Authentication): ResponseEntity<MeResponse> {
        val userId = UserId.of(authentication.principal as String)
        val user = userRepository.findById(userId) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(MeResponse(userId = user.id.value.toString(), email = user.email, name = user.name))
    }
}
