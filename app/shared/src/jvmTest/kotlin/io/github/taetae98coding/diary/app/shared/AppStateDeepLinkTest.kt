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
        test("TC-FILE-UPLOAD-NOTIFICATION-FEATURE-008 알림을 선택하면 더보기 위에 FileHome 화면을 열고 뒤로가면 더보기가 보인다") {
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

        test("TC-FILE-UPLOAD-NOTIFICATION-FEATURE-009 이미 FileHome 화면을 보고 있으면 새로 열지 않는다") {
            val initialBackStack = listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key, FileHomeNavKey)
            val appState = createAppState(*initialBackStack.toTypedArray())

            appState.openDeepLink(deepLink = ScreenDeepLink.FILE_HOME)

            appState.backStack.toList() shouldBe initialBackStack
            appState.backStack.removeLastOrNull()
            appState.backStack.last() shouldBe TopLevelNavigation.More.key
        }

        test("TC-FILE-UPLOAD-NOTIFICATION-FEATURE-020 FileHome에서 연 FileAdd를 보고 있으면 알림을 선택해도 FileAdd를 유지하고 뒤로가면 FileHome이 보인다") {
            val initialBackStack = listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key, FileHomeNavKey, FileAddNavKey)
            val appState = createAppState(*initialBackStack.toTypedArray())

            appState.openDeepLink(deepLink = ScreenDeepLink.FILE_HOME)

            appState.backStack.toList() shouldBe initialBackStack
            appState.backStack.removeLastOrNull()
            appState.backStack.last() shouldBe FileHomeNavKey
        }

        test("TC-FILE-UPLOAD-NOTIFICATION-FEATURE-008 FileHome 위가 아닌 곳에 열린 FileAdd는 다른 화면처럼 닫고 FileHome을 연다") {
            val appState = createAppState(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key, FileAddNavKey)

            appState.openDeepLink(deepLink = ScreenDeepLink.FILE_HOME)

            appState.backStack.toList() shouldBe listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key, FileHomeNavKey)
        }

        test("TC-FILE-UPLOAD-NOTIFICATION-FEATURE-021 FileHome에서 연 FileDetail을 보고 있으면 알림을 선택했을 때 FileDetail을 닫고 FileHome을 연다") {
            val appState = createAppState(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key, FileHomeNavKey, FileDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()))

            appState.openDeepLink(deepLink = ScreenDeepLink.FILE_HOME)

            appState.backStack.toList() shouldBe listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key, FileHomeNavKey)
            appState.backStack.removeLastOrNull()
            appState.backStack.last() shouldBe TopLevelNavigation.More.key
        }

        test("TC-FILE-UPLOAD-NOTIFICATION-FEATURE-022 목록과 함께 표시한 FileDetail 위에서 연 FileAdd를 보고 있으면 알림을 선택해도 배치를 유지한다") {
            val detail = FileDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
            val initialBackStack = listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key, FileHomeNavKey, detail, FileAddNavKey)
            val appState = createAppState(*initialBackStack.toTypedArray())

            appState.openDeepLink(deepLink = ScreenDeepLink.FILE_HOME)

            appState.backStack.toList() shouldBe initialBackStack
        }

        test("TC-FILE-UPLOAD-NOTIFICATION-FEATURE-023 목록과 함께 선택 전 FileAdd가 놓여 있으면 알림을 선택해도 배치를 유지하고 뒤로가면 더보기로 돌아간다") {
            val initialBackStack = listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key, FileHomeNavKey)
            val appState = createAppState(*initialBackStack.toTypedArray(), directive = TWO_PANE_DIRECTIVE)

            appState.openDeepLink(deepLink = ScreenDeepLink.FILE_HOME)

            appState.backStack.toList() shouldBe initialBackStack
            appState.backStack.removeLastOrNull()
            appState.backStack.last() shouldBe TopLevelNavigation.More.key
        }

        test("TC-FILE-UPLOAD-NOTIFICATION-FEATURE-024 목록과 함께 상세 영역에 FileDetail이 놓여 있으면 알림을 선택했을 때 FileDetail을 닫고 선택 전 상태로 FileHome을 연다") {
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
