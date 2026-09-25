package io.github.taetae98coding.diary.compose.permission

import android.Manifest
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import io.github.taetae98coding.diary.core.permission.Permission
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.spyk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RememberIsPermissionGrantedTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-LOCATION-PERMISSION-DOMAIN-008 허용 여부를 확인해도 시스템 위치 권한 요청이 시작되지 않는다`() {
        val registry = launchIgnoringRegistry()

        setIsPermissionGranted(registry = registry)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(GRANTED_CONTENT + false).assertIsDisplayed()
        verify(exactly = 0) { registry.onLaunch(any(), any<ActivityResultContract<Any?, Any?>>(), any(), any()) }
    }

    @Test
    fun `TC-LOCATION-PERMISSION-DOMAIN-009 앱 밖에서 위치 권한을 허용하고 돌아오면 바뀐 허용 여부가 반영된다`() {
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)

        setIsPermissionGranted(lifecycleOwner = lifecycleOwner)
        composeRule.onNodeWithText(GRANTED_CONTENT + false).assertIsDisplayed()

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(*LOCATION_PERMISSION_LIST)
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(GRANTED_CONTENT + true).assertIsDisplayed()
    }

    @Test
    fun `TC-LOCATION-PERMISSION-DOMAIN-009 앱 밖에서 위치 권한을 거두고 돌아오면 바뀐 허용 여부가 반영된다`() {
        shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(*LOCATION_PERMISSION_LIST)
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)

        setIsPermissionGranted(lifecycleOwner = lifecycleOwner)
        composeRule.onNodeWithText(GRANTED_CONTENT + true).assertIsDisplayed()

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        shadowOf(RuntimeEnvironment.getApplication()).denyPermissions(*LOCATION_PERMISSION_LIST)
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(GRANTED_CONTENT + false).assertIsDisplayed()
    }

    @Test
    fun `TC-LOCATION-PERMISSION-DOMAIN-012 대략적인 위치만 허용된 상태도 허용으로 확인한다`() {
        shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)

        setIsPermissionGranted()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(GRANTED_CONTENT + true).assertIsDisplayed()
    }

    private fun setIsPermissionGranted(
        registry: ActivityResultRegistry = launchIgnoringRegistry(),
        lifecycleOwner: TestLifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED),
    ) {
        val registryOwner =
            object : ActivityResultRegistryOwner {
                override val activityResultRegistry: ActivityResultRegistry = registry
            }

        composeRule.setContent {
            CompositionLocalProvider(
                LocalLifecycleOwner provides lifecycleOwner,
                LocalActivityResultRegistryOwner provides registryOwner,
            ) {
                val isGranted = rememberIsPermissionGranted(Permission.LOCATION)

                Text(text = GRANTED_CONTENT + isGranted)
            }
        }
    }

    private fun launchIgnoringRegistry(): ActivityResultRegistry {
        val registry = spyk<ActivityResultRegistry>()
        every { registry.onLaunch(any(), any<ActivityResultContract<Any?, Any?>>(), any(), any()) } just runs

        return registry
    }

    companion object {
        private const val GRANTED_CONTENT = "허용 여부="

        private val LOCATION_PERMISSION_LIST =
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
    }
}
