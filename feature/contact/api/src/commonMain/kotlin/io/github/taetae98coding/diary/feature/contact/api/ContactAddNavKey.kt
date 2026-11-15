package io.github.taetae98coding.diary.feature.contact.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
public data class ContactAddNavKey(
    val initialTagId: Uuid? = null,
    // 화면이 여러 번 쌓여도 결과를 돌려줄 연락처 입력을 서로 구분하도록 요청마다 다른 키를 받는다.
    val requestKey: Uuid? = null,
    val initialName: String = "",
    val initialDescription: String = "",
    val initialPhoneNumberList: List<String> = emptyList(),
    val initialBirthday: LocalDate? = null,
    val isStandalone: Boolean = false,
) : ScreenNavKey {
    override val screenName: String get() = "ContactAdd"
}
