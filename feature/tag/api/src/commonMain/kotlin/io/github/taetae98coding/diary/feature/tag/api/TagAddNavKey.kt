package io.github.taetae98coding.diary.feature.tag.api

import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
public data class TagAddNavKey(
    // 태그 입력에서 연 화면만 추가 결과를 돌려받고, 화면이 여러 번 쌓여도 서로 구분되도록 요청마다 다른 키를 받는다.
    val requestKey: Uuid? = null,
) : ScreenNavKey {
    override val screenName: String get() = "TagAdd"
}
