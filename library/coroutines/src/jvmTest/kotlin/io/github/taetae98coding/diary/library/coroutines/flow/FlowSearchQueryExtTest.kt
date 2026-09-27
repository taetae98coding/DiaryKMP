package io.github.taetae98coding.diary.library.coroutines.flow

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
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

        test("화면이 검색어를 알려 주기 전에는 흘려보내지 않는다") {
            runTest {
                val queryList =
                    flow<String?> {
                        emit(null)
                        delay(INPUT_IDLE_DELAY * 2)
                    }.debounceReportedSearchQuery()
                        .toList()

                queryList shouldBe emptyList()
            }
        }

        test("처음 알려 준 검색어는 기다리지 않고 흘려보내고 그 뒤의 변경은 입력을 멈춘 뒤 흘려보낸다") {
            runTest {
                val timeList = mutableListOf<Long>()
                val queryList =
                    flow<String?> {
                        emit(null)
                        emit("Tra")
                        delay(INPUT_IDLE_DELAY * 2)
                        emit("Trav")
                        delay(INPUT_IDLE_DELAY / 2)
                        emit("Trave")
                        delay(INPUT_IDLE_DELAY * 2)
                    }.debounceReportedSearchQuery()
                        .onEach { timeList.add(testScheduler.currentTime) }
                        .toList()

                queryList shouldBe listOf("Tra", "Trave")
                timeList.first() shouldBe 0L
            }
        }

        test("처음 알려 준 뒤 비운 검색어는 기다리지 않고 흘려보낸다") {
            runTest {
                val queryList =
                    flow<String?> {
                        emit("Tra")
                        delay(INPUT_IDLE_DELAY * 2)
                        emit("Trav")
                        emit("")
                    }.debounceReportedSearchQuery()
                        .toList()

                queryList shouldBe listOf("Tra", "")
            }
        }
    })
