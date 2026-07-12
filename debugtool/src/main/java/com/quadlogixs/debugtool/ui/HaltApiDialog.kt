package com.quadlogixs.debugtool.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.quadlogixs.debugtool.ui.components.BaseButton
import com.quadlogixs.debugtool.ui.components.BodySmallText
import com.quadlogixs.debugtool.ui.components.CardContainer
import com.quadlogixs.debugtool.ui.components.LabelMediumText
import com.quadlogixs.debugtool.ui.components.TitleMediumText
import com.quadlogixs.debugtool.ui.components.ResourceImage
import com.quadlogixs.debugtool.ui.components.TextInputFieldApp
import com.quadlogixs.debugtool.ui.components.safeClickable
import com.quadlogixs.debugtool.ui.components.SpaceDefault
import com.quadlogixs.debugtool.ui.components.SpacerHeight
import com.quadlogixs.debugtool.ui.components.SpacerWeight
import com.quadlogixs.debugtool.ui.components.SpacerWidth
import com.quadlogixs.debugtool.ui.components.sdp
import com.quadlogixs.debugtool.ui.showDebugToast
import com.quadlogixs.debugtool.ui.components.textSdp
import com.quadlogixs.debugtool.ui.components.HorizontalLineDivider
import com.quadlogixs.debugtool.core.HaltHeaderModel
import com.quadlogixs.debugtool.core.MockApiResponseStore
import com.quadlogixs.debugtool.core.MockResponsePreset
import com.quadlogixs.debugtool.core.SavedMockResponse
import com.quadlogixs.debugtool.core.network.HaltInterceptResult
import com.quadlogixs.debugtool.core.di.DebugApiHalter
import okhttp3.Headers
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun HaltApiDialog() {
    val pending = DebugApiHalter.pending.collectAsState().value
    val context = LocalContext.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    pending?.let { interc ->
        when (interc) {
            is DebugApiHalter.Interception.Req -> {
                var text by remember { mutableStateOf(beautifyJson(interc.originalJson)) }
                var url by remember { mutableStateOf(interc.request.url.toString()) }

                var headerJson by remember {
                    mutableStateOf(headersToJson(interc.request.headers))
                }

                Dialog(onDismissRequest = {}) {
                    CardContainer(
                        modifier = Modifier
                            .fillMaxWidth(), content = {
                            Column(
                                modifier = Modifier
                                    .padding(10.sdp)
                                    .fillMaxSize()){
                                SpaceDefault()
                                Row {
                                    TitleMediumText(
                                        text = "Edit & Send Request",
                                        overrideColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                    SpacerWeight(1f)
                                    ResourceImage(
                                        image = com.quadlogixs.debugtool.R.drawable.ic_close_receipt,
                                        modifier = Modifier
                                            .size(18.sdp)
                                            .safeClickable {
                                                interc.deferred.complete(
                                                    HaltInterceptResult(
                                                        url = interc.request.url.toString(),
                                                        bodyJson = interc.originalJson,
                                                        headers= interc.request.headers
                                                    )
                                                )
                                            })
                                }
                                SpaceDefault()
                                HorizontalLineDivider(horizontalPadding = 0.dp)
                                        Column(
                                            modifier = Modifier.weight(1f)
                                                .verticalScroll(rememberScrollState())
                                        ) {
                                            SpacerHeight(5.sdp)
                                            Box(
                                                modifier = Modifier.fillMaxWidth(),
                                                contentAlignment = Alignment.TopEnd
                                            ) {
                                                TextInputFieldApp(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(top = 5.dp),
                                                    value = url,
                                                    singleLine = false,
                                                    maxLines = 3,
                                                    heightField = 70.sdp,
                                                    onValueChange = {
                                                        url = it
                                                    },
                                                    labelTextSize = 14.textSdp,
                                                    fieldLabel = "URL",
                                                    placeholder = "Enter Request URL",
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Unspecified)
                                                )
                                                Column {
                                                    SpacerHeight(30.sdp)
                                                    ResourceImage(
                                                        image = com.quadlogixs.debugtool.R.drawable.ic_copy,
                                                        modifier = Modifier
                                                            .size(18.sdp)
                                                            .safeClickable {
                                                                clipboardManager.setText(
                                                                    AnnotatedString(
                                                                        url
                                                                    )
                                                                )
                                                                context.showDebugToast("Url copied to clipboard")
                                                            })
                                                }
                                            }
                                            TextInputFieldApp(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(350.sdp)
                                                    .padding(top = 5.dp),
                                                value = headerJson,
                                                maxLines = Int.MAX_VALUE,
                                                singleLine = false,
                                                heightField = 350.sdp,
                                                onValueChange = { headerJson = it },
                                                fieldLabel = "Headers",
                                                placeholder = "Enter Header Json"
                                            )
                                            SpacerHeight(10.sdp)

                                            //BodySmallText(text = "${interc.request.url}")
                                            Box(
                                                modifier = Modifier.fillMaxWidth(),
                                                contentAlignment = Alignment.TopEnd
                                            ) {
                                                TextInputFieldApp(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(350.sdp)
                                                        .padding(top = 5.dp),
                                                    value = text,
                                                    labelTextSize = 14.textSdp,
                                                    singleLine = false,
                                                    heightField = 350.sdp,
                                                    maxLines = Int.MAX_VALUE,
                                                    onValueChange = {
                                                        text = it
                                                    },
                                                    fieldLabel = "Request",
                                                    placeholder = "Enter Request Json",
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Unspecified)
                                                )
                                                Column {
                                                    SpacerHeight(30.sdp)
                                                    ResourceImage(
                                                        image = com.quadlogixs.debugtool.R.drawable.ic_copy,
                                                        modifier = Modifier
                                                            .size(18.sdp)
                                                            .safeClickable {
                                                                clipboardManager.setText(
                                                                    AnnotatedString(
                                                                        text
                                                                    )
                                                                )
                                                                context.showDebugToast("Json copied to clipboard")
                                                            })
                                                }

                                            }

                                        }
                                        SpaceDefault()
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            BaseButton(
                                                modifier = Modifier.height(30.dp),
                                                primary = false,
                                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                                borderColor = MaterialTheme.colorScheme.outline,
                                                shape = MaterialTheme.shapes.small,
                                                enabled = true,
                                                content = {
                                                    LabelMediumText(
                                                        text = "Use Original",
                                                        fontSize = 12.textSdp,
                                                        overrideColor = MaterialTheme.colorScheme.onTertiary
                                                    )
                                                },
                                                onClick = {
                                                    interc.deferred.complete(
                                                        HaltInterceptResult(
                                                            url = interc.request.url.toString(),
                                                            bodyJson = interc.originalJson,
                                                            headers= interc.request.headers
                                                        )
                                                    )
                                                }
                                            )

                                            SpacerWidth(10.sdp)
                                            BaseButton(
                                                modifier = Modifier.height(30.dp),
                                                primary = true,
                                                containerColor = MaterialTheme.colorScheme.secondary,
                                                borderColor = Color.Unspecified,
                                                shape = MaterialTheme.shapes.small,
                                                enabled = true,
                                                content = {
                                                    LabelMediumText(
                                                        text = "Proceed",
                                                        fontSize = 12.textSdp,
                                                        overrideColor = MaterialTheme.colorScheme.background
                                                    )
                                                },
                                                onClick = {
                                                    if (text.isValidJson() && url.isNotBlank() && headerJson.isValidJson()) {
                                                        /* interc.deferred.complete(
                                                             HaltInterceptResult(
                                                                 url = url,
                                                                 bodyJson = text
                                                             )
                                                         )*/
                                                        interc.deferred.complete(
                                                            HaltInterceptResult(
                                                                url = url,
                                                                bodyJson = text,
                                                                headers = jsonToOkHttpHeaders(headerJson)
                                                            )
                                                        )

                                                    } else {
                                                        context.showDebugToast("Invalid Json Data")
                                                    }
                                                }
                                            )

                                        }
                                    }


                        }
                    )
                }
            }

            is DebugApiHalter.Interception.Resp -> {
                var text by remember { mutableStateOf(interc.originalJson) }

                Dialog(onDismissRequest = {}) {
                    CardContainer(
                        modifier = Modifier
                            .fillMaxWidth(), content = {
                            Column(
                                modifier = Modifier
                                    .padding(10.sdp)
                                    .fillMaxWidth()
                            ) {
                                SpaceDefault()
                                Row {
                                    TitleMediumText(
                                        text = "Edit & Send Request",
                                        overrideColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                    SpacerWeight(1f)
                                    ResourceImage(
                                        image = com.quadlogixs.debugtool.R.drawable.ic_close_receipt,
                                        modifier = Modifier
                                            .size(18.sdp)
                                            .safeClickable {
                                                interc.deferred.complete(interc.originalJson)
                                            })
                                }
                                SpaceDefault()
                                HorizontalLineDivider(horizontalPadding = 0.dp)
                                SpacerHeight(5.sdp)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    BodySmallText(text = "${interc.response?.request?.url?.toString() ?: ""}")
                                    SpacerWidth(5.sdp)
                                    ResourceImage(
                                        image = com.quadlogixs.debugtool.R.drawable.ic_copy,
                                        modifier = Modifier
                                            .size(18.sdp)
                                            .safeClickable {
                                                clipboardManager.setText(
                                                    AnnotatedString(
                                                        interc.response?.request?.url.toString()
                                                            ?: ""
                                                    )
                                                )
                                                context.showDebugToast("Url copied to clipboard")
                                            })
                                }
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.TopEnd
                                ) {
                                    TextInputFieldApp(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(350.sdp)
                                            .padding(top = 5.dp),
                                        value = text,
                                        labelTextSize = 14.textSdp,
                                        singleLine = false,
                                        heightField = 350.sdp,
                                        maxLines = Int.MAX_VALUE,
                                        onValueChange = {
                                            text = it
                                        },
                                        fieldLabel = "Response",
                                        placeholder = "Enter Respose Json",
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Unspecified)
                                    )
                                    Column {
                                        SpacerHeight(30.sdp)
                                        ResourceImage(
                                            image = com.quadlogixs.debugtool.R.drawable.ic_copy,
                                            modifier = Modifier
                                                .size(18.sdp)
                                                .safeClickable {
                                                    clipboardManager.setText(
                                                        AnnotatedString(
                                                            text
                                                        )
                                                    )
                                                    context.showDebugToast("Json copied to clipboard")
                                                })
                                    }
                                }
                                SpaceDefault()
                                HaltMockResponseActions(
                                    onApplyPreset = { preset ->
                                        text = when (preset) {
                                            MockResponsePreset.EMPTY -> MockApiResponseStore.PRESET_EMPTY_BODY
                                            MockResponsePreset.UNAUTHORIZED -> MockApiResponseStore.PRESET_401_BODY
                                            MockResponsePreset.SERVER_ERROR -> MockApiResponseStore.PRESET_500_BODY
                                            MockResponsePreset.TIMEOUT -> MockApiResponseStore.PRESET_EMPTY_BODY
                                        }
                                    },
                                    onSaveAsMock = { preset ->
                                        val requestUrl = interc.response?.request?.url?.toString()
                                        if (requestUrl.isNullOrBlank()) {
                                            context.showDebugToast("Unable to save mock — missing URL")
                                            return@HaltMockResponseActions
                                        }
                                        val method = interc.response?.request?.method ?: "POST"
                                        val body = when (preset) {
                                            null -> if (text.isValidJson()) text else interc.originalJson
                                            MockResponsePreset.EMPTY -> MockApiResponseStore.PRESET_EMPTY_BODY
                                            MockResponsePreset.UNAUTHORIZED -> MockApiResponseStore.PRESET_401_BODY
                                            MockResponsePreset.SERVER_ERROR -> MockApiResponseStore.PRESET_500_BODY
                                            MockResponsePreset.TIMEOUT -> MockApiResponseStore.PRESET_EMPTY_BODY
                                        }
                                        val mock = if (preset != null) {
                                            MockApiResponseStore.createPreset(
                                                urlPattern = requestUrl,
                                                httpMethod = method,
                                                preset = preset,
                                            )
                                        } else {
                                            SavedMockResponse(
                                                urlPattern = requestUrl,
                                                httpMethod = method,
                                                statusCode = interc.response?.code ?: 200,
                                                responseBody = body,
                                                label = "Saved from Halt",
                                            )
                                        }
                                        MockApiResponseStore.save(mock)
                                        context.showDebugToast("Mock saved for this URL")
                                    },
                                )
                                SpaceDefault()
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    BaseButton(
                                        modifier = Modifier.height(30.dp),
                                        primary = false,
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                        borderColor = MaterialTheme.colorScheme.outline,
                                        shape = MaterialTheme.shapes.small,
                                        enabled = true,
                                        content = {
                                            LabelMediumText(
                                                text = "Use Original",
                                                fontSize = 12.textSdp,
                                                overrideColor = MaterialTheme.colorScheme.onTertiary
                                            )
                                        },
                                        onClick = {
                                            interc.deferred.complete(interc.originalJson)
                                        }
                                    )

                                    SpacerWidth(10.sdp)
                                    BaseButton(
                                        modifier = Modifier.height(30.dp),
                                        primary = true,
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        borderColor = Color.Unspecified,
                                        shape = MaterialTheme.shapes.small,
                                        enabled = true,
                                        content = {
                                            LabelMediumText(
                                                text = "Proceed",
                                                fontSize = 12.textSdp,
                                                overrideColor = MaterialTheme.colorScheme.background
                                            )
                                        },
                                        onClick = {
                                            if (text.isValidJson()) {
                                                interc.deferred.complete(text)
                                            } else {
                                                context.showDebugToast("Invalid Json Request")
                                            }
                                        }
                                    )

                                }
                            }
                        })
                }

            }
        }
    }
}

