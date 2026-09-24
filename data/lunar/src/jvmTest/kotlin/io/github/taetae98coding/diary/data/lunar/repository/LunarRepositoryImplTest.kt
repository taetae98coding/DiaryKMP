package io.github.taetae98coding.diary.data.lunar.repository

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.calendar.database.api.datasource.LunarLocalDataSource
import io.github.taetae98coding.diary.core.calendar.database.api.entity.LunarDateLocalEntity
import io.github.taetae98coding.diary.core.calendar.database.api.transaction.LunarTransaction
import io.github.taetae98coding.diary.core.calendar.network.api.datasource.LunarRemoteDataSource
import io.github.taetae98coding.diary.core.calendar.network.api.entity.LunarDateRemoteEntity
import io.github.taetae98coding.diary.data.lunar.datasource.LunarDirtyDataSource
import io.github.taetae98coding.diary.data.lunar.mapper.toDomain
import io.github.taetae98coding.diary.data.lunar.mapper.toLocal
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.Runs
import io.mockk.andThenJust
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange

private const val YEAR = 2026
private const val OTHER_YEAR = 2027

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class LunarRepositoryImplTest :
    FunSpec({
        test("TC-LUNAR-FETCH-DATA-001 원격 음력 자료로 요청한 연도의 캐시를 교체한다") {
            val remoteList = listOf(remoteLunarDate(day = 1), remoteLunarDate(day = 2))
            val expectedLocalList = remoteList.map { remote -> remote.toLocal(solarYear = YEAR) }
            val remoteDataSource = mockk<LunarRemoteDataSource>()
            coEvery { remoteDataSource.get(year = YEAR) } returns remoteList
            val transaction = mockk<LunarTransaction>()
            coEvery { transaction.upsert(solarYear = YEAR, lunarDateList = expectedLocalList) } just Runs
            val repository = repository(remoteDataSource = remoteDataSource, transaction = transaction)

            repository.fetch(year = YEAR) shouldBe expectedLocalList.map { local -> local.toDomain() }

            coVerify(exactly = 1) { remoteDataSource.get(year = YEAR) }
            coVerify(exactly = 1) { transaction.upsert(solarYear = YEAR, lunarDateList = expectedLocalList) }
            coVerify(exactly = 0) { transaction.upsert(solarYear = match { year -> year != YEAR }, lunarDateList = any()) }
        }

        test("TC-LUNAR-FETCH-DATA-002 원격이 자료를 제공하지 않으면 요청한 연도의 캐시를 제거한다") {
            val remoteDataSource = mockk<LunarRemoteDataSource>()
            coEvery { remoteDataSource.get(year = YEAR) } returns emptyList()
            val transaction = mockk<LunarTransaction>()
            coEvery { transaction.upsert(solarYear = YEAR, lunarDateList = emptyList()) } just Runs
            val repository = repository(remoteDataSource = remoteDataSource, transaction = transaction)

            repository.fetch(year = YEAR) shouldBe emptyList()

            coVerify(exactly = 1) { transaction.upsert(solarYear = YEAR, lunarDateList = emptyList()) }
        }

        test("TC-LUNAR-FETCH-DATA-003 원격 조회가 실패하면 기존 캐시를 유지한다") {
            val failure = TestException(fixtureMonkey.giveMeOne())
            val remoteDataSource = mockk<LunarRemoteDataSource>()
            coEvery { remoteDataSource.get(year = YEAR) } throws failure
            val transaction = mockk<LunarTransaction>(relaxed = true)
            val repository = repository(remoteDataSource = remoteDataSource, transaction = transaction)

            val actual = shouldThrowExactly<TestException> { repository.fetch(year = YEAR) }

            actual shouldBeSameInstanceAs failure
            coVerify(exactly = 0) { transaction.upsert(solarYear = any(), lunarDateList = any()) }
        }

        test("TC-LUNAR-FETCH-DATA-004 로컬 캐시 교체가 실패하면 실패를 그대로 전달한다") {
            val failure = TestException(fixtureMonkey.giveMeOne())
            val remoteList = listOf(remoteLunarDate(day = 1))
            val remoteDataSource = mockk<LunarRemoteDataSource>()
            coEvery { remoteDataSource.get(year = YEAR) } returns remoteList
            val transaction = mockk<LunarTransaction>()
            coEvery { transaction.upsert(solarYear = YEAR, lunarDateList = any()) } throws failure
            val repository = repository(remoteDataSource = remoteDataSource, transaction = transaction)

            val actual = shouldThrowExactly<TestException> { repository.fetch(year = YEAR) }

            actual shouldBeSameInstanceAs failure
        }

        test("TC-LUNAR-FETCH-DATA-005 이미 동기화에 성공한 연도는 다시 원격 조회하지 않고 로컬 캐시를 결과로 제공한다") {
            val remoteList = listOf(remoteLunarDate(day = 1))
            val localList = remoteList.map { remote -> remote.toLocal(solarYear = YEAR) }
            val remoteDataSource = mockk<LunarRemoteDataSource>()
            coEvery { remoteDataSource.get(year = YEAR) } returns remoteList
            val localDataSource = mockk<LunarLocalDataSource>()
            every { localDataSource.get(dateRange = LocalDate(YEAR, 1, 1)..LocalDate(YEAR, 12, 31)) } returns flowOf(localList)
            val transaction = mockk<LunarTransaction>()
            coEvery { transaction.upsert(solarYear = YEAR, lunarDateList = localList) } just Runs
            val repository = repository(remoteDataSource = remoteDataSource, localDataSource = localDataSource, transaction = transaction)

            repository.fetch(year = YEAR)

            repository.fetch(year = YEAR) shouldBe localList.map { local -> local.toDomain() }
            coVerify(exactly = 1) { remoteDataSource.get(year = YEAR) }
            coVerify(exactly = 1) { transaction.upsert(solarYear = YEAR, lunarDateList = localList) }
        }

        test("TC-LUNAR-FETCH-DATA-006 원격 조회 실패 후 다시 요청하면 다시 원격 조회한다") {
            val failure = TestException(fixtureMonkey.giveMeOne())
            val remoteList = listOf(remoteLunarDate(day = 1))
            val expectedLocalList = remoteList.map { remote -> remote.toLocal(solarYear = YEAR) }
            val remoteDataSource = mockk<LunarRemoteDataSource>()
            coEvery { remoteDataSource.get(year = YEAR) } throws failure andThen remoteList
            val transaction = mockk<LunarTransaction>()
            coEvery { transaction.upsert(solarYear = YEAR, lunarDateList = expectedLocalList) } just Runs
            val repository = repository(remoteDataSource = remoteDataSource, transaction = transaction)

            shouldThrowExactly<TestException> { repository.fetch(year = YEAR) }
            repository.fetch(year = YEAR)

            coVerify(exactly = 2) { remoteDataSource.get(year = YEAR) }
            coVerify(exactly = 1) { transaction.upsert(solarYear = YEAR, lunarDateList = expectedLocalList) }
        }

        test("TC-LUNAR-FETCH-DATA-006 로컬 캐시 교체 실패 후 다시 요청하면 다시 원격 조회한다") {
            val failure = TestException(fixtureMonkey.giveMeOne())
            val remoteList = listOf(remoteLunarDate(day = 1))
            val expectedLocalList = remoteList.map { remote -> remote.toLocal(solarYear = YEAR) }
            val remoteDataSource = mockk<LunarRemoteDataSource>()
            coEvery { remoteDataSource.get(year = YEAR) } returns remoteList
            val transaction = mockk<LunarTransaction>()
            coEvery { transaction.upsert(solarYear = YEAR, lunarDateList = expectedLocalList) } throws failure andThenJust Runs
            val repository = repository(remoteDataSource = remoteDataSource, transaction = transaction)

            shouldThrowExactly<TestException> { repository.fetch(year = YEAR) }
            repository.fetch(year = YEAR)

            coVerify(exactly = 2) { remoteDataSource.get(year = YEAR) }
            coVerify(exactly = 2) { transaction.upsert(solarYear = YEAR, lunarDateList = expectedLocalList) }
        }

        test("TC-LUNAR-FETCH-DATA-007 한 연도의 동기화 이력은 다른 연도의 동기화를 막지 않는다") {
            val remoteList = listOf(remoteLunarDate(day = 1))
            val remoteDataSource = mockk<LunarRemoteDataSource>()
            coEvery { remoteDataSource.get(year = any()) } returns remoteList
            val transaction = mockk<LunarTransaction>()
            coEvery { transaction.upsert(solarYear = any(), lunarDateList = any()) } just Runs
            val repository = repository(remoteDataSource = remoteDataSource, transaction = transaction)

            repository.fetch(year = YEAR)
            repository.fetch(year = OTHER_YEAR)

            coVerify(exactly = 1) { remoteDataSource.get(year = OTHER_YEAR) }
            coVerify(exactly = 1) { transaction.upsert(solarYear = OTHER_YEAR, lunarDateList = remoteList.map { remote -> remote.toLocal(solarYear = OTHER_YEAR) }) }
        }

        test("TC-LUNAR-FETCH-DATA-008 자료를 제공하지 않은 연도도 다시 요청하면 원격 조회를 생략한다") {
            val remoteDataSource = mockk<LunarRemoteDataSource>()
            coEvery { remoteDataSource.get(year = YEAR) } returns emptyList()
            val localDataSource = mockk<LunarLocalDataSource>()
            every { localDataSource.get(dateRange = any()) } returns flowOf(emptyList())
            val transaction = mockk<LunarTransaction>()
            coEvery { transaction.upsert(solarYear = YEAR, lunarDateList = emptyList()) } just Runs
            val repository = repository(remoteDataSource = remoteDataSource, localDataSource = localDataSource, transaction = transaction)

            repository.fetch(year = YEAR)
            repository.fetch(year = YEAR) shouldBe emptyList()

            coVerify(exactly = 1) { remoteDataSource.get(year = YEAR) }
            coVerify(exactly = 1) { transaction.upsert(solarYear = YEAR, lunarDateList = emptyList()) }
        }

        test("기간별 조회는 로컬 캐시의 음력 날짜를 순서대로 도메인 모델로 제공한다") {
            val dateRange = LocalDateRange(LocalDate(2026, 8, 17), LocalDate(2026, 8, 23))
            val localList = listOf(localLunarDate(solar = LocalDate(2026, 8, 17)), localLunarDate(solar = LocalDate(2026, 8, 18)))
            val localDataSource = mockk<LunarLocalDataSource>()
            every { localDataSource.get(dateRange = dateRange) } returns flowOf(localList)
            val repository = repository(localDataSource = localDataSource)

            repository.get(dateRange = dateRange).collect { actual -> actual shouldBe localList.map { local -> local.toDomain() } }
        }
    })

private fun repository(
    remoteDataSource: LunarRemoteDataSource = mockk(),
    localDataSource: LunarLocalDataSource = mockk(),
    transaction: LunarTransaction = mockk(),
): LunarRepositoryImpl =
    LunarRepositoryImpl(
        lunarRemoteDataSource = remoteDataSource,
        lunarLocalDataSource = localDataSource,
        lunarTransaction = transaction,
        lunarDirtyDataSource = LunarDirtyDataSource(),
    )

private fun remoteLunarDate(day: Int): LunarDateRemoteEntity =
    LunarDateRemoteEntity(
        solar = LocalDate(YEAR, 1, day),
        year = YEAR - 1,
        month = 11,
        day = 12 + day,
        isLeapMonth = false,
    )

private fun localLunarDate(solar: LocalDate): LunarDateLocalEntity =
    LunarDateLocalEntity(
        solar = solar,
        solarYear = solar.year,
        lunarYear = fixtureMonkey.giveMeOne(),
        lunarMonth = fixtureMonkey.giveMeOne(),
        lunarDay = fixtureMonkey.giveMeOne(),
        isLeapMonth = fixtureMonkey.giveMeOne(),
    )

private class TestException(
    message: String,
) : RuntimeException(message)
