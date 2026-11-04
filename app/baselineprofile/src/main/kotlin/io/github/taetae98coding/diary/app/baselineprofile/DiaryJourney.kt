package io.github.taetae98coding.diary.app.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import java.util.regex.Pattern

private const val TIMEOUT_MILLIS = 10_000L

// 기기 언어에 따라 영어나 한국어 이름이 보이므로 둘 다 찾는다.
private val TOP_LEVEL_NAVIGATION_LABEL_LIST =
    listOf(
        "Memo|메모",
        "Tag|태그",
        "Routine|루틴",
        "More|더보기",
        "Calendar|캘린더",
    )

// 계측 인자는 build.gradle.kts의 flavor가 대상 앱의 applicationId로 정한다.
internal val targetPackageName: String =
    requireNotNull(InstrumentationRegistry.getArguments().getString("targetAppId")) {
        "targetAppId instrumentation argument is missing"
    }

// 앱과 캘린더 홈이 시작하자마자 띄우는 권한 요청이 화면을 가리지 않게 미리 허용한다.
private val STARTUP_PERMISSION_LIST =
    listOf(
        "android.permission.POST_NOTIFICATIONS",
        "android.permission.ACCESS_COARSE_LOCATION",
        "android.permission.ACCESS_FINE_LOCATION",
    )

internal fun MacrobenchmarkScope.grantStartupPermission() {
    STARTUP_PERMISSION_LIST.forEach { permission ->
        device.executeShellCommand("pm grant $packageName $permission")
    }
}

internal fun MacrobenchmarkScope.waitForTopLevelNavigation() {
    findTopLevelNavigationItem(label = TOP_LEVEL_NAVIGATION_LABEL_LIST.first())
}

internal fun MacrobenchmarkScope.visitTopLevelNavigation() {
    TOP_LEVEL_NAVIGATION_LABEL_LIST.forEach { label ->
        findTopLevelNavigationItem(label = label).click()
        device.waitForIdle()
    }
}

// 찾지 못한 채 진행하면 다른 화면의 경로가 프로필에 담기므로 바로 실패시킨다.
private fun MacrobenchmarkScope.findTopLevelNavigationItem(label: String): UiObject2 =
    checkNotNull(device.wait(Until.findObject(By.text(Pattern.compile(label))), TIMEOUT_MILLIS)) {
        "Top level navigation item is not found: $label"
    }
