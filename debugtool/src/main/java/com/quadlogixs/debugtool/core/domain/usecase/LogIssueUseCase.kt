package com.quadlogixs.debugtool.core.domain.usecase

import com.quadlogixs.debugtool.api.ResponseStates
import com.quadlogixs.debugtool.core.domain.entity.LogIssueRequestEntity
import com.quadlogixs.debugtool.core.domain.repository.LogIssueRepo
import javax.inject.Inject

class LogIssueUseCase @Inject constructor(
    private val logIssueRepo: LogIssueRepo,
) {
    suspend operator fun invoke(params: LogIssueRequestEntity): ResponseStates<Unit> =
        logIssueRepo.logIssue(params)
}
