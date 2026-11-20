package io.github.taetae98coding.diary.feature.core.list

import app.cash.turbine.test
import io.github.taetae98coding.diary.domain.memo.usecase.DeleteMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestoreMemoUseCase
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.uuid.Uuid

class ListItemDeleteActionsTest : FunSpec() {
    init {
        test("삭제에 성공하면 지정한 Effect를 보내고 복원은 보내지 않는다") {
            runTest {
                val id = Arb.kotlinUuid().next()
                val useCases = UseCases()
                useCases.succeedAll()
                val actions = actions(useCases = useCases)

                actions.effect.test {
                    actions.delete(id = id)
                    awaitItem() shouldBe TestEffect.Deleted(id = id)

                    actions.restore(id = id)
                    expectNoEvents()
                }

                coVerify(exactly = 1) { useCases.delete(parameter = id) }
                coVerify(exactly = 1) { useCases.restore(parameter = id) }
            }
        }

        test("Effect를 지정하지 않으면 삭제에 성공해도 Effect를 보내지 않는다") {
            runTest {
                val id = Arb.kotlinUuid().next()
                val useCases = UseCases()
                useCases.succeedAll()
                val actions = ListItemDeleteActions<TestEffect>(deleteUseCase = useCases.delete, restoreUseCase = useCases.restore)

                actions.effect.test {
                    actions.delete(id = id)

                    expectNoEvents()
                }

                coVerify(exactly = 1) { useCases.delete(parameter = id) }
            }
        }

        test("삭제가 실패하면 Effect를 보내지 않는다") {
            runTest {
                val id = Arb.kotlinUuid().next()
                val useCases = UseCases()
                coEvery { useCases.delete(parameter = any()) } returns Result.failure(IllegalStateException())
                val actions = actions(useCases = useCases)

                actions.effect.test {
                    actions.delete(id = id)

                    expectNoEvents()
                }
            }
        }

        test("같은 항목의 같은 동작이 진행 중이면 다시 실행하지 않고 끝나면 다시 받는다") {
            runTest {
                val id = Arb.kotlinUuid().next()
                val useCases = UseCases()
                val pending = CompletableDeferred<Result<Int>>()
                coEvery { useCases.delete(parameter = id) } coAnswers { pending.await() }
                coEvery { useCases.restore(parameter = id) } coAnswers { pending.await() }
                val actions = actions(useCases = useCases)

                val firstDelete = launch { actions.delete(id = id) }
                val firstRestore = launch { actions.restore(id = id) }
                runCurrent()
                actions.delete(id = id)
                actions.restore(id = id)
                coVerify(exactly = 1) { useCases.delete(parameter = id) }
                coVerify(exactly = 1) { useCases.restore(parameter = id) }

                pending.complete(Result.success(1))
                firstDelete.join()
                firstRestore.join()
                actions.delete(id = id)
                actions.restore(id = id)

                coVerify(exactly = 2) { useCases.delete(parameter = id) }
                coVerify(exactly = 2) { useCases.restore(parameter = id) }
            }
        }

        test("같은 항목이라도 삭제와 복원, 다른 항목은 진행 중인 동작과 함께 실행한다") {
            runTest {
                val id = Arb.kotlinUuid().next()
                val otherId = Arb.kotlinUuid().next()
                val useCases = UseCases()
                val pending = CompletableDeferred<Result<Int>>()
                coEvery { useCases.delete(parameter = any()) } coAnswers { pending.await() }
                coEvery { useCases.restore(parameter = any()) } coAnswers { pending.await() }
                val actions = actions(useCases = useCases)

                launch { actions.delete(id = id) }
                launch { actions.restore(id = id) }
                launch { actions.delete(id = otherId) }
                runCurrent()

                coVerify(exactly = 1) { useCases.delete(parameter = id) }
                coVerify(exactly = 1) { useCases.restore(parameter = id) }
                coVerify(exactly = 1) { useCases.delete(parameter = otherId) }

                pending.complete(Result.success(1))
                runCurrent()
            }
        }
    }

    private sealed interface TestEffect {
        data class Deleted(
            val id: Uuid,
        ) : TestEffect
    }

    private class UseCases {
        val delete = mockk<DeleteMemoUseCase>()
        val restore = mockk<RestoreMemoUseCase>()

        fun succeedAll() {
            coEvery { delete(parameter = any()) } returns Result.success(1)
            coEvery { restore(parameter = any()) } returns Result.success(1)
        }
    }

    private fun actions(useCases: UseCases): ListItemDeleteActions<TestEffect> =
        ListItemDeleteActions(
            deleteUseCase = useCases.delete,
            restoreUseCase = useCases.restore,
            deletedEffect = TestEffect::Deleted,
        )
}
