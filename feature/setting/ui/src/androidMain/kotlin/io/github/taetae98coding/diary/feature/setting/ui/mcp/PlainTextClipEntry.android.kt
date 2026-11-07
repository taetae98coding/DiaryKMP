package io.github.taetae98coding.diary.feature.setting.ui.mcp

import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry

internal actual fun plainTextClipEntry(text: String): ClipEntry = ClipEntry(ClipData.newPlainText(text, text))
