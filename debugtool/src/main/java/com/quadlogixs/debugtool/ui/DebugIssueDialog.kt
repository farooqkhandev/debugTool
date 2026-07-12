package com.quadlogixs.debugtool.ui

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.view.PixelCopy
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.createBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quadlogixs.debugtool.R
import com.quadlogixs.debugtool.ui.components.BodySmallText
import com.quadlogixs.debugtool.ui.components.CardContainer
import com.quadlogixs.debugtool.ui.components.LabelMediumText
import com.quadlogixs.debugtool.ui.components.RadioButtonWithText
import com.quadlogixs.debugtool.ui.components.ShowLoader
import com.quadlogixs.debugtool.ui.components.TextButton
import com.quadlogixs.debugtool.ui.components.TitleMediumText
import com.quadlogixs.debugtool.ui.components.ResourceImage
import com.quadlogixs.debugtool.ui.components.TextInputFieldApp
import com.quadlogixs.debugtool.ui.components.safeClickable
import com.quadlogixs.debugtool.ui.components.SpacerHeight
import com.quadlogixs.debugtool.ui.components.SpacerWeight
import com.quadlogixs.debugtool.ui.components.sdp
import com.quadlogixs.debugtool.ui.components.textSdp
import com.quadlogixs.debugtool.ui.components.SpaceDefault
import com.quadlogixs.debugtool.ui.components.AssignToItem
import com.quadlogixs.debugtool.ui.showDebugToast
import com.quadlogixs.debugtool.ui.theme.LocalExtraThemeColors
import com.quadlogixs.debugtool.ui.theme.DebugColors
import com.quadlogixs.debugtool.ui.theme.DebugToolTheme
import com.quadlogixs.debugtool.core.domain.entity.LogIssueRequestEntity
import com.quadlogixs.debugtool.core.domain.entity.UploadAttachmentRequestEntity
import com.quadlogixs.debugtool.api.ResponseStates
import com.quadlogixs.debugtool.ui.toBase64
import timber.log.Timber
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugIssueDialog(
    onClickCallBack: () -> Unit = {},
    viewModel: DebugToolViewModel = hiltViewModel(),
    routeTrail: String = ""
) {
    DebugToolTheme {
        DebugIssueDialogContent(
            onClickCallBack = onClickCallBack,
            viewModel = viewModel,
            routeTrail = routeTrail,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DebugIssueDialogContent(
    onClickCallBack: () -> Unit,
    viewModel: DebugToolViewModel,
    routeTrail: String,
) {
    Timber.d("DebugIssueDialog")

    val activity = LocalContext.current as? Activity

    val logIssueState by viewModel.logIssueState.collectAsStateWithLifecycle()
    val priority by viewModel.priority.collectAsStateWithLifecycle()
    val severity by viewModel.severity.collectAsStateWithLifecycle()
    var assignedToState by remember { mutableStateOf(viewModel.getListOfAssignsItems()) }
    var selectedAssignedItem by remember {
        mutableStateOf(assignedToState.find { it.isChecked } ?: assignedToState.first())
    }

    var showLoader by remember { mutableStateOf(false) }

    val context = LocalContext.current


    var description by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(assignedToState.size > 1) }

    var selectedPriority by remember { mutableStateOf("High") }
    var selectedSaverity by remember { mutableStateOf("Critical") }

    val stepsList = remember { mutableStateListOf<String>() }
    var screenShotBase64: String? by remember { mutableStateOf(null) }
    var screenShotBitmap: Bitmap? by remember { mutableStateOf(null) }
    var showImageEditor: Boolean by remember { mutableStateOf(false) }

    val testBase64 = getDrawableBase64(
        context = context,
        drawableResId = R.drawable.app_background
    )
    Dialog(
        onDismissRequest = { onClickCallBack() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.sdp)
        ) {
            CardContainer(
                modifier = Modifier.fillMaxSize(),
                containerColor = DebugColors.Background,
                borderColor = DebugColors.Border,
                content = {
                    if (!showImageEditor) {
                        Column(
                            modifier = Modifier
                                .padding(8.sdp)
                                .fillMaxSize()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Row {
                                    TitleMediumText(
                                        text = "Report Bug",
                                        overrideColor = DebugColors.TextPrimary
                                    )
                                    SpacerWeight(1f)
                                    ResourceImage(
                                        image = com.quadlogixs.debugtool.R.drawable.ic_close_receipt,
                                        modifier = Modifier
                                            .size(18.sdp)
                                            .safeClickable {
                                                onClickCallBack()
                                            }
                                    )
                                }
                                SpacerHeight(10.sdp)
                                TextInputFieldApp(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 5.dp),
                                    value = description,
                                    singleLine = false,
                                    maxLines = 3,
                                    heightField = 70.sdp,
                                    onValueChange = {
                                        description = it
                                    },
                                    trailing = {
                                        Box(
                                            modifier = Modifier.height(70.sdp),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            if (screenShotBitmap != null) {
                                                Box(
                                                    contentAlignment = Alignment.TopEnd,
                                                    modifier = Modifier
                                                        .padding(vertical = 3.sdp)
                                                        .border(
                                                            1.dp,
                                                            LocalExtraThemeColors.current.disableOutline
                                                        )
                                                ) {
                                                    ResourceImage(
                                                        modifier = Modifier
                                                            .fillMaxHeight()
                                                            .safeClickable {
                                                                showImageEditor = true
                                                            },
                                                        image = screenShotBitmap
                                                    )
                                                    ResourceImage(
                                                        image = com.quadlogixs.debugtool.R.drawable.ic_close_receipt,
                                                        modifier = Modifier
                                                            .size(15.sdp)
                                                            .safeClickable {
                                                                screenShotBase64 = null
                                                                screenShotBitmap = null
                                                            }
                                                    )
                                                }
                                            } else {
                                                ResourceImage(
                                                    modifier = Modifier
                                                        .safeClickable {
                                                            activity?.let {
                                                                    captureMainScreen(activity = activity, callback = {
                                                                        screenShotBitmap =it
                                                                    })
                                                            }
                                                        }
                                                        .size(22.sdp),
                                                    image = com.quadlogixs.debugtool.R.drawable.ic_camera
                                                )
                                            }
                                        }
                                    },
                                    labelTextSize = 14.textSdp,
                                    fieldLabel = "Description",
                                    placeholder = "Enter Description",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Unspecified)
                                )
                                SpacerHeight(10.sdp)
                                Row {
                                    LabelMediumText(
                                        text = "Assigned To",
                                        fontSize = 14.textSdp,
                                        overrideColor = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    SpacerWeight(1f)

                                    ResourceImage(
                                        modifier = Modifier
                                            .safeClickable { expanded = !expanded }
                                            .size(22.sdp),
                                        image = if (expanded) com.quadlogixs.debugtool.R.drawable.ic_arrow_up_round else com.quadlogixs.debugtool.R.drawable.ic_arrow_down_round
                                    )
                                }
                                SpacerHeight(10.sdp)

                                if (expanded) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 220.sdp)
                                            .verticalScroll(rememberScrollState()),
                                    ) {
                                        assignedToState.forEachIndexed { index, item ->
                                            AssignToItem(
                                                item = item,
                                                isChecked = item.isChecked,
                                                onSelectItem = {
                                                    selectedAssignedItem = item
                                                    assignedToState = assignedToState.mapIndexed { i, assignee ->
                                                        assignee.copy(isChecked = i == index)
                                                    }
                                                    expanded = false
                                                },
                                            )
                                            if (index != assignedToState.lastIndex) {
                                                SpacerHeight(5.sdp)
                                            }
                                        }
                                    }
                                } else {
                                    AssignToItem(
                                        item = selectedAssignedItem,
                                        isChecked = true,
                                        onSelectItem = {
                                            expanded = true
                                        },
                                    )
                                    SpacerHeight(5.sdp)
                                }


                                SpacerHeight(10.sdp)
                                TextInputFieldApp(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 5.dp),
                                    value = area,
                                    singleLine = false,
                                    maxLines = 1,
                                    onValueChange = {
                                        area = it
                                    },
                                    labelTextSize = 14.textSdp,
                                    supportText = "Default area is set to AikDigial\\Android Bug",
                                    fieldLabel = "Area (Optional)",
                                    placeholder = "Area",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Unspecified)
                                )
                                SpacerHeight(10.sdp)
                                TextInputFieldApp(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 5.dp),
                                    value = tags,
                                    singleLine = false,
                                    maxLines = 1,
                                    onValueChange = {
                                        tags = it
                                    },
                                    labelTextSize = 14.textSdp,
                                    fieldLabel = "Tags (Optional)",
                                    placeholder = "Tags",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Unspecified)
                                )
                                SpacerHeight(15.sdp)

                                StepsToReproduceSection(
                                    steps = stepsList,
                                    onStepsChanged = { updatedSteps ->
                                        // Here you can update your ViewModel or form state
                                        Timber.d("Updated Steps: $updatedSteps")
                                    }
                                )
                                SpacerHeight(6.sdp)

                                LabelMediumText(
                                    text = "Priority",
                                    fontSize = 14.textSdp,
                                    overrideColor = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    listOf("High", "Urgent", "Medium", "Low").forEach { option ->
                                        RadioButtonWithText(
                                            selected = selectedPriority == option,
                                            title = option,
                                            compact = true,
                                            bodyContent = {
                                                BodySmallText(
                                                    text = option,
                                                    fontSize = 10.textSdp,
                                                    maxLines = 1,
                                                    softWrap = false,
                                                    overflow = TextOverflow.Ellipsis,
                                                    overrideColor = if (selectedPriority == option) {
                                                        MaterialTheme.colorScheme.onPrimary
                                                    } else {
                                                        MaterialTheme.colorScheme.onBackground
                                                    },
                                                )
                                            },
                                            onSelectedChanged = { selectedPriority = option },
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                }
                                SpacerHeight(10.sdp)

                                LabelMediumText(
                                    text = "Severity",
                                    fontSize = 14.textSdp,
                                    overrideColor = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    listOf("Critical", "High", "Medium", "Low").forEach { option ->
                                        RadioButtonWithText(
                                            selected = selectedSaverity == option,
                                            title = option,
                                            compact = true,
                                            bodyContent = {
                                                BodySmallText(
                                                    text = option,
                                                    fontSize = 10.textSdp,
                                                    maxLines = 1,
                                                    softWrap = false,
                                                    overflow = TextOverflow.Ellipsis,
                                                    overrideColor = if (selectedSaverity == option) {
                                                        MaterialTheme.colorScheme.onPrimary
                                                    } else {
                                                        MaterialTheme.colorScheme.onBackground
                                                    },
                                                )
                                            },
                                            onSelectedChanged = { selectedSaverity = option },
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                }

                            }

                            SpacerHeight(5.sdp)
                            TextButton(
                                text = "Submit to Azure DevOps",
                                enabled = description.isNotEmpty() && description.length >= 3,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(1.sdp)
                                    .align(Alignment.CenterHorizontally)
                            ) {
                                /*viewModel.logIssue(
                                    LogIssueRequestEntity(
                                        title = "",
                                        description = description,
                                        areaPath = area.takeIf { it.isNotEmpty() }
                                            ?: "AikDigital\\Android Bug",
                                        customStepsToReproduce = stepsList.toList(),
                                        reproduceStepsRoute = routeTrail,
                                        issueType = "Bug",
                                        assignedTo = selectedAssignedItem.emailAddress,
                                        base64Screenshot = ""
                                    )
                                )*/
                                if (screenShotBitmap != null) {
                                    screenShotBase64 = screenShotBitmap?.toBase64()
                                    viewModel.reportIssueWithAttachment(
                                        uploadRequest = UploadAttachmentRequestEntity(
                                            base64Screenshot = testBase64
                                        ),
                                        issueRequest = LogIssueRequestEntity(
                                            title = "",
                                            description = description,
                                            areaPath = area.takeIf { it.isNotEmpty() }
                                                ?: "AikDigital\\Android Bug",
                                            customStepsToReproduce = stepsList.toList(),
                                            reproduceStepsRoute = routeTrail,
                                            issueType = "Bug",
                                            assignedTo = selectedAssignedItem.emailAddress,
                                            base64Screenshot = screenShotBase64
                                        )
                                    )
                                } else {
                                    viewModel.logIssue(
                                        LogIssueRequestEntity(
                                            title = "",
                                            description = description,
                                            areaPath = area.takeIf { it.isNotEmpty() }
                                                ?: "AikDigital\\Android Bug",
                                            customStepsToReproduce = stepsList.toList(),
                                            reproduceStepsRoute = routeTrail,
                                            issueType = "Bug",
                                            assignedTo = selectedAssignedItem.emailAddress,
                                            base64Screenshot = ""
                                        )
                                    )
                                }
                            }
                            SpacerHeight(30.sdp)
                        }
                    } else {
                        screenShotBitmap?.let {
                            ImageEditorDebugTool(bitmap = it) {
                                screenShotBitmap = it
                                showImageEditor = false
                            }
                        }
                    }
                }
            )
            if (showLoader) {
                ShowLoader()
            }
        }

        when (logIssueState) {
            is ResponseStates.Loading -> {
                LaunchedEffect(Unit) {
                    showLoader = true
                }
            }

            is ResponseStates.Success -> LaunchedEffect(Unit) {
                showLoader = false
                onClickCallBack()
                viewModel.clearLogIssueState()
                context.showDebugToast("Issue reported")
            }

            is ResponseStates.Failure -> LaunchedEffect(Unit) {
                if ((logIssueState as ResponseStates.Failure).httpCode == 200) {
                    onClickCallBack()
                    context.showDebugToast("Issue reported")
                } else {
                    context.showDebugToast("Something went wrong!")
                }
                viewModel.clearLogIssueState()
                showLoader = false
            }

            else -> {
                LaunchedEffect(Unit) {
                    showLoader = false
                    viewModel.clearLogIssueState()
                }
            }
        }

    }
}

