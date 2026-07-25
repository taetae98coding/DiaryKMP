package io.github.taetae98coding.diary.core.ipnetwork.impl.datasource

import io.github.taetae98coding.diary.core.ipnetwork.api.datasource.IpRemoteDataSource
import io.github.taetae98coding.diary.core.ipnetwork.api.entity.IpRemoteEntity
import io.github.taetae98coding.diary.core.ipnetwork.impl.IpNetworkTestKoinApplication
import io.github.taetae98coding.diary.core.ipnetwork.impl.di.IpHttpClientEngine
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.plugin.module.dsl.koinApplication

class IpRemoteDataSourceImplTest :
    FunSpec({
        test("TC-CURRENT-LOCATION-DATA-002 ip-api 응답 JSON의 위경도를 그대로 제공한다") {
            val engine = createMockEngine()
            val dataSource = createDataSource(engine)

            val actual = dataSource.get()

            actual shouldBe
                IpRemoteEntity(
                    latitude = 99.9999,
                    longitude = 999.999,
                )
        }

        test("TC-CURRENT-LOCATION-DATA-001 위경도 fields 파라미터를 포함한 URL로 요청한다") {
            val engine = createMockEngine()
            val dataSource = createDataSource(engine)

            dataSource.get()

            engine.requestHistory
                .single()
                .url
                .toString() shouldBe "http://ip-api.com/json?fields=192"
        }
    }) {
    public companion object {
        private fun createMockEngine(): MockEngine =
            MockEngine {
                respond(
                    content = readResource("ip-api-response.json"),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            }

        private fun createDataSource(engine: HttpClientEngine): IpRemoteDataSource =
            koinApplication<IpNetworkTestKoinApplication> {
                modules(
                    module {
                        single<HttpClientEngine>(qualifier = named<IpHttpClientEngine>()) { engine }
                    },
                )
            }.koin.get()

        private fun readResource(name: String): String = checkNotNull(IpRemoteDataSourceImplTest::class.java.classLoader.getResource(name)).readText()
    }
}
