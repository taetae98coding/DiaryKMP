package io.github.taetae98coding.diary.app.shared.navigation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import io.github.taetae98coding.diary.app.shared.AppState
import io.github.taetae98coding.diary.app.shared.rememberAppState
import io.github.taetae98coding.diary.core.navigation.ScreenDeepLink
import io.github.taetae98coding.diary.feature.file.api.FileAddNavKey
import io.github.taetae98coding.diary.feature.file.api.FileHomeNavKey
import io.kotest.matchers.collections.shouldContainExactly
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OpenDeepLinkEffectTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-FILE-UPLOAD-NOTIFICATION-FEATURE-010 앱이 시작되며 받은 알림 주소로 화면이 만들어진 뒤 FileHome 화면을 연다`() {
        lateinit var appState: AppState

        AppDeepLink.open(deepLink = ScreenDeepLink.FILE_HOME)
        composeRule.setContent {
            appState = rememberAppState()
            OpenDeepLinkEffect(deepLink = AppDeepLink.deepLink, appState = appState)
        }

        composeRule.runOnIdle {
            appState.backStack shouldContainExactly listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key, FileHomeNavKey)
        }
        composeRule.runOnIdle { appState.backStack.removeLastOrNull() }
        composeRule.runOnIdle {
            appState.backStack shouldContainExactly listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key)
        }
    }

    @Test
    fun `TC-FILE-UPLOAD-NOTIFICATION-FEATURE-009 앱이 화면 뒤에 있고 마지막으로 보던 화면이 FileHome이면 알림을 선택해도 새로 열지 않는다`() {
        lateinit var appState: AppState
        val lifecycleOwner = TestLifecycleOwner(initialState = Lifecycle.State.RESUMED)

        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                appState = rememberAppState()
                OpenDeepLinkEffect(deepLink = AppDeepLink.deepLink, appState = appState)
            }
        }
        composeRule.runOnIdle {
            appState.backStack.add(TopLevelNavigation.More.key)
            appState.backStack.add(FileHomeNavKey)
            lifecycleOwner.currentState = Lifecycle.State.CREATED
        }
        composeRule.runOnIdle { AppDeepLink.open(deepLink = ScreenDeepLink.FILE_HOME) }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }

        composeRule.runOnIdle {
            appState.backStack shouldContainExactly listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key, FileHomeNavKey)
        }
        composeRule.runOnIdle { appState.backStack.removeLastOrNull() }
        composeRule.runOnIdle {
            appState.backStack shouldContainExactly listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key)
        }
    }

    @Test
    fun `TC-FILE-UPLOAD-NOTIFICATION-FEATURE-020 앱이 화면 뒤에 있고 마지막으로 보던 화면이 FileHome에서 연 FileAdd면 알림을 선택해도 FileAdd를 유지한다`() {
        lateinit var appState: AppState
        val lifecycleOwner = TestLifecycleOwner(initialState = Lifecycle.State.RESUMED)

        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                appState = rememberAppState()
                OpenDeepLinkEffect(deepLink = AppDeepLink.deepLink, appState = appState)
            }
        }
        composeRule.runOnIdle {
            appState.backStack.add(TopLevelNavigation.More.key)
            appState.backStack.add(FileHomeNavKey)
            appState.backStack.add(FileAddNavKey)
            lifecycleOwner.currentState = Lifecycle.State.CREATED
        }
        composeRule.runOnIdle { AppDeepLink.open(deepLink = ScreenDeepLink.FILE_HOME) }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }

        composeRule.runOnIdle {
            appState.backStack shouldContainExactly listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key, FileHomeNavKey, FileAddNavKey)
        }
    }
}
