package com.quadlogixs.debugtool.core.domain.entity

data class UploadAttachmentRequestEntity(
    val organization: String = "",
    val project: String = "",
    val base64Screenshot: String? = null,
)
