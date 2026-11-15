package io.github.taetae98coding.diary.data.routine.repository

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.database.api.routinefilter.datasource.AccountRoutineFilterLocalDataSource
import io.github.taetae98coding.diary.core.database.api.routinefilter.entity.RoutineViewScopeLocalEntity
import io.github.taetae98coding.diary.core.model.routine.RoutineViewScope
import io.github.taetae98coding.diary.core.testing.account.accountUser
import io.github.taetae98coding.diary.data.routine.mapper.toDomain
import io.github.taetae98coding.diary.data.routine.mapper.toLocal
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

private class RoutineFilterRepositoryTestException : RuntimeException()

class AccountRoutineFilterRepositoryImplTest :
    FunSpec({
        test("TC-829e32df-b32a-4101-812c-58ee288ec037 보기 범위를 한 번도 바꾸지 않은 계정의 보기 범위는 오늘 할 루틴이다") {
            val account = Arb.accountUser().next()
            val dataSource = mockk<AccountRoutineFilterLocalDataSource>()
            every { dataSource.findViewScope(accountId = account.id) } returns flowOf(null)
            val repository = AccountRoutineFilterRepositoryImpl(accountRoutineFilterLocalDataSource = dataSource)

            repository.getViewScope(account = account).test {
                awaitItem() shouldBe RoutineViewScope.TODAY
                awaitComplete()
            }
        }

        test("TC-13d78fca-59cd-4698-96fc-b8c0ee88b159 계정마다 저장된 보기 범위를 전달하고 저장된 선택이 없으면 오늘 할 루틴을 전달한다") {
            val first = Arb.accountUser().next()
            val second = Arb.accountUser().next()
            val third = Arb.accountUser().next()
            val dataSource = mockk<AccountRoutineFilterLocalDataSource>()
            every { dataSource.findViewScope(accountId = first.id) } returns flowOf(RoutineViewScopeLocalEntity.ALL)
            every { dataSource.findViewScope(accountId = second.id) } returns flowOf(RoutineViewScopeLocalEntity.TODAY)
            every { dataSource.findViewScope(accountId = third.id) } returns flowOf(null)
            val repository = AccountRoutineFilterRepositoryImpl(accountRoutineFilterLocalDataSource = dataSource)

            listOf(
                second to RoutineViewScope.TODAY,
                first to RoutineViewScope.ALL,
                third to RoutineViewScope.TODAY,
                first to RoutineViewScope.ALL,
            ).forEach { (account, expected) ->
                repository.getViewScope(account = account).test {
                    awaitItem() shouldBe expected
                    awaitComplete()
                }
            }
        }

        test("TC-2298fe03-2886-410b-95bb-6ce7241ef79b 저장된 보기 범위 선택이 지워지면 오늘 할 루틴으로 돌아간다") {
            val account = Arb.accountUser().next()
            val localFlow = MutableStateFlow<RoutineViewScopeLocalEntity?>(RoutineViewScopeLocalEntity.ALL)
            val dataSource = mockk<AccountRoutineFilterLocalDataSource>()
            every { dataSource.findViewScope(accountId = account.id) } returns localFlow
            val repository = AccountRoutineFilterRepositoryImpl(accountRoutineFilterLocalDataSource = dataSource)

            repository.getViewScope(account = account).test {
                awaitItem() shouldBe RoutineViewScope.ALL

                localFlow.value = null

                awaitItem() shouldBe RoutineViewScope.TODAY
                cancelAndIgnoreRemainingEvents()
            }
        }

        test("TC-80220c94-0abb-4324-ba26-4297993a422d 고른 보기 범위를 현재 계정의 선택으로 기기에 저장한다") {
            RoutineViewScope.entries.forEach { viewScope ->
                val account = Arb.accountUser().next()
                val dataSource = mockk<AccountRoutineFilterLocalDataSource>()
                coEvery { dataSource.upsertViewScope(accountId = any(), viewScope = any()) } returns Unit
                val repository = AccountRoutineFilterRepositoryImpl(accountRoutineFilterLocalDataSource = dataSource)

                repository.upsertViewScope(account = account, viewScope = viewScope)

                coVerify(exactly = 1) { dataSource.upsertViewScope(accountId = account.id, viewScope = viewScope.toLocal()) }
            }
        }

        test("TC-ab4d2410-0156-42b1-b2f5-e77016b9d15e 보기 범위 저장의 실패를 삼키지 않고 그대로 전달한다") {
            val dataSource = mockk<AccountRoutineFilterLocalDataSource>()
            coEvery { dataSource.upsertViewScope(accountId = any(), viewScope = any()) } throws RoutineFilterRepositoryTestException()
            val repository = AccountRoutineFilterRepositoryImpl(accountRoutineFilterLocalDataSource = dataSource)

            shouldThrowExactly<RoutineFilterRepositoryTestException> { repository.upsertViewScope(account = Arb.accountUser().next(), viewScope = RoutineViewScope.ALL) }
        }

        test("TC-d86bcda0-2d05-4e0b-b4d8-57ad3926daa0 태그 선택 전체 해제는 태그 선택만 지우고 보기 범위 저장을 요청하지 않는다") {
            val account = Arb.accountUser().next()
            val dataSource = mockk<AccountRoutineFilterLocalDataSource>()
            coEvery { dataSource.deleteAll(accountId = any()) } returns Unit
            val repository = AccountRoutineFilterRepositoryImpl(accountRoutineFilterLocalDataSource = dataSource)

            repository.deleteAll(account = account)

            coVerify(exactly = 1) { dataSource.deleteAll(accountId = account.id) }
            coVerify(exactly = 0) { dataSource.upsertViewScope(accountId = any(), viewScope = any()) }
        }

        test("태그 선택과 해제는 현재 계정의 그 태그로 기기에 반영한다") {
            val account = Arb.accountUser().next()
            val tagId = Arb.kotlinUuid().next()
            val dataSource = mockk<AccountRoutineFilterLocalDataSource>()
            coEvery { dataSource.upsert(accountId = any(), tagId = any()) } returns Unit
            coEvery { dataSource.delete(accountId = any(), tagId = any()) } returns Unit
            val repository = AccountRoutineFilterRepositoryImpl(accountRoutineFilterLocalDataSource = dataSource)

            repository.upsert(account = account, tagId = tagId)
            repository.delete(account = account, tagId = tagId)

            coVerify(exactly = 1) { dataSource.upsert(accountId = account.id, tagId = tagId) }
            coVerify(exactly = 1) { dataSource.delete(accountId = account.id, tagId = tagId) }
        }

        test("선택한 태그의 식별값은 중복 없이 전달한다") {
            val account = Arb.accountUser().next()
            val tagId = Arb.kotlinUuid().next()
            val otherTagId = Arb.kotlinUuid().next()
            val dataSource = mockk<AccountRoutineFilterLocalDataSource>()
            every { dataSource.getTagIdList(accountId = account.id) } returns flowOf(listOf(tagId, otherTagId, tagId))
            val repository = AccountRoutineFilterRepositoryImpl(accountRoutineFilterLocalDataSource = dataSource)

            repository.getTagIdSet(account = account).test {
                awaitItem() shouldBe setOf(tagId, otherTagId)
                awaitComplete()
            }
        }

        test("보기 범위는 같은 의미의 로컬 값과 짝을 이룬다") {
            mapOf(
                RoutineViewScope.TODAY to RoutineViewScopeLocalEntity.TODAY,
                RoutineViewScope.ALL to RoutineViewScopeLocalEntity.ALL,
            ).forEach { (viewScope, local) ->
                viewScope.toLocal() shouldBe local
                local.toDomain() shouldBe viewScope
            }
        }
    })
