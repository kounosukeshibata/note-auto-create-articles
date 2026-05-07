package com.example.domain

class DuplicateUserException(email: String) : RuntimeException("User already exists: $email")
