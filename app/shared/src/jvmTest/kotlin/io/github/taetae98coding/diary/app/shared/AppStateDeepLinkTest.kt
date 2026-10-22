package io.github.taetae98coding.diary.app.shared

import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldState
import androidx.navigation3.runtime.NavBackStack
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelNavigation
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelReselectEvent
import io.github.taetae98coding.diary.core.navigation.ScreenDeepLink
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.file.api.FileAddNavKey
import io.github.taetae98coding.diary.feature.file.api.FileDetailNavKey
import io.github.taetae98coding.diary.feature.file.api.FileHomeNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrHomeNavKey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class AppStateDeepLinkTest :
    FunSpec({
        test("TC-5b734721-c6fe-4d3d-a2bc-e744c156e188 알림을 선택하면 더보기 위에 FileHome 화면을 열고 뒤로가면 더보기가 보인다") {
            listOf(
                listOf(TopLevelNavigation.Calendar.key),
                listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.Memo.key, MemoDetailNavKey(fixtureMonkey.giveMeOne<Uuid>())),
                listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key),
                listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key, QrHomeNavKey),
            ).forEach { backStack ->
                val appState = createAppState(*backStack.toTypedArray())

                appState.openDeepLink(deepLink = ScreenDeepLink.FILE_HOME)

                appState.backStack.toList() shouldBe listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key, FileHomeNavKey)
                appState.backStack.removeLastOrNull()
                appState.backStack.last() shouldBe TopLevelNavigation.More.key
            }
        }

        test("TC-66288305-8c99-4bde-9280-d147cf3171a6 이미 FileHome 화면을 보고 있으면 새로 열지 않는다") {
            val initialBackStack = listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key, FileHomeNavKey)
            val appState = createAppState(*initialBackStack.toTypedArray())

            appState.openDeepLink(deepLink = ScreenDeepLink.FILE_HOME)

            appState.backStack.toList() shouldBe initialBackStack
            appState.backStack.removeLastOrNull()
            appState.backStack.last() shouldBe TopLevelNavigation.More.key
        }

        test("TC-41f772e5-d9ec-4108-8edc-7af5961a0a26 FileHome에서 연 FileAdd를 보고 있으면 알림을 선택해도 FileAdd를 유지하고 뒤로가면 FileHome이 보인다") {
            val initialBackStack = listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key, FileHomeNavKey, FileAddNavKey)
            val appState = createAppState(*initialBackStack.toTypedArray())

            appState.openDeepLink(deepLink = ScreenDeepLink.FILE_HOME)

            appState.backStack.toList() shouldBe initialBackStack
            appState.backStack.removeLastOrNull()
            appState.backStack.last() shouldBe FileHomeNavKey
        }

        test("TC-5b734721-c6fe-4d3d-a2bc-e744c156e188 FileHome 위가 아닌 곳에 열린 FileAdd는 다른 화면처럼 닫고 FileHome을 연다") {
            val appState = createAppState(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key, FileAddNavKey)

            appState.openDeepLink(deepLink = ScreenDeepLink.FILE_HOME)

            appState.backStack.toList() shouldBe listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key, FileHomeNavKey)
        }

        test("TC-323336ff-c39e-4d3b-b447-ba5497a00ee9 FileHome에서 연 FileDetail을 보고 있으면 알림을 선택했을 때 FileDetail을 닫고 FileHome을 연다") {
            val appState = createAppState(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key, FileHomeNavKey, FileDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()))

            appState.openDeepLink(deepLink = ScreenDeepLink.FILE_HOME)

            appState.backStack.toList() shouldBe listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key, FileHomeNavKey)
            appState.backStack.removeLastOrNull()
            appState.backStack.last() shouldBe TopLevelNavigation.More.key
        }

        test("TC-0d02a609-d833-4ec1-9cd1-e33ddbee22e7 목록과 함께 표시한 FileDetail 위에서 연 FileAdd를 보고 있으면 알림을 선택해도 배치를 유지한다") {
            val detail = FileDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
            val initialBackStack = listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key, FileHomeNavKey, detail, FileAddNavKey)
            val appState = createAppState(*initialBackStack.toTypedArray())

            appState.openDeepLink(deepLink = ScreenDeepLink.FILE_HOME)

            appState.backStack.toList() shouldBe initialBackStack
        }

        test("TC-e9877edd-5af6-4908-8137-589a6bcf6cda 목록과 함께 선택 전 FileAdd가 놓여 있으면 알림을 선택해도 배치를 유지하고 뒤로가면 더보기로 돌아간다") {
            val initialBackStack = listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key, FileHomeNavKey)
            val appState = createAppState(*initialBackStack.toTypedArray(), directive = TWO_PANE_DIRECTIVE)

            appState.openDeepLink(deepLink = ScreenDeepLink.FILE_HOME)

            appState.backStack.toList() shouldBe initialBackStack
            appState.backStack.removeLastOrNull()
            appState.backStack.last() shouldBe TopLevelNavigation.More.key
        }

        test("TC-fc7083f5-9f79-49b4-940a-0a580d483704 목록과 함께 상세 영역에 FileDetail이 놓여 있으면 알림을 선택했을 때 FileDetail을 닫고 선택 전 상태로 FileHome을 연다") {
            val detail = FileDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
            val appState = createAppState(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key, FileHomeNavKey, detail, directive = TWO_PANE_DIRECTIVE)

            appState.openDeepLink(deepLink = ScreenDeepLink.FILE_HOME)

            appState.backStack.toList() shouldBe listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key, FileHomeNavKey)
            appState.backStack.removeLastOrNull()
            appState.backStack.last() shouldBe TopLevelNavigation.More.key
        }

        test("알 수 없는 주소는 보던 화면을 그대로 둔다") {
            val initialBackStack = listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.Memo.key)
            val appState = createAppState(*initialBackStack.toTypedArray())

            appState.openDeepLink(deepLink = "diary://${fixtureMonkey.giveMeOne<Uuid>()}")

            appState.backStack.toList() shouldBe initialBackStack
        }
    }) {
    private companion object {
        private val TWO_PANE_DIRECTIVE: PaneScaffoldDirective = PaneScaffoldDirective.Default.copy(maxHorizontalPartitions = 2)

        private fun createAppState(
            vararg keys: ScreenNavKey,
            directive: PaneScaffoldDirective = PaneScaffoldDirective.Default,
        ): AppState =
            AppState(
                backStack = NavBackStack(*keys),
                scaffoldState = mockk<NavigationSuiteScaffoldState>(relaxed = true),
                reselectEvent = TopLevelReselectEvent(),
                paneScaffoldDirectiveProvider = { directive },
            )
    }
}
