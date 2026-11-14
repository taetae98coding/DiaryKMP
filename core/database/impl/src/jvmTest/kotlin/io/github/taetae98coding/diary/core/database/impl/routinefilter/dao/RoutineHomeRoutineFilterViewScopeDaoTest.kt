package io.github.taetae98coding.diary.core.database.impl.routinefilter.dao

import androidx.room3.useReaderConnection
import app.cash.turbine.test
import io.github.taetae98coding.diary.core.database.api.routinefilter.entity.RoutineViewScopeLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.routine.insertRoutineTestTag
import io.github.taetae98coding.diary.core.database.impl.routine.routineTestDatabase
import io.github.taetae98coding.diary.core.database.impl.routinefilter.datasource.AccountRoutineFilterLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.AccountDataTransactionImpl
import io.github.taetae98coding.diary.core.testing.tag.localTag
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlin.uuid.Uuid

class RoutineHomeRoutineFilterViewScopeDaoTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var dataSource: AccountRoutineFilterLocalDataSourceImpl

        beforeTest {
            database = routineTestDatabase()
            dataSource = AccountRoutineFilterLocalDataSourceImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun viewScope(accountId: Uuid): RoutineViewScopeLocalEntity? = dataSource.findViewScope(accountId = accountId).first()

        test("보기 범위를 한 번도 저장하지 않은 계정은 저장된 선택이 없다") {
            viewScope(Arb.kotlinUuid().next()).shouldBeNull()
        }

        test("TC-80220c94-0abb-4324-ba26-4297993a422d 보기 범위 선택을 현재 계정의 선택 하나로 즉시 저장한다") {
            val accountId = Arb.kotlinUuid().next()
            dataSource.upsertViewScope(accountId = accountId, viewScope = RoutineViewScopeLocalEntity.TODAY)

            dataSource.upsertViewScope(accountId = accountId, viewScope = RoutineViewScopeLocalEntity.ALL)

            viewScope(accountId) shouldBe RoutineViewScopeLocalEntity.ALL
            database.routineFilterRowCount(accountId = accountId) shouldBe 1L
        }

        test("TC-80220c94-0abb-4324-ba26-4297993a422d 보기 범위 선택은 서버 동기화 대상으로 기록되지 않는다") {
            val accountId = Arb.kotlinUuid().next()

            dataSource.upsertViewScope(accountId = accountId, viewScope = RoutineViewScopeLocalEntity.ALL)

            database.accountRoutineSyncDao().findPending(accountId = accountId).shouldBeEmpty()
            database.accountTagSyncDao().findPending(accountId = accountId).shouldBeEmpty()
            database.accountRoutineTagSyncDao().findPending(accountId = accountId).shouldBeEmpty()
            database.accountRoutineRecordSyncDao().findPending(accountId = accountId).shouldBeEmpty()
        }

        test("저장된 보기 범위가 바뀌면 관찰 중인 선택이 이어서 전달된다") {
            val accountId = Arb.kotlinUuid().next()

            dataSource.findViewScope(accountId = accountId).distinctUntilChanged().test {
                awaitItem().shouldBeNull()

                dataSource.upsertViewScope(accountId = accountId, viewScope = RoutineViewScopeLocalEntity.ALL)
                awaitItem() shouldBe RoutineViewScopeLocalEntity.ALL

                dataSource.upsertViewScope(accountId = accountId, viewScope = RoutineViewScopeLocalEntity.TODAY)
                awaitItem() shouldBe RoutineViewScopeLocalEntity.TODAY

                cancelAndIgnoreRemainingEvents()
            }
        }

        test("TC-13d78fca-59cd-4698-96fc-b8c0ee88b159 계정마다 서로 다른 보기 범위 선택을 유지한다") {
            val first = Arb.kotlinUuid().next()
            val second = Arb.kotlinUuid().next()
            val third = Arb.kotlinUuid().next()
            dataSource.upsertViewScope(accountId = first, viewScope = RoutineViewScopeLocalEntity.ALL)
            dataSource.upsertViewScope(accountId = second, viewScope = RoutineViewScopeLocalEntity.TODAY)

            // 계정이 바뀌어도, 로그아웃 뒤 같은 계정으로 다시 로그인해도 계정별 선택은 그대로 조회된다.
            listOf(second, first, third, first).map { accountId -> viewScope(accountId) } shouldBe
                listOf(RoutineViewScopeLocalEntity.TODAY, RoutineViewScopeLocalEntity.ALL, null, RoutineViewScopeLocalEntity.ALL)
        }

        RoutineViewScopeLocalEntity.entries.forEach { scope ->
            test("TC-d86bcda0-2d05-4e0b-b4d8-57ad3926daa0 태그 선택 전체 해제는 보기 범위 $scope 선택을 바꾸지 않는다") {
                val accountId = Arb.kotlinUuid().next()
                val tag = Arb.localTag(isFinished = false, isDeleted = false).next()
                database.insertRoutineTestTag(accountId, tag)
                dataSource.upsert(accountId = accountId, tagId = tag.id)
                dataSource.upsertViewScope(accountId = accountId, viewScope = scope)

                dataSource.deleteAll(accountId = accountId)

                dataSource.getTagIdList(accountId = accountId).first().shouldBeEmpty()
                viewScope(accountId) shouldBe scope
            }
        }

        test("태그를 선택하거나 해제해도 보기 범위 선택은 그대로다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = Arb.localTag(isFinished = false, isDeleted = false).next()
            database.insertRoutineTestTag(accountId, tag)
            dataSource.upsertViewScope(accountId = accountId, viewScope = RoutineViewScopeLocalEntity.ALL)

            dataSource.upsert(accountId = accountId, tagId = tag.id)
            viewScope(accountId) shouldBe RoutineViewScopeLocalEntity.ALL

            dataSource.delete(accountId = accountId, tagId = tag.id)
            viewScope(accountId) shouldBe RoutineViewScopeLocalEntity.ALL
        }

        test("보기 범위를 바꿔도 선택한 태그는 그대로다") {
            val accountId = Arb.kotlinUuid().next()
            val tag = Arb.localTag(isFinished = false, isDeleted = false).next()
            database.insertRoutineTestTag(accountId, tag)
            dataSource.upsert(accountId = accountId, tagId = tag.id)

            dataSource.upsertViewScope(accountId = accountId, viewScope = RoutineViewScopeLocalEntity.ALL)

            dataSource.getTagIdList(accountId = accountId).first() shouldBe listOf(tag.id)
        }

        test("TC-2298fe03-2886-410b-95bb-6ce7241ef79b 강제 전체 재동기화로 계정 데이터를 지우면 그 계정의 보기 범위 선택이 지워진다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            dataSource.upsertViewScope(accountId = accountId, viewScope = RoutineViewScopeLocalEntity.ALL)
            dataSource.upsertViewScope(accountId = otherAccountId, viewScope = RoutineViewScopeLocalEntity.ALL)

            AccountDataTransactionImpl(database = database).delete(accountId = accountId)

            viewScope(accountId).shouldBeNull()
            viewScope(otherAccountId) shouldBe RoutineViewScopeLocalEntity.ALL
        }
    })

private suspend fun DiaryDatabase.routineFilterRowCount(accountId: Uuid): Long =
    useReaderConnection { transactor ->
        transactor.usePrepared("SELECT COUNT(*) FROM routine_filter WHERE account_id = ?") { statement ->
            statement.bindText(1, accountId.toString())
            statement.step()
            statement.getLong(0)
        }
    }
