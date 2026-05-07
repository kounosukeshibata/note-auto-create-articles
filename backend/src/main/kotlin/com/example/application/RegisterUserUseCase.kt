package com.example.application

interface RegisterUserUseCase {
    fun execute(input: RegisterUserInput): RegisterUserOutput
}
