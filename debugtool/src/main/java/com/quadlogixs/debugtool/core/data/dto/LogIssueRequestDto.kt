package com.quadlogixs.debugtool.core.data.dto

import com.google.gson.annotations.SerializedName
import com.quadlogixs.debugtool.core.domain.entity.LogIssueRequestEntity

data class LogIssueRequestDto(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("organization") val organization: String,
    @SerializedName("project") val project: String,
    @SerializedName("issueType") val issueType: String,
    @SerializedName("screenshot") val screenshot: String? = null
)

fun LogIssueRequestEntity.toDto(): LogIssueRequestDto {
    return LogIssueRequestDto(
        title = this.title,
        description = this.description,
        screenshot = this.base64Screenshot,
        organization = this.organization,
        project = this.project,
        issueType = this.issueType
    )
}