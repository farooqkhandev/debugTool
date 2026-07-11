package com.quadlogixs.debugtool.core.di

import android.app.Activity
import android.content.Context
import android.view.Window
import androidx.metrics.performance.JankStats
import androidx.metrics.performance.JankStats.OnFrameListener
import com.quadlogixs.debugtool.core.FrameDataProvider
import com.quadlogixs.debugtool.core.FileJankLogRepositoryImp
import com.quadlogixs.debugtool.core.JankFrameProviderImpl
import com.quadlogixs.debugtool.core.JankLogRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object JankBindingsModule {

/*
    @Binds
    abstract fun bindFrameDataProvider(
        impl: JankFrameProviderImpl
    ): FrameDataProvider

    @Binds
    abstract fun bindJankLogRepository(
        impl: FileJankLogRepositoryImp
    ): JankLogRepository*/


    @Singleton
    @Provides
    fun provideFrameDataProvider(
        logRepository: JankLogRepository
    ): FrameDataProvider = JankFrameProviderImpl(logRepository)

    @Singleton
    @Provides
    fun provideJankLogRepository(
        @ApplicationContext context: Context
    ): JankLogRepository = FileJankLogRepositoryImp(context)
}

@Module
@InstallIn(ActivityComponent::class)
object JankStatsModule {

    @Provides
    fun provideOnFrameListener(
        frameDataProvider: FrameDataProvider
    ): OnFrameListener = frameDataProvider

    @Provides
    fun provideWindow(activity: Activity): Window = activity.window

    @Provides
    fun provideJankStats(
        window: Window,
        frameListener: OnFrameListener
    ): JankStats = JankStats.createAndTrack(window, frameListener)
}

