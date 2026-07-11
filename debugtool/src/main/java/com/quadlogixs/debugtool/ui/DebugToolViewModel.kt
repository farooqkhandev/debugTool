package com.quadlogixs.debugtool.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quadlogixs.debugtool.api.AssignedTo
import com.quadlogixs.debugtool.api.DebugToolRegistry
import com.quadlogixs.debugtool.core.domain.entity.LogIssueRequestEntity
import com.quadlogixs.debugtool.core.domain.entity.UploadAttachmentRequestEntity
import com.quadlogixs.debugtool.core.domain.usecase.LogIssueUseCase
import com.quadlogixs.debugtool.core.domain.usecase.UploadAttachmentUseCase
import com.quadlogixs.debugtool.api.ResponseStates
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class DebugToolViewModel @Inject constructor(
    private val logAzureIssueUseCase: LogIssueUseCase,
    private val uploadAttachMenUseCase: UploadAttachmentUseCase,
) : ViewModel() {

    private val _logIssueState: MutableStateFlow<ResponseStates<Unit>> =
        MutableStateFlow(ResponseStates.Idle)
    val logIssueState: StateFlow<ResponseStates<Unit>> = _logIssueState.asStateFlow()


    val priority: MutableStateFlow<List<String>> = MutableStateFlow(listOf())
    val severity: MutableStateFlow<List<String>> = MutableStateFlow(listOf())

    init {
        initConfigurations()
    }

    private fun initConfigurations() {
        priority.value = listOf("High", "Urgent", "Medium", "Low")
        severity.value = listOf("Critical", "High", "Medium", "Low")
    }

    fun getListOfAssignsItems() = DebugToolRegistry.config.assignees.ifEmpty { defaultAssignees() }

    private fun defaultAssignees() = listOf(
        AssignedTo(name = "Farooq Khan", emailAddress = "farooq.khan@appinsnap.com"),
        AssignedTo(name = "Arqum Yousaf", emailAddress = "arqum.yousaf@appinsnap.com"),
        AssignedTo(name = "Nasir Malik", emailAddress = "nasir.malik@appinsnap.com"),
    )


    /**
     * Report an issue with a screenshot.
     * 1. Upload the screenshot and get the URL.
     * 2. Log the issue using the URL.
     */
    fun reportIssueWithAttachment(
        uploadRequest: UploadAttachmentRequestEntity,
        issueRequest: LogIssueRequestEntity
    ) {
        viewModelScope.launch {
            _logIssueState.value = ResponseStates.Loading

            // Step 1: Upload attachment
            when (val uploadResult = uploadAttachMenUseCase(uploadRequest)) {
                is ResponseStates.Success -> {
                    Timber.d("Success Data-> ${uploadResult.data}")
                    val attachmentUrl = uploadResult.data.url
                    // Step 2: Log issue with attachment
                    val updatedRequest = issueRequest.copy(
                        attachmentUrl = attachmentUrl
                    )
                    //Step 3: Send Report with Attachment
                    _logIssueState.value = logAzureIssueUseCase(updatedRequest)
                }

                is ResponseStates.Failure -> {
                    Timber.d("uploadResult.httpCode -> ${uploadResult.httpCode}")
                    Timber.d("Failure -> ${uploadResult.error}")
                    _logIssueState.value =
                        ResponseStates.Failure(
                            300,
                            "Attachment upload failed: ${uploadResult.error}"
                        )
                }

                else -> Unit
            }
        }
    }


    fun logIssue(request: LogIssueRequestEntity) {
        viewModelScope.launch {
            _logIssueState.value = ResponseStates.Loading
            _logIssueState.value = logAzureIssueUseCase(request)
        }
    }

    fun clearLogIssueState() {
        viewModelScope.launch {
            _logIssueState.value = ResponseStates.Idle
        }
    }
}
