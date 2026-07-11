package com.quadlogixs.debugtool.core.domain.repository

import com.quadlogixs.debugtool.core.domain.entity.LogIssueRequestEntity
import com.quadlogixs.debugtool.api.ResponseStates

interface LogIssueRepo {
    suspend fun logIssue(issue: LogIssueRequestEntity): ResponseStates<Unit>
}