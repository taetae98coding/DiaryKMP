package io.github.taetae98coding.diary.core.web.network.impl

import io.github.taetae98coding.diary.logger.core.DiaryLog
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.github.taetae98coding.diary.logger.core.DiaryLoggerDelegate
import io.github.taetae98coding.diary.logger.crashlytics.api.CrashlyticsLog
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.mockk.every
import io.mockk.mockk

class RemoteResponseFailureReportTest :
    FunSpec({
        test("TC-39709ef2-65e5-4345-92be-878e6c8f587c 웹 링크의 페이지 정보를 가져오는 요청은 대상이 아니다") {
            listOf(HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden, HttpStatusCode.InternalServerError).forEach { status ->
                val reportList = mutableListOf<CrashlyticsLog>()
                val delegate = mockk<DiaryLoggerDelegate>()
                every { delegate.log(log = any()) } answers { (firstArg<DiaryLog>() as? CrashlyticsLog)?.let(reportList::add) }
                DiaryLogger.add(delegate = delegate)
                val client = createWebPageHttpClient(engine = MockEngine { respond(content = "", status = status) })

                client.get("https://example.test/page")

                reportList.shouldBeEmpty()
            }
        }
    })
