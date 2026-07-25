package io.github.taetae98coding.diary.core.supabase.impl

import io.github.jan.supabase.SupabaseClient
import io.github.taetae98coding.diary.core.supabase.impl.di.SupabaseHttpClientEngine
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.plugin.module.dsl.koinApplication

class SupabaseModuleTest :
    FunSpec({
        test("주입된 HttpClientEngine으로 SupabaseClient를 생성한다") {
            val engine = MockEngine { respondOk() }
            val client = createSupabaseClient(engine)

            client.config.networkConfig.httpEngine shouldBe engine

            client.close()
        }
    }) {
    companion object {
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
