package io.github.taetae98coding.diary.feature.core.list

import app.cash.turbine.test
import io.github.taetae98coding.diary.domain.memo.usecase.DeleteMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FinishMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestartMemoUseCase
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

class ListItemActionsTest : FunSpec() {
    init {
        test("완료·다시 시작·삭제에 성공하면 지정한 Effect를 보내고 복원은 보내지 않는다") {
            runTest {
                val id = Arb.kotlinUuid().next()
                val useCases = UseCases()
                useCases.succeedAll()
                val actions = actions(useCases = useCases)

                actions.effect.test {
                    actions.finish(id = id)
                    awaitItem() shouldBe TestEffect.Finished(id = id)

                    actions.restart(id = id)
                    awaitItem() shouldBe TestEffect.Restarted(id = id)

                    actions.delete(id = id)
                    awaitItem() shouldBe TestEffect.Deleted(id = id)

                    actions.restore(id = id)
                    expectNoEvents()
                }

                coVerify(exactly = 1) { useCases.restore(parameter = id) }
            }
        }

        test("Effect를 지정하지 않은 동작은 성공해도 Effect를 보내지 않는다") {
            runTest {
                val id = Arb.kotlinUuid().next()
                val useCases = UseCases()
                useCases.succeedAll()
                val actions =
                    ListItemActions<TestEffect>(
                        finishUseCase = useCases.finish,
                        restartUseCase = useCases.restart,
                        deleteUseCase = useCases.delete,
                        restoreUseCase = useCases.restore,
                    )

                actions.effect.test {
                    actions.finish(id = id)
                    actions.restart(id = id)
                    actions.delete(id = id)

                    expectNoEvents()
                }

                coVerify(exactly = 1) { useCases.finish(parameter = id) }
                coVerify(exactly = 1) { useCases.restart(parameter = id) }
                coVerify(exactly = 1) { useCases.delete(parameter = id) }
            }
        }

        test("동작이 실패하면 Effect를 보내지 않는다") {
            runTest {
                val id = Arb.kotlinUuid().next()
                val useCases = UseCases()
                coEvery { useCases.finish(parameter = any()) } returns Result.failure(IllegalStateException())
                val actions = actions(useCases = useCases)

                actions.effect.test {
                    actions.finish(id = id)

                    expectNoEvents()
                }
            }
        }

        test("같은 항목의 같은 동작이 진행 중이면 다시 실행하지 않고 끝나면 다시 받는다") {
            runTest {
                val id = Arb.kotlinUuid().next()
                val useCases = UseCases()
                val pending = CompletableDeferred<Result<Int>>()
                coEvery { useCases.finish(parameter = id) } coAnswers { pending.await() }
                val actions = actions(useCases = useCases)

                val first = launch { actions.finish(id = id) }
                runCurrent()
                actions.finish(id = id)
                coVerify(exactly = 1) { useCases.finish(parameter = id) }

                pending.complete(Result.success(1))
                first.join()
                coEvery { useCases.finish(parameter = id) } returns Result.success(1)
                actions.finish(id = id)

                coVerify(exactly = 2) { useCases.finish(parameter = id) }
            }
        }

        test("같은 항목이라도 다른 동작이나 다른 항목은 진행 중인 동작과 함께 실행한다") {
            runTest {
                val id = Arb.kotlinUuid().next()
                val otherId = Arb.kotlinUuid().next()
                val useCases = UseCases()
                val pending = CompletableDeferred<Result<Int>>()
                coEvery { useCases.finish(parameter = any()) } coAnswers { pending.await() }
                coEvery { useCases.delete(parameter = any()) } coAnswers { pending.await() }
                val actions = actions(useCases = useCases)

                launch { actions.finish(id = id) }
                launch { actions.delete(id = id) }
                launch { actions.finish(id = otherId) }
                runCurrent()

                coVerify(exactly = 1) { useCases.finish(parameter = id) }
                coVerify(exactly = 1) { useCases.delete(parameter = id) }
                coVerify(exactly = 1) { useCases.finish(parameter = otherId) }

                pending.complete(Result.success(1))
                runCurrent()
            }
        }
    }

    private sealed interface TestEffect {
        data class Finished(
            val id: Uuid,
        ) : TestEffect

        data class Restarted(
            val id: Uuid,
        ) : TestEffect

        data class Deleted(
            val id: Uuid,
        ) : TestEffect
    }

    private class UseCases {
        val finish = mockk<FinishMemoUseCase>()
        val restart = mockk<RestartMemoUseCase>()
        val delete = mockk<DeleteMemoUseCase>()
        val restore = mockk<RestoreMemoUseCase>()

        fun succeedAll() {
            coEvery { finish(parameter = any()) } returns Result.success(1)
            coEvery { restart(parameter = any()) } returns Result.success(1)
            coEvery { delete(parameter = any()) } returns Result.success(1)
            coEvery { restore(parameter = any()) } returns Result.success(1)
        }
    }

    private fun actions(useCases: UseCases): ListItemActions<TestEffect> =
        ListItemActions(
            finishUseCase = useCases.finish,
            restartUseCase = useCases.restart,
            deleteUseCase = useCases.delete,
            restoreUseCase = useCases.restore,
            finishedEffect = TestEffect::Finished,
            restartedEffect = TestEffect::Restarted,
            deletedEffect = TestEffect::Deleted,
        )
}
