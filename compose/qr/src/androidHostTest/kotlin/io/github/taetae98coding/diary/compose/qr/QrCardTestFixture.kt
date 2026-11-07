package io.github.taetae98coding.diary.compose.qr

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.core.testing.qr.qr
import io.github.taetae98coding.diary.core.testing.qr.qrDetail
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

internal fun testQr(): Qr = Arb.qr(isDeleted = false, detail = Arb.qrDetail().next()).next()

internal fun SemanticsNodeInteractionsProvider.qrCodeValueList(): List<String> =
    onAllNodes(SemanticsMatcher.keyIsDefined(QrCodeValueKey), useUnmergedTree = true)
        .fetchSemanticsNodes()
        .map { node -> node.config[QrCodeValueKey] }
