package com.example.petadopt.ui.state

import androidx.compose.runtime.*
import com.example.petadopt.data.model.Pet

class MatchState {
    var likedPets = mutableStateListOf<Pet>()
        private set

    fun like(pet: Pet) {
        likedPets.add(pet)
    }
}