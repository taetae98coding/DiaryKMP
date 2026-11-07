@file:OptIn(ExperimentalComposeUiApi::class)

package io.github.taetae98coding.diary.feature.setting.ui.mcp

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry

internal actual fun plainTextClipEntry(text: String): ClipEntry = ClipEntry.withPlainText(text)
