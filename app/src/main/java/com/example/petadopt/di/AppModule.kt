package com.example.petadopt.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // AppModule больше не нужен, так как SupabaseConfig предоставляет модули напрямую
}