package io.github.taetae98coding.diary.data.holiday.repository

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.holiday.database.api.datasource.HolidayLocalDataSource
import io.github.taetae98coding.diary.core.holiday.database.api.entity.HolidayLocalEntity
import io.github.taetae98coding.diary.core.holiday.database.api.transaction.HolidayTransaction
import io.github.taetae98coding.diary.core.holiday.network.api.datasource.HolidayRemoteDataSource
import io.github.taetae98coding.diary.core.holiday.network.api.entity.HolidayRemoteEntity
import io.github.taetae98coding.diary.data.holiday.datasource.HolidayDirtyDataSource
import io.github.taetae98coding.diary.data.holiday.mapper.toDomain
import io.github.taetae98coding.diary.data.holiday.mapper.toLocal
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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class HolidayRepositoryImplTest :
    FunSpec({
        test("TC-HOLIDAY-FETCH-DATA-001 원격 공휴일로 요청한 연도의 캐시를 교체한다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val remoteHolidayList = listOf(remoteHoliday(), remoteHoliday())
            val expectedHolidayList = remoteHolidayList.map { remote -> remote.toLocal(year = year) }
            val remoteDataSource = mockk<HolidayRemoteDataSource>()
            coEvery { remoteDataSource.get(year = year) } returns remoteHolidayList
            val localDataSource = mockk<HolidayLocalDataSource>()
            val transaction = mockk<HolidayTransaction>()
            coEvery {
                transaction.upsert(
                    year = year,
                    holidayList = expectedHolidayList,
                )
            } just Runs
            val repository =
                HolidayRepositoryImpl(
                    holidayRemoteDataSource = remoteDataSource,
                    holidayLocalDataSource = localDataSource,
                    holidayTransaction = transaction,
                    holidayDirtyDataSource = HolidayDirtyDataSource(),
                )

            repository.fetch(year = year) shouldBe expectedHolidayList.map { local -> local.toDomain() }

            coVerify(exactly = 1) { remoteDataSource.get(year = year) }
            coVerify(exactly = 1) {
                transaction.upsert(
                    year = year,
                    holidayList = expectedHolidayList,
                )
            }
            coVerify(exactly = 0) {
                transaction.upsert(
                    year = match { requestedYear -> requestedYear != year },
                    holidayList = any(),
                )
            }
        }

        test("TC-HOLIDAY-FETCH-DATA-002 원격이 공휴일을 제공하지 않으면 요청한 연도의 캐시를 제거한다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val remoteDataSource = mockk<HolidayRemoteDataSource>()
            coEvery { remoteDataSource.get(year = year) } returns emptyList()
            val localDataSource = mockk<HolidayLocalDataSource>()
            val transaction = mockk<HolidayTransaction>()
            coEvery { transaction.upsert(year = year, holidayList = emptyList()) } just Runs
            val repository =
                HolidayRepositoryImpl(
                    holidayRemoteDataSource = remoteDataSource,
                    holidayLocalDataSource = localDataSource,
                    holidayTransaction = transaction,
                    holidayDirtyDataSource = HolidayDirtyDataSource(),
                )

            repository.fetch(year = year) shouldBe emptyList()

            coVerify(exactly = 1) { transaction.upsert(year = year, holidayList = emptyList()) }
        }

        test("TC-HOLIDAY-FETCH-DATA-009 공휴일을 제공하지 않은 연도도 다시 요청하면 원격 조회를 생략한다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val remoteDataSource = mockk<HolidayRemoteDataSource>()
            coEvery { remoteDataSource.get(year = year) } returns emptyList()
            val localDataSource = mockk<HolidayLocalDataSource>()
            every { localDataSource.get(year = year) } returns flowOf(emptyList())
            val transaction = mockk<HolidayTransaction>()
            coEvery { transaction.upsert(year = year, holidayList = emptyList()) } just Runs
            val repository =
                HolidayRepositoryImpl(
                    holidayRemoteDataSource = remoteDataSource,
                    holidayLocalDataSource = localDataSource,
                    holidayTransaction = transaction,
                    holidayDirtyDataSource = HolidayDirtyDataSource(),
                )

            repository.fetch(year = year)
            repository.fetch(year = year) shouldBe emptyList()

            coVerify(exactly = 1) { remoteDataSource.get(year = year) }
            coVerify(exactly = 1) { transaction.upsert(year = year, holidayList = emptyList()) }
        }

        test("TC-HOLIDAY-FETCH-DATA-010 재조회를 생략한 연도는 로컬 캐시에 저장된 공휴일을 결과로 제공한다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val remoteHolidayList = listOf(remoteHoliday())
            val localHolidayList = listOf(localHoliday(year = year), localHoliday(year = year))
            val remoteDataSource = mockk<HolidayRemoteDataSource>()
            coEvery { remoteDataSource.get(year = year) } returns remoteHolidayList
            val localDataSource = mockk<HolidayLocalDataSource>()
            every { localDataSource.get(year = year) } returns flowOf(localHolidayList)
            val transaction = mockk<HolidayTransaction>()
            coEvery { transaction.upsert(year = year, holidayList = any()) } just Runs
            val repository =
                HolidayRepositoryImpl(
                    holidayRemoteDataSource = remoteDataSource,
                    holidayLocalDataSource = localDataSource,
                    holidayTransaction = transaction,
                    holidayDirtyDataSource = HolidayDirtyDataSource(),
                )

            repository.fetch(year = year)

            repository.fetch(year = year) shouldBe localHolidayList.map { local -> local.toDomain() }
            coVerify(exactly = 1) { remoteDataSource.get(year = year) }
        }

        test("TC-HOLIDAY-FETCH-DATA-003 원격 조회가 실패하면 기존 캐시를 유지한다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val failure = TestException(fixtureMonkey.giveMeOne())
            val remoteDataSource = mockk<HolidayRemoteDataSource>()
            coEvery { remoteDataSource.get(year = year) } throws failure
            val localDataSource = mockk<HolidayLocalDataSource>(relaxed = true)
            val transaction = mockk<HolidayTransaction>(relaxed = true)
            val repository =
                HolidayRepositoryImpl(
                    holidayRemoteDataSource = remoteDataSource,
                    holidayLocalDataSource = localDataSource,
                    holidayTransaction = transaction,
                    holidayDirtyDataSource = HolidayDirtyDataSource(),
                )

            val actual =
                shouldThrowExactly<TestException> {
                    repository.fetch(year = year)
                }

            actual shouldBeSameInstanceAs failure
            coVerify(exactly = 0) { transaction.upsert(year = any(), holidayList = any()) }
        }

        test("TC-HOLIDAY-FETCH-DATA-004 로컬 캐시 교체가 실패하면 기존 캐시를 유지한다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val remoteHolidayList = listOf(remoteHoliday())
            val expectedHolidayList = remoteHolidayList.map { remote -> remote.toLocal(year = year) }
            val failure = TestException(fixtureMonkey.giveMeOne())
            val remoteDataSource = mockk<HolidayRemoteDataSource>()
            coEvery { remoteDataSource.get(year = year) } returns remoteHolidayList
            val localDataSource = mockk<HolidayLocalDataSource>()
            val transaction = mockk<HolidayTransaction>()
            coEvery {
                transaction.upsert(
                    year = year,
                    holidayList = expectedHolidayList,
                )
            } throws failure
            val repository =
                HolidayRepositoryImpl(
                    holidayRemoteDataSource = remoteDataSource,
                    holidayLocalDataSource = localDataSource,
                    holidayTransaction = transaction,
                    holidayDirtyDataSource = HolidayDirtyDataSource(),
                )

            val actual =
                shouldThrowExactly<TestException> {
                    repository.fetch(year = year)
                }

            actual shouldBeSameInstanceAs failure
        }

        test("TC-HOLIDAY-FETCH-DATA-005 이미 동기화에 성공한 연도는 다시 원격 조회하지 않는다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val remoteHolidayList = listOf(remoteHoliday(), remoteHoliday())
            val expectedHolidayList = remoteHolidayList.map { remote -> remote.toLocal(year = year) }
            val remoteDataSource = mockk<HolidayRemoteDataSource>()
            coEvery { remoteDataSource.get(year = year) } returns remoteHolidayList
            val localDataSource = mockk<HolidayLocalDataSource>()
            every { localDataSource.get(year = year) } returns flowOf(expectedHolidayList)
            val transaction = mockk<HolidayTransaction>()
            coEvery {
                transaction.upsert(
                    year = year,
                    holidayList = expectedHolidayList,
                )
            } just Runs
            val repository =
                HolidayRepositoryImpl(
                    holidayRemoteDataSource = remoteDataSource,
                    holidayLocalDataSource = localDataSource,
                    holidayTransaction = transaction,
                    holidayDirtyDataSource = HolidayDirtyDataSource(),
                )

            repository.fetch(year = year)
            repository.fetch(year = year)

            coVerify(exactly = 1) { remoteDataSource.get(year = year) }
            coVerify(exactly = 1) {
                transaction.upsert(
                    year = year,
                    holidayList = expectedHolidayList,
                )
            }
        }

        test("TC-HOLIDAY-FETCH-DATA-006 원격 조회 실패 후 다시 요청하면 다시 원격 조회한다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val remoteHolidayList = listOf(remoteHoliday())
            val expectedHolidayList = remoteHolidayList.map { remote -> remote.toLocal(year = year) }
            val failure = TestException(fixtureMonkey.giveMeOne())
            val remoteDataSource = mockk<HolidayRemoteDataSource>()
            coEvery { remoteDataSource.get(year = year) } throws failure andThen remoteHolidayList
            val localDataSource = mockk<HolidayLocalDataSource>()
            val transaction = mockk<HolidayTransaction>()
            coEvery {
                transaction.upsert(
                    year = year,
                    holidayList = expectedHolidayList,
                )
            } just Runs
            val repository =
                HolidayRepositoryImpl(
                    holidayRemoteDataSource = remoteDataSource,
                    holidayLocalDataSource = localDataSource,
                    holidayTransaction = transaction,
                    holidayDirtyDataSource = HolidayDirtyDataSource(),
                )

            shouldThrowExactly<TestException> {
                repository.fetch(year = year)
            }
            repository.fetch(year = year)

            coVerify(exactly = 2) { remoteDataSource.get(year = year) }
            coVerify(exactly = 1) {
                transaction.upsert(
                    year = year,
                    holidayList = expectedHolidayList,
                )
            }
        }

        test("TC-HOLIDAY-FETCH-DATA-006 로컬 캐시 교체 실패 후 다시 요청하면 다시 원격 조회한다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val remoteHolidayList = listOf(remoteHoliday())
            val expectedHolidayList = remoteHolidayList.map { remote -> remote.toLocal(year = year) }
            val failure = TestException(fixtureMonkey.giveMeOne())
            val remoteDataSource = mockk<HolidayRemoteDataSource>()
            coEvery { remoteDataSource.get(year = year) } returns remoteHolidayList
            val localDataSource = mockk<HolidayLocalDataSource>()
            val transaction = mockk<HolidayTransaction>()
            coEvery {
                transaction.upsert(
                    year = year,
                    holidayList = expectedHolidayList,
                )
            } throws failure andThenJust Runs
            val repository =
                HolidayRepositoryImpl(
                    holidayRemoteDataSource = remoteDataSource,
                    holidayLocalDataSource = localDataSource,
                    holidayTransaction = transaction,
                    holidayDirtyDataSource = HolidayDirtyDataSource(),
                )

            shouldThrowExactly<TestException> {
                repository.fetch(year = year)
            }
            repository.fetch(year = year)

            coVerify(exactly = 2) { remoteDataSource.get(year = year) }
            coVerify(exactly = 2) {
                transaction.upsert(
                    year = year,
                    holidayList = expectedHolidayList,
                )
            }
        }

        test("TC-HOLIDAY-FETCH-DATA-007 한 연도의 동기화 이력은 다른 연도의 동기화를 막지 않는다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val otherYear = generateSequence { fixtureMonkey.giveMeOne<Int>() }.first { candidate -> candidate != year }
            val remoteHolidayList = listOf(remoteHoliday())
            val remoteDataSource = mockk<HolidayRemoteDataSource>()
            coEvery { remoteDataSource.get(year = any()) } returns remoteHolidayList
            val localDataSource = mockk<HolidayLocalDataSource>()
            val transaction = mockk<HolidayTransaction>()
            coEvery { transaction.upsert(year = any(), holidayList = any()) } just Runs
            val repository =
                HolidayRepositoryImpl(
                    holidayRemoteDataSource = remoteDataSource,
                    holidayLocalDataSource = localDataSource,
                    holidayTransaction = transaction,
                    holidayDirtyDataSource = HolidayDirtyDataSource(),
                )

            repository.fetch(year = year)
            repository.fetch(year = otherYear)

            coVerify(exactly = 1) { remoteDataSource.get(year = otherYear) }
            coVerify(exactly = 1) {
                transaction.upsert(
                    year = otherYear,
                    holidayList = remoteHolidayList.map { remote -> remote.toLocal(year = otherYear) },
                )
            }
        }

        test("동기화 이력을 공유하면 다른 요청 경로에서도 성공한 연도를 다시 원격 조회하지 않는다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val remoteHolidayList = listOf(remoteHoliday())
            val remoteDataSource = mockk<HolidayRemoteDataSource>()
            coEvery { remoteDataSource.get(year = year) } returns remoteHolidayList
            val localDataSource = mockk<HolidayLocalDataSource>()
            every { localDataSource.get(year = year) } returns flowOf(remoteHolidayList.map { remote -> remote.toLocal(year = year) })
            val transaction = mockk<HolidayTransaction>()
            coEvery { transaction.upsert(year = year, holidayList = any()) } just Runs
            val dirtyDataSource = HolidayDirtyDataSource()

            HolidayRepositoryImpl(
                holidayRemoteDataSource = remoteDataSource,
                holidayLocalDataSource = localDataSource,
                holidayTransaction = transaction,
                holidayDirtyDataSource = dirtyDataSource,
            ).fetch(year = year)
            HolidayRepositoryImpl(
                holidayRemoteDataSource = remoteDataSource,
                holidayLocalDataSource = localDataSource,
                holidayTransaction = transaction,
                holidayDirtyDataSource = dirtyDataSource,
            ).fetch(year = year)

            coVerify(exactly = 1) { remoteDataSource.get(year = year) }
        }

        test("전체 로컬 공휴일을 순서대로 도메인 공휴일로 제공한다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val otherYear = generateSequence { fixtureMonkey.giveMeOne<Int>() }.first { candidate -> candidate != year }
            val localHolidayList = listOf(localHoliday(year = otherYear), localHoliday(year = year))
            val localDataSource = mockk<HolidayLocalDataSource>()
            every { localDataSource.get() } returns flowOf(localHolidayList)
            val repository =
                HolidayRepositoryImpl(
                    holidayRemoteDataSource = mockk(),
                    holidayLocalDataSource = localDataSource,
                    holidayTransaction = mockk(),
                    holidayDirtyDataSource = HolidayDirtyDataSource(),
                )

            repository.get().test {
                awaitItem() shouldBe localHolidayList.map { local -> local.toDomain() }
                awaitComplete()
            }
        }

        test("전체 로컬 공휴일 조회 오류를 동일한 원인으로 전파한다") {
            val failure = TestException(fixtureMonkey.giveMeOne())
            val localDataSource = mockk<HolidayLocalDataSource>()
            every { localDataSource.get() } returns flow { throw failure }
            val repository =
                HolidayRepositoryImpl(
                    holidayRemoteDataSource = mockk(),
                    holidayLocalDataSource = localDataSource,
                    holidayTransaction = mockk(),
                    holidayDirtyDataSource = HolidayDirtyDataSource(),
                )

            repository.get().test {
                awaitError() shouldBeSameInstanceAs failure
            }
        }

        test("요청한 연도의 로컬 공휴일을 도메인 공휴일로 제공한다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val localHolidayList = listOf(localHoliday(year = year), localHoliday(year = year))
            val localDataSource = mockk<HolidayLocalDataSource>()
            val transaction = mockk<HolidayTransaction>()
            every { localDataSource.get(year = year) } returns flowOf(localHolidayList)
            val repository =
                HolidayRepositoryImpl(
                    holidayRemoteDataSource = mockk(),
                    holidayLocalDataSource = localDataSource,
                    holidayTransaction = transaction,
                    holidayDirtyDataSource = HolidayDirtyDataSource(),
                )

            repository.get(year = year).test {
                awaitItem() shouldBe localHolidayList.map { local -> local.toDomain() }
                awaitComplete()
            }
        }

        test("요청한 연도에 저장된 로컬 공휴일이 없으면 빈 목록을 제공한다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val localDataSource = mockk<HolidayLocalDataSource>()
            val transaction = mockk<HolidayTransaction>()
            every { localDataSource.get(year = year) } returns flowOf(emptyList())
            val repository =
                HolidayRepositoryImpl(
                    holidayRemoteDataSource = mockk(),
                    holidayLocalDataSource = localDataSource,
                    holidayTransaction = transaction,
                    holidayDirtyDataSource = HolidayDirtyDataSource(),
                )

            repository.get(year = year).test {
                awaitItem() shouldBe emptyList()
                awaitComplete()
            }
        }
    })

private class TestException(
    message: String,
) : RuntimeException(message)

private fun remoteHoliday(): HolidayRemoteEntity {
    val start = randomDate()
    return HolidayRemoteEntity(
        name = fixtureMonkey.giveMeOne(),
        isHoliday = fixtureMonkey.giveMeOne(),
        start = start,
        endInclusive = start,
    )
}

private fun localHoliday(year: Int): HolidayLocalEntity {
    val start = randomDate()

    return HolidayLocalEntity(
        year = year,
        name = fixtureMonkey.giveMeOne(),
        isHoliday = fixtureMonkey.giveMeOne(),
        start = start,
        endInclusive = start,
    )
}

private fun randomDate(): LocalDate =
    LocalDate(
        year = 2000 + (fixtureMonkey.giveMeOne<Int>().toUInt() % 100u).toInt(),
        month = 1 + (fixtureMonkey.giveMeOne<Int>().toUInt() % 12u).toInt(),
        day = 1 + (fixtureMonkey.giveMeOne<Int>().toUInt() % 28u).toInt(),
    )