@Composable
private fun HaltMockResponseActions(
    onApplyPreset: (MockResponsePreset) -> Unit,
    onSaveAsMock: (MockResponsePreset?) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        LabelMediumText(
            text = "Saved Mocks",
            fontSize = 12.textSdp,
            overrideColor = MaterialTheme.colorScheme.onPrimary,
        )
        SpacerHeight(5.sdp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.sdp),
        ) {
            HaltMockPresetButton(label = "Empty {}") {
                onApplyPreset(MockResponsePreset.EMPTY)
            }
            HaltMockPresetButton(label = "401") {
                onApplyPreset(MockResponsePreset.UNAUTHORIZED)
            }
            HaltMockPresetButton(label = "500") {
                onApplyPreset(MockResponsePreset.SERVER_ERROR)
            }
        }
        SpacerHeight(8.sdp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.sdp),
        ) {
            HaltMockPresetButton(label = "Save as Mock") {
                onSaveAsMock(null)
            }
            HaltMockPresetButton(label = "Save 401") {
                onSaveAsMock(MockResponsePreset.UNAUTHORIZED)
            }
            HaltMockPresetButton(label = "Save 500") {
                onSaveAsMock(MockResponsePreset.SERVER_ERROR)
            }
        }
    }
}

@Composable
private fun HaltMockPresetButton(
    label: String,
    onClick: () -> Unit,
) {
    BaseButton(
        modifier = Modifier.height(30.dp),
        primary = false,
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        borderColor = MaterialTheme.colorScheme.outline,
        shape = MaterialTheme.shapes.small,
        enabled = true,
        content = {
            LabelMediumText(
                text = label,
                fontSize = 11.textSdp,
                overrideColor = MaterialTheme.colorScheme.onTertiary,
            )
        },
        onClick = onClick,
    )
}


