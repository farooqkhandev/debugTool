package com.quadlogixs.debugtool.core.domain.entity

data class LogIssueRequestEntity(
    val title: String = "App Title",
    val description: String,
    val organization: String = "devops-ais",
    val assignedTo: String = "farooq.khan@appinsnap.com",
    val project: String = "AikDigital",
    val areaPath: String = "AikDigital\\Android Bug",
    val state: WorkItemState = WorkItemState.New,
    val issueType: String,
    val base64Screenshot: String? = null,
    val attachmentUrl: String? = null,
    val comment: String ?= null,
    val reproduceStepsRoute: String = "",
    val customStepsToReproduce: List<String> = listOf()
)

enum class WorkItemState(val value: String) {
    New("New"),
    In_Progress("In Progress"),
    Re_Open("Re-Open"),
    Done("Done"),
    In_QA("In QA"),
    Client_Review("Client Review"),
    To_Do("To Do"),
    Resolved("In Code Review"),
    In_Code_Review("Ready for Deployment"),
    App_Prod_Issue("App Prod Issue"),
    Backend_Prod_Issue("Backend Prod Issue"),
    Micro_Bank_Prod_Issue("Micro Bank Prod Issue")
}

