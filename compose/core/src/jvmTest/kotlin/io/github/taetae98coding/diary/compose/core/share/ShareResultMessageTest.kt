package io.github.taetae98coding.diary.compose.core.share

import androidx.compose.material3.SnackbarHostState
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

class ShareResultMessageTest :
    FunSpec({
        test("복사로 대신하면 복사 안내를, 공유하지 못하면 실패 안내를 고른다") {
            val failedMessage = Arb.nonBlankString().next()
            val copiedMessage = Arb.nonBlankString().next()
            val message = ShareResultMessage(failedMessage = failedMessage, copiedMessage = copiedMessage)

            message.messageOf(result = ShareResult.COPIED) shouldBe copiedMessage
            message.messageOf(result = ShareResult.FAILED) shouldBe failedMessage
        }

        test("공유를 마치거나 닫으면 안내를 고르지 않는다") {
            val message = ShareResultMessage(failedMessage = Arb.nonBlankString().next(), copiedMessage = Arb.nonBlankString().next())

            message.messageOf(result = ShareResult.COMPLETED).shouldBeNull()
            message.messageOf(result = ShareResult.DISMISSED).shouldBeNull()
        }

        test("복사로 대신하거나 공유하지 못하면 고른 안내를 스낵바로 띄운다") {
            runTest {
                val failedMessage = Arb.nonBlankString().next()
                val copiedMessage = Arb.nonBlankString().next()
                val message = ShareResultMessage(failedMessage = failedMessage, copiedMessage = copiedMessage)
                val snackbarHostState = SnackbarHostState()

                backgroundScope.launch { snackbarHostState.showShareResult(result = ShareResult.COPIED, message = message) }
                runCurrent()
                snackbarHostState.currentSnackbarData?.visuals?.message shouldBe copiedMessage

                backgroundScope.launch { snackbarHostState.showShareResult(result = ShareResult.FAILED, message = message) }
                runCurrent()
                snackbarHostState.currentSnackbarData?.visuals?.message shouldBe failedMessage
            }
        }

        test("공유를 마치거나 닫으면 스낵바를 띄우지 않는다") {
            runTest {
                val message = ShareResultMessage(failedMessage = Arb.nonBlankString().next(), copiedMessage = Arb.nonBlankString().next())
                val snackbarHostState = SnackbarHostState()

                snackbarHostState.showShareResult(result = ShareResult.COMPLETED, message = message)
                snackbarHostState.showShareResult(result = ShareResult.DISMISSED, message = message)

                snackbarHostState.currentSnackbarData.shouldBeNull()
            }
        }
    })
