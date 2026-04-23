package com.softwavegamess.smgrealestate.di

import com.softwavegamess.smgrealestate.data.repository.PropertyRepositoryImpl
import com.softwavegamess.smgrealestate.domain.repository.PropertyRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPropertyRepository(impl: PropertyRepositoryImpl): PropertyRepository
}
