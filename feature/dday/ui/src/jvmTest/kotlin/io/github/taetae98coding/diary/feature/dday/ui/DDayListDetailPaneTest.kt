@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.dday.ui

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.dday.api.DDayAddNavKey
import io.github.taetae98coding.diary.feature.dday.api.DDayDetailNavKey
import io.github.taetae98coding.diary.feature.dday.api.DDayHomeFilterNavKey
import io.github.taetae98coding.diary.feature.dday.api.DDayHomeNavKey
import io.github.taetae98coding.diary.feature.dday.api.isDDayAddOnDetailPane
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagHomeNavKey
import io.github.taetae98coding.diary.feature.tag.api.navigateToTagAddFromFilter
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class DDayListDetailPaneTest :
    FunSpec({
        test("TC-168d5f2f-9a83-44a0-8681-9b4d7d7c5836 상세 영역에 디데이 추가가 놓이면 목록의 추가 수단을 두지 않는다") {
            val backStackCases =
                listOf(
                    listOf(ListDetailOtherNavKey, DDayHomeNavKey),
                    listOf(ListDetailOtherNavKey, DDayHomeNavKey, DDayHomeFilterNavKey),
                    listOf(ListDetailOtherNavKey, DDayHomeNavKey, detailKey(), DDayAddNavKey()),
                )

            backStackCases.forEach { backStack ->
                backStack.isDDayAddOnDetailPane() shouldBe true
            }
        }

        test("TC-38ea0ab8-c88f-42a0-b637-3633db1f1ab7 상세 영역에 디데이 상세가 놓이면 목록의 추가 수단을 둔다") {
            val backStackCases =
                listOf(
                    listOf(ListDetailOtherNavKey, DDayHomeNavKey, detailKey()),
                    listOf(ListDetailOtherNavKey, DDayHomeNavKey, detailKey(), DDayHomeFilterNavKey),
                )

            backStackCases.forEach { backStack ->
                backStack.isDDayAddOnDetailPane() shouldBe false
            }
        }

        test("TC-4ff90937-184a-449b-8fe6-ac1b9b0ef240 디데이 목록에서 이어 진입한 디데이 추가와 상세는 상세 pane이다") {
            val detail = detailKey()
            val addBackStack = listOf(ListDetailOtherNavKey, DDayHomeNavKey, DDayAddNavKey())
            val detailBackStack = listOf(ListDetailOtherNavKey, DDayHomeNavKey, detail)
            val addOverDetailBackStack = listOf(ListDetailOtherNavKey, DDayHomeNavKey, detail, DDayAddNavKey())

            metadataOf(backStack = addBackStack, key = DDayAddNavKey()).keys shouldBe detailPaneMetadataKeys
            metadataOf(backStack = detailBackStack, key = detail).keys shouldBe detailPaneMetadataKeys
            metadataOf(backStack = addOverDetailBackStack, key = DDayAddNavKey()).keys shouldBe detailPaneMetadataKeys
            metadataOf(backStack = detailBackStack, key = DDayHomeNavKey).keys shouldBe listPaneMetadataKeys
        }

        test("TC-4fede1f8-2097-47ef-ae8b-15ab904755a1 다른 화면에서 진입한 디데이 상세와 추가는 배치에 참여하지 않는다") {
            val tagDetailKey = TagDetailNavKey(id = Arb.kotlinUuid().next())
            val detail = detailKey()
            val tagAddKey = DDayAddNavKey(initialTagId = tagDetailKey.id)
            val cases =
                listOf(
                    listOf(ListDetailCalendarNavKey, detail) to detail,
                    listOf(ListDetailTimetableNavKey, detail) to detail,
                    listOf(TagHomeNavKey, tagDetailKey, detail) to detail,
                    listOf(ListDetailSearchNavKey, detail) to detail,
                    listOf(TagHomeNavKey, tagDetailKey, tagAddKey) to tagAddKey,
                    // 디데이 목록 위에서 다른 화면을 거쳐 진입해도 목록과 함께 표시하지 않는다.
                    listOf(DDayHomeNavKey, ListDetailCalendarNavKey, detail) to detail,
                    listOf(DDayHomeNavKey, DDayAddNavKey(initialTagId = tagDetailKey.id)) to DDayAddNavKey(initialTagId = tagDetailKey.id),
                )

            cases.forEach { (backStack, key) ->
                metadataOf(backStack = backStack, key = key).shouldBeEmpty()
            }
        }

        test("TC-a9409ff5-1fe1-4b07-b18c-6f41aac570e3 목록의 뒤로가기는 상세 영역의 화면과 관계없이 더보기로 돌아간다") {
            val detailCases =
                listOf(
                    emptyList(),
                    listOf(DDayAddNavKey()),
                    listOf(detailKey()),
                    listOf(detailKey(), DDayAddNavKey()),
                )

            detailCases.forEach { detailKeyList ->
                val backStack = NavBackStack(ListDetailOtherNavKey, DDayHomeNavKey, *detailKeyList.toTypedArray())

                backStack.navigateUpFromDDayHome()

                backStack shouldContainExactly listOf(ListDetailOtherNavKey)
            }
        }

        test("TC-bc75a624-537e-4c24-9821-601267390281 디데이 상세가 놓여 있으면 새 디데이의 상세로 교체한다") {
            val first = detailKey()
            val second = detailKey()
            val backStack = NavBackStack(ListDetailOtherNavKey, DDayHomeNavKey, first)

            backStack.navigateToDDayDetail(id = second.id)

            backStack shouldContainExactly listOf(ListDetailOtherNavKey, DDayHomeNavKey, second)
        }

        test("TC-db2c9587-68c2-4531-b8e4-7974c519be40 목록에서 디데이를 고르면 쌓인 상세와 디데이 추가를 걷어내고 그 상세로 바꾼다") {
            val detailCases =
                listOf(
                    listOf(DDayAddNavKey()),
                    listOf(detailKey(), DDayAddNavKey()),
                )

            detailCases.forEach { detailKeyList ->
                val selected = detailKey()
                val backStack = NavBackStack(ListDetailOtherNavKey, DDayHomeNavKey, *detailKeyList.toTypedArray())

                backStack.navigateToDDayDetail(id = selected.id)

                backStack shouldContainExactly listOf(ListDetailOtherNavKey, DDayHomeNavKey, selected)
            }
        }

        test("TC-d498c6d5-5f5b-467d-b7f3-54a94ccb4f5f 목록의 디데이 추가는 초기 태그 없는 새 디데이 추가를 상세 위에 놓는다") {
            val detail = detailKey()
            val backStack = NavBackStack(ListDetailOtherNavKey, DDayHomeNavKey, detail)

            backStack.navigateToDDayAddFromHome()

            backStack shouldContainExactly listOf(ListDetailOtherNavKey, DDayHomeNavKey, detail, DDayAddNavKey())
            (backStack.last() as DDayAddNavKey).initialTagId.shouldBeNull()
        }

        test("TC-c1e8b9d4-5123-4000-965c-4405cff2f850 필터의 태그 추가는 필터를 닫고 TagAdd 화면으로 한 번 이동한다") {
            val backStack = NavBackStack(ListDetailOtherNavKey, DDayHomeNavKey, DDayHomeFilterNavKey)

            backStack.navigateToTagAddFromFilter()

            backStack shouldContainExactly listOf(ListDetailOtherNavKey, DDayHomeNavKey, TagAddNavKey())
        }

        test("TC-2a2bbcb7-49a4-4bb3-9c07-8314b47f7f5f TagAdd 화면에서 돌아오면 필터가 다시 열리지 않고 추가한 태그를 선택으로 돌려받지 않는다") {
            val backStack = NavBackStack(ListDetailOtherNavKey, DDayHomeNavKey, DDayHomeFilterNavKey)

            backStack.navigateToTagAddFromFilter()
            val tagAddKey = backStack.last() as TagAddNavKey
            backStack.removeLastOrNull()

            tagAddKey.requestKey.shouldBeNull()
            backStack shouldContainExactly listOf(ListDetailOtherNavKey, DDayHomeNavKey)
        }
    }) {
    companion object {
        private val detailPaneMetadataKeys: Set<String> =
            (ListDetailSceneStrategy.detailPane() + ListDetailSceneStrategy.preferredPaneSize(width = 0.5f)).keys

        private val listPaneMetadataKeys: Set<String> =
            (ListDetailSceneStrategy.listPane(detailPlaceholder = {}) + ListDetailSceneStrategy.preferredPaneSize(width = 0.5f)).keys

        private fun detailKey(): DDayDetailNavKey = DDayDetailNavKey(id = Arb.kotlinUuid().next())

        private fun metadataOf(
            backStack: List<ScreenNavKey>,
            key: ScreenNavKey,
        ): Map<String, Any> {
            val provider = entryProvider<ScreenNavKey> { dDayEntry(backStack = NavBackStack(*backStack.toTypedArray())) }

            return provider(key).metadata
        }
    }
}

// `더보기`처럼 디데이 목록으로 들어가는 화면을 대신한다. 그 기능의 화면 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object ListDetailOtherNavKey : ScreenNavKey {
    override val screenName: String
        get() = "Other"
}

private data object ListDetailCalendarNavKey : ScreenNavKey {
    override val screenName: String
        get() = "CalendarStub"
}

private data object ListDetailTimetableNavKey : ScreenNavKey {
    override val screenName: String
        get() = "TimetableStub"
}

private data object ListDetailSearchNavKey : ScreenNavKey {
    override val screenName: String
        get() = "SearchStub"
}
