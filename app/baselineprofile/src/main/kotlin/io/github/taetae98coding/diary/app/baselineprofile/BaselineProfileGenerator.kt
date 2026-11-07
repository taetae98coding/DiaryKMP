package io.github.taetae98coding.diary.app.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
public class BaselineProfileGenerator {
    @get:Rule
    public val rule: BaselineProfileRule = BaselineProfileRule()

    // 시작 경로만 startup profile에 넣어야 dex layout 최적화가 시작에 필요한 클래스만 앞에 모은다.
    @Test
    public fun startup() {
        rule.collect(
            packageName = targetPackageName,
            includeInStartupProfile = true,
        ) {
            grantStartupPermission()
            pressHome()
            startActivityAndWait()
            waitForTopLevelNavigation()
        }
    }

    @Test
    public fun topLevelNavigation() {
        rule.collect(packageName = targetPackageName) {
            grantStartupPermission()
            pressHome()
            startActivityAndWait()
            waitForTopLevelNavigation()
            visitTopLevelNavigation()
        }
    }
}
