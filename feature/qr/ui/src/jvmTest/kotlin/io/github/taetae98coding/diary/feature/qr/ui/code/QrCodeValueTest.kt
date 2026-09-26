package io.github.taetae98coding.diary.feature.qr.ui.code

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class QrCodeValueTest :
    FunSpec({
        test("TC-QR-ADD-DOMAIN-002 가장 큰 QR에 가장 낮은 오류 복원 수준으로 담을 수 있는지로 길이를 판정한다") {
            val caseList =
                listOf(
                    "1".repeat(MAX_NUMERIC_LENGTH) to true,
                    "1".repeat(MAX_NUMERIC_LENGTH + 1) to false,
                    "A".repeat(MAX_ALPHANUMERIC_LENGTH) to true,
                    "A".repeat(MAX_ALPHANUMERIC_LENGTH + 1) to false,
                    "a".repeat(MAX_BYTE_LENGTH) to true,
                    "a".repeat(MAX_BYTE_LENGTH + 1) to false,
                    "한".repeat(MAX_KOREAN_LENGTH) to true,
                    "한".repeat(MAX_KOREAN_LENGTH + 1) to false,
                    "1".repeat(MAX_BYTE_LENGTH - 1) + "a" to true,
                    "1".repeat(MAX_BYTE_LENGTH) + "a" to false,
                )

            caseList.forEach { (value, expected) ->
                value.fitsInQrCode() shouldBe expected
            }
        }

        test("짧은 값과 줄바꿈이 섞인 값은 QR에 담을 수 있다") {
            listOf("a", " ", "a\nb", "https://example.com").forEach { value ->
                value.fitsInQrCode() shouldBe true
            }
        }
    }) {
    private companion object {
        const val MAX_NUMERIC_LENGTH = 7089
        const val MAX_ALPHANUMERIC_LENGTH = 4296
        const val MAX_BYTE_LENGTH = 2953
        const val MAX_KOREAN_LENGTH = 984
    }
}