@Composable
fun StepsToReproduceSection(
    steps: SnapshotStateList<String>,
    onStepsChanged: (List<String>) -> Unit
) {
    Column {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LabelMediumText(
                text = "Steps to Reproduce (Optional)",
                fontSize = 14.textSdp,
                overrideColor = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
            SpacerWeight(1f)
            // Add icon to add a new input field
            ResourceImage(
                image = R.drawable.ic_add,
                modifier = Modifier.safeClickable {
                    steps.add("") // Add empty step
                    onStepsChanged(steps)
                }
            )
        }

        SpacerHeight(10.sdp)
        // Show fields only if we have steps
        if (steps.isNotEmpty()) {
            steps.forEachIndexed { index, step ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextInputFieldApp(
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 5.dp),
                        value = step,
                        singleLine = false,
                        maxLines = 1,
                        onValueChange = {
                            steps[index] = it
                            onStepsChanged(steps)
                        },
                        labelTextSize = 14.textSdp,
                        fieldLabel = "Step ${index + 1}",
                        placeholder = "Enter step",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Unspecified)
                    )

                    // Delete icon to remove the input field
                    ResourceImage(
                        image = R.drawable.ic_delete,
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .safeClickable {
                                steps.removeAt(index)
                                onStepsChanged(steps)
                            }
                    )
                }
            }
        }
    }
}

fun getDrawableBase64(context: Context, drawableResId: Int): String {
    val bitmap = BitmapFactory.decodeResource(context.resources, drawableResId)
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.NO_WRAP)
}



fun captureMainScreen(activity: Activity, callback: (Bitmap?) -> Unit) {
    val window = activity.window
    val bitmap = createBitmap(window.decorView.width, window.decorView.height)
    try {
        PixelCopy.request(window, bitmap, { copyResult ->
            if (copyResult == PixelCopy.SUCCESS) {
                callback(bitmap)
            } else {
                callback(null)
            }
        }, Handler(Looper.getMainLooper()))
    } catch (e: IllegalArgumentException) {
        callback(null)
    }
}


@Preview
@Composable
fun PreviewDialog() {
    DebugIssueDialog(routeTrail = "")
}