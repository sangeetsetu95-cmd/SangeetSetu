package com.sangeetsetu.app.di

import com.sangeetsetu.app.data.repository.*
import com.sangeetsetu.app.domain.repository.*
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
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): IUserRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): IAuthRepository

    @Binds
    @Singleton
    abstract fun bindBookingRepository(
        bookingRepositoryImpl: BookingRepositoryImpl
    ): IBookingRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl
    ): INotificationRepository

    @Binds
    @Singleton
    abstract fun bindMainRepository(
        mainRepositoryImpl: MainRepositoryImpl
    ): IMainRepository

    @Binds
    @Singleton
    abstract fun bindStorageRepository(
        storageRepositoryImpl: StorageRepositoryImpl
    ): IStorageRepository

    @Binds
    @Singleton
    abstract fun bindSystemLogRepository(
        systemLogRepositoryImpl: SystemLogRepositoryImpl
    ): ISystemLogRepository

    @Binds
    @Singleton
    abstract fun bindAdminCategoryRepository(
        adminCategoryRepositoryImpl: AdminCategoryRepositoryImpl
    ): IAdminCategoryRepository

    @Binds
    @Singleton
    abstract fun bindDynamicFormRepository(
        dynamicFormRepositoryImpl: DynamicFormRepositoryImpl
    ): IDynamicFormRepository

    @Binds
    @Singleton
    abstract fun bindEventRepository(
        eventRepositoryImpl: EventRepositoryImpl
    ): IEventRepository

    @Binds
    @Singleton
    abstract fun bindConfigRepository(
        configRepositoryImpl: ConfigRepositoryImpl
    ): IConfigRepository
}
