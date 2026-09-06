package io.github.taetae98coding.diary.app.permission

import android.Manifest
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RequestNotificationPermissionEffectTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-NOTIFICATION-PERMISSION-FEATURE-001 알림 권한이 결정되어 있지 않으면 앱 시작 시 시스템 알림 권한 요청이 시작된다`() {
        val registry = PermissionResultRegistry(result = false)

        setRequestNotificationPermissionEffect(registry = registry)
        composeRule.waitForIdle()

        registry.launchedPermissionList shouldHaveSize 1
    }

    @Test
    fun `시스템 알림 권한 요청에 알림 표시 권한이 포함된다`() {
        val registry = PermissionResultRegistry(result = false)

        setRequestNotificationPermissionEffect(registry = registry)
        composeRule.waitForIdle()

        registry.launchedPermissionList.single() shouldBe Manifest.permission.POST_NOTIFICATIONS
    }

    @Test
    fun `TC-NOTIFICATION-PERMISSION-FEATURE-002 알림 권한이 이미 허용되어 있으면 요청이 시작되지 않는다`() {
        shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        val registry = PermissionResultRegistry(result = true)

        setRequestNotificationPermissionEffect(registry = registry)
        composeRule.waitForIdle()

        registry.launchedPermissionList.shouldBeEmpty()
    }

    @Test
    fun `TC-NOTIFICATION-PERMISSION-FEATURE-003 요청을 허용해도 앱 화면이 유지되고 별도 안내가 표시되지 않는다`() {
        val registry = PermissionResultRegistry(result = true)

        setRequestNotificationPermissionEffect(registry = registry)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(APP_CONTENT, substring = true).assertIsDisplayed()
        composeRule.onRoot().onChildren().assertCountEquals(1)
    }

    @Test
    fun `TC-NOTIFICATION-PERMISSION-FEATURE-003 요청을 거부해도 앱 화면이 유지되고 별도 안내가 표시되지 않는다`() {
        val registry = PermissionResultRegistry(result = false)

        setRequestNotificationPermissionEffect(registry = registry)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(APP_CONTENT, substring = true).assertIsDisplayed()
        composeRule.onRoot().onChildren().assertCountEquals(1)
    }

    @Test
    fun `TC-NOTIFICATION-PERMISSION-DOMAIN-002 앱 화면이 다시 구성되어도 요청 조건을 다시 확인하지 않는다`() {
        val registry = PermissionResultRegistry(result = false)
        val recomposeCount = mutableIntStateOf(0)

        setRequestNotificationPermissionEffect(
            registry = registry,
            recomposeCount = recomposeCount,
        )
        composeRule.waitForIdle()

        composeRule.runOnIdle { recomposeCount.intValue++ }
        composeRule.waitForIdle()

        registry.launchedPermissionList shouldHaveSize 1
    }

    @Test
    fun `TC-NOTIFICATION-PERMISSION-DOMAIN-002 앱이 백그라운드에 갔다가 돌아와도 요청 조건을 다시 확인하지 않는다`() {
        val registry = PermissionResultRegistry(result = false)
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.STARTED)

        setRequestNotificationPermissionEffect(
            registry = registry,
            lifecycleOwner = lifecycleOwner,
        )
        composeRule.waitForIdle()

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.STARTED }
        composeRule.waitForIdle()

        registry.launchedPermissionList shouldHaveSize 1
    }

    @Test
    fun `TC-NOTIFICATION-PERMISSION-DOMAIN-003 앱 화면이 처음부터 다시 시작되면 요청 조건을 다시 확인한다`() {
        val registry = PermissionResultRegistry(result = false)
        val restorationTester = StateRestorationTester(composeRule)

        restorationTester.setContent {
            RequestNotificationPermissionContent(registry = registry)
        }
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        registry.launchedPermissionList shouldHaveSize 2
    }

    private fun setRequestNotificationPermissionEffect(
        registry: ActivityResultRegistry,
        lifecycleOwner: TestLifecycleOwner = TestLifecycleOwner(Lifecycle.State.STARTED),
        recomposeCount: MutableIntState = mutableIntStateOf(0),
    ) {
        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                RequestNotificationPermissionContent(
                    registry = registry,
                    recomposeCount = recomposeCount,
                )
            }
        }
    }

    @Composable
    private fun RequestNotificationPermissionContent(
        registry: ActivityResultRegistry,
        recomposeCount: MutableIntState = mutableIntStateOf(0),
    ) {
        val registryOwner =
            object : ActivityResultRegistryOwner {
                override val activityResultRegistry: ActivityResultRegistry = registry
            }

        CompositionLocalProvider(LocalActivityResultRegistryOwner provides registryOwner) {
            // 재구성 계기를 효과 호출부와 같은 람다에서 읽어야 효과가 다시 호출된다.
            Text(text = "$APP_CONTENT ${recomposeCount.intValue}")
            RequestNotificationPermissionEffect()
        }
    }

    private class PermissionResultRegistry(
        private val result: Boolean,
    ) : ActivityResultRegistry() {
        val launchedPermissionList = mutableListOf<String>()

        override fun <I, O> onLaunch(
            requestCode: Int,
            contract: ActivityResultContract<I, O>,
            input: I,
            options: ActivityOptionsCompat?,
        ) {
            launchedPermissionList += input.toString()
            dispatchResult(requestCode, result)
        }
    }

    companion object {
        private const val APP_CONTENT = "앱 화면"
    }
}
