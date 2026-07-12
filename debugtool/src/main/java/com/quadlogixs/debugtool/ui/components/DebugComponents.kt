package com.quadlogixs.debugtool.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quadlogixs.debugtool.api.AssignedTo
import com.quadlogixs.debugtool.R
import com.quadlogixs.debugtool.ui.theme.LocalExtraThemeColors

val SpaceDefault: Dp = 8.dp

@Composable
fun SpaceDefault() = Spacer(modifier = Modifier.height(SpaceDefault))

val Int.sdp: Dp
    @Composable get() = this.dp

val Int.textSdp: TextUnit
    @Composable get() = this.sp

@Composable
fun SpacerHeight(height: Dp) = Spacer(modifier = Modifier.height(height))

@Composable
fun SpacerWidth(width: Dp) = Spacer(modifier = Modifier.width(width))

@Composable
fun RowScope.SpacerWeight(weight: Float) = Spacer(modifier = Modifier.weight(weight))

fun Modifier.safeClickable(enabled: Boolean = true, onClick: () -> Unit): Modifier =
    clickable(
        enabled = enabled,
        indication = null,
        interactionSource = MutableInteractionSource(),
        onClick = onClick,
    )

@Composable
fun LabelSmallText(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    overrideColor: Color = color,
    fontSize: TextUnit = MaterialTheme.typography.labelSmall.fontSize,
    textAlign: TextAlign = TextAlign.Start,
    fontWeight: FontWeight? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = fontSize, fontWeight = fontWeight),
        color = overrideColor,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Composable
fun LabelMediumText(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    overrideColor: Color = color,
    fontSize: TextUnit = MaterialTheme.typography.labelMedium.fontSize,
    fontWeight: FontWeight? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(fontSize = fontSize, fontWeight = fontWeight),
        color = overrideColor,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Composable
fun LabelLargeText(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    overrideColor: Color = color,
    fontSize: TextUnit = MaterialTheme.typography.labelLarge.fontSize,
    fontWeight: FontWeight? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(fontSize = fontSize, fontWeight = fontWeight),
        color = overrideColor,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Composable
fun BodySmallText(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    overrideColor: Color = color,
    fontSize: TextUnit = MaterialTheme.typography.bodySmall.fontSize,
    textAlign: TextAlign = TextAlign.Start,
    fontWeight: FontWeight? = null,
    maxLines: Int = Int.MAX_VALUE,
    softWrap: Boolean = true,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.bodySmall.copy(fontSize = fontSize, fontWeight = fontWeight),
        color = overrideColor,
        textAlign = textAlign,
        maxLines = maxLines,
        softWrap = softWrap,
        overflow = overflow,
    )
}

@Composable
fun BodyMediumText(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    overrideColor: Color = color,
    fontSize: TextUnit = MaterialTheme.typography.bodyMedium.fontSize,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(fontSize = fontSize),
        color = overrideColor,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Composable
fun TitleLargeText(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    overrideColor: Color = color,
    fontSize: TextUnit = MaterialTheme.typography.titleLarge.fontSize,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.titleLarge.copy(fontSize = fontSize, fontWeight = fontWeight),
        color = overrideColor,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Composable
fun TitleMediumText(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    overrideColor: Color = color,
    fontSize: TextUnit = MaterialTheme.typography.titleMedium.fontSize,
    fontWeight: FontWeight? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontSize = fontSize, fontWeight = fontWeight),
        color = overrideColor,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Composable
fun CardContainer(
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 0.dp,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    contentAlignment: Alignment = Alignment.TopStart,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val clickableModifier = if (onClick != null) modifier.safeClickable { onClick() } else modifier
    Surface(
        modifier = clickableModifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        color = containerColor,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
    ) {
        Box(
            modifier = Modifier.padding(12.dp),
            contentAlignment = contentAlignment,
            content = content,
        )
    }
}

@Composable
fun AppSwitch(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Switch(
        modifier = modifier,
        checked = isChecked,
        onCheckedChange = onCheckedChange,
    )
}

@Composable
fun <T> ResourceImage(
    modifier: Modifier = Modifier,
    image: T?,
    colorFilter: ColorFilter? = null,
    alignment: Alignment = Alignment.Center,
    alpha: Float = DefaultAlpha,
    contentScale: ContentScale = ContentScale.Fit,
) {
    when (image) {
        is Int -> Image(
            painter = painterResource(id = image),
            contentDescription = null,
            modifier = modifier,
            colorFilter = colorFilter,
            alignment = alignment,
            contentScale = contentScale,
            alpha = alpha,
        )
        is Bitmap -> Image(
            bitmap = image.asImageBitmap(),
            contentDescription = null,
            modifier = modifier,
            colorFilter = colorFilter,
            alignment = alignment,
            contentScale = contentScale,
            alpha = alpha,
        )
        is String -> if (image.isNotEmpty()) {
            Text(text = image, modifier = modifier)
        }
        null -> Unit
        else -> Unit
    }
}

@Composable
fun TextInputFieldApp(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    onFieldClick: (() -> Unit)? = null,
    placeholder: String = "",
    fieldLabel: String = "",
    labelTextSize: TextUnit = 14.sp,
    supportText: String = "",
    readOnly: Boolean = false,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    heightField: Dp = 56.dp,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    transformation: VisualTransformation = visualTransformation,
    onDoneAction: () -> Unit = {},
) {
    Column(modifier = modifier) {
        if (fieldLabel.isNotBlank()) {
            LabelLargeText(
                text = fieldLabel,
                fontSize = labelTextSize,
                fontWeight = FontWeight.Bold,
                overrideColor = MaterialTheme.colorScheme.onPrimary,
            )
            SpacerHeight(6.dp)
        }
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = heightField),
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            readOnly = readOnly,
            placeholder = if (placeholder.isNotBlank()) ({ Text(placeholder) }) else null,
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines,
            visualTransformation = transformation,
            keyboardOptions = keyboardOptions.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onDoneAction() }),
            leadingIcon = leading,
            trailingIcon = trailing,
        )
        if (supportText.isNotBlank()) {
            SpacerHeight(4.dp)
            BodySmallText(text = supportText, overrideColor = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    onFieldClick?.let { click ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.dp)
                .safeClickable { click() },
        )
    }
}

