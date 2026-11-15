package io.github.taetae98coding.diary.feature.routine.ui

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineAddNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagHomeNavKey
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.flow.emptyFlow

class RoutineEntryTagDetailPaneTest :
    FunSpec({
        test("TC-df3776fd-72c1-4073-ab11-c00755a0d436 TagDetail 루틴 탭에서 이어진 루틴 추가와 루틴 상세는 목록·상세 배치에 참여하지 않는다") {
            val tagDetailKey = TagDetailNavKey(id = Arb.kotlinUuid().next())
            val tagDetailBackStack = listOf(TagDetailPaneOtherNavKey, TagHomeNavKey, tagDetailKey)
            val addKey = RoutineAddNavKey(initialTagId = tagDetailKey.id)
            val detailKey = RoutineDetailNavKey(id = Arb.kotlinUuid().next(), isStandalone = true)

            metadataOf(backStack = tagDetailBackStack + addKey, key = addKey).shouldBeEmpty()
            metadataOf(backStack = tagDetailBackStack + detailKey, key = detailKey).shouldBeEmpty()
        }
    })

private fun metadataOf(
    backStack: List<ScreenNavKey>,
    key: ScreenNavKey,
): Map<String, Any> {
    val provider =
        entryProvider<ScreenNavKey> {
            routineEntry(
                backStack = NavBackStack(*backStack.toTypedArray()),
                homeReselectEvent = emptyFlow(),
            )
        }

    return provider(key).metadata
}

// `더보기`처럼 태그 목록으로 들어가는 화면을 대신한다. 그 기능의 화면 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object TagDetailPaneOtherNavKey : ScreenNavKey {
    override val screenName: String
        get() = "Other"
}
