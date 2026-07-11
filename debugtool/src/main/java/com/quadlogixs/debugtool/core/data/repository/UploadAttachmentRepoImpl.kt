package com.quadlogixs.debugtool.core.data.repository

import android.util.Base64
import com.quadlogixs.debugtool.api.DebugToolRegistry
import com.quadlogixs.debugtool.api.ResponseStates
import com.quadlogixs.debugtool.core.data.AzureDevOpsApi
import com.quadlogixs.debugtool.core.data.AzureDevOpsClientFactory
import com.quadlogixs.debugtool.core.data.parseAttachmentResponse
import com.quadlogixs.debugtool.core.domain.entity.UploadAttachmentRequestEntity
import com.quadlogixs.debugtool.core.domain.entity.UploadAttachmentResponseEntity
import com.quadlogixs.debugtool.core.domain.repository.UploadAttachmentRepo
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadAttachmentRepoImpl @Inject constructor(
    private val api: AzureDevOpsApi,
) : UploadAttachmentRepo {

    override suspend fun uploadAttachment(
        request: UploadAttachmentRequestEntity,
    ): ResponseStates<UploadAttachmentResponseEntity> {
        return runCatching {
            val azure = DebugToolRegistry.config.azure
            val fileBytes = Base64.decode(
                request.base64Screenshot?.substringAfter(",") ?: "",
                Base64.DEFAULT,
            )
            val requestBody = fileBytes.toRequestBody("application/octet-stream".toMediaType())
            val mimeType = request.base64Screenshot
                ?.substringBefore(",")
                ?.substringAfter(":")
                ?.substringBefore(";")
            val extension = when (mimeType) {
                "image/jpeg" -> "jpg"
                "image/png" -> "png"
                else -> "png"
            }
            val fileName = "screenshot_${System.currentTimeMillis()}.$extension"
            val uploadUrl =
                "https://dev.azure.com/${azure.organization}/${azure.project}/_apis/wit/attachments?fileName=$fileName&api-version=7.2-preview.4"

            val response = api.uploadAttachment(
                url = uploadUrl,
                body = requestBody,
                auth = AzureDevOpsClientFactory.authHeader(),
            )

            if (!response.isSuccessful) {
                return ResponseStates.Failure(
                    response.code(),
                    response.errorBody()?.string().orEmpty().ifBlank { "Upload failed" },
                )
            }

            val raw = response.body()?.string().orEmpty()
            val ref = parseAttachmentResponse(raw, response.headers()["Location"])
                ?: return ResponseStates.Failure(201, "Created, but server returned no attachment reference")

            Timber.d("Upload attachment success: $ref")
            ResponseStates.Success(response.code(), ref)
        }.getOrElse {
            ResponseStates.Failure(500, it.message ?: "Upload failed")
        }
    }
}
