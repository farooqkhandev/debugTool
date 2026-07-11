package com.quadlogixs.debugtool.core.data.dto

import com.google.gson.annotations.SerializedName

data class UploadAttachmentResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("url") val url: String,
)
/*
fun LogIssueRequestEntity.toDto(): LogIssueRequestDto {
    return LogIssueRequestDto(
        title = this.title,
        description = this.description,
        screenshot = this.base64Screenshot,
        organization = this.organization,
        project = this.project,
        issueType = this.issueType
    )
}*/
