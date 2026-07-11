package com.quadlogixs.debugtool.core.data.repository

import com.quadlogixs.debugtool.api.DebugToolRegistry
import com.quadlogixs.debugtool.api.ResponseStates
import com.quadlogixs.debugtool.core.data.AzureDevOpsApi
import com.quadlogixs.debugtool.core.data.AzureDevOpsClientFactory
import com.quadlogixs.debugtool.core.data.dto.AzurePatchOperation
import com.quadlogixs.debugtool.core.domain.entity.LogIssueRequestEntity
import com.quadlogixs.debugtool.core.domain.repository.LogIssueRepo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogIssueRepoImpl @Inject constructor(
    private val api: AzureDevOpsApi,
) : LogIssueRepo {

    override suspend fun logIssue(request: LogIssueRequestEntity): ResponseStates<Unit> {
        return runCatching {
            val azure = DebugToolRegistry.config.azure
            val url =
                "https://dev.azure.com/${azure.organization}/${azure.project}/_apis/wit/workitems/\$Bug?api-version=7.1"

            val reproStepsHtml = buildString {
                append(request.description)
                append("<br><b>Repro Steps:</b><br>")
                append("<b>Route:</b> ${request.reproduceStepsRoute}<br><br>")
                request.customStepsToReproduce.forEachIndexed { index, step ->
                    append("${index + 1}. $step<br>")
                }
            }

            val body = buildList {
                add(AzurePatchOperation("add", "/fields/System.Title", request.description))
                add(AzurePatchOperation("add", "/fields/System.AssignedTo", request.assignedTo))
                add(AzurePatchOperation("add", "/fields/System.State", request.state))
                add(AzurePatchOperation("add", "/fields/System.AreaPath", request.areaPath))
                add(AzurePatchOperation("add", "/fields/Microsoft.VSTS.TCM.ReproSteps", reproStepsHtml))
                request.attachmentUrl?.takeIf { it.isNotBlank() }?.let { attachmentUrl ->
                    add(
                        AzurePatchOperation(
                            op = "add",
                            path = "/relations/-",
                            value = mapOf(
                                "rel" to "Hyperlink",
                                "url" to attachmentUrl,
                                "attributes" to mapOf("comment" to "Screenshot evidence"),
                            ),
                        ),
                    )
                }
            }

            val response = api.createWorkItem(
                url = url,
                body = body,
                auth = AzureDevOpsClientFactory.authHeader(),
            )

            if (response.isSuccessful) {
                ResponseStates.Success(response.code(), Unit)
            } else {
                ResponseStates.Failure(
                    response.code(),
                    response.errorBody()?.string().orEmpty().ifBlank { "Failed to log issue" },
                )
            }
        }.getOrElse {
            ResponseStates.Failure(500, it.message ?: "Failed to log issue")
        }
    }
}
