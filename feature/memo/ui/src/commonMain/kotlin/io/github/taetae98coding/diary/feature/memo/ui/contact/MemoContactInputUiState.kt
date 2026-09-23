package io.github.taetae98coding.diary.feature.memo.ui.contact

import io.github.taetae98coding.diary.core.model.contact.Contact

internal data class MemoContactInputUiState(
    val selectedContactList: List<Contact> = emptyList(),
)
