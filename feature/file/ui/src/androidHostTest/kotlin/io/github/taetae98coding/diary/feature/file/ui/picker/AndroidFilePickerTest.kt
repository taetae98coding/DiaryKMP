package io.github.taetae98coding.diary.feature.file.ui.picker

import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AndroidFilePickerTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-FILE-HOME-DOMAIN-008 선택 도구가 열린 동안 화면이 재생성되어도 고른 파일을 올린다`() {
        val uri = Uri.parse("content://documents/${fixtureMonkey.giveMeOne<Int>()}")
        val harness = ActivityResultRegistryHarness()
        val pickedList = mutableListOf<FileUri>()
        lateinit var picker: FilePicker
        val tester = StateRestorationTester(composeRule)

        tester.setContent {
            CompositionLocalProvider(LocalActivityResultRegistryOwner provides harness.owner) {
                picker = rememberFilePicker(onPick = { value -> pickedList += value })
            }
        }
        composeRule.runOnIdle { picker.open() }
        tester.emulateSavedInstanceStateRestore()
        composeRule.runOnIdle { harness.registry.dispatchResult(harness.launchedRequestCode.shouldNotBeNull(), uri) }

        composeRule.runOnIdle { pickedList shouldBe listOf(FileUri(uri.toString())) }
    }

    @Test
    fun `TC-FILE-HOME-DOMAIN-008 선택 도구가 열린 동안 시스템이 앱을 정리했다가 다시 만들어도 고른 파일을 올린다`() {
        val uri = Uri.parse("content://documents/${fixtureMonkey.giveMeOne<Int>()}")
        var harness = ActivityResultRegistryHarness()
        val pickedList = mutableListOf<FileUri>()
        lateinit var picker: FilePicker
        val tester = StateRestorationTester(composeRule)

        tester.setContent {
            CompositionLocalProvider(LocalActivityResultRegistryOwner provides harness.owner) {
                picker = rememberFilePicker(onPick = { value -> pickedList += value })
            }
        }
        composeRule.runOnIdle { picker.open() }
        val requestCode = harness.launchedRequestCode.shouldNotBeNull()
        // 시스템이 앱을 정리하면 결과 전달 수단도 새로 만들어지고, 남겨 둔 상태에서 선택 도구를 연 기록을 되살린다.
        val savedState = Bundle().also { bundle -> harness.registry.onSaveInstanceState(bundle) }
        harness = ActivityResultRegistryHarness().also { restored -> restored.registry.onRestoreInstanceState(savedState) }
        tester.emulateSavedInstanceStateRestore()
        composeRule.runOnIdle { harness.registry.dispatchResult(requestCode, uri) }

        composeRule.runOnIdle { pickedList shouldBe listOf(FileUri(uri.toString())) }
    }
}

// 선택 도구를 실제로 여는 대신 연 기록만 남기고, 결과 전달과 상태 저장·복원은 결과 전달 수단의 실제 동작을 쓴다.
private class ActivityResultRegistryHarness {
    private val requestCodeSlot = slot<Int>()

    val registry: ActivityResultRegistry =
        spyk<ActivityResultRegistry>().also { registry ->
            every { registry.onLaunch(capture(requestCodeSlot), any<ActivityResultContract<Any?, Any?>>(), any(), any()) } just Runs
        }

    val owner: ActivityResultRegistryOwner =
        mockk<ActivityResultRegistryOwner>().also { owner -> every { owner.activityResultRegistry } returns registry }

    val launchedRequestCode: Int?
        get() = if (requestCodeSlot.isCaptured) requestCodeSlot.captured else null
}
