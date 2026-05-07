package com.example.application

interface LoginUserUseCase {
    fun execute(input: LoginUserInput): LoginUserOutput
}
