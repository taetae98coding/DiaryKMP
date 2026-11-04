package io.github.taetae98coding.diary.core.supabase.impl

import io.github.jan.supabase.SupabaseClient
import io.github.taetae98coding.diary.core.supabase.impl.di.SupabaseHttpClientEngine
import io.github.taetae98coding.diary.logger.core.DiaryLog
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.github.taetae98coding.diary.logger.core.DiaryLoggerDelegate
import io.github.taetae98coding.diary.logger.crashlytics.api.CrashlyticsLog
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.Headers
import io.ktor.http.HttpStatusCode
import io.ktor.util.reflect.typeInfo
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.json.JsonObject
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.plugin.module.dsl.koinApplication

class SupabaseRemoteResponseFailureReportTest :
    FunSpec({
        test("TC-a03f08d5-1c4f-4bf2-bcf0-c166ec3f0baa Diary 서버 요청이 500 응답을 받으면 오류 보고가 한 번 남는다") {
            val reportList = mutableListOf<CrashlyticsLog>()
            val delegate = mockk<DiaryLoggerDelegate>()
            every { delegate.log(log = any()) } answers { (firstArg<DiaryLog>() as? CrashlyticsLog)?.let(reportList::add) }
            DiaryLogger.add(delegate = delegate)
            val client = createSupabaseClient(engine = MockEngine { respond(content = "{}", status = HttpStatusCode.InternalServerError) })

            runCatching {
                SupabaseFunctionImpl(client = client)(
                    function = FUNCTION,
                    body = JsonObject(emptyMap()),
                    typeInfo = typeInfo<JsonObject>(),
                    headers = Headers.Empty,
                    requestTimeout = null,
                )
            }

            reportList.single().message shouldContain "/functions/v1/$FUNCTION 500"
            client.close()
        }
    }) {
    private companion object {
        private const val FUNCTION = "v1-report-test"

        private fun createSupabaseClient(engine: HttpClientEngine): SupabaseClient =
            koinApplication<SupabaseTestKoinApplication> {
                modules(
                    module {
                        single<HttpClientEngine>(qualifier = named<SupabaseHttpClientEngine>()) { engine }
                    },
                )
            }.koin.get()
    }
}
