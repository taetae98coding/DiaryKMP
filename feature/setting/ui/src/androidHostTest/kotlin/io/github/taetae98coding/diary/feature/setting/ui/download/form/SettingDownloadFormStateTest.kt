package io.github.taetae98coding.diary.feature.setting.ui.download.form

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadProxySetting
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val STORED_ADDRESS = "http://192.168.0.10:27180"
private const val EDITING_ADDRESS = "http://10.0.0.5:27180"
private const val OTHER_ADDRESS = "http://172.16.4.2:27180"

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SettingDownloadFormStateTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-SETTING-DOWNLOAD-FEATURE-015 화면이 재생성되어도 입력 중이던 주소를 유지한다`() {
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var state: SettingDownloadFormState

        restorationTester.setContent {
            state = rememberSettingDownloadFormState(initialSetting = MusicDownloadProxySetting(address = STORED_ADDRESS))
        }

        composeRule.runOnIdle { state.addressState.setTextAndPlaceCursorAtEnd(EDITING_ADDRESS) }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle { state.setting shouldBe MusicDownloadProxySetting(address = EDITING_ADDRESS) }
    }

    @Test
    fun `TC-SETTING-DOWNLOAD-FEATURE-017 저장된 주소를 초기 값으로 편집 상태를 만든다`() {
        lateinit var state: SettingDownloadFormState

        composeRule.setContent {
            state = rememberSettingDownloadFormState(initialSetting = MusicDownloadProxySetting(address = STORED_ADDRESS))
        }

        composeRule.runOnIdle { state.setting shouldBe MusicDownloadProxySetting(address = STORED_ADDRESS) }
    }

    @Test
    fun `TC-SETTING-DOWNLOAD-FEATURE-017 저장된 주소가 바뀌어도 편집 중인 값을 덮어쓰지 않는다`() {
        val storedSetting = mutableStateOf(MusicDownloadProxySetting(address = STORED_ADDRESS))
        lateinit var state: SettingDownloadFormState

        composeRule.setContent {
            val setting by storedSetting
            state = rememberSettingDownloadFormState(initialSetting = setting)
        }

        composeRule.runOnIdle { state.addressState.setTextAndPlaceCursorAtEnd(EDITING_ADDRESS) }
        composeRule.runOnIdle { storedSetting.value = MusicDownloadProxySetting(address = OTHER_ADDRESS) }

        composeRule.runOnIdle { state.setting shouldBe MusicDownloadProxySetting(address = EDITING_ADDRESS) }
    }
}
