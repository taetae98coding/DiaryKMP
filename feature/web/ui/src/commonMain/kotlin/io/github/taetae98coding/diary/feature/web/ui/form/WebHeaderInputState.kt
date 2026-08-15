package io.github.taetae98coding.diary.feature.web.ui.form

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList

@Stable
internal class WebHeaderRowState(
    initialName: String,
    initialValue: String,
) {
    val nameState: TextFieldState = TextFieldState(initialText = initialName)
    val valueState: TextFieldState = TextFieldState(initialText = initialValue)

    val name: String
        get() = nameState.text.toString()

    val value: String
        get() = valueState.text.toString()
}

@Stable
internal class WebHeaderInputState(
    initialRowList: List<WebHeaderRowState>,
) {
    val rowList: SnapshotStateList<WebHeaderRowState> = initialRowList.toMutableStateList()

    fun add() {
        rowList.add(WebHeaderRowState(initialName = "", initialValue = ""))
    }

    fun remove(row: WebHeaderRowState) {
        rowList.remove(row)
    }

    fun clear() {
        rowList.clear()
    }

    companion object {
        val Saver: Saver<WebHeaderInputState, List<String>> =
            Saver(
                save = { state -> state.rowList.flatMap { row -> listOf(row.name, row.value) } },
                restore = { saved ->
                    WebHeaderInputState(
                        initialRowList =
                            saved
                                .chunked(size = 2)
                                .map { chunk -> WebHeaderRowState(initialName = chunk.first(), initialValue = chunk.last()) },
                    )
                },
            )
    }
}

@Composable
internal fun rememberWebHeaderInputState(initialRowList: List<WebHeaderRowState> = emptyList()): WebHeaderInputState =
    rememberSaveable(saver = WebHeaderInputState.Saver) {
        WebHeaderInputState(initialRowList = initialRowList)
    }
