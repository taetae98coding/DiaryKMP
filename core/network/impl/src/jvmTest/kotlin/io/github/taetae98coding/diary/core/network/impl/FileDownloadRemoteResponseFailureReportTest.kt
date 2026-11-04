package io.github.taetae98coding.diary.core.network.impl

import io.github.taetae98coding.diary.logger.core.DiaryLog
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.github.taetae98coding.diary.logger.core.DiaryLoggerDelegate
import io.github.taetae98coding.diary.logger.crashlytics.api.CrashlyticsLog
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.mockk.every
import io.mockk.mockk

class FileDownloadRemoteResponseFailureReportTest :
    FunSpec({
        test("TC-a03f08d5-1c4f-4bf2-bcf0-c166ec3f0baa 보관한 파일의 내용을 내려받는 요청이 500 응답을 받으면 오류 보고가 한 번 남는다") {
            val reportList = recordCrashlyticsLog()
            val client = createFileDownloadHttpClient(engine = MockEngine { respond(content = "", status = HttpStatusCode.InternalServerError) })

            client.get("https://$HOST/$PATH")

            reportList shouldHaveSize 1
        }

        test("TC-c0362129-f138-4b9e-b2a1-66c8da9ef22f 파일 내용을 내려받는 요청의 보고에는 주소 경로 없이 서버 이름만 담긴다") {
            val reportList = recordCrashlyticsLog()
            val client = createFileDownloadHttpClient(engine = MockEngine { respond(content = "", status = HttpStatusCode.Forbidden) })

            client.get("https://$HOST/$PATH?token=$TOKEN")

            val report = reportList.single()
            listOf(report.message, report.throwable.message.orEmpty(), report.issueKey).forEach { text ->
                text shouldContain "GET $HOST"
                text shouldNotContain PATH
                text shouldNotContain TOKEN
            }
            report.message shouldContain "403"
            report.issueKey shouldBe "GET $HOST"
        }
    }) {
    private companion object {
        private const val HOST = "storage.example.test"
        private const val PATH = "object/sign/files/account-id/file-id"
        private const val TOKEN = "signed-token"

        private fun recordCrashlyticsLog(): List<CrashlyticsLog> {
            val reportList = mutableListOf<CrashlyticsLog>()
            val delegate = mockk<DiaryLoggerDelegate>()
            every { delegate.log(log = any()) } answers { (firstArg<DiaryLog>() as? CrashlyticsLog)?.let(reportList::add) }
            DiaryLogger.add(delegate = delegate)
            return reportList
        }
    }
}
