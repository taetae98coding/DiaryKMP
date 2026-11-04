@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.data.core.cache

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

class FetchedKeySetTest :
    FunSpec({
        test("기록되지 않은 키는 받아 온 결과를 돌려준다") {
            val fetched = Arb.string().next()

            TestFetchedKeySet().fetchOnce(key = Arb.int().next(), cached = { error("cached") }, fetch = { fetched }) shouldBe fetched
        }

        test("받아 온 키는 다시 받지 않고 기억해 둔 결과를 돌려준다") {
            val key = Arb.int().next()
            val cached = Arb.string().next()
            val keySet = TestFetchedKeySet()
            keySet.fetchOnce(key = key, cached = { error("cached") }, fetch = { Arb.string().next() })

            keySet.fetchOnce(key = key, cached = { cached }, fetch = { error("fetch") }) shouldBe cached
        }

        test("한 키를 받아 와도 다른 키는 받아 오지 않은 것이다") {
            val key = Arb.int().next()
            val otherKey = generateSequence { Arb.int().next() }.first { candidate -> candidate != key }
            val fetched = Arb.string().next()
            val keySet = TestFetchedKeySet()
            keySet.fetchOnce(key = key, cached = { error("cached") }, fetch = { Arb.string().next() })

            keySet.fetchOnce(key = otherKey, cached = { error("cached") }, fetch = { fetched }) shouldBe fetched
        }

        test("받아 오기에 실패한 키는 기록하지 않아 다음 요청에서 다시 받는다") {
            val key = Arb.int().next()
            val fetched = Arb.string().next()
            val keySet = TestFetchedKeySet()
            shouldThrowExactly<IllegalStateException> {
                keySet.fetchOnce(key = key, cached = { error("cached") }, fetch = { throw IllegalStateException() })
            }

            keySet.fetchOnce(key = key, cached = { error("cached") }, fetch = { fetched }) shouldBe fetched
        }

        test("새로 만든 기록은 이전 기록을 이어받지 않는다") {
            val key = Arb.int().next()
            val fetched = Arb.string().next()
            TestFetchedKeySet().fetchOnce(key = key, cached = { error("cached") }, fetch = { Arb.string().next() })

            TestFetchedKeySet().fetchOnce(key = key, cached = { error("cached") }, fetch = { fetched }) shouldBe fetched
        }

        test("같은 키를 받는 중에 다시 요청하면 끝나기를 기다렸다가 기억해 둔 결과를 돌려준다") {
            runTest {
                val key = Arb.int().next()
                val cached = Arb.string().next()
                val response = CompletableDeferred<String>()
                var fetchCount = 0
                val keySet = TestFetchedKeySet()

                val first =
                    async {
                        keySet.fetchOnce(key = key, cached = { error("cached") }, fetch = {
                            fetchCount++
                            response.await()
                        })
                    }
                runCurrent()
                val second =
                    async {
                        keySet.fetchOnce(key = key, cached = { cached }, fetch = {
                            fetchCount++
                            response.await()
                        })
                    }
                runCurrent()

                second.isCompleted shouldBe false

                response.complete(Arb.string().next())
                first.await()

                second.await() shouldBe cached
                fetchCount shouldBe 1
            }
        }

        test("같은 키를 받는 중에 다시 요청하고 앞선 요청이 실패하면 이어서 다시 받는다") {
            runTest {
                val key = Arb.int().next()
                val fetched = Arb.string().next()
                val response = CompletableDeferred<String>()
                val keySet = TestFetchedKeySet()

                val first = async { runCatching { keySet.fetchOnce(key = key, cached = { error("cached") }, fetch = { response.await() }) } }
                runCurrent()
                val second = async { keySet.fetchOnce(key = key, cached = { error("cached") }, fetch = { fetched }) }
                runCurrent()

                response.completeExceptionally(IllegalStateException())

                first.await().shouldBeFailure<IllegalStateException>()
                second.await() shouldBe fetched
            }
        }

        test("다른 키를 받는 중이어도 기다리지 않고 받는다") {
            runTest {
                val key = Arb.int().next()
                val otherKey = generateSequence { Arb.int().next() }.first { candidate -> candidate != key }
                val fetched = Arb.string().next()
                val response = CompletableDeferred<String>()
                val keySet = TestFetchedKeySet()

                val first = async { keySet.fetchOnce(key = key, cached = { error("cached") }, fetch = { response.await() }) }
                runCurrent()

                keySet.fetchOnce(key = otherKey, cached = { error("cached") }, fetch = { fetched }) shouldBe fetched
                first.isCompleted shouldBe false

                response.complete(Arb.string().next())
                first.await()
            }
        }
    })

private class TestFetchedKeySet : FetchedKeySet<Int>()
