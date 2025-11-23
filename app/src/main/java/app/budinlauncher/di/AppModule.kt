package app.budinlauncher.di

import android.content.Context
import android.content.pm.PackageManager
import app.budinlauncher.data.repository.AppRepositoryImpl
import app.budinlauncher.data.repository.SettingsRepositoryImpl
import app.budinlauncher.data.repository.UsageStatsRepositoryImpl
import app.budinlauncher.domain.repository.AppRepository
import app.budinlauncher.domain.repository.SettingsRepository
import app.budinlauncher.domain.repository.UsageStatsRepository
import app.budinlauncher.domain.usecase.GetAppsUseCase
import app.budinlauncher.domain.usecase.GetScreenTimeUseCase
import app.budinlauncher.domain.usecase.LaunchAppUseCase
import app.budinlauncher.domain.usecase.SearchAppsUseCase
import app.budinlauncher.domain.usecase.UpdateSettingsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePackageManager(@ApplicationContext context: Context): PackageManager {
        return context.packageManager
    }

    @Provides
    @Singleton
    fun provideAppRepository(
        @ApplicationContext context: Context,
        packageManager: PackageManager
    ): AppRepository {
        return AppRepositoryImpl(context, packageManager)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context): SettingsRepository {
        return SettingsRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideUsageStatsRepository(@ApplicationContext context: Context): UsageStatsRepository {
        return UsageStatsRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideGetAppsUseCase(repository: AppRepository): GetAppsUseCase {
        return GetAppsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSearchAppsUseCase(repository: AppRepository): SearchAppsUseCase {
        return SearchAppsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideLaunchAppUseCase(repository: AppRepository): LaunchAppUseCase {
        return LaunchAppUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetScreenTimeUseCase(repository: UsageStatsRepository): GetScreenTimeUseCase {
        return GetScreenTimeUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUpdateSettingsUseCase(repository: SettingsRepository): UpdateSettingsUseCase {
        return UpdateSettingsUseCase(repository)
    }
}