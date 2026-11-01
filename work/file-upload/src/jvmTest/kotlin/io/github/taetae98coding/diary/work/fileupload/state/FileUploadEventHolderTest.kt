package io.github.taetae98coding.diary.work.fileupload.state

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.file.FileUploadEvent
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class FileUploadEventHolderTest : FunSpec() {
    init {
        test("두 화면이 함께 결과를 받는 동안 끝난 결과는 두 화면 모두에 한 번씩 전달한다") {
            listOf(
                FileUploadEvent.Succeeded(fileId = fixtureMonkey.giveMeOne<Uuid>()),
                FileUploadEvent.TooLarge,
                FileUploadEvent.Failed,
            ).forEach { event ->
                runTest {
                    val holder = FileUploadEventHolder()

                    holder.event.test {
                        val first = this
                        holder.event.test {
                            holder.send(event = event)

                            awaitItem() shouldBe event
                            expectNoEvents()
                        }
                        first.awaitItem() shouldBe event
                        first.expectNoEvents()
                    }
                }
            }
        }

        test("결과를 받던 화면이 받기를 멈춘 뒤 끝난 결과는 다시 받기 시작해도 전달하지 않는다") {
            runTest {
                val holder = FileUploadEventHolder()
                holder.event.test { expectNoEvents() }

                holder.send(event = FileUploadEvent.Failed)

                holder.event.test { expectNoEvents() }
            }
        }

        test("한 화면이 받기를 멈춰도 남은 화면은 계속 결과를 받는다") {
            runTest {
                val holder = FileUploadEventHolder()

                holder.event.test {
                    holder.event.test { cancelAndIgnoreRemainingEvents() }

                    holder.send(event = FileUploadEvent.Failed)

                    awaitItem() shouldBe FileUploadEvent.Failed
                    expectNoEvents()
                }
            }
        }
    }
}
