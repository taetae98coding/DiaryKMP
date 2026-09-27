package io.github.taetae98coding.diary.feature.qr.ui.add

import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isPopup
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.domain.qr.usecase.AddQrUseCase
import io.github.taetae98coding.diary.domain.setting.usecase.GetDefaultMapProviderUseCase
import io.github.taetae98coding.diary.feature.qr.ui.code.QrCodeValueKey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow

internal const val INFO_TAB_INDEX: Int = 0
internal const val QR_TAB_INDEX: Int = 1
internal const val TITLE_INPUT_INDEX: Int = 0
internal const val DESCRIPTION_INPUT_INDEX: Int = 1
internal const val TEXT_INPUT_INDEX: Int = 0

internal fun SemanticsNodeInteractionsProvider.qrCodeValue(): String =
    onNode(SemanticsMatcher.keyIsDefined(QrCodeValueKey))
        .fetchSemanticsNode()
        .config[QrCodeValueKey]

internal fun SemanticsNodeInteractionsProvider.onTab(index: Int): SemanticsNodeInteraction = onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab))[index]

internal fun SemanticsNodeInteractionsProvider.isTabSelected(index: Int): Boolean = onTab(index).fetchSemanticsNode().config[SemanticsProperties.Selected]

// 이미 고른 탭은 다시 누르지 않는다. 입력 칸의 커서 손잡이가 떠 있으면 누른 좌표를 가로채므로 탭의 누르기 동작을 직접 실행한다.
internal fun SemanticsNodeInteractionsProvider.selectTab(index: Int) {
    if (isTabSelected(index)) return

    onTab(index).performSemanticsAction(SemanticsActions.OnClick)
}

internal fun SemanticsNodeInteractionsProvider.onTitleInput(): SemanticsNodeInteraction {
    selectTab(INFO_TAB_INDEX)
    return onAllNodes(hasSetTextAction())[TITLE_INPUT_INDEX]
}

internal fun SemanticsNodeInteractionsProvider.onDescriptionInput(): SemanticsNodeInteraction {
    selectTab(INFO_TAB_INDEX)
    return onAllNodes(hasSetTextAction())[DESCRIPTION_INPUT_INDEX]
}

internal fun SemanticsNodeInteractionsProvider.onQrTextInput(): SemanticsNodeInteraction {
    selectTab(QR_TAB_INDEX)
    return onAllNodes(hasSetTextAction())[TEXT_INPUT_INDEX]
}

internal fun SemanticsNodeInteractionsProvider.onQrFieldInput(label: String): SemanticsNodeInteraction {
    selectTab(QR_TAB_INDEX)
    return onNode(hasSetTextAction() and hasText(label))
}

internal fun SemanticsNodeInteractionsProvider.selectFormat(
    formatLabel: String,
    formatName: String,
) {
    selectTab(QR_TAB_INDEX)
    onNode(hasText(formatLabel) and hasClickAction()).performSemanticsAction(SemanticsActions.OnClick)
    onAllNodes(hasText(formatName) and hasClickAction() and hasAnyAncestor(isPopup())).onLast().performSemanticsAction(SemanticsActions.OnClick)
}

internal fun SemanticsNodeInteractionsProvider.qrFieldLabelList(): List<String> =
    onAllNodes(hasSetTextAction())
        .fetchSemanticsNodes()
        .map { node ->
            node.config
                .getOrNull(SemanticsProperties.Text)
                .orEmpty()
                .joinToString { text -> text.text }
        }

internal fun SemanticsNode.editableText(): String = config[SemanticsProperties.EditableText].text

internal fun SemanticsNodeInteraction.editableText(): String =
    fetchSemanticsNode()
        .config[SemanticsProperties.EditableText]
        .text

internal fun SemanticsNodeInteractionsProvider.titleInputText(): String = onTitleInput().editableText()

internal fun SemanticsNodeInteractionsProvider.descriptionInputText(): String = onDescriptionInput().editableText()

internal fun SemanticsNodeInteractionsProvider.qrTextInputText(): String = onQrTextInput().editableText()

internal fun SemanticsNodeInteractionsProvider.actionNameList(): List<String> =
    onAllNodes(hasClickAction() or hasSetTextAction())
        .fetchSemanticsNodes()
        .map { node ->
            node.config.getOrNull(SemanticsProperties.ContentDescription)?.joinToString()
                ?: node.config
                    .getOrNull(SemanticsProperties.Text)
                    .orEmpty()
                    .joinToString { text -> text.text }
        }

internal fun SemanticsNodeInteractionsProvider.visibleTextList(): List<String> =
    onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text), useUnmergedTree = true)
        .fetchSemanticsNodes()
        .flatMap { node -> node.config[SemanticsProperties.Text] }
        .map { text -> text.text }

internal val qrTestFixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

internal const val MAX_NUMERIC_LENGTH: Int = 7089

// 입력 칸을 거치지 않고 상태를 바꾸므로, 프레임이 상태 변경을 알리는 것과 같게 변경 직후 알림을 보낸다.
internal fun ComposeTestRule.write(action: () -> Unit) {
    runOnIdle {
        action()
        Snapshot.sendApplyNotifications()
    }
}

// 지도 제공자의 실제 표시 요소는 만들 수 없으므로, 지도를 쓰지 않는 화면 테스트는 기본 지도를 읽지 못한 상태로 둔다.
internal fun qrAddViewModel(addQrUseCase: AddQrUseCase): QrAddViewModel {
    val getDefaultMapProviderUseCase = mockk<GetDefaultMapProviderUseCase>()
    every { getDefaultMapProviderUseCase(Unit) } returns flowOf(Result.failure(IllegalStateException("기본 지도를 읽지 못함")))
    return QrAddViewModel(addQrUseCase = addQrUseCase, getDefaultMapProviderUseCase = getDefaultMapProviderUseCase)
}

internal fun screenTestViewModel(
    effect: Flow<QrAddEffect> = emptyFlow(),
    uiState: QrAddUiState = QrAddUiState(),
): QrAddViewModel {
    val viewModel = mockk<QrAddViewModel>(relaxed = true)
    every { viewModel.uiState } returns MutableStateFlow(uiState)
    every { viewModel.effect } returns effect
    return viewModel
}

internal fun effectViewModel(effect: QrAddEffect): QrAddViewModel {
    val channel = Channel<QrAddEffect>(capacity = Channel.BUFFERED)
    val viewModel = screenTestViewModel(effect = channel.receiveAsFlow())
    every { viewModel.add(detail = any()) } answers { channel.trySend(effect).getOrThrow() }
    return viewModel
}
