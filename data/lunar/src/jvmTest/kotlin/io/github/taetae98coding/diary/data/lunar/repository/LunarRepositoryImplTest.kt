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

private const val MIN_SOLAR_YEAR = 1_000
private const val SOLAR_YEAR_SPAN = 9_000

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class LunarRepositoryImplTest :
    FunSpec({
        test("TC-LUNAR-FETCH-DATA-001 원격 음력 자료로 요청한 연도의 캐시를 교체한다") {
            val year = solarYear()
            val remoteList = listOf(remoteLunarDate(year = year, day = 1), remoteLunarDate(year = year, day = 2))
            val expectedLocalList = remoteList.map { remote -> remote.toLocal(solarYear = year) }
            val remoteDataSource = mockk<LunarRemoteDataSource>()
            coEvery { remoteDataSource.get(year = year) } returns remoteList
            val transaction = mockk<LunarTransaction>()
            coEvery { transaction.upsert(solarYear = year, lunarDateList = expectedLocalList) } just Runs
            val repository = repository(remoteDataSource = remoteDataSource, transaction = transaction)

            repository.fetch(year = year) shouldBe expectedLocalList.map { local -> local.toDomain() }

            coVerify(exactly = 1) { remoteDataSource.get(year = year) }
            coVerify(exactly = 1) { transaction.upsert(solarYear = year, lunarDateList = expectedLocalList) }
            coVerify(exactly = 0) { transaction.upsert(solarYear = match { solarYear -> solarYear != year }, lunarDateList = any()) }
        }

        test("TC-LUNAR-FETCH-DATA-002 원격이 자료를 제공하지 않으면 요청한 연도의 캐시를 제거한다") {
            val year = solarYear()
            val remoteDataSource = mockk<LunarRemoteDataSource>()
            coEvery { remoteDataSource.get(year = year) } returns emptyList()
            val transaction = mockk<LunarTransaction>()
            coEvery { transaction.upsert(solarYear = year, lunarDateList = emptyList()) } just Runs
            val repository = repository(remoteDataSource = remoteDataSource, transaction = transaction)

            repository.fetch(year = year) shouldBe emptyList()

            coVerify(exactly = 1) { transaction.upsert(solarYear = year, lunarDateList = emptyList()) }
        }

        test("TC-LUNAR-FETCH-DATA-003 원격 조회가 실패하면 기존 캐시를 유지한다") {
            val year = solarYear()
            val failure = TestException(fixtureMonkey.giveMeOne())
            val remoteDataSource = mockk<LunarRemoteDataSource>()
            coEvery { remoteDataSource.get(year = year) } throws failure
            val transaction = mockk<LunarTransaction>(relaxed = true)
            val repository = repository(remoteDataSource = remoteDataSource, transaction = transaction)

            val actual = shouldThrowExactly<TestException> { repository.fetch(year = year) }

            actual shouldBeSameInstanceAs failure
            coVerify(exactly = 0) { transaction.upsert(solarYear = any(), lunarDateList = any()) }
        }

        test("TC-LUNAR-FETCH-DATA-004 기기 저장이 실패하면 동기화가 실패로 끝난다") {
            val year = solarYear()
            val failure = TestException(fixtureMonkey.giveMeOne())
            val remoteList = listOf(remoteLunarDate(year = year, day = 1))
            val remoteDataSource = mockk<LunarRemoteDataSource>()
            coEvery { remoteDataSource.get(year = year) } returns remoteList
            val transaction = mockk<LunarTransaction>()
            coEvery { transaction.upsert(solarYear = year, lunarDateList = any()) } throws failure
            val repository = repository(remoteDataSource = remoteDataSource, transaction = transaction)

            val actual = shouldThrowExactly<TestException> { repository.fetch(year = year) }

            actual shouldBeSameInstanceAs failure
        }

        test("TC-LUNAR-FETCH-DATA-005 이미 동기화에 성공한 연도는 다시 원격 조회하지 않고 로컬 캐시를 결과로 제공한다") {
            val year = solarYear()
            val remoteList = listOf(remoteLunarDate(year = year, day = 1))
            val localList = remoteList.map { remote -> remote.toLocal(solarYear = year) }
            val remoteDataSource = mockk<LunarRemoteDataSource>()
            coEvery { remoteDataSource.get(year = year) } returns remoteList
            val localDataSource = mockk<LunarLocalDataSource>()
            every { localDataSource.get(dateRange = LocalDate(year, 1, 1)..LocalDate(year, 12, 31)) } returns flowOf(localList)
            val transaction = mockk<LunarTransaction>()
            coEvery { transaction.upsert(solarYear = year, lunarDateList = localList) } just Runs
            val repository = repository(remoteDataSource = remoteDataSource, localDataSource = localDataSource, transaction = transaction)

            repository.fetch(year = year)

            repository.fetch(year = year) shouldBe localList.map { local -> local.toDomain() }
            coVerify(exactly = 1) { remoteDataSource.get(year = year) }
            coVerify(exactly = 1) { transaction.upsert(solarYear = year, lunarDateList = localList) }
        }

        test("TC-LUNAR-FETCH-DATA-014 앱 프로세스를 새로 시작하면 성공했던 연도도 다시 원격 조회한다") {
            val year = solarYear()
            val remoteList = listOf(remoteLunarDate(year = year, day = 1))
            val localList = remoteList.map { remote -> remote.toLocal(solarYear = year) }
            val remoteDataSource = mockk<LunarRemoteDataSource>()
            coEvery { remoteDataSource.get(year = year) } returns remoteList
            val transaction = mockk<LunarTransaction>()
            coEvery { transaction.upsert(solarYear = year, lunarDateList = localList) } just Runs
            repository(remoteDataSource = remoteDataSource, transaction = transaction).fetch(year = year)

            // 저장소를 새로 만들어 빈 동기화 이력으로 시작하는 새 프로세스를 흉내 낸다.
            repository(remoteDataSource = remoteDataSource, transaction = transaction).fetch(year = year)

            coVerify(exactly = 2) { remoteDataSource.get(year = year) }
        }

        test("TC-LUNAR-FETCH-DATA-006 원격 조회 실패 후 다시 요청하면 다시 원격 조회한다") {
            val year = solarYear()
            val failure = TestException(fixtureMonkey.giveMeOne())
            val remoteList = listOf(remoteLunarDate(year = year, day = 1))
            val expectedLocalList = remoteList.map { remote -> remote.toLocal(solarYear = year) }
            val remoteDataSource = mockk<LunarRemoteDataSource>()
            coEvery { remoteDataSource.get(year = year) } throws failure andThen remoteList
            val transaction = mockk<LunarTransaction>()
            coEvery { transaction.upsert(solarYear = year, lunarDateList = expectedLocalList) } just Runs
            val repository = repository(remoteDataSource = remoteDataSource, transaction = transaction)

            shouldThrowExactly<TestException> { repository.fetch(year = year) }
            repository.fetch(year = year)

            coVerify(exactly = 2) { remoteDataSource.get(year = year) }
            coVerify(exactly = 1) { transaction.upsert(solarYear = year, lunarDateList = expectedLocalList) }
        }

        test("TC-LUNAR-FETCH-DATA-006 로컬 캐시 교체 실패 후 다시 요청하면 다시 원격 조회한다") {
            val year = solarYear()
            val failure = TestException(fixtureMonkey.giveMeOne())
            val remoteList = listOf(remoteLunarDate(year = year, day = 1))
            val expectedLocalList = remoteList.map { remote -> remote.toLocal(solarYear = year) }
            val remoteDataSource = mockk<LunarRemoteDataSource>()
            coEvery { remoteDataSource.get(year = year) } returns remoteList
            val transaction = mockk<LunarTransaction>()
            coEvery { transaction.upsert(solarYear = year, lunarDateList = expectedLocalList) } throws failure andThenJust Runs
            val repository = repository(remoteDataSource = remoteDataSource, transaction = transaction)

            shouldThrowExactly<TestException> { repository.fetch(year = year) }
            repository.fetch(year = year)

            coVerify(exactly = 2) { remoteDataSource.get(year = year) }
            coVerify(exactly = 2) { transaction.upsert(solarYear = year, lunarDateList = expectedLocalList) }
        }

        test("TC-LUNAR-FETCH-DATA-007 한 연도의 동기화 이력은 다른 연도의 동기화를 막지 않는다") {
            val year = solarYear()
            val remoteList = listOf(remoteLunarDate(year = year, day = 1))
            val remoteDataSource = mockk<LunarRemoteDataSource>()
            coEvery { remoteDataSource.get(year = any()) } returns remoteList
            val transaction = mockk<LunarTransaction>()
            coEvery { transaction.upsert(solarYear = any(), lunarDateList = any()) } just Runs
            val repository = repository(remoteDataSource = remoteDataSource, transaction = transaction)

            repository.fetch(year = year)
            repository.fetch(year = year + 1)

            coVerify(exactly = 1) { remoteDataSource.get(year = year + 1) }
            coVerify(exactly = 1) { transaction.upsert(solarYear = year + 1, lunarDateList = remoteList.map { remote -> remote.toLocal(solarYear = year + 1) }) }
        }

        test("TC-LUNAR-FETCH-DATA-008 자료를 제공하지 않은 연도도 다시 요청하면 원격 조회를 생략한다") {
            val year = solarYear()
            val remoteDataSource = mockk<LunarRemoteDataSource>()
            coEvery { remoteDataSource.get(year = year) } returns emptyList()
            val localDataSource = mockk<LunarLocalDataSource>()
            every { localDataSource.get(dateRange = any()) } returns flowOf(emptyList())
            val transaction = mockk<LunarTransaction>()
            coEvery { transaction.upsert(solarYear = year, lunarDateList = emptyList()) } just Runs
            val repository = repository(remoteDataSource = remoteDataSource, localDataSource = localDataSource, transaction = transaction)

            repository.fetch(year = year)
            repository.fetch(year = year) shouldBe emptyList()

            coVerify(exactly = 1) { remoteDataSource.get(year = year) }
            coVerify(exactly = 1) { transaction.upsert(solarYear = year, lunarDateList = emptyList()) }
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

private fun solarYear(): Int = fixtureMonkey.giveMeOne<Int>().mod(SOLAR_YEAR_SPAN) + MIN_SOLAR_YEAR

private fun remoteLunarDate(
    year: Int,
    day: Int,
): LunarDateRemoteEntity =
    LunarDateRemoteEntity(
        solar = LocalDate(year, 1, day),
        year = fixtureMonkey.giveMeOne(),
        month = fixtureMonkey.giveMeOne(),
        day = fixtureMonkey.giveMeOne(),
        isLeapMonth = fixtureMonkey.giveMeOne(),
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
