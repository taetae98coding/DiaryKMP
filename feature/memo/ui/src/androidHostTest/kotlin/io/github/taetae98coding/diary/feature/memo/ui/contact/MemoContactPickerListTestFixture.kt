package io.github.taetae98coding.diary.feature.memo.ui.contact

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeContentTestRule

private const val CONTACT_PICKER_WAIT_ATTEMPT_COUNT: Int = 500
private const val CONTACT_PICKER_WAIT_INTERVAL_MILLIS: Long = 10

internal fun ComposeContentTestRule.contactPickerList(): SemanticsNodeInteraction = onNode(hasTestTag(MEMO_CONTACT_PICKER_LIST_TEST_TAG))

/**
 * 선택 목록은 페이지 단위로 준비되므로 첫 페이지가 목록에 나타날 때까지 프레임을 진행시킨다.
 */
internal fun ComposeContentTestRule.awaitContactPickerRows() {
    awaitContactPicker(description = "첫 페이지가 준비되지 않았다") {
        contactPickerList().fetchSemanticsNode().children.isNotEmpty()
    }
}

private fun ComposeContentTestRule.awaitContactPicker(
    description: String,
    condition: () -> Boolean,
) {
    repeat(CONTACT_PICKER_WAIT_ATTEMPT_COUNT) {
        waitForIdle()
        if (condition()) return
        advanceContactPicker()
    }

    error(description)
}

/**
 * 목록 갱신은 백그라운드 조회가 끝난 뒤에 반영되므로 프레임과 실제 시간을 함께 진행시킨다.
 */
private fun ComposeContentTestRule.advanceContactPicker() {
    mainClock.advanceTimeByFrame()
    @Suppress("ForbiddenMethodCall")
    Thread.sleep(CONTACT_PICKER_WAIT_INTERVAL_MILLIS)
}
