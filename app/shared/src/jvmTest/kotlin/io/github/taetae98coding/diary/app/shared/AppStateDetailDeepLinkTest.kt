@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.app.shared

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldState
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import io.github.taetae98coding.diary.app.shared.di.DiaryKoinApplication
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelNavigation
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelReselectEvent
import io.github.taetae98coding.diary.app.shared.navigation.openDeepLink
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistDetailNavKey
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistHomeNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactDetailNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactHomeNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.dday.api.DDayDetailNavKey
import io.github.taetae98coding.diary.feature.dday.api.DDayHomeNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceDetailNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceHomeNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.playlist.api.MusicDetailNavKey
import io.github.taetae98coding.diary.feature.playlist.api.PlaylistHomeNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrDetailNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrHomeNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineAddNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineDetailNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineFinishedListNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagFinishedListNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagRoutineFinishedListNavKey
import io.github.taetae98coding.diary.feature.web.api.WebDetailNavKey
import io.github.taetae98coding.diary.feature.web.api.WebHomeNavKey
import io.github.taetae98coding.diary.feature.web.api.WebMemoFinishedListNavKey
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.mockk
import org.koin.plugin.module.dsl.koinApplication
import kotlin.uuid.Uuid

class AppStateDetailDeepLinkTest :
    FunSpec({
        test("TC-308f8a17-0dd3-4e86-9640-3411623074ff 앱을 새로 시작하면 목록 화면 위에 상세 화면을 연다") {
            detailCaseList().forEach { case ->
                val id = Arb.kotlinUuid().next()
                val appState = createAppState(TopLevelNavigation.DEFAULT.key)

                appState.openDeepLink(deepLink = "diary://${case.host}?id=$id", isLaunch = true)

                appState.backStack.toList() shouldBe case.listBackStack + case.detailKeyOf(id)
                appState.backStack.removeLastOrNull()
                appState.backStack.last() shouldBe case.listBackStack.last()
            }
        }

        test("TC-623b7d4f-6590-44ee-87c7-2984e6d1af57 보던 화면이 남아 있으면 그 위에 상세 화면을 연다") {
            listOf(
                listOf(TopLevelNavigation.Calendar.key),
                listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.Memo.key),
                listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.Memo.key, MemoDetailNavKey(Arb.kotlinUuid().next())),
                listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.Tag.key, TagDetailNavKey(Arb.kotlinUuid().next())),
            ).forEach { initialBackStack ->
                val id = Arb.kotlinUuid().next()
                val appState = createAppState(*initialBackStack.toTypedArray())

                appState.openDeepLink(deepLink = "diary://memo-detail?id=$id")

                appState.backStack.toList() shouldBe initialBackStack + MemoDetailNavKey(id = id, isStandalone = true)
                appState.backStack.removeLastOrNull()
                appState.backStack.toList() shouldBe initialBackStack
            }
        }

        test("TC-78950452-bd6c-4403-bee9-33266b6eef48 보던 화면이 고른 항목의 상세 화면이면 더 쌓지 않는다") {
            listOf(false, true).forEach { isStandalone ->
                val id = Arb.kotlinUuid().next()
                val initialBackStack = listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.Memo.key, MemoDetailNavKey(id = id, isStandalone = isStandalone))
                val appState = createAppState(*initialBackStack.toTypedArray())

                appState.openDeepLink(deepLink = "diary://memo-detail?id=$id")

                appState.backStack.toList() shouldBe initialBackStack
                appState.backStack.removeLastOrNull()
                appState.backStack.last() shouldBe TopLevelNavigation.Memo.key
            }
        }

        test("TC-21a264da-384a-4a43-934d-521849f5c051 앱을 새로 시작해 연 상세 화면은 목록·상세 배치에 참여하지 않는다") {
            detailCaseList().forEach { case ->
                val appState = createAppState(TopLevelNavigation.DEFAULT.key)

                appState.openDeepLink(deepLink = "diary://${case.host}?id=${Arb.kotlinUuid().next()}", isLaunch = true)

                appState.detailPaneMetadataKeysOf(appState.backStack.last()).shouldBeEmpty()
                appState.backStack.removeLastOrNull()
                appState.backStack.toList() shouldBe case.listBackStack
            }
        }

        test("TC-21a264da-384a-4a43-934d-521849f5c051 목록이나 상세 영역을 보던 중 연 상세 화면은 목록·상세 배치에 참여하지 않는다") {
            viewingCaseList().forEach { case ->
                val appState = createAppState(*case.initialBackStack.toTypedArray())
                val viewingKey = case.initialBackStack.last()
                val viewingDetailPaneMetadataKeys = appState.detailPaneMetadataKeysOf(viewingKey)

                appState.openDeepLink(deepLink = "diary://${case.host}?id=${Arb.kotlinUuid().next()}")

                appState.backStack.size shouldBe case.initialBackStack.size + 1
                appState.detailPaneMetadataKeysOf(appState.backStack.last()).shouldBeEmpty()
                appState.backStack.removeLastOrNull()
                appState.backStack.toList() shouldBe case.initialBackStack
                appState.detailPaneMetadataKeysOf(viewingKey) shouldBe viewingDetailPaneMetadataKeys
                if (case.isViewingOnDetailPane) viewingDetailPaneMetadataKeys shouldContainAll ListDetailSceneStrategy.detailPane().keys
            }
        }

        test("앱 시작 주소가 아니면 시작 화면만 있어도 그 위에 상세 화면을 연다") {
            detailCaseList().forEach { case ->
                val id = Arb.kotlinUuid().next()
                val appState = createAppState(TopLevelNavigation.DEFAULT.key)

                appState.openDeepLink(deepLink = "diary://${case.host}?id=$id")

                appState.backStack.toList() shouldBe listOf(TopLevelNavigation.DEFAULT.key, case.detailKeyOf(id))
            }
        }

        test("id를 항목 식별자로 읽지 못하면 보던 화면을 그대로 둔다") {
            detailCaseList().forEach { case ->
                val initialBackStack = listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.Memo.key)
                val appState = createAppState(*initialBackStack.toTypedArray())

                appState.openDeepLink(deepLink = "diary://${case.host}?id=not-a-uuid")

                appState.backStack.toList() shouldBe initialBackStack
            }
        }
    }) {
    private data class DetailCase(
        val host: String,
        val listBackStack: List<ScreenNavKey>,
        val detailKeyOf: (Uuid) -> ScreenNavKey,
    )

    private data class ViewingCase(
        val host: String,
        val initialBackStack: List<ScreenNavKey>,
        val isViewingOnDetailPane: Boolean = false,
    )

    private companion object {
        // 앱과 같은 Koin 구성에서 모은 entry로 판정해, 화면이 실제로 받는 배치 메타데이터를 확인한다.
        private val entryInstallerList by lazy { koinApplication<DiaryKoinApplication>().koin.get<AppDependencies>().entryInstallerList }

        private fun detailCaseList(): List<DetailCase> {
            val more = listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key)

            return listOf(
                DetailCase("memo-detail", listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.Memo.key)) { id -> MemoDetailNavKey(id = id, isStandalone = true) },
                DetailCase("tag-detail", listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.Tag.key)) { id -> TagDetailNavKey(id = id, isStandalone = true) },
                DetailCase("place-detail", more + PlaceHomeNavKey, ::PlaceDetailNavKey),
                DetailCase("web-detail", more + WebHomeNavKey, ::WebDetailNavKey),
                DetailCase("contact-detail", more + ContactHomeNavKey) { id -> ContactDetailNavKey(id = id, isStandalone = true) },
                DetailCase("checklist-detail", more + ChecklistHomeNavKey) { id -> ChecklistDetailNavKey(id = id, isStandalone = true) },
                DetailCase("qr-detail", more + QrHomeNavKey) { id -> QrDetailNavKey(id = id, isStandalone = true) },
                DetailCase("music-detail", more + PlaylistHomeNavKey) { id -> MusicDetailNavKey(id = id, isStandalone = true) },
                DetailCase("dday-detail", more + DDayHomeNavKey) { id -> DDayDetailNavKey(id = id, isStandalone = true) },
                DetailCase("routine-detail", listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.Routine.key)) { id -> RoutineDetailNavKey(id = id, isStandalone = true) },
            )
        }

        private fun viewingCaseList(): List<ViewingCase> {
            val memo = listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.Memo.key)
            val tag = listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.Tag.key)
            val more = listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key)
            val routine = listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.Routine.key)

            fun id(): Uuid = Arb.kotlinUuid().next()

            return listOf(
                ViewingCase("memo-detail", memo),
                ViewingCase("memo-detail", memo + MemoDetailNavKey(id()), isViewingOnDetailPane = true),
                ViewingCase("memo-detail", memo + MemoAddNavKey(), isViewingOnDetailPane = true),
                ViewingCase("memo-detail", memo + MemoFinishedListNavKey),
                ViewingCase("memo-detail", memo + MemoFinishedListNavKey + MemoDetailNavKey(id()), isViewingOnDetailPane = true),
                ViewingCase("memo-detail", tag + TagDetailNavKey(id()) + TagMemoFinishedListNavKey(tagId = id())),
                ViewingCase("memo-detail", more + ContactHomeNavKey + ContactDetailNavKey(id()) + ContactMemoFinishedListNavKey(contactId = id())),
                ViewingCase("memo-detail", more + PlaceHomeNavKey + PlaceDetailNavKey(id()) + PlaceMemoFinishedListNavKey(placeId = id())),
                ViewingCase("memo-detail", more + WebHomeNavKey + WebDetailNavKey(id()) + WebMemoFinishedListNavKey(webId = id())),
                ViewingCase("tag-detail", tag),
                ViewingCase("tag-detail", tag + TagDetailNavKey(id()), isViewingOnDetailPane = true),
                ViewingCase("tag-detail", tag + TagFinishedListNavKey),
                ViewingCase("tag-detail", tag + TagFinishedListNavKey + TagDetailNavKey(id()), isViewingOnDetailPane = true),
                ViewingCase("place-detail", more + PlaceHomeNavKey),
                ViewingCase("web-detail", more + WebHomeNavKey),
                ViewingCase("contact-detail", more + ContactHomeNavKey),
                ViewingCase("contact-detail", more + ContactHomeNavKey + ContactDetailNavKey(id()), isViewingOnDetailPane = true),
                ViewingCase("checklist-detail", more + ChecklistHomeNavKey),
                ViewingCase("checklist-detail", more + ChecklistHomeNavKey + ChecklistDetailNavKey(id()), isViewingOnDetailPane = true),
                ViewingCase("qr-detail", more + QrHomeNavKey),
                ViewingCase("qr-detail", more + QrHomeNavKey + QrDetailNavKey(id()), isViewingOnDetailPane = true),
                ViewingCase("music-detail", more + PlaylistHomeNavKey),
                ViewingCase("music-detail", more + PlaylistHomeNavKey + MusicDetailNavKey(id()), isViewingOnDetailPane = true),
                ViewingCase("dday-detail", more + DDayHomeNavKey),
                ViewingCase("dday-detail", more + DDayHomeNavKey + DDayDetailNavKey(id()), isViewingOnDetailPane = true),
                ViewingCase("routine-detail", routine),
                ViewingCase("routine-detail", routine + RoutineDetailNavKey(id()), isViewingOnDetailPane = true),
                ViewingCase("routine-detail", routine + RoutineAddNavKey(), isViewingOnDetailPane = true),
                ViewingCase("routine-detail", routine + RoutineFinishedListNavKey),
                ViewingCase("routine-detail", routine + RoutineFinishedListNavKey + RoutineDetailNavKey(id()), isViewingOnDetailPane = true),
                ViewingCase("routine-detail", tag + TagDetailNavKey(id()) + TagRoutineFinishedListNavKey(tagId = id())),
            )
        }

        private fun AppState.detailPaneMetadataKeysOf(key: ScreenNavKey): Set<String> {
            val provider =
                entryProvider<ScreenNavKey> {
                    entryInstallerList.forEach { installer -> with(installer) { install(navigator = this@detailPaneMetadataKeysOf) } }
                }

            return provider(key).metadata.keys intersect ListDetailSceneStrategy.detailPane().keys
        }

        private fun createAppState(vararg keys: ScreenNavKey): AppState =
            AppState(
                backStack = NavBackStack(*keys),
                scaffoldState = mockk<NavigationSuiteScaffoldState>(relaxed = true),
                reselectEvent = TopLevelReselectEvent(),
                paneScaffoldDirectiveProvider = { PaneScaffoldDirective.Default },
            )
    }
}
