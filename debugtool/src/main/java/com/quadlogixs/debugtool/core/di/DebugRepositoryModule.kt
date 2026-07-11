package com.quadlogixs.debugtool.core.di

import com.quadlogixs.debugtool.api.DebugToolRegistry
import com.quadlogixs.debugtool.core.data.AzureDevOpsApi
import com.quadlogixs.debugtool.core.data.AzureDevOpsClientFactory
import com.quadlogixs.debugtool.core.data.repository.LogIssueRepoImpl
import com.quadlogixs.debugtool.core.data.repository.UploadAttachmentRepoImpl
import com.quadlogixs.debugtool.core.domain.repository.LogIssueRepo
import com.quadlogixs.debugtool.core.domain.repository.UploadAttachmentRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DebugRepositoryModule {

    @Singleton
    @Provides
    fun provideAzureDevOpsApi(): AzureDevOpsApi =
        AzureDevOpsClientFactory.createApi(DebugToolRegistry.config.azure)

    @Singleton
    @Provides
    fun provideLogIssueRepository(impl: LogIssueRepoImpl): LogIssueRepo = impl

    @Singleton
    @Provides
    fun provideUploadAttachmentRepository(impl: UploadAttachmentRepoImpl): UploadAttachmentRepo = impl
}
