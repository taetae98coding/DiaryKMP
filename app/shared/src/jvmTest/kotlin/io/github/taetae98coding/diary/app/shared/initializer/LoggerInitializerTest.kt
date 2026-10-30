package io.github.taetae98coding.diary.app.shared.initializer

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.logger.console.api.ConsoleLog
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class LoggerInitializerTest :
    FunSpec({
        // 기록 수단은 전역 창구에 등록된 뒤 해제할 수 없어, 배포 빌드 초기화를 먼저 확인하고 같은 테스트 안에서 디버그 빌드 초기화를 이어서 확인한다.
        test("TC-c78b0f9b-54b5-40bd-9864-5ac76c6dd1e7 배포 빌드로 초기화하면 콘솔 로그가 남지 않고 디버그 빌드로 초기화하면 남는다") {
            val releaseMessage = "release${fixtureMonkey.giveMeOne<Int>()}"
            val debugMessage = "debug${fixtureMonkey.giveMeOne<Int>()}"

            LoggerInitializer.initialize(isDebug = false)
            val releaseOutput = captureConsole { DiaryLogger.log(log = ConsoleLog(message = releaseMessage)) }

            LoggerInitializer.initialize(isDebug = true)
            val debugOutput = captureConsole { DiaryLogger.log(log = ConsoleLog(message = debugMessage)) }

            releaseOutput shouldNotContain releaseMessage
            debugOutput shouldContain debugMessage
        }
    }) {
    private companion object {
        val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

        fun captureConsole(block: () -> Unit): String {
            val originalOut = System.out
            val outputStream = ByteArrayOutputStream()
            System.setOut(PrintStream(outputStream))

            try {
                block()
            } finally {
                System.setOut(originalOut)
            }

            return outputStream.toString()
        }
    }
}
