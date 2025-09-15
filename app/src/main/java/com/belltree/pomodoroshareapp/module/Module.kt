package com.belltree.pomodoroshareapp.module

import com.belltree.pomodoroshareapp.domain.repository.AuthRepository
import com.belltree.pomodoroshareapp.domain.repository.AuthRepositoryImpl
import com.belltree.pomodoroshareapp.domain.repository.CommentRepository
import com.belltree.pomodoroshareapp.domain.repository.CommentRepositoryImpl
import com.belltree.pomodoroshareapp.domain.repository.RecordRepository
import com.belltree.pomodoroshareapp.domain.repository.RecordRepositoryImpl
import com.belltree.pomodoroshareapp.domain.repository.SpaceRepository
import com.belltree.pomodoroshareapp.domain.repository.SpaceRepositoryImpl
import com.belltree.pomodoroshareapp.domain.repository.UserRepository
import com.belltree.pomodoroshareapp.domain.repository.UserRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository

    @Binds
    abstract fun bindRecordRepository(
        impl: RecordRepositoryImpl
    ): RecordRepository

    @Binds
    abstract fun bindSpaceRepository(
        impl: SpaceRepositoryImpl
    ): SpaceRepository

    @Binds
    abstract fun bindCommentRepository(
        impl: CommentRepositoryImpl
    ): CommentRepository
}