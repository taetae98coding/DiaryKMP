package io.github.taetae98coding.diary.feature.qr.ui.add

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.feature.qr.ui.code.QrCodeValueKey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.receiveAsFlow

internal const val TITLE_INPUT_INDEX: Int = 0
internal const val DESCRIPTION_INPUT_INDEX: Int = 1
internal const val VALUE_INPUT_INDEX: Int = 2
internal const val INPUT_COUNT: Int = 3

internal fun SemanticsNodeInteractionsProvider.qrCodeValue(): String =
    onNode(SemanticsMatcher.keyIsDefined(QrCodeValueKey))
        .fetchSemanticsNode()
        .config[QrCodeValueKey]

internal fun SemanticsNodeInteractionsProvider.onTitleInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[TITLE_INPUT_INDEX]

internal fun SemanticsNodeInteractionsProvider.onDescriptionInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[DESCRIPTION_INPUT_INDEX]

internal fun SemanticsNodeInteractionsProvider.onQrValueInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[VALUE_INPUT_INDEX]

internal fun SemanticsNodeInteractionsProvider.inputCount(): Int = onAllNodes(hasSetTextAction()).fetchSemanticsNodes().size

internal fun SemanticsNodeInteraction.editableText(): String =
    fetchSemanticsNode()
        .config[SemanticsProperties.EditableText]
        .text

internal fun SemanticsNodeInteractionsProvider.titleInputText(): String = onTitleInput().editableText()

internal fun SemanticsNodeInteractionsProvider.descriptionInputText(): String = onDescriptionInput().editableText()

internal fun SemanticsNodeInteractionsProvider.qrValueInputText(): String = onQrValueInput().editableText()

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
