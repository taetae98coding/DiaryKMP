package io.github.taetae98coding.diary.feature.checklist.ui.add

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import io.github.taetae98coding.diary.compose.core.input.DiaryDescriptionInputState
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInputState
import io.github.taetae98coding.diary.compose.core.input.rememberDiaryDescriptionInputState
import io.github.taetae98coding.diary.compose.core.input.rememberDiaryTitleInputState
import io.github.taetae98coding.diary.core.model.checklist.ChecklistDetail
import io.github.taetae98coding.diary.feature.checklist.ui.item.ChecklistItemInputState
import io.github.taetae98coding.diary.feature.checklist.ui.item.rememberChecklistItemInputState
import kotlin.uuid.Uuid

@Stable
internal class ChecklistAddFormState(
    val titleState: DiaryTitleInputState,
    val descriptionState: DiaryDescriptionInputState,
    val itemInputState: ChecklistItemInputState,
    val itemInputBringIntoViewRequester: BringIntoViewRequester,
    val scrollState: ScrollState,
    val snackbarHostState: SnackbarHostState,
    private val itemStateList: SnapshotStateList<ChecklistAddItem>,
) {
    val itemList: List<ChecklistAddItem>
        get() = itemStateList

    val detail: ChecklistDetail
        get() =
            ChecklistDetail(
                title = titleState.text.toString(),
                description = descriptionState.text.toString(),
            )

    val itemContentList: List<String>
        get() =
            buildList {
                addAll(itemStateList.map(ChecklistAddItem::content))
                if (itemInputState.isAddable) add(itemInputState.content)
            }

    fun addItem(): Boolean {
        if (!itemInputState.isAddable) return false

        itemStateList.add(ChecklistAddItem(key = Uuid.random().toString(), content = itemInputState.content))
        itemInputState.clear()
        return true
    }

    fun removeItem(key: String) {
        itemStateList.removeAll { item -> item.key == key }
    }

    fun clear() {
        titleState.clearText()
        descriptionState.clearText()
        itemInputState.clear()
        itemStateList.clear()
    }
}

@Composable
internal fun rememberChecklistAddFormState(initialItemList: List<ChecklistAddItem> = emptyList()): ChecklistAddFormState {
    val titleState = rememberDiaryTitleInputState()
    val descriptionState = rememberDiaryDescriptionInputState()
    val itemInputState = rememberChecklistItemInputState()
    val itemInputBringIntoViewRequester = remember { BringIntoViewRequester() }
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val itemStateList = rememberSaveable(saver = ChecklistAddItemListSaver) { mutableStateListOf(*initialItemList.toTypedArray()) }

    return remember(titleState, descriptionState, itemInputState, itemInputBringIntoViewRequester, scrollState, snackbarHostState, itemStateList) {
        ChecklistAddFormState(
            titleState = titleState,
            descriptionState = descriptionState,
            itemInputState = itemInputState,
            itemInputBringIntoViewRequester = itemInputBringIntoViewRequester,
            scrollState = scrollState,
            snackbarHostState = snackbarHostState,
            itemStateList = itemStateList,
        )
    }
}

private val ChecklistAddItemListSaver: Saver<SnapshotStateList<ChecklistAddItem>, List<String>> =
    Saver(
        save = { itemList -> itemList.flatMap { item -> listOf(item.key, item.content) } },
        restore = { saved ->
            saved
                .chunked(2) { (key, content) -> ChecklistAddItem(key = key, content = content) }
                .toMutableStateList()
        },
    )

private fun List<ChecklistAddItem>.toMutableStateList(): SnapshotStateList<ChecklistAddItem> = mutableStateListOf(*toTypedArray())
