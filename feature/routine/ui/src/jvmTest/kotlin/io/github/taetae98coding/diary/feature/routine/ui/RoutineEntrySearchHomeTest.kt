package io.github.taetae98coding.diary.feature.routine.ui

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineDetailNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagHomeNavKey
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.flow.emptyFlow

class RoutineEntrySearchHomeTest :
    FunSpec({
        test("TC-e058120f-c1a6-4f58-8697-8d728357a1be SearchHome에서 연 루틴 상세는 목록·상세 배치에 참여하지 않는다") {
            val backStackList =
                listOf(
                    listOf(SearchEntryMoreHomeNavKey, SearchEntrySearchHomeNavKey),
                    listOf(SearchEntryMoreHomeNavKey, SearchEntryMemoHomeNavKey, SearchEntrySearchHomeNavKey),
                    listOf(SearchEntryMoreHomeNavKey, TagHomeNavKey, SearchEntrySearchHomeNavKey),
                    listOf(RoutineHomeNavKey, SearchEntryMoreHomeNavKey, SearchEntrySearchHomeNavKey),
                )

            backStackList.forEach { searchBackStack ->
                // 전환 이력의 자리는 인스턴스로 찾으므로 검사하는 키를 그대로 전환 이력에 넣는다.
                val detailKey = RoutineDetailNavKey(id = Arb.kotlinUuid().next(), isStandalone = true)
                val backStack = searchBackStack + detailKey
                val provider =
                    entryProvider<ScreenNavKey> {
                        routineEntry(
                            backStack = NavBackStack(*backStack.toTypedArray()),
                            homeReselectEvent = emptyFlow(),
                        )
                    }

                provider(detailKey).metadata.shouldBeEmpty()
            }
        }
    })

// `더보기`, 메모 목록, 검색 화면을 대신한다. 그 기능의 화면 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object SearchEntryMoreHomeNavKey : ScreenNavKey {
    override val screenName: String
        get() = "MoreHomeStandIn"
}

private data object SearchEntryMemoHomeNavKey : ScreenNavKey {
    override val screenName: String
        get() = "MemoHomeStandIn"
}

private data object SearchEntrySearchHomeNavKey : ScreenNavKey {
    override val screenName: String
        get() = "SearchHomeStandIn"
}
