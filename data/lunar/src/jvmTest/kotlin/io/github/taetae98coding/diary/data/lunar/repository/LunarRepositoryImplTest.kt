package io.github.taetae98coding.diary.data.lunar.repository

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.calendar.database.api.datasource.LunarLocalDataSource
import io.github.taetae98coding.diary.core.calendar.database.api.entity.LunarDateLocalEntity
import io.github.taetae98coding.diary.core.calendar.database.api.transaction.LunarTransaction
import io.github.taetae98coding.diary.core.calendar.network.api.datasource.LunarRemoteDataSource
import io.github.taetae98coding.diary.core.calendar.network.api.entity.LunarDateRemoteEntity
import io.github.taetae98coding.diary.data.lunar.cache.LunarFetchedYearSet
import io.github.taetae98coding.diary.data.lunar.mapper.toDomain
import io.github.taetae98coding.diary.data.lunar.mapper.toLocal
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.Runs
import io.mockk.andThenJust
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange

private const val MIN_SOLAR_YEAR = 1_000
private const val SOLAR_YEAR_SPAN = 9_000

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class LunarRepositoryImplTest :
    FunSpec({
        test("TC-3e9afe01-23d6-4f46-8445-bf9a9a75408e 원격 음력 자료로 요청한 연도의 캐시를 교체한다") {
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

        test("TC-77a8f0ae-7aa7-4553-a40a-d70cca1e0f3d 원격이 자료를 제공하지 않으면 요청한 연도의 캐시를 제거한다") {
            val year = solarYear()
            val remoteDataSource = mockk<LunarRemoteDataSource>()
            coEvery { remoteDataSource.get(year = year) } returns emptyList()
            val transaction = mockk<LunarTransaction>()
            coEvery { transaction.upsert(solarYear = year, lunarDateList = emptyList()) } just Runs
            val repository = repository(remoteDataSource = remoteDataSource, transaction = transaction)

            repository.fetch(year = year) shouldBe emptyList()

            coVerify(exactly = 1) { transaction.upsert(solarYear = year, lunarDateList = emptyList()) }
        }

        test("TC-bb5e708d-6bdc-4f75-8de8-e595ef74089c 원격 조회가 실패하면 기존 캐시를 유지한다") {
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

        test("TC-b51289c8-284c-4230-9b59-888fffa5251e 기기 저장이 실패하면 동기화가 실패로 끝난다") {
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

        test("TC-405372aa-8b92-4b88-8a3e-d17620142a52 이미 동기화에 성공한 연도는 다시 원격 조회하지 않고 로컬 캐시를 결과로 제공한다") {
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

        test("TC-508ba7b1-8859-4ac3-b3e1-1ae8cd7f241a 앱 프로세스를 새로 시작하면 성공했던 연도도 다시 원격 조회한다") {
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

        test("TC-5adc31f5-980a-44e7-a704-261fa601dc5c 원격 조회 실패 후 다시 요청하면 다시 원격 조회한다") {
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

        test("TC-5adc31f5-980a-44e7-a704-261fa601dc5c 로컬 캐시 교체 실패 후 다시 요청하면 다시 원격 조회한다") {
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

        test("TC-0cc93d30-6dbd-4063-85a8-51b277f5629e 한 연도의 동기화 이력은 다른 연도의 동기화를 막지 않는다") {
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

        test("TC-057e5aa1-675f-4b27-82f7-0350a818aca6 자료를 제공하지 않은 연도도 다시 요청하면 원격 조회를 생략한다") {
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

        test("TC-cf5abbb6-1513-47d0-be76-a683874d489e 같은 연도의 동기화가 진행 중이면 새로 원격 조회하지 않고 그 결과를 함께 기다린다") {
            runTest {
                val year = solarYear()
                val remoteList = listOf(remoteLunarDate(year = year, day = 1))
                val localList = remoteList.map { remote -> remote.toLocal(solarYear = year) }
                val response = CompletableDeferred<List<LunarDateRemoteEntity>>()
                val remoteDataSource = mockk<LunarRemoteDataSource>()
                coEvery { remoteDataSource.get(year = year) } coAnswers { response.await() }
                val localDataSource = mockk<LunarLocalDataSource>()
                every { localDataSource.get(dateRange = LocalDate(year, 1, 1)..LocalDate(year, 12, 31)) } returns flowOf(localList)
                val transaction = mockk<LunarTransaction>()
                coEvery { transaction.upsert(solarYear = year, lunarDateList = localList) } just Runs
                val repository = repository(remoteDataSource = remoteDataSource, localDataSource = localDataSource, transaction = transaction)

                val first = async { repository.fetch(year = year) }
                runCurrent()
                val second = async { repository.fetch(year = year) }
                runCurrent()

                second.isCompleted shouldBe false

                response.complete(remoteList)
                first.await()

                second.await() shouldBe localList.map { local -> local.toDomain() }
                coVerify(exactly = 1) { remoteDataSource.get(year = year) }
            }
        }

        test("TC-b0b6885a-a995-4da6-951f-e47afe65be49 진행 중이던 동기화가 실패하면 함께 기다린 요청이 다시 원격 조회한다") {
            runTest {
                val year = solarYear()
                val remoteList = listOf(remoteLunarDate(year = year, day = 1))
                val localList = remoteList.map { remote -> remote.toLocal(solarYear = year) }
                val response = CompletableDeferred<List<LunarDateRemoteEntity>>()
                val remoteDataSource = mockk<LunarRemoteDataSource>()
                coEvery { remoteDataSource.get(year = year) } coAnswers { response.await() } andThen remoteList
                val transaction = mockk<LunarTransaction>()
                coEvery { transaction.upsert(solarYear = year, lunarDateList = localList) } just Runs
                val repository = repository(remoteDataSource = remoteDataSource, transaction = transaction)

                val first = async { runCatching { repository.fetch(year = year) } }
                runCurrent()
                val second = async { repository.fetch(year = year) }
                runCurrent()

                response.completeExceptionally(TestException(fixtureMonkey.giveMeOne()))

                first.await().shouldBeFailure<TestException>()
                second.await() shouldBe localList.map { local -> local.toDomain() }
                coVerify(exactly = 2) { remoteDataSource.get(year = year) }
            }
        }

        test("TC-38855674-7dfc-4058-9e5f-d2ea56695b30 다른 연도의 동기화는 진행 중인 동기화를 기다리지 않는다") {
            runTest {
                val year = solarYear()
                val remoteList = listOf(remoteLunarDate(year = year + 1, day = 1))
                val response = CompletableDeferred<List<LunarDateRemoteEntity>>()
                val remoteDataSource = mockk<LunarRemoteDataSource>()
                coEvery { remoteDataSource.get(year = year) } coAnswers { response.await() }
                coEvery { remoteDataSource.get(year = year + 1) } returns remoteList
                val transaction = mockk<LunarTransaction>()
                coEvery { transaction.upsert(solarYear = any(), lunarDateList = any()) } just Runs
                val repository = repository(remoteDataSource = remoteDataSource, transaction = transaction)

                val first = async { repository.fetch(year = year) }
                runCurrent()

                repository.fetch(year = year + 1) shouldBe remoteList.map { remote -> remote.toLocal(solarYear = year + 1).toDomain() }
                first.isCompleted shouldBe false

                response.complete(emptyList())
                first.await()
            }
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
        lunarFetchedYearSet = LunarFetchedYearSet(),
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
