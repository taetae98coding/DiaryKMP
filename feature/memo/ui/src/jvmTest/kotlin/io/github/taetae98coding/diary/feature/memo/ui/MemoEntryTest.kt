@file:OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)

package io.github.taetae98coding.diary.feature.memo.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.scene.BottomSheetSceneStrategy
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactDetailNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoHomeFilterNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoHomeNavKey
import io.github.taetae98coding.diary.feature.memo.api.navigateToMemoDetail
import io.github.taetae98coding.diary.feature.place.api.PlaceDetailNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagHomeNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.web.api.WebDetailNavKey
import io.github.taetae98coding.diary.feature.web.api.WebMemoFinishedListNavKey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class MemoEntryTest :
    FunSpec({
        test("TC-69618ec9-ff62-4854-acd2-a344b4e97e43 메모 목록에서 이어 진입한 상세 화면은 목록·상세 배치의 상세 pane이다") {
            val detailKey = MemoDetailNavKey(id = Uuid.random())
            val addKey = MemoAddNavKey()
            val backStackCases =
                listOf(
                    listOf(OtherTopLevelNavKey, MemoHomeNavKey, detailKey) to detailKey,
                    listOf(OtherTopLevelNavKey, MemoHomeNavKey, addKey) to addKey,
                    listOf(OtherTopLevelNavKey, MemoHomeNavKey, addKey, detailKey) to detailKey,
                    // 목록 pane까지 스캔이 이어지도록 최상단이 아닌 상세 화면도 상세 pane을 유지해야 한다.
                    listOf(OtherTopLevelNavKey, MemoHomeNavKey, addKey, detailKey) to addKey,
                    listOf(OtherTopLevelNavKey, MemoHomeNavKey, detailKey, addKey) to addKey,
                    listOf(OtherTopLevelNavKey, MemoHomeNavKey, detailKey, addKey) to detailKey,
                    listOf(OtherTopLevelNavKey, MemoHomeNavKey, detailKey, MemoHomeFilterNavKey) to detailKey,
                )

            backStackCases.forEach { (backStack, key) ->
                metadataOf(backStack = backStack, key = key).keys shouldBe detailPaneMetadataKeys
            }
        }

        test("TC-ec5cc549-0d17-49af-9e6d-ac3998c3ce92 목록에서 메모를 선택하면 그 메모의 상세가 상세 영역에 놓인다") {
            val selectedId = fixtureMonkey.giveMeOne<Uuid>()
            val backStack = NavBackStack<ScreenNavKey>(OtherTopLevelNavKey, MemoHomeNavKey)

            backStack.navigateToMemoDetail(selectedId)

            backStack.last() shouldBe MemoDetailNavKey(id = selectedId)
            metadataOf(backStack = backStack.toList(), key = backStack.last()).keys shouldBe detailPaneMetadataKeys
        }

        test("TC-54d0f158-e2b8-4e17-aa11-965d5435bd36 상세 영역에 메모 상세가 놓인 동안 목록에서 추가를 실행하면 메모 추가가 상세 영역에 놓인다") {
            val detailKey = MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
            val backStack = NavBackStack<ScreenNavKey>(OtherTopLevelNavKey, MemoHomeNavKey, detailKey)

            backStack.navigateToMemoAddFromHome()

            backStack.toList() shouldBe listOf(OtherTopLevelNavKey, MemoHomeNavKey, detailKey, MemoAddNavKey())
            metadataOf(backStack = backStack.toList(), key = backStack.last()).keys shouldBe detailPaneMetadataKeys
        }

        test("TC-bc758a5f-a423-4e44-b44d-b2b2486e8884 필터를 열어도 상세 영역의 화면은 목록·상세 배치의 상세 pane으로 남는다") {
            val detailKey = MemoDetailNavKey(id = Uuid.random())
            val addKey = MemoAddNavKey()
            val backStackCases =
                listOf(
                    listOf(OtherTopLevelNavKey, MemoHomeNavKey, detailKey, MemoHomeFilterNavKey) to detailKey,
                    listOf(OtherTopLevelNavKey, MemoHomeNavKey, addKey, MemoHomeFilterNavKey) to addKey,
                )

            backStackCases.forEach { (backStack, key) ->
                metadataOf(backStack = backStack, key = key).keys shouldBe detailPaneMetadataKeys
                metadataOf(backStack = backStack, key = MemoHomeFilterNavKey).keys shouldBe BottomSheetSceneStrategy.bottomSheet().keys
            }
        }

        test("TC-50bf2616-fbd6-494b-aa25-ab3ae5cd84d7 필터를 열어도 목록의 메모 추가 제공 여부는 상세 영역에 놓인 화면을 따른다") {
            val detailKey = MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
            val addKey = MemoAddNavKey()
            val backStackCases =
                listOf(
                    listOf(OtherTopLevelNavKey, MemoHomeNavKey, detailKey) to true,
                    listOf(OtherTopLevelNavKey, MemoHomeNavKey, detailKey, MemoHomeFilterNavKey) to true,
                    listOf(OtherTopLevelNavKey, MemoHomeNavKey, addKey, detailKey, MemoHomeFilterNavKey) to true,
                    listOf(OtherTopLevelNavKey, MemoHomeNavKey) to false,
                    listOf(OtherTopLevelNavKey, MemoHomeNavKey, MemoHomeFilterNavKey) to false,
                    listOf(OtherTopLevelNavKey, MemoHomeNavKey, addKey, MemoHomeFilterNavKey) to false,
                    listOf(OtherTopLevelNavKey, MemoHomeNavKey, detailKey, addKey, MemoHomeFilterNavKey) to false,
                )

            backStackCases.forEach { (backStack, isMemoDetailOnDetailPane) ->
                backStack.isMemoDetailOnDetailPane() shouldBe isMemoDetailOnDetailPane
            }
        }

        test(
            "TC-e21e272c-e40f-4e9d-aad7-bf5a54dfb035 TC-82555806-07af-4604-9e2c-148db442cb25 TC-e9e30493-4fea-45ac-aa7b-13d43f9f057a " +
                "메모 목록이나 완료된 메모 목록에서 진입하지 않은 상세 화면은 목록·상세 배치에 참여하지 않는다",
        ) {
            val detailKey = MemoDetailNavKey(id = Uuid.random())
            val tagDetailKey = TagDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
            val tagDetailAddKey = MemoAddNavKey(primaryTagId = tagDetailKey.id)
            val calendarAddKey =
                MemoAddNavKey(
                    initialDateRange =
                        MemoAddNavKey.InitialDateRange(
                            start = LocalDate(2026, 8, 3),
                            endInclusive = LocalDate(2026, 8, 4),
                        ),
                )
            val backStackCases =
                listOf(
                    listOf(OtherTopLevelNavKey, detailKey) to detailKey,
                    listOf(OtherTopLevelNavKey, calendarAddKey) to calendarAddKey,
                    listOf(OtherTopLevelNavKey, CalendarTimetableStandInNavKey, detailKey) to detailKey,
                    listOf(OtherTopLevelNavKey, TagHomeNavKey, tagDetailKey, detailKey) to detailKey,
                    listOf(OtherTopLevelNavKey, TagHomeNavKey, tagDetailKey, tagDetailAddKey) to tagDetailAddKey,
                )

            backStackCases.forEach { (backStack, key) ->
                metadataOf(backStack = backStack, key = key).shouldBeEmpty()
            }
        }

        test("TC-5d4035f7-5d0b-4a78-9d62-bc8832d86406 TagMemoFinishedList에서 이어 진입한 상세 화면은 목록·상세 배치의 상세 pane이다") {
            val tagId = Uuid.random()
            val tagMemoFinishedListKey = TagMemoFinishedListNavKey(tagId = tagId)
            val detailKey = MemoDetailNavKey(id = Uuid.random())
            val backStackCases =
                listOf(
                    listOf(OtherTopLevelNavKey, tagMemoFinishedListKey, detailKey) to detailKey,
                    listOf(
                        OtherTopLevelNavKey,
                        TagDetailNavKey(id = tagId),
                        tagMemoFinishedListKey,
                        detailKey,
                    ) to detailKey,
                )

            backStackCases.forEach { (backStack, key) ->
                metadataOf(backStack = backStack, key = key).keys shouldBe detailPaneMetadataKeys
            }
        }

        test("TC-82555806-07af-4604-9e2c-148db442cb25 상세 pane의 scene 키는 진입한 목록 pane마다 다르다") {
            val tagId = Uuid.random()
            val tagMemoFinishedListKey = TagMemoFinishedListNavKey(tagId = tagId)
            val otherTagMemoFinishedListKey = TagMemoFinishedListNavKey(tagId = Uuid.random())
            val detailKey = MemoDetailNavKey(id = Uuid.random())
            val addKey = MemoAddNavKey()
            val sceneKeyCases =
                listOf(
                    listOf(OtherTopLevelNavKey, MemoHomeNavKey, detailKey) to MemoHomeNavKey,
                    listOf(OtherTopLevelNavKey, MemoHomeNavKey, addKey) to MemoHomeNavKey,
                    listOf(OtherTopLevelNavKey, tagMemoFinishedListKey, detailKey) to tagMemoFinishedListKey,
                    listOf(OtherTopLevelNavKey, otherTagMemoFinishedListKey, detailKey) to otherTagMemoFinishedListKey,
                    // TagHome 목록·상세 배치를 지나 진입해도 scene 키는 바로 위의 완료된 메모 목록이다.
                    listOf(OtherTopLevelNavKey, TagHomeNavKey, TagDetailNavKey(id = tagId), tagMemoFinishedListKey, detailKey) to
                        tagMemoFinishedListKey,
                    // TagDetail 메모 탭에서 진입한 상세는 목록·상세 배치에 참여하지 않는다.
                    listOf(OtherTopLevelNavKey, TagHomeNavKey, TagDetailNavKey(id = tagId), detailKey) to null,
                )

            sceneKeyCases.forEach { (backStack, expectedSceneKey) ->
                backStack.memoDetailPaneSceneKey(key = backStack.last()) shouldBe expectedSceneKey
            }
        }

        test("TC-69de256a-e21f-4869-9aee-af7ea3d53bd0 항목 완료 메모 목록에서 이어 진입한 상세 화면은 목록·상세 배치의 상세 pane이다") {
            val detailKey = MemoDetailNavKey(id = Uuid.random())

            entityFinishedListCases().forEach { (entityDetailKey, finishedListKey) ->
                val backStackCases =
                    listOf(
                        listOf(OtherTopLevelNavKey, finishedListKey, detailKey),
                        listOf(OtherTopLevelNavKey, entityDetailKey, finishedListKey, detailKey),
                    )

                backStackCases.forEach { backStack ->
                    metadataOf(backStack = backStack, key = detailKey).keys shouldBe detailPaneMetadataKeys
                }
            }
        }

        test("TC-b0eb45ed-3fc5-4394-880d-24592d42bb5e 항목 완료 메모 목록에서 진입하지 않은 상세 화면은 이 배치에 참여하지 않는다") {
            val detailKey = MemoDetailNavKey(id = Uuid.random())
            val addKey = MemoAddNavKey()
            val tagMemoFinishedListKey = TagMemoFinishedListNavKey(tagId = Uuid.random())

            entityFinishedListCases().forEach { (entityDetailKey, finishedListKey) ->
                val detailFromMemoTab = listOf(OtherTopLevelNavKey, entityDetailKey, detailKey)
                val addFromMemoTab = listOf(OtherTopLevelNavKey, entityDetailKey, addKey)
                val detailFromTagMemoFinishedList = listOf(OtherTopLevelNavKey, entityDetailKey, finishedListKey, tagMemoFinishedListKey, detailKey)

                detailFromMemoTab.memoDetailPaneSceneKey(key = detailKey).shouldBeNull()
                addFromMemoTab.memoDetailPaneSceneKey(key = addKey).shouldBeNull()
                detailFromTagMemoFinishedList.memoDetailPaneSceneKey(key = detailKey) shouldBe tagMemoFinishedListKey
                listOf(OtherTopLevelNavKey, entityDetailKey, finishedListKey, detailKey).memoDetailPaneSceneKey(key = detailKey) shouldBe
                    finishedListKey
            }
            listOf(OtherTopLevelNavKey, detailKey).memoDetailPaneSceneKey(key = detailKey).shouldBeNull()
            listOf(OtherTopLevelNavKey, MemoHomeNavKey, MemoFinishedListNavKey, detailKey).memoDetailPaneSceneKey(key = detailKey) shouldBe
                MemoFinishedListNavKey
        }

        test("TC-d5d2c870-c1a8-4977-bd89-f6f37f88fd13 MemoFinishedList 화면은 목록·상세 배치의 목록 pane이다") {
            val backStack = listOf(OtherTopLevelNavKey, MemoHomeNavKey, MemoFinishedListNavKey)

            metadataOf(backStack = backStack, key = MemoFinishedListNavKey).keys shouldBe listPaneMetadataKeys
        }

        test("TC-dd8970a8-0084-4755-9ece-aade2474e3d2 TC-4aad5acb-d137-4580-90d6-73ba27ba770c 완료된 메모 목록에서 고른 메모의 상세는 그 목록의 상세 pane에 놓인다") {
            val previousDetailKey = MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
            val homeDetailCases: List<List<ScreenNavKey>> = listOf(emptyList(), listOf(previousDetailKey))

            homeDetailCases.forEach { homeDetailList ->
                val selectedId = fixtureMonkey.giveMeOne<Uuid>()
                val backStack = NavBackStack<ScreenNavKey>(OtherTopLevelNavKey, MemoHomeNavKey, *homeDetailList.toTypedArray(), MemoFinishedListNavKey)

                backStack.navigateToMemoDetail(selectedId)

                backStack.toList() shouldBe listOf(OtherTopLevelNavKey, MemoHomeNavKey) + homeDetailList + MemoFinishedListNavKey + MemoDetailNavKey(id = selectedId)
                backStack.memoDetailPaneSceneKey(key = backStack.last()) shouldBe MemoFinishedListNavKey
                metadataOf(backStack = backStack.toList(), key = backStack.last()).keys shouldBe detailPaneMetadataKeys
            }
        }

        test("TC-cfd4bb51-93d4-41cf-881b-af6f01ec14ae 다른 메모를 고르면 앞 메모 상세를 쌓지 않고 교체한다") {
            val selectedId = fixtureMonkey.giveMeOne<Uuid>()
            val backStack =
                NavBackStack<ScreenNavKey>(
                    OtherTopLevelNavKey,
                    MemoHomeNavKey,
                    MemoFinishedListNavKey,
                    MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()),
                )

            backStack.navigateToMemoDetail(selectedId)

            backStack.toList() shouldBe listOf(OtherTopLevelNavKey, MemoHomeNavKey, MemoFinishedListNavKey, MemoDetailNavKey(id = selectedId))
            backStack.memoDetailPaneSceneKey(key = backStack.last()) shouldBe MemoFinishedListNavKey
        }

        test("TC-95e17dbf-5c89-4501-aaee-fd14f21e11ef MemoFinishedList에서 진입하지 않은 상세 화면은 이 배치에 참여하지 않는다") {
            val detailKey = MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
            val tagMemoFinishedListKey = TagMemoFinishedListNavKey(tagId = fixtureMonkey.giveMeOne<Uuid>())
            val sceneKeyCases =
                buildList {
                    add(listOf(OtherTopLevelNavKey, MemoHomeNavKey, detailKey) to MemoHomeNavKey)
                    // 완료된 메모 목록을 연 채 메모 홈 상세가 아래에 남아 있어도, 그 상세는 메모 홈의 배치에 속한다.
                    add(listOf(OtherTopLevelNavKey, MemoHomeNavKey, detailKey, MemoFinishedListNavKey) to MemoHomeNavKey)
                    add(listOf(OtherTopLevelNavKey, detailKey) to null)
                    add(listOf(OtherTopLevelNavKey, CalendarTimetableStandInNavKey, detailKey) to null)
                    add(listOf(OtherTopLevelNavKey, tagMemoFinishedListKey, detailKey) to tagMemoFinishedListKey)
                    entityFinishedListCases().forEach { (entityDetailKey, finishedListKey) ->
                        add(listOf(OtherTopLevelNavKey, entityDetailKey, finishedListKey, detailKey) to finishedListKey)
                    }
                }

            sceneKeyCases.forEach { (backStack, expectedSceneKey) ->
                backStack.memoDetailPaneSceneKey(key = detailKey) shouldBe expectedSceneKey
            }
        }

        test("목록·상세 배치에 참여하지 않는 상세 화면에는 scene 키가 없다") {
            val detailKey = MemoDetailNavKey(id = Uuid.random())
            val backStack = listOf(OtherTopLevelNavKey, detailKey)

            backStack.memoDetailPaneSceneKey(key = detailKey).shouldBeNull()
        }
    }) {
    companion object {
        private val detailPaneMetadataKeys: Set<String> =
            (ListDetailSceneStrategy.detailPane() + ListDetailSceneStrategy.preferredPaneSize(width = 0.5f)).keys

        private val listPaneMetadataKeys: Set<String> =
            (ListDetailSceneStrategy.listPane(detailPlaceholder = {}) + ListDetailSceneStrategy.preferredPaneSize(width = 0.5f)).keys

        private fun metadataOf(
            backStack: List<ScreenNavKey>,
            key: ScreenNavKey,
        ): Map<String, Any> {
            val provider =
                entryProvider<ScreenNavKey> {
                    memoEntry(
                        backStack = NavBackStack(*backStack.toTypedArray()),
                        homeReselectEvent = emptyFlow(),
                    )
                }

            return provider(key).metadata
        }
    }
}

// 캘린더 홈처럼 메모 목록이 아닌 진입 화면을 대신한다. 캘린더 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object OtherTopLevelNavKey : ScreenNavKey {
    override val screenName: String
        get() = "OtherTopLevel"
}

private data object CalendarTimetableStandInNavKey : ScreenNavKey {
    override val screenName: String
        get() = "CalendarTimetableStandIn"
}

private fun entityFinishedListCases(): List<Pair<ScreenNavKey, ScreenNavKey>> {
    val contactId = Uuid.random()
    val placeId = Uuid.random()
    val webId = Uuid.random()

    return listOf(
        ContactDetailNavKey(id = contactId) to ContactMemoFinishedListNavKey(contactId = contactId),
        PlaceDetailNavKey(id = placeId) to PlaceMemoFinishedListNavKey(placeId = placeId),
        WebDetailNavKey(id = webId) to WebMemoFinishedListNavKey(webId = webId),
    )
}
