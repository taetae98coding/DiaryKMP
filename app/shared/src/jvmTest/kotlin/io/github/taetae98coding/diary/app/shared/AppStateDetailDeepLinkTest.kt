package io.github.taetae98coding.diary.app.shared

import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldState
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelNavigation
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelReselectEvent
import io.github.taetae98coding.diary.app.shared.navigation.openDeepLink
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistDetailNavKey
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistHomeNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactDetailNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactHomeNavKey
import io.github.taetae98coding.diary.feature.dday.api.DDayDetailNavKey
import io.github.taetae98coding.diary.feature.dday.api.DDayHomeNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceDetailNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceHomeNavKey
import io.github.taetae98coding.diary.feature.playlist.api.MusicDetailNavKey
import io.github.taetae98coding.diary.feature.playlist.api.PlaylistHomeNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrDetailNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrHomeNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.web.api.WebDetailNavKey
import io.github.taetae98coding.diary.feature.web.api.WebHomeNavKey
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.mockk
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

                appState.backStack.toList() shouldBe initialBackStack + MemoDetailNavKey(id)
                appState.backStack.removeLastOrNull()
                appState.backStack.toList() shouldBe initialBackStack
            }
        }

        test("TC-78950452-bd6c-4403-bee9-33266b6eef48 보던 화면이 고른 항목의 상세 화면이면 더 쌓지 않는다") {
            val id = Arb.kotlinUuid().next()
            val initialBackStack = listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.Memo.key, MemoDetailNavKey(id))
            val appState = createAppState(*initialBackStack.toTypedArray())

            appState.openDeepLink(deepLink = "diary://memo-detail?id=$id")

            appState.backStack.toList() shouldBe initialBackStack
            appState.backStack.removeLastOrNull()
            appState.backStack.last() shouldBe TopLevelNavigation.Memo.key
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

    private companion object {
        private fun detailCaseList(): List<DetailCase> {
            val more = listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key)

            return listOf(
                DetailCase("memo-detail", listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.Memo.key), ::MemoDetailNavKey),
                DetailCase("tag-detail", listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.Tag.key), ::TagDetailNavKey),
                DetailCase("place-detail", more + PlaceHomeNavKey, ::PlaceDetailNavKey),
                DetailCase("web-detail", more + WebHomeNavKey, ::WebDetailNavKey),
                DetailCase("contact-detail", more + ContactHomeNavKey, ::ContactDetailNavKey),
                DetailCase("checklist-detail", more + ChecklistHomeNavKey, ::ChecklistDetailNavKey),
                DetailCase("qr-detail", more + QrHomeNavKey, ::QrDetailNavKey),
                DetailCase("music-detail", more + PlaylistHomeNavKey, ::MusicDetailNavKey),
                DetailCase("dday-detail", more + DDayHomeNavKey, ::DDayDetailNavKey),
            )
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
