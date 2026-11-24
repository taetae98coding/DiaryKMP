package io.github.taetae98coding.diary.core.supabase.impl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk

class SupabaseModuleTest :
    FunSpec({
        test("주입된 HttpClientEngine으로 SupabaseClient를 생성한다") {
            val engine = MockEngine { respondOk() }
            val client = createTestSupabaseClient(engine)

            client.config.networkConfig.httpEngine shouldBe engine

            client.close()
        }
    })
