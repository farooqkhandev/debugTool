package com.quadlogixs.debugtool.core.domain.repository

import com.quadlogixs.debugtool.core.domain.entity.UploadAttachmentRequestEntity
import com.quadlogixs.debugtool.core.domain.entity.UploadAttachmentResponseEntity
import com.quadlogixs.debugtool.api.ResponseStates

interface UploadAttachmentRepo {
    suspend fun uploadAttachment(issue: UploadAttachmentRequestEntity): ResponseStates<UploadAttachmentResponseEntity>
}