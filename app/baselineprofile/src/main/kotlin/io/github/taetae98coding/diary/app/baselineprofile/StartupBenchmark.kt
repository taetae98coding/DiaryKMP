package io.github.taetae98coding.diary.app.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
public class StartupBenchmark {
    @get:Rule
    public val rule: MacrobenchmarkRule = MacrobenchmarkRule()

    @Test
    public fun startupWithoutCompilation() {
        startup(compilationMode = CompilationMode.None())
    }

    @Test
    public fun startupWithBaselineProfile() {
        startup(compilationMode = CompilationMode.Partial(baselineProfileMode = BaselineProfileMode.Require))
    }

    private fun startup(compilationMode: CompilationMode) {
        rule.measureRepeated(
            packageName = targetPackageName,
            metrics = listOf(StartupTimingMetric()),
            compilationMode = compilationMode,
            iterations = 10,
            startupMode = StartupMode.COLD,
            setupBlock = {
                grantStartupPermission()
                pressHome()
            },
        ) {
            startActivityAndWait()
            waitForTopLevelNavigation()
        }
    }
}
