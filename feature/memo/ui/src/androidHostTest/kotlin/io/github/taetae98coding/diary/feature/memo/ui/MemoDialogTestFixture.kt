package io.github.taetae98coding.diary.feature.memo.ui

import androidx.activity.ComponentDialog
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import org.robolectric.shadows.ShadowDialog

/**
 * 선택 목록에는 확인 버튼이 없으므로 목록을 닫는 조작인 뒤로가기를 다이얼로그 창에 전달한다.
 */
internal fun ComposeContentTestRule.closeDialogByBack() {
    val dialog = ShadowDialog.getLatestDialog() as ComponentDialog

    runOnUiThread { dialog.onBackPressedDispatcher.onBackPressed() }
    waitForIdle()
}
