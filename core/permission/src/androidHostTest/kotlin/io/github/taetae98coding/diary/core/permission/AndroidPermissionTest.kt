package io.github.taetae98coding.diary.core.permission

import android.Manifest
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AndroidPermissionTest {
    @Test
    fun `알림 권한은 알림을 화면에 표시하는 권한만 요청한다`() {
        Permission.NOTIFICATION.toAndroidPermissionList() shouldContainExactly listOf(Manifest.permission.POST_NOTIFICATIONS)
    }

    @Test
    fun `위치 권한은 가장 정확한 위치 사용 권한을 포함해 요청한다`() {
        Permission.LOCATION.toAndroidPermissionList() shouldContainExactly
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
    }

    @Test
    fun `카메라 권한은 카메라 사용 권한 하나만 요청한다`() {
        Permission.CAMERA.toAndroidPermissionList() shouldContainExactly listOf(Manifest.permission.CAMERA)
    }

    @Test
    fun `카메라 권한이 허용되어 있으면 허용 여부 조회 결과가 허용이다`() {
        shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(Manifest.permission.CAMERA)

        RuntimeEnvironment.getApplication().isPermissionGranted(Permission.CAMERA) shouldBe true
    }

    @Test
    fun `권한이 허용되지 않았으면 허용 여부 조회 결과가 허용되지 않음이다`() {
        RuntimeEnvironment.getApplication().isPermissionGranted(Permission.LOCATION) shouldBe false
    }

    @Test
    fun `대략적인 위치만 허용되어 있어도 허용 여부 조회 결과가 허용이다`() {
        shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)

        RuntimeEnvironment.getApplication().isPermissionGranted(Permission.LOCATION) shouldBe true
    }

    @Test
    fun `알림 권한이 허용되어 있으면 허용 여부 조회 결과가 허용이다`() {
        shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)

        RuntimeEnvironment.getApplication().isPermissionGranted(Permission.NOTIFICATION) shouldBe true
    }
}