@Composable
fun ShowLoader(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun BaseButton(
    modifier: Modifier = Modifier,
    primary: Boolean = true,
    enabled: Boolean = true,
    onClick: () -> Unit,
    containerColor: Color = if (primary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
    borderColor: Color = Color.Unspecified,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(8.dp),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable () -> Unit,
) {
    Button(
        modifier = modifier,
        enabled = enabled,
        onClick = onClick,
        shape = shape,
        contentPadding = contentPadding,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        content = { content() },
    )
}

@Composable
fun BaseButton(
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    BaseButton(modifier = modifier, enabled = enabled, onClick = onClick) {
        Text(text)
    }
}

@Composable
fun TextButton(
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean = true,
    primary: Boolean = true,
    onClick: () -> Unit,
) = BaseButton(modifier = modifier, text = text, enabled = enabled, onClick = onClick)

@Composable
fun AppRadioButton(
    selected: Boolean,
    enabled: Boolean = true,
    onSelectedChanged: (() -> Unit)? = null,
) {
    RadioButton(
        selected = selected,
        enabled = enabled,
        onClick = { onSelectedChanged?.invoke() },
    )
}

@Composable
fun RadioButtonWithText(
    modifier: Modifier = Modifier,
    selected: Boolean,
    title: String = "",
    onSelectedChanged: ((Boolean) -> Unit)? = null,
    enabled: Boolean = true,
    compact: Boolean = false,
    bodyContent: @Composable () -> Unit = {
        BodySmallText(
            text = title,
            maxLines = 1,
            softWrap = false,
        )
    },
) {
    val content = @Composable {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppRadioButton(
                selected = selected,
                enabled = enabled,
                onSelectedChanged = { onSelectedChanged?.invoke(!selected) },
            )
            bodyContent()
        }
    }
    if (compact) {
        CompositionLocalProvider(
            LocalMinimumInteractiveComponentSize provides 0.dp,
        ) {
            content()
        }
    } else {
        content()
    }
}

@Composable
fun HorizontalLineDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    horizontalPadding: Dp = 10.dp,
    color: Color = MaterialTheme.colorScheme.outline,
) {
    HorizontalDivider(
        modifier = modifier.padding(horizontal = horizontalPadding),
        thickness = thickness,
        color = color,
    )
}

@Composable
fun NoRecentFoundItem(
    modifier: Modifier = Modifier,
    title: String,
    icon: Int = R.drawable.ic_no_history,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ResourceImage(image = icon, modifier = Modifier.size(48.dp))
        SpacerHeight(8.dp)
        BodySmallText(text = title, textAlign = TextAlign.Center)
    }
}

private fun assigneeInitials(name: String): String {
    val parts = name.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
    return when {
        parts.size >= 2 -> (parts[0].firstOrNull()?.uppercaseChar()?.toString().orEmpty()) +
            (parts[1].firstOrNull()?.uppercaseChar()?.toString().orEmpty())
        parts.size == 1 -> parts[0].firstOrNull()?.uppercaseChar()?.toString().orEmpty()
        else -> ""
    }
}

@Composable
fun AssignToItem(
    modifier: Modifier = Modifier,
    item: AssignedTo = AssignedTo(),
    isChecked: Boolean = false,
    onSelectItem: (AssignedTo) -> Unit = {},
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(50.sdp)
            .safeClickable { onSelectItem(item) },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.sdp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center,
            ) {
                TitleMediumText(text = assigneeInitials(item.name))
            }
            SpacerWidth(10.sdp)
            TitleMediumText(
                text = item.name,
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
            RadioButton(
                selected = isChecked,
                onClick = { onSelectItem(item) },
            )
        }
    }
}

@Composable
fun ItemWithSwitch(
    title: String,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    checked: Boolean = false,
    isChecked: Boolean = checked,
    icon: Int? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            ResourceImage(image = icon, modifier = Modifier.size(24.sdp))
            SpacerWidth(8.sdp)
        }
        LabelMediumText(text = title, modifier = Modifier.weight(1f))
        AppSwitch(isChecked = isChecked, onCheckedChange = onCheckedChange)
    }
}

fun isValidJson(input: String): Boolean =
    runCatching { com.google.gson.JsonParser.parseString(input) }.isSuccess
