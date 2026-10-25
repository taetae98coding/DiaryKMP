package io.github.taetae98coding.diary.data.qr.repository

import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import androidx.paging.PagingSource
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.qr.datasource.AccountQrLocalDataSource
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import io.github.taetae98coding.diary.core.database.api.qr.transaction.AccountQrTransaction
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.testing.qr.localQr
import io.github.taetae98coding.diary.core.testing.qr.qr
import io.github.taetae98coding.diary.core.testing.qr.qrDetail
import io.github.taetae98coding.diary.data.qr.mapper.toDomain
import io.github.taetae98coding.diary.data.qr.mapper.toLocal
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountQrRepositoryImplTest :
    FunSpec({
        test("TC-7221dee8-4896-4a4d-a339-46fd8892bcce QR을 현재 계정 식별자와 로컬 모델로 변환해 로컬 저장소에 저장한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val qr = fixtureMonkey.qr(isDeleted = false)
            val localDataSource = mockk<AccountQrLocalDataSource>()
            val transaction = mockk<AccountQrTransaction>()
            coEvery { transaction.upsert(accountId = account.id, qrList = listOf(qr.toLocal())) } just Runs
            val repository = AccountQrRepositoryImpl(accountQrLocalDataSource = localDataSource, accountQrTransaction = transaction)

            repository.upsert(account = account, qr = qr)

            coVerify(exactly = 1) {
                transaction.upsert(accountId = account.id, qrList = listOf(qr.toLocal()))
            }
        }

        test("TC-7b950adf-1ab9-4742-9b91-33003d0955b6 기기 저장이 실패하면 추가를 성공으로 다루지 않고 실패를 전달한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val qr = fixtureMonkey.qr(isDeleted = false)
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val localDataSource = mockk<AccountQrLocalDataSource>()
            val transaction = mockk<AccountQrTransaction>()
            coEvery { transaction.upsert(accountId = account.id, qrList = listOf(qr.toLocal())) } throws throwable
            val repository = AccountQrRepositoryImpl(accountQrLocalDataSource = localDataSource, accountQrTransaction = transaction)

            shouldThrow<IllegalStateException> {
                repository.upsert(account = account, qr = qr)
            } shouldBeSameInstanceAs throwable
        }

        test("TC-7dd1be57-2cd6-4d1b-9763-d9f703a9023f QR 목록 페이지 조회는 현재 계정의 로컬 QR을 도메인 모델로 변환해 전달한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val localQrList = List(2) { fixtureMonkey.localQr(isDeleted = false) }
            val localDataSource = mockk<AccountQrLocalDataSource>()
            val transaction = mockk<AccountQrTransaction>()
            every { localDataSource.page(accountId = account.id) } returns pagingSource(localQrList)
            val repository = AccountQrRepositoryImpl(accountQrLocalDataSource = localDataSource, accountQrTransaction = transaction)

            repository.page(account = account).first().items() shouldBe localQrList.map { local -> local.toDomain() }
        }

        test("TC-4734c4ca-1393-4615-9e81-d80193fa4a00 QR 찾기는 현재 계정의 로컬 QR을 도메인 모델로 변환해 전달한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val localQr = fixtureMonkey.localQr(isDeleted = true)
            val localDataSource = mockk<AccountQrLocalDataSource>()
            every { localDataSource.find(accountId = account.id, qrId = localQr.id) } returns flowOf(localQr)
            val repository = AccountQrRepositoryImpl(accountQrLocalDataSource = localDataSource, accountQrTransaction = mockk())

            repository.find(account = account, qrId = localQr.id).first() shouldBe localQr.toDomain()
        }

        test("TC-7576dbc8-7db2-49f3-96e3-9f1c337fb63c QR 찾기는 기기에 저장된 QR만 조회하고 내려받아 저장하지 않는다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val localQr = fixtureMonkey.localQr(isDeleted = false)
            val localDataSource = mockk<AccountQrLocalDataSource>()
            val transaction = mockk<AccountQrTransaction>()
            every { localDataSource.find(accountId = account.id, qrId = localQr.id) } returns flowOf(localQr)
            val repository = AccountQrRepositoryImpl(accountQrLocalDataSource = localDataSource, accountQrTransaction = transaction)

            repository.find(account = account, qrId = localQr.id).first()

            coVerify(exactly = 0) { transaction.upsert(accountId = any(), qrList = any()) }
        }

        test("찾는 QR이 기기에 없으면 없음을 전달한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val id = fixtureMonkey.giveMeOne<Uuid>()
            val localDataSource = mockk<AccountQrLocalDataSource>()
            every { localDataSource.find(accountId = account.id, qrId = id) } returns flowOf(null)
            val repository = AccountQrRepositoryImpl(accountQrLocalDataSource = localDataSource, accountQrTransaction = mockk())

            repository.find(account = account, qrId = id).first().shouldBeNull()
        }

        test("TC-92975a11-7beb-4f64-bd53-24a2718ccf23 수정 요청은 내용과 수정 시각을 현재 계정 기준의 로컬 모델로 반영한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val id = fixtureMonkey.giveMeOne<Uuid>()
            val detail = fixtureMonkey.qrDetail()
            val updatedAt = fixtureMonkey.giveMeOne<Instant>()
            val transaction = mockk<AccountQrTransaction>()
            coEvery { transaction.updateDetail(accountId = account.id, qrId = id, detail = detail.toLocal(), updatedAt = updatedAt) } returns 1
            val repository = AccountQrRepositoryImpl(accountQrLocalDataSource = mockk(), accountQrTransaction = transaction)

            repository.updateDetail(account = account, qrId = id, detail = detail, updatedAt = updatedAt) shouldBe 1
        }

        listOf(true, false).forEach { isDeleted ->
            test("TC-9f59d662-8251-4eb6-85e1-8fd2875148cf 삭제 여부를 $isDeleted 로 바꾸는 요청은 삭제 여부와 수정 시각만 현재 계정 기준으로 반영한다") {
                val account = fixtureMonkey.giveMeOne<Account.User>()
                val qrId = fixtureMonkey.giveMeOne<Uuid>()
                val updatedAt = fixtureMonkey.giveMeOne<Instant>()
                val updatedCount = fixtureMonkey.giveMeOne<Int>()
                val localDataSource = mockk<AccountQrLocalDataSource>()
                val transaction = mockk<AccountQrTransaction>()
                coEvery {
                    transaction.updateDeleted(accountId = account.id, qrId = qrId, isDeleted = isDeleted, updatedAt = updatedAt)
                } returns updatedCount
                val repository = AccountQrRepositoryImpl(accountQrLocalDataSource = localDataSource, accountQrTransaction = transaction)

                repository.updateDeleted(account = account, qrId = qrId, isDeleted = isDeleted, updatedAt = updatedAt) shouldBe updatedCount

                coVerify(exactly = 0) { transaction.upsert(accountId = any(), qrList = any()) }
            }
        }

        test("삭제 여부 저장이 실패하면 실패를 그대로 전파한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val localDataSource = mockk<AccountQrLocalDataSource>()
            val transaction = mockk<AccountQrTransaction>()
            coEvery {
                transaction.updateDeleted(accountId = any(), qrId = any(), isDeleted = any(), updatedAt = any())
            } throws throwable
            val repository = AccountQrRepositoryImpl(accountQrLocalDataSource = localDataSource, accountQrTransaction = transaction)

            shouldThrow<IllegalStateException> {
                repository.updateDeleted(
                    account = account,
                    qrId = fixtureMonkey.giveMeOne<Uuid>(),
                    isDeleted = fixtureMonkey.giveMeOne<Boolean>(),
                    updatedAt = fixtureMonkey.giveMeOne<Instant>(),
                )
            } shouldBeSameInstanceAs throwable
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun pagingSource(qrList: List<QrLocalEntity>): PagingSource<Int, QrLocalEntity> =
            mockk(relaxed = true) {
                coEvery { load(any()) } returns
                    PagingSource.LoadResult.Page(
                        data = qrList,
                        prevKey = null,
                        nextKey = null,
                    )
            }

        private suspend fun <T : Any> PagingData<T>.items(): List<T> =
            coroutineScope {
                val presenter =
                    object : PagingDataPresenter<T>(mainContext = coroutineContext) {
                        override suspend fun presentPagingDataEvent(event: PagingDataEvent<T>) = Unit
                    }
                val collection = launch { presenter.collectFrom(this@items) }

                presenter.loadStateFlow
                    .filterNotNull()
                    .first { loadStates -> loadStates.refresh is LoadState.NotLoading }
                val result = presenter.snapshot().items

                collection.cancelAndJoin()
                result
            }
    }
}
