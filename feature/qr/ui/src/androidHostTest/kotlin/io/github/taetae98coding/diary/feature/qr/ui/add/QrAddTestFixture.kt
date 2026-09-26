package io.github.taetae98coding.diary.feature.qr.ui.add

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.feature.qr.ui.code.QrCodeValueKey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey

internal fun SemanticsNodeInteractionsProvider.qrCodeValue(): String =
    onNode(SemanticsMatcher.keyIsDefined(QrCodeValueKey))
        .fetchSemanticsNode()
        .config[QrCodeValueKey]

internal fun SemanticsNodeInteractionsProvider.onQrValueInput(): SemanticsNodeInteraction = onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.SetText))

internal fun SemanticsNodeInteractionsProvider.qrValueInputText(): String =
    onQrValueInput()
        .fetchSemanticsNode()
        .config[SemanticsProperties.EditableText]
        .text

internal fun SemanticsNodeInteractionsProvider.visibleTextList(): List<String> =
    onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text), useUnmergedTree = true)
        .fetchSemanticsNodes()
        .flatMap { node -> node.config[SemanticsProperties.Text] }
        .map { text -> text.text }

internal val qrTestFixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

internal fun FixtureMonkey.qrValue(): String = "qr-" + giveMeOne<String>()

internal const val MAX_NUMERIC_LENGTH: Int = 7089
