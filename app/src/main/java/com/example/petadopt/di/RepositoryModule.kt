package com.example.petadopt.di

import com.example.petadopt.data.repository.AdminRepository
import com.example.petadopt.data.repository.AuthRepository
import com.example.petadopt.data.repository.GigaChatRepository
import com.example.petadopt.data.repository.PetRepository
import com.example.petadopt.data.repository.QuestionnaireRepository
import com.example.petadopt.data.repository.StorageRepository
import com.example.petadopt.data.repository.S3StorageRepository
import com.example.petadopt.data.repository.SupabaseAuthRepository
import com.example.petadopt.data.repository.SupabasePetRepository
import com.example.petadopt.data.repository.SupabaseQuestionnaireRepository
import com.example.petadopt.domain.usecase.GetRiskAssessmentUseCase
import com.example.petadopt.domain.usecase.GetRiskAssessmentHistoryUseCase
import com.example.petadopt.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        authRepository: SupabaseAuthRepository
    ): AuthRepository {
        return authRepository
    }

    @Provides
    @Singleton
    fun providePetRepository(
        petRepository: SupabasePetRepository
    ): PetRepository {
        return petRepository
    }

    @Provides
    @Singleton
    fun provideQuestionnaireRepository(
        questionnaireRepository: SupabaseQuestionnaireRepository
    ): QuestionnaireRepository {
        return questionnaireRepository
    }

    @Provides
    @Singleton
    fun provideAdminRepository(
        petRepository: SupabasePetRepository
    ): AdminRepository {
        return AdminRepository(petRepository)
    }

    @Provides
    @Singleton
    fun provideStorageRepository(
        storageRepository: S3StorageRepository
    ): StorageRepository {
        return storageRepository
    }

    @Provides
    @Singleton
    fun provideGigaChatRepository(): GigaChatRepository {
        return GigaChatRepository()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    // Auth UseCases
    @Provides
    fun provideLoginUseCase(repository: AuthRepository): LoginUseCase {
        return LoginUseCase(repository)
    }

    @Provides
    fun provideRegisterUseCase(repository: AuthRepository): RegisterUseCase {
        return RegisterUseCase(repository)
    }

    @Provides
    fun provideGetUserUseCase(repository: AuthRepository): GetUserUseCase {
        return GetUserUseCase(repository)
    }

    @Provides
    fun provideUpdateUserProfileUseCase(repository: AuthRepository): UpdateUserProfileUseCase {
        return UpdateUserProfileUseCase(repository)
    }

    @Provides
    fun provideLogoutUseCase(repository: AuthRepository): LogoutUseCase {
        return LogoutUseCase(repository)
    }

    @Provides
    fun provideIsLoggedInUseCase(repository: AuthRepository): IsLoggedInUseCase {
        return IsLoggedInUseCase(repository)
    }

    @Provides
    fun provideGetCurrentUserIdUseCase(repository: AuthRepository): GetCurrentUserIdUseCase {
        return GetCurrentUserIdUseCase(repository)
    }

    @Provides
    fun provideIsCurrentUserAdminUseCase(repository: AuthRepository): IsCurrentUserAdminUseCase {
        return IsCurrentUserAdminUseCase(repository)
    }

    @Provides
    fun provideIsCurrentUserShelterUseCase(repository: AuthRepository): IsCurrentUserShelterUseCase {
        return IsCurrentUserShelterUseCase(repository)
    }

    // Pet UseCases
    @Provides
    fun provideGetPetsUseCase(repository: PetRepository): GetPetsUseCase {
        return GetPetsUseCase(repository)
    }

    @Provides
    fun provideGetPetByIdUseCase(repository: PetRepository): GetPetByIdUseCase {
        return GetPetByIdUseCase(repository)
    }

    @Provides
    fun provideGetLikedPetsUseCase(repository: PetRepository): GetLikedPetsUseCase {
        return GetLikedPetsUseCase(repository)
    }

    @Provides
    fun provideSubmitApplicationUseCase(repository: PetRepository): SubmitApplicationUseCase {
        return SubmitApplicationUseCase(repository)
    }

    @Provides
    fun provideGetUserApplicationsUseCase(repository: PetRepository): GetUserApplicationsUseCase {
        return GetUserApplicationsUseCase(repository)
    }

    @Provides
    fun provideGetAppliedPetIdsUseCase(repository: PetRepository): GetAppliedPetIdsUseCase {
        return GetAppliedPetIdsUseCase(repository)
    }

    @Provides
    fun provideLikePetUseCase(repository: PetRepository): LikePetUseCase {
        return LikePetUseCase(repository)
    }

    @Provides
    fun provideUnlikePetUseCase(repository: PetRepository): UnlikePetUseCase {
        return UnlikePetUseCase(repository)
    }

    @Provides
    fun provideUpdateApplicationStatusUseCase(repository: PetRepository): UpdateApplicationStatusUseCase {
        return UpdateApplicationStatusUseCase(repository)
    }

    // Questionnaire UseCases
    @Provides
    fun provideSaveQuestionnaireUseCase(repository: QuestionnaireRepository): SaveQuestionnaireUseCase {
        return SaveQuestionnaireUseCase(repository)
    }

    @Provides
    fun provideGetQuestionnaireUseCase(repository: QuestionnaireRepository): GetQuestionnaireUseCase {
        return GetQuestionnaireUseCase(repository)
    }

    @Provides
    fun provideDeleteQuestionnaireUseCase(repository: QuestionnaireRepository): DeleteQuestionnaireUseCase {
        return DeleteQuestionnaireUseCase(repository)
    }

    // Admin UseCases
    @Provides
    fun provideCreatePetUseCase(repository: AdminRepository): CreatePetUseCase {
        return CreatePetUseCase(repository)
    }

    @Provides
    fun provideUpdatePetUseCase(repository: AdminRepository): UpdatePetUseCase {
        return UpdatePetUseCase(repository)
    }

    @Provides
    fun provideDeletePetUseCase(repository: AdminRepository): DeletePetUseCase {
        return DeletePetUseCase(repository)
    }

    @Provides
    fun provideGetAllPetsUseCase(repository: AdminRepository): GetAllPetsUseCase {
        return GetAllPetsUseCase(repository)
    }

    @Provides
    fun provideUpdatePetStatusUseCaseAdmin(repository: AdminRepository): UpdatePetStatusUseCaseAdmin {
        return UpdatePetStatusUseCaseAdmin(repository)
    }

    @Provides
    fun provideGetPetsByShelterUseCase(repository: PetRepository): GetPetsByShelterUseCase {
        return GetPetsByShelterUseCase(repository)
    }

    // Storage UseCases
    @Provides
    fun provideUploadImageUseCase(storageRepository: StorageRepository): UploadImageUseCase {
        return UploadImageUseCase(storageRepository)
    }

    @Provides
    fun provideUploadImagesUseCase(storageRepository: StorageRepository): UploadImagesUseCase {
        return UploadImagesUseCase(storageRepository)
    }

    @Provides
    fun provideDeleteImageUseCase(storageRepository: StorageRepository): DeleteImageUseCase {
        return DeleteImageUseCase(storageRepository)
    }

    // Risk Assessment UseCases
    @Provides
    fun provideAssessRiskUseCase(repository: GigaChatRepository): AssessRiskUseCase {
        return AssessRiskUseCase(repository)
    }

    @Provides
    fun provideRiskAssessmentUseCases(assessRisk: AssessRiskUseCase): RiskAssessmentUseCases {
        return RiskAssessmentUseCases(assessRisk)
    }

    // Risk Assessment Data UseCases
    @Provides
    fun provideGetRiskAssessmentUseCase(repository: QuestionnaireRepository): GetRiskAssessmentUseCase {
        return GetRiskAssessmentUseCase(repository)
    }

    @Provides
    fun provideGetRiskAssessmentHistoryUseCase(repository: QuestionnaireRepository): GetRiskAssessmentHistoryUseCase {
        return GetRiskAssessmentHistoryUseCase(repository)
    }
}
