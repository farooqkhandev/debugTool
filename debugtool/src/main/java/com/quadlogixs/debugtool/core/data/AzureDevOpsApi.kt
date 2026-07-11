package com.quadlogixs.debugtool.core.data

import com.quadlogixs.debugtool.core.data.dto.AzurePatchOperation
import com.quadlogixs.debugtool.core.domain.entity.UploadAttachmentResponseEntity
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Url

interface AzureDevOpsApi {
    @POST
    suspend fun uploadAttachment(
        @Url url: String,
        @Body body: RequestBody,
        @Header("Authorization") auth: String,
    ): Response<ResponseBody>

    @PATCH
    suspend fun createWorkItem(
        @Url url: String,
        @Body body: List<AzurePatchOperation>,
        @Header("Authorization") auth: String,
        @Header("Content-Type") contentType: String = "application/json-patch+json",
    ): Response<ResponseBody>
}

internal fun parseAttachmentResponse(raw: String, location: String?): UploadAttachmentResponseEntity? {
    if (raw.isNotBlank()) {
        return runCatching {
            com.google.gson.Gson().fromJson(raw, UploadAttachmentResponseEntity::class.java)
        }.getOrNull()
    }
    val loc = location ?: return null
    val id = Regex("""/attachments/([0-9a-fA-F-]{36})\b""").find(loc)?.groupValues?.getOrNull(1)
    return id?.let { UploadAttachmentResponseEntity(id = it, url = loc) }
}
