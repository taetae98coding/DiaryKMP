package io.github.taetae98coding.diary.work.mcp.server

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next

class McpServerAddressTest :
    FunSpec({
        test("TC-b7bae2a8-ca68-4e09-9edf-04843f1f51d6 받은 포트로 이 Mac 안에서만 접속하는 연결 주소를 만든다") {
            val port = Arb.int(1..65_535).next()

            mcpServerAddress(port = port) shouldBe "http://127.0.0.1:$port/mcp"
        }
    })
