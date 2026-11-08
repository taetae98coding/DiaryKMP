package io.github.taetae98coding.diary.feature.dday.ui

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.dday.api.DDayAddNavKey
import io.github.taetae98coding.diary.feature.dday.api.DDayDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagHomeNavKey
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class DDayEntryTagDetailPaneTest :
    FunSpec({
        test("TC-2e9c1549-ff1d-4e1a-909a-3f437a5276c7 TagDetail 디데이 탭에서 이어진 디데이 추가와 디데이 상세는 목록·상세 배치에 참여하지 않는다") {
            val tagDetailKey = TagDetailNavKey(id = Arb.kotlinUuid().next())
            val tagDetailBackStack = listOf(TagDetailPaneOtherNavKey, TagHomeNavKey, tagDetailKey)
            val addKey = DDayAddNavKey(initialTagId = tagDetailKey.id)
            val detailKey = DDayDetailNavKey(id = Arb.kotlinUuid().next())

            metadataOf(backStack = tagDetailBackStack + addKey, key = addKey).shouldBeEmpty()
            metadataOf(backStack = tagDetailBackStack + detailKey, key = detailKey).shouldBeEmpty()
        }
    })

private fun metadataOf(
    backStack: List<ScreenNavKey>,
    key: ScreenNavKey,
): Map<String, Any> {
    val provider = entryProvider<ScreenNavKey> { dDayEntry(backStack = NavBackStack(*backStack.toTypedArray())) }

    return provider(key).metadata
}

// `더보기`처럼 태그 목록으로 들어가는 화면을 대신한다. 그 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object TagDetailPaneOtherNavKey : ScreenNavKey {
    override val screenName: String
        get() = "Other"
}
