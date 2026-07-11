package com.quadlogixs.debugtool.core.domain.entity

data class UploadAttachmentRequestEntity(
    val organization: String = "devops-ais",
    val project: String = "AikDigital",
    val base64Screenshot: String? = null,
)
