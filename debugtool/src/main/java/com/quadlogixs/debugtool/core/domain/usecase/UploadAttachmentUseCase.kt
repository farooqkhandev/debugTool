package com.quadlogixs.debugtool.core.domain.usecase

import com.quadlogixs.debugtool.api.ResponseStates
import com.quadlogixs.debugtool.core.domain.entity.UploadAttachmentRequestEntity
import com.quadlogixs.debugtool.core.domain.entity.UploadAttachmentResponseEntity
import com.quadlogixs.debugtool.core.domain.repository.UploadAttachmentRepo
import javax.inject.Inject

class UploadAttachmentUseCase @Inject constructor(
    private val uploadAttachmentRepo: UploadAttachmentRepo,
) {
    suspend operator fun invoke(
        params: UploadAttachmentRequestEntity,
    ): ResponseStates<UploadAttachmentResponseEntity> = uploadAttachmentRepo.uploadAttachment(params)
}