@Composable
fun EditableHeadersSection(
    headers: MutableList<HaltHeaderModel>,
    onHeadersChanged: (MutableList<HaltHeaderModel>) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TitleMediumText(text = "Headers")
            SpacerWeight(1f)

            ResourceImage(
                image = com.quadlogixs.debugtool.R.drawable.ic_add,
                modifier = Modifier
                    .size(20.sdp)
                    .safeClickable {
                        headers.add(HaltHeaderModel("", ""))
                        onHeadersChanged(headers)
                    }
            )
        }

        SpacerHeight(5.sdp)

        headers.forEachIndexed { index, header ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextInputFieldApp(
                    modifier = Modifier.weight(1f),
                    value = header.key,
                    onValueChange = {
                        headers[index] = headers[index].copy(key = it)
                        onHeadersChanged(headers)
                    },
                    fieldLabel = "Key"
                )

                SpacerWidth(5.sdp)

                TextInputFieldApp(
                    modifier = Modifier.weight(1f),
                    value = header.value,
                    onValueChange = {
                        headers[index] = headers[index].copy(value = it)
                        onHeadersChanged(headers)
                    },
                    fieldLabel = "Value"
                )

                SpacerWidth(5.sdp)

                ResourceImage(
                    image = com.quadlogixs.debugtool.R.drawable.ic_delete,
                    modifier = Modifier
                        .size(20.sdp)
                        .safeClickable {
                            headers.removeAt(index)
                            onHeadersChanged(headers)
                        }
                )
            }

            SpacerHeight(5.sdp)
        }
    }
}

fun beautifyJson(json: String): String {
    return try {
        when {
            json.trim().startsWith("{") -> JSONObject(json).toString(4)
            json.trim().startsWith("[") -> JSONArray(json).toString(4)
            else -> json // not valid JSON object/array
        }
    } catch (e: Exception) {
        json // fallback to original
    }
}

fun headersToJson(headers: Headers): String {
    val json = JSONObject()
    headers.names().forEach { key ->
        json.put(key, headers[key])
    }
    return json.toString(4) // pretty print
}
fun jsonToOkHttpHeaders(jsonString: String): Headers {
    val builder = Headers.Builder()

    try {
        val jsonObject = JSONObject(jsonString)
        val keys = jsonObject.keys()

        while (keys.hasNext()) {
            val key = keys.next()
            val value = jsonObject.optString(key)

            if (key.isNotBlank()) {
                builder.add(key.trim(), value.trim())
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return builder.build()
}



fun String.isValidJson(): Boolean {
    return try {
        when {
            this.trim().startsWith("{") -> {
                JSONObject(this)
                true
            }

            this.trim().startsWith("[") -> {
                JSONArray(this)
                true
            }

            else -> false
        }
    } catch (e: Exception) {
        false
    }
}

