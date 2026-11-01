package io.github.taetae98coding.diary.library.coroutines.flow

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest

class FlowSearchQueryExtTest :
    FunSpec({
        test("입력을 멈춘 뒤의 검색어만 흘려보낸다") {
            runTest {
                val queryList =
                    flow {
                        emit("T")
                        delay(INPUT_IDLE_DELAY / 2)
                        emit("Tr")
                        delay(INPUT_IDLE_DELAY * 2)
                        emit("Tra")
                        delay(INPUT_IDLE_DELAY * 2)
                    }.debounceSearchQuery()
                        .toList()

                queryList shouldBe listOf("Tr", "Tra")
            }
        }

        test("빈 검색어는 기다리지 않고 흘려보낸다") {
            runTest {
                val queryList =
                    flow {
                        emit("Tra")
                        emit("")
                    }.debounceSearchQuery()
                        .toList()

                queryList shouldBe listOf("")
            }
        }

        test("공백만 있는 검색어도 기다리지 않고 흘려보낸다") {
            runTest {
                val queryList =
                    flow {
                        emit("Tra")
                        emit("   ")
                    }.debounceSearchQuery()
                        .toList()

                queryList shouldBe listOf("   ")
            }
        }
    })
