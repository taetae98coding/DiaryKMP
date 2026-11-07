@file:OptIn(ExperimentalComposeUiApi::class)

package io.github.taetae98coding.diary.feature.setting.ui.mcp

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import java.awt.datatransfer.StringSelection

internal actual fun plainTextClipEntry(text: String): ClipEntry = ClipEntry(StringSelection(text))
