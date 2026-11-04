package io.github.taetae98coding.diary.core.ip.network.impl

import io.github.taetae98coding.diary.logger.core.DiaryLog
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.github.taetae98coding.diary.logger.core.DiaryLoggerDelegate
import io.github.taetae98coding.diary.logger.crashlytics.api.CrashlyticsLog
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.mockk.every
import io.mockk.mockk

class RemoteResponseFailureReportTest :
    FunSpec({
        test("TC-a03f08d5-1c4f-4bf2-bcf0-c166ec3f0baa 위치 추정 요청이 500 응답을 받으면 오류 보고가 한 번 남는다") {
            val reportList = mutableListOf<CrashlyticsLog>()
            val delegate = mockk<DiaryLoggerDelegate>()
            every { delegate.log(log = any()) } answers { (firstArg<DiaryLog>() as? CrashlyticsLog)?.let(reportList::add) }
            DiaryLogger.add(delegate = delegate)
            val engine = MockEngine { respond(content = "", status = HttpStatusCode.InternalServerError) }
            val client = createIpHttpClient(engine = engine)

            runCatching { client.get("report-test") }

            reportList shouldHaveSize 1
        }
    })
