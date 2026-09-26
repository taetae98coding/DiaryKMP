package io.github.taetae98coding.diary.feature.qr.ui.scan

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

private fun FixtureMonkey.qrValue(): String = "qr-" + giveMeOne<String>()

class QrScanReaderTest :
    FunSpec({
        test("TC-QR-SCAN-FEATURE-007 QR을 읽으면 읽은 값을 한 번 넘긴다") {
            val value = fixtureMonkey.qrValue()
            val readList = mutableListOf<String>()
            val reader = QrScanReader(onRead = { read -> readList += read })

            reader.read(value = value)

            readList shouldBe listOf(value)
        }

        test("TC-QR-SCAN-DOMAIN-002 담긴 값을 받을 수 없는 QR은 읽지 않은 것으로 보고 계속 스캔한다") {
            listOf("", "1".repeat(MAX_NUMERIC_LENGTH + 1)).forEach { rejectedValue ->
                val value = fixtureMonkey.qrValue()
                val readList = mutableListOf<String>()
                val reader = QrScanReader(onRead = { read -> readList += read })

                reader.read(value = rejectedValue)
                reader.read(value = value)

                readList shouldBe listOf(value)
            }
        }

        test("TC-QR-SCAN-DOMAIN-003 QR을 하나 읽으면 그 뒤에 읽은 QR은 쓰지 않는다") {
            val firstValue = fixtureMonkey.qrValue()
            val secondValue = fixtureMonkey.qrValue()
            val readList = mutableListOf<String>()
            val reader = QrScanReader(onRead = { read -> readList += read })

            reader.read(value = firstValue)
            reader.read(value = secondValue)

            readList shouldBe listOf(firstValue)
        }

        test("같은 QR을 여러 번 읽어도 한 번만 넘긴다") {
            val value = fixtureMonkey.qrValue()
            val readList = mutableListOf<String>()
            val reader = QrScanReader(onRead = { read -> readList += read })

            repeat(3) { reader.read(value = value) }

            readList shouldBe listOf(value)
        }
    }) {
    private companion object {
        const val MAX_NUMERIC_LENGTH = 7089
    }
}
