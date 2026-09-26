package io.github.taetae98coding.diary.compose.permission

import android.Manifest
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
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
import io.github.taetae98coding.diary.core.permission.Permission
import io.github.taetae98coding.diary.core.permission.PermissionResult
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.slot
import io.mockk.spyk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RequestPermissionEffectTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-NOTIFICATION-PERMISSION-FEATURE-001 알림 권한이 허용되어 있지 않으면 앱 시작 시 시스템 알림 권한 요청이 시작된다`() {
        val registry = PermissionResultRegistry(result = notificationResult(isGranted = false))

        setRequestPermissionEffect(permission = Permission.NOTIFICATION, registry = registry)
        composeRule.waitForIdle()

        registry.launchedPermissionList shouldHaveSize 1
    }

    @Test
    fun `TC-NOTIFICATION-PERMISSION-DOMAIN-006 Android에서는 하나의 알림 권한만 요청한다`() {
        val registry = PermissionResultRegistry(result = notificationResult(isGranted = false))

        setRequestPermissionEffect(permission = Permission.NOTIFICATION, registry = registry)
        composeRule.waitForIdle()

        registry.launchedPermissionList.single() shouldContainExactly listOf(Manifest.permission.POST_NOTIFICATIONS)
    }

    @Test
    fun `TC-NOTIFICATION-PERMISSION-FEATURE-002 알림 권한이 이미 허용되어 있으면 요청이 시작되지 않는다`() {
        shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        val registry = PermissionResultRegistry(result = notificationResult(isGranted = true))
        val resultList = mutableListOf<PermissionResult>()

        setRequestPermissionEffect(
            permission = Permission.NOTIFICATION,
            registry = registry,
            onResult = { result -> resultList += result },
        )
        composeRule.waitForIdle()

        registry.launchedPermissionList.shouldBeEmpty()
        resultList.single() shouldBe PermissionResult.ALREADY_GRANTED
    }

    @Test
    fun `TC-NOTIFICATION-PERMISSION-FEATURE-003 요청을 허용해도 앱 화면이 유지되고 별도 안내가 표시되지 않는다`() {
        val registry = PermissionResultRegistry(result = notificationResult(isGranted = true))
        val resultList = mutableListOf<PermissionResult>()

        setRequestPermissionEffect(
            permission = Permission.NOTIFICATION,
            registry = registry,
            onResult = { result -> resultList += result },
        )
        composeRule.waitForIdle()

        composeRule.onNodeWithText(APP_CONTENT, substring = true).assertIsDisplayed()
        composeRule.onRoot().onChildren().assertCountEquals(1)
        resultList.single() shouldBe PermissionResult.GRANTED
    }

    @Test
    fun `TC-NOTIFICATION-PERMISSION-FEATURE-003 요청을 거부해도 앱 화면이 유지되고 별도 안내가 표시되지 않는다`() {
        val registry = PermissionResultRegistry(result = notificationResult(isGranted = false))
        val resultList = mutableListOf<PermissionResult>()

        setRequestPermissionEffect(
            permission = Permission.NOTIFICATION,
            registry = registry,
            onResult = { result -> resultList += result },
        )
        composeRule.waitForIdle()

        composeRule.onNodeWithText(APP_CONTENT, substring = true).assertIsDisplayed()
        composeRule.onRoot().onChildren().assertCountEquals(1)
        resultList.single() shouldBe PermissionResult.DENIED
    }

    @Test
    fun `TC-NOTIFICATION-PERMISSION-DOMAIN-002 앱 안에서 다른 화면으로 이동해도 요청 조건을 다시 확인하지 않는다`() {
        val registry = PermissionResultRegistry(result = notificationResult(isGranted = false))
        var currentScreen by mutableStateOf(FIRST_SCREEN)

        // 알림 권한은 앱 화면이 시작될 때 요청하므로, 요청 지점은 그대로 두고 그 안에 보이는 화면만 바꾼다.
        composeRule.setContent {
            val registryOwner =
                object : ActivityResultRegistryOwner {
                    override val activityResultRegistry: ActivityResultRegistry = registry
                }

            CompositionLocalProvider(LocalActivityResultRegistryOwner provides registryOwner) {
                RequestPermissionEffect(permission = Permission.NOTIFICATION, onResult = {})
                Text(text = currentScreen)
            }
        }
        composeRule.waitForIdle()

        composeRule.runOnIdle { currentScreen = SECOND_SCREEN }
        composeRule.onNodeWithText(SECOND_SCREEN).assertIsDisplayed()
        composeRule.runOnIdle { currentScreen = FIRST_SCREEN }
        composeRule.onNodeWithText(FIRST_SCREEN).assertIsDisplayed()

        registry.launchedPermissionList shouldHaveSize 1
    }

    @Test
    fun `TC-NOTIFICATION-PERMISSION-DOMAIN-002 앱이 백그라운드에 갔다가 돌아와도 요청 조건을 다시 확인하지 않는다`() {
        val registry = PermissionResultRegistry(result = notificationResult(isGranted = false))
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.STARTED)

        setRequestPermissionEffect(
            permission = Permission.NOTIFICATION,
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
        val registry = PermissionResultRegistry(result = notificationResult(isGranted = false))
        val restorationTester = StateRestorationTester(composeRule)

        restorationTester.setContent {
            RequestPermissionContent(permission = Permission.NOTIFICATION, registry = registry)
        }
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        registry.launchedPermissionList shouldHaveSize 2
    }

    @Test
    fun `TC-LOCATION-PERMISSION-DOMAIN-002 화면이 다시 구성되어도 요청 조건을 다시 확인하지 않는다`() {
        val registry = PermissionResultRegistry(result = locationResult(isFineGranted = false, isCoarseGranted = false))
        val recomposeCount = mutableIntStateOf(0)

        setRequestPermissionEffect(
            permission = Permission.LOCATION,
            registry = registry,
            recomposeCount = recomposeCount,
        )
        composeRule.waitForIdle()

        composeRule.runOnIdle { recomposeCount.intValue++ }
        composeRule.waitForIdle()

        registry.launchedPermissionList shouldHaveSize 1
    }

    @Test
    fun `TC-LOCATION-PERMISSION-DOMAIN-002 다른 화면으로 이동했다가 복귀해도 요청 조건을 다시 확인하지 않는다`() {
        val registry = PermissionResultRegistry(result = locationResult(isFineGranted = false, isCoarseGranted = false))
        var isScreenVisible by mutableStateOf(true)

        // 내비게이션처럼 떠난 화면은 composition에서 내리되 저장 상태는 보관하고, 화면 전체를 담는 자리에 요청 기록을 둔다.
        composeRule.setContent {
            CompositionLocalProvider(LocalPermissionRequestHistory provides rememberPermissionRequestHistory()) {
                val saveableStateHolder = rememberSaveableStateHolder()

                if (isScreenVisible) {
                    saveableStateHolder.SaveableStateProvider(key = SCREEN_KEY) {
                        RequestPermissionContent(permission = Permission.LOCATION, registry = registry)
                    }
                }
            }
        }
        composeRule.waitForIdle()

        composeRule.runOnIdle { isScreenVisible = false }
        composeRule.waitForIdle()
        composeRule.runOnIdle { isScreenVisible = true }
        composeRule.waitForIdle()

        registry.launchedPermissionList shouldHaveSize 1
    }

    @Test
    fun `TC-LOCATION-PERMISSION-DOMAIN-002 앱이 백그라운드에 갔다가 돌아와도 요청 조건을 다시 확인하지 않는다`() {
        val registry = PermissionResultRegistry(result = locationResult(isFineGranted = false, isCoarseGranted = false))
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.STARTED)

        setRequestPermissionEffect(
            permission = Permission.LOCATION,
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
    fun `TC-LOCATION-PERMISSION-DOMAIN-003 화면이 처음부터 다시 시작되면 요청 조건을 다시 확인한다`() {
        val registry = PermissionResultRegistry(result = locationResult(isFineGranted = false, isCoarseGranted = false))
        val restorationTester = StateRestorationTester(composeRule)

        restorationTester.setContent {
            RequestPermissionContent(permission = Permission.LOCATION, registry = registry)
        }
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        registry.launchedPermissionList shouldHaveSize 2
    }

    @Test
    fun `TC-LOCATION-PERMISSION-DOMAIN-007 Android에서는 정확한 위치 사용 권한을 포함해 요청한다`() {
        val registry = PermissionResultRegistry(result = locationResult(isFineGranted = false, isCoarseGranted = false))

        setRequestPermissionEffect(permission = Permission.LOCATION, registry = registry)
        composeRule.waitForIdle()

        registry.launchedPermissionList.single() shouldContain Manifest.permission.ACCESS_FINE_LOCATION
    }

    @Test
    fun `대략적인 위치만 허용해도 허용으로 처리한다`() {
        val registry = PermissionResultRegistry(result = locationResult(isFineGranted = false, isCoarseGranted = true))
        val resultList = mutableListOf<PermissionResult>()

        setRequestPermissionEffect(
            permission = Permission.LOCATION,
            registry = registry,
            onResult = { result -> resultList += result },
        )
        composeRule.waitForIdle()

        resultList.single() shouldBe PermissionResult.GRANTED
    }

    @Test
    fun `TC-LOCATION-PERMISSION-DOMAIN-010 앱 안에서 요청을 허용하면 응답 직후 바뀐 허용 여부가 반영된다`() {
        val requestCode = slot<Int>()
        val registry = spyk<ActivityResultRegistry>()
        every { registry.onLaunch(capture(requestCode), any<ActivityResultContract<Any?, Any?>>(), any(), any()) } just runs

        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides TestLifecycleOwner(Lifecycle.State.RESUMED)) {
                val registryOwner =
                    object : ActivityResultRegistryOwner {
                        override val activityResultRegistry: ActivityResultRegistry = registry
                    }

                CompositionLocalProvider(LocalActivityResultRegistryOwner provides registryOwner) {
                    val permissionManager = rememberPermissionManager()
                    val isGranted = rememberIsPermissionGranted(Permission.LOCATION, permissionManager)

                    Text(text = GRANTED_CONTENT + isGranted)
                    RequestPermissionEffect(
                        permission = Permission.LOCATION,
                        onResult = {},
                        permissionManager = permissionManager,
                    )
                }
            }
        }
        composeRule.waitForIdle()

        requestCode.isCaptured shouldBe true
        composeRule.onNodeWithText(GRANTED_CONTENT + false).assertIsDisplayed()

        // 실제 시스템은 허용 응답과 함께 권한 상태도 바꾸므로 응답 전에 권한을 허용한다.
        composeRule.runOnIdle {
            val result = locationResult(isFineGranted = true, isCoarseGranted = true)
            shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(*result.keys.toTypedArray())
            registry.dispatchResult(requestCode.captured, result)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(GRANTED_CONTENT + true).assertIsDisplayed()
    }

    private fun setRequestPermissionEffect(
        permission: Permission,
        registry: ActivityResultRegistry,
        onResult: (PermissionResult) -> Unit = {},
        lifecycleOwner: TestLifecycleOwner = TestLifecycleOwner(Lifecycle.State.STARTED),
        recomposeCount: MutableIntState = mutableIntStateOf(0),
    ) {
        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                RequestPermissionContent(
                    permission = permission,
                    registry = registry,
                    onResult = onResult,
                    recomposeCount = recomposeCount,
                )
            }
        }
    }

    @Composable
    private fun RequestPermissionContent(
        permission: Permission,
        registry: ActivityResultRegistry,
        onResult: (PermissionResult) -> Unit = {},
        recomposeCount: MutableIntState = mutableIntStateOf(0),
    ) {
        val registryOwner =
            object : ActivityResultRegistryOwner {
                override val activityResultRegistry: ActivityResultRegistry = registry
            }

        CompositionLocalProvider(LocalActivityResultRegistryOwner provides registryOwner) {
            // 재구성 계기를 효과 호출부와 같은 람다에서 읽어야 효과가 다시 호출된다.
            Text(text = "$APP_CONTENT ${recomposeCount.intValue}")
            RequestPermissionEffect(
                permission = permission,
                onResult = onResult,
            )
        }
    }

    private class PermissionResultRegistry(
        private val result: Map<String, Boolean>,
    ) : ActivityResultRegistry() {
        val launchedPermissionList = mutableListOf<List<String>>()

        override fun <I, O> onLaunch(
            requestCode: Int,
            contract: ActivityResultContract<I, O>,
            input: I,
            options: ActivityOptionsCompat?,
        ) {
            launchedPermissionList += (input as Array<*>).map { permission -> permission.toString() }

            // 실제 시스템은 허용 응답과 함께 권한 상태도 바꾸므로 조회 결과가 응답을 따라가게 한다.
            val grantedList = result.filterValues { isGranted -> isGranted }.keys
            if (grantedList.isNotEmpty()) {
                shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(*grantedList.toTypedArray())
            }

            dispatchResult(requestCode, result)
        }
    }

    companion object {
        private const val APP_CONTENT = "앱 화면"
        private const val GRANTED_CONTENT = "허용 여부="
        private const val FIRST_SCREEN = "첫 화면"
        private const val SECOND_SCREEN = "다른 화면"
        private const val SCREEN_KEY = "screen"

        private fun notificationResult(isGranted: Boolean) = mapOf(Manifest.permission.POST_NOTIFICATIONS to isGranted)

        private fun locationResult(
            isFineGranted: Boolean,
            isCoarseGranted: Boolean,
        ) = mapOf(
            Manifest.permission.ACCESS_FINE_LOCATION to isFineGranted,
            Manifest.permission.ACCESS_COARSE_LOCATION to isCoarseGranted,
        )
    }
}
