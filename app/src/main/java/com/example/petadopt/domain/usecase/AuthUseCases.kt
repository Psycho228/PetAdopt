package com.example.petadopt.domain.usecase

import com.example.petadopt.data.model.User
import com.example.petadopt.data.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String) {
        repository.login(email, password)
    }
}

@Singleton
class RegisterUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String, name: String): User {
        return repository.register(email, password, name)
    }
}

@Singleton
class GetUserUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): User? {
        return repository.getUser()
    }
}

@Singleton
class UpdateUserProfileUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(name: String, email: String) {
        repository.updateUserProfile(name, email)
    }
}

@Singleton
class LogoutUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke() {
        repository.logout()
    }
}

@Singleton
class IsLoggedInUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(): Boolean {
        return repository.isLoggedIn
    }
}

@Singleton
class GetCurrentUserIdUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(): String? {
        return repository.currentUserId
    }
}

@Singleton
class IsCurrentUserAdminUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): Boolean {
        return repository.isCurrentUserAdmin()
    }
}

@Singleton
class IsCurrentUserShelterUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): Boolean {
        return repository.isCurrentUserShelter()
    }
}
