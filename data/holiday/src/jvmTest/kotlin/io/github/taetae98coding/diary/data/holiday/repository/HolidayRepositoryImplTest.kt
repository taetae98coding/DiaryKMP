package io.github.taetae98coding.diary.data.holiday.repository

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.calendar.database.api.datasource.HolidayLocalDataSource
import io.github.taetae98coding.diary.core.calendar.database.api.entity.HolidayCountryLocalEntity
import io.github.taetae98coding.diary.core.calendar.database.api.entity.HolidayLocalEntity
import io.github.taetae98coding.diary.core.calendar.database.api.transaction.HolidayTransaction
import io.github.taetae98coding.diary.core.calendar.network.api.datasource.HolidayRemoteDataSource
import io.github.taetae98coding.diary.core.calendar.network.api.entity.HolidayCountryRemoteEntity
import io.github.taetae98coding.diary.core.calendar.network.api.entity.HolidayRemoteEntity
import io.github.taetae98coding.diary.core.model.holiday.HolidayCountry
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
            val expectedHolidayList = remoteHolidayList.map { remote -> remote.toLocal(country = HolidayCountry.KOREA, year = year) }
            val remoteDataSource = mockk<HolidayRemoteDataSource>()
            coEvery { remoteDataSource.get(country = HolidayCountryRemoteEntity.KOREA, year = year) } returns remoteHolidayList
            val localDataSource = mockk<HolidayLocalDataSource>()
            val transaction = mockk<HolidayTransaction>()
            coEvery {
                transaction.upsert(
                    country = HolidayCountryLocalEntity.KOREA,
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

            repository.fetch(country = HolidayCountry.KOREA, year = year) shouldBe expectedHolidayList.map { local -> local.toDomain() }

            coVerify(exactly = 1) { remoteDataSource.get(country = HolidayCountryRemoteEntity.KOREA, year = year) }
            coVerify(exactly = 1) {
                transaction.upsert(
                    country = HolidayCountryLocalEntity.KOREA,
                    year = year,
                    holidayList = expectedHolidayList,
                )
            }
            coVerify(exactly = 0) {
                transaction.upsert(
                    country = any(),
                    year = match { requestedYear -> requestedYear != year },
                    holidayList = any(),
                )
            }
        }

        test("TC-HOLIDAY-FETCH-DATA-002 원격이 공휴일을 제공하지 않으면 요청한 연도의 캐시를 제거한다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val remoteDataSource = mockk<HolidayRemoteDataSource>()
            coEvery { remoteDataSource.get(country = HolidayCountryRemoteEntity.KOREA, year = year) } returns emptyList()
            val localDataSource = mockk<HolidayLocalDataSource>()
            val transaction = mockk<HolidayTransaction>()
            coEvery { transaction.upsert(country = HolidayCountryLocalEntity.KOREA, year = year, holidayList = emptyList()) } just Runs
            val repository =
                HolidayRepositoryImpl(
                    holidayRemoteDataSource = remoteDataSource,
                    holidayLocalDataSource = localDataSource,
                    holidayTransaction = transaction,
                    holidayDirtyDataSource = HolidayDirtyDataSource(),
                )

            repository.fetch(country = HolidayCountry.KOREA, year = year) shouldBe emptyList()

            coVerify(exactly = 1) { transaction.upsert(country = HolidayCountryLocalEntity.KOREA, year = year, holidayList = emptyList()) }
        }

        test("TC-HOLIDAY-FETCH-DATA-009 공휴일을 제공하지 않은 연도도 다시 요청하면 원격 조회를 생략한다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val remoteDataSource = mockk<HolidayRemoteDataSource>()
            coEvery { remoteDataSource.get(country = HolidayCountryRemoteEntity.KOREA, year = year) } returns emptyList()
            val localDataSource = mockk<HolidayLocalDataSource>()
            every { localDataSource.get(countrySet = setOf(HolidayCountryLocalEntity.KOREA), year = year) } returns flowOf(emptyList())
            val transaction = mockk<HolidayTransaction>()
            coEvery { transaction.upsert(country = HolidayCountryLocalEntity.KOREA, year = year, holidayList = emptyList()) } just Runs
            val repository =
                HolidayRepositoryImpl(
                    holidayRemoteDataSource = remoteDataSource,
                    holidayLocalDataSource = localDataSource,
                    holidayTransaction = transaction,
                    holidayDirtyDataSource = HolidayDirtyDataSource(),
                )

            repository.fetch(country = HolidayCountry.KOREA, year = year)
            repository.fetch(country = HolidayCountry.KOREA, year = year) shouldBe emptyList()

            coVerify(exactly = 1) { remoteDataSource.get(country = HolidayCountryRemoteEntity.KOREA, year = year) }
            coVerify(exactly = 1) { transaction.upsert(country = HolidayCountryLocalEntity.KOREA, year = year, holidayList = emptyList()) }
        }

        test("TC-HOLIDAY-FETCH-DATA-010 재조회를 생략한 연도는 로컬 캐시에 저장된 공휴일을 결과로 제공한다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val remoteHolidayList = listOf(remoteHoliday())
            val localHolidayList = listOf(localHoliday(year = year), localHoliday(year = year))
            val remoteDataSource = mockk<HolidayRemoteDataSource>()
            coEvery { remoteDataSource.get(country = HolidayCountryRemoteEntity.KOREA, year = year) } returns remoteHolidayList
            val localDataSource = mockk<HolidayLocalDataSource>()
            every { localDataSource.get(countrySet = setOf(HolidayCountryLocalEntity.KOREA), year = year) } returns flowOf(localHolidayList)
            val transaction = mockk<HolidayTransaction>()
            coEvery { transaction.upsert(country = HolidayCountryLocalEntity.KOREA, year = year, holidayList = any()) } just Runs
            val repository =
                HolidayRepositoryImpl(
                    holidayRemoteDataSource = remoteDataSource,
                    holidayLocalDataSource = localDataSource,
                    holidayTransaction = transaction,
                    holidayDirtyDataSource = HolidayDirtyDataSource(),
                )

            repository.fetch(country = HolidayCountry.KOREA, year = year)

            repository.fetch(country = HolidayCountry.KOREA, year = year) shouldBe localHolidayList.map { local -> local.toDomain() }
            coVerify(exactly = 1) { remoteDataSource.get(country = HolidayCountryRemoteEntity.KOREA, year = year) }
        }

        test("TC-HOLIDAY-FETCH-DATA-003 원격 조회가 실패하면 기존 캐시를 유지한다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val failure = TestException(fixtureMonkey.giveMeOne())
            val remoteDataSource = mockk<HolidayRemoteDataSource>()
            coEvery { remoteDataSource.get(country = HolidayCountryRemoteEntity.KOREA, year = year) } throws failure
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
                    repository.fetch(country = HolidayCountry.KOREA, year = year)
                }

            actual shouldBeSameInstanceAs failure
            coVerify(exactly = 0) { transaction.upsert(country = any(), year = any(), holidayList = any()) }
        }

        test("TC-HOLIDAY-FETCH-DATA-004 로컬 캐시 교체가 실패하면 기존 캐시를 유지한다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val remoteHolidayList = listOf(remoteHoliday())
            val expectedHolidayList = remoteHolidayList.map { remote -> remote.toLocal(country = HolidayCountry.KOREA, year = year) }
            val failure = TestException(fixtureMonkey.giveMeOne())
            val remoteDataSource = mockk<HolidayRemoteDataSource>()
            coEvery { remoteDataSource.get(country = HolidayCountryRemoteEntity.KOREA, year = year) } returns remoteHolidayList
            val localDataSource = mockk<HolidayLocalDataSource>()
            val transaction = mockk<HolidayTransaction>()
            coEvery {
                transaction.upsert(
                    country = HolidayCountryLocalEntity.KOREA,
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
                    repository.fetch(country = HolidayCountry.KOREA, year = year)
                }

            actual shouldBeSameInstanceAs failure
        }

        test("TC-HOLIDAY-FETCH-DATA-005 이미 동기화에 성공한 연도는 다시 원격 조회하지 않는다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val remoteHolidayList = listOf(remoteHoliday(), remoteHoliday())
            val expectedHolidayList = remoteHolidayList.map { remote -> remote.toLocal(country = HolidayCountry.KOREA, year = year) }
            val remoteDataSource = mockk<HolidayRemoteDataSource>()
            coEvery { remoteDataSource.get(country = HolidayCountryRemoteEntity.KOREA, year = year) } returns remoteHolidayList
            val localDataSource = mockk<HolidayLocalDataSource>()
            every { localDataSource.get(countrySet = setOf(HolidayCountryLocalEntity.KOREA), year = year) } returns flowOf(expectedHolidayList)
            val transaction = mockk<HolidayTransaction>()
            coEvery {
                transaction.upsert(
                    country = HolidayCountryLocalEntity.KOREA,
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

            repository.fetch(country = HolidayCountry.KOREA, year = year)
            repository.fetch(country = HolidayCountry.KOREA, year = year)

            coVerify(exactly = 1) { remoteDataSource.get(country = HolidayCountryRemoteEntity.KOREA, year = year) }
            coVerify(exactly = 1) {
                transaction.upsert(
                    country = HolidayCountryLocalEntity.KOREA,
                    year = year,
                    holidayList = expectedHolidayList,
                )
            }
        }

        test("TC-HOLIDAY-FETCH-DATA-006 원격 조회 실패 후 다시 요청하면 다시 원격 조회한다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val remoteHolidayList = listOf(remoteHoliday())
            val expectedHolidayList = remoteHolidayList.map { remote -> remote.toLocal(country = HolidayCountry.KOREA, year = year) }
            val failure = TestException(fixtureMonkey.giveMeOne())
            val remoteDataSource = mockk<HolidayRemoteDataSource>()
            coEvery { remoteDataSource.get(country = HolidayCountryRemoteEntity.KOREA, year = year) } throws failure andThen remoteHolidayList
            val localDataSource = mockk<HolidayLocalDataSource>()
            val transaction = mockk<HolidayTransaction>()
            coEvery {
                transaction.upsert(
                    country = HolidayCountryLocalEntity.KOREA,
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
                repository.fetch(country = HolidayCountry.KOREA, year = year)
            }
            repository.fetch(country = HolidayCountry.KOREA, year = year)

            coVerify(exactly = 2) { remoteDataSource.get(country = HolidayCountryRemoteEntity.KOREA, year = year) }
            coVerify(exactly = 1) {
                transaction.upsert(
                    country = HolidayCountryLocalEntity.KOREA,
                    year = year,
                    holidayList = expectedHolidayList,
                )
            }
        }

        test("TC-HOLIDAY-FETCH-DATA-006 로컬 캐시 교체 실패 후 다시 요청하면 다시 원격 조회한다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val remoteHolidayList = listOf(remoteHoliday())
            val expectedHolidayList = remoteHolidayList.map { remote -> remote.toLocal(country = HolidayCountry.KOREA, year = year) }
            val failure = TestException(fixtureMonkey.giveMeOne())
            val remoteDataSource = mockk<HolidayRemoteDataSource>()
            coEvery { remoteDataSource.get(country = HolidayCountryRemoteEntity.KOREA, year = year) } returns remoteHolidayList
            val localDataSource = mockk<HolidayLocalDataSource>()
            val transaction = mockk<HolidayTransaction>()
            coEvery {
                transaction.upsert(
                    country = HolidayCountryLocalEntity.KOREA,
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
                repository.fetch(country = HolidayCountry.KOREA, year = year)
            }
            repository.fetch(country = HolidayCountry.KOREA, year = year)

            coVerify(exactly = 2) { remoteDataSource.get(country = HolidayCountryRemoteEntity.KOREA, year = year) }
            coVerify(exactly = 2) {
                transaction.upsert(
                    country = HolidayCountryLocalEntity.KOREA,
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
            coEvery { remoteDataSource.get(country = any(), year = any()) } returns remoteHolidayList
            val localDataSource = mockk<HolidayLocalDataSource>()
            val transaction = mockk<HolidayTransaction>()
            coEvery { transaction.upsert(country = any(), year = any(), holidayList = any()) } just Runs
            val repository =
                HolidayRepositoryImpl(
                    holidayRemoteDataSource = remoteDataSource,
                    holidayLocalDataSource = localDataSource,
                    holidayTransaction = transaction,
                    holidayDirtyDataSource = HolidayDirtyDataSource(),
                )

            repository.fetch(country = HolidayCountry.KOREA, year = year)
            repository.fetch(country = HolidayCountry.KOREA, year = otherYear)

            coVerify(exactly = 1) { remoteDataSource.get(country = HolidayCountryRemoteEntity.KOREA, year = otherYear) }
            coVerify(exactly = 1) {
                transaction.upsert(
                    country = HolidayCountryLocalEntity.KOREA,
                    year = otherYear,
                    holidayList = remoteHolidayList.map { remote -> remote.toLocal(country = HolidayCountry.KOREA, year = otherYear) },
                )
            }
        }

        test("동기화 이력을 공유하면 다른 요청 경로에서도 성공한 연도를 다시 원격 조회하지 않는다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val remoteHolidayList = listOf(remoteHoliday())
            val remoteDataSource = mockk<HolidayRemoteDataSource>()
            coEvery { remoteDataSource.get(country = HolidayCountryRemoteEntity.KOREA, year = year) } returns remoteHolidayList
            val localDataSource = mockk<HolidayLocalDataSource>()
            every { localDataSource.get(countrySet = setOf(HolidayCountryLocalEntity.KOREA), year = year) } returns flowOf(remoteHolidayList.map { remote -> remote.toLocal(country = HolidayCountry.KOREA, year = year) })
            val transaction = mockk<HolidayTransaction>()
            coEvery { transaction.upsert(country = HolidayCountryLocalEntity.KOREA, year = year, holidayList = any()) } just Runs
            val dirtyDataSource = HolidayDirtyDataSource()

            HolidayRepositoryImpl(
                holidayRemoteDataSource = remoteDataSource,
                holidayLocalDataSource = localDataSource,
                holidayTransaction = transaction,
                holidayDirtyDataSource = dirtyDataSource,
            ).fetch(country = HolidayCountry.KOREA, year = year)
            HolidayRepositoryImpl(
                holidayRemoteDataSource = remoteDataSource,
                holidayLocalDataSource = localDataSource,
                holidayTransaction = transaction,
                holidayDirtyDataSource = dirtyDataSource,
            ).fetch(country = HolidayCountry.KOREA, year = year)

            coVerify(exactly = 1) { remoteDataSource.get(country = HolidayCountryRemoteEntity.KOREA, year = year) }
        }

        test("전체 로컬 공휴일을 순서대로 도메인 공휴일로 제공한다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val otherYear = generateSequence { fixtureMonkey.giveMeOne<Int>() }.first { candidate -> candidate != year }
            val localHolidayList = listOf(localHoliday(year = otherYear), localHoliday(year = year))
            val localDataSource = mockk<HolidayLocalDataSource>()
            every { localDataSource.get(countrySet = setOf(HolidayCountryLocalEntity.KOREA)) } returns flowOf(localHolidayList)
            val repository =
                HolidayRepositoryImpl(
                    holidayRemoteDataSource = mockk(),
                    holidayLocalDataSource = localDataSource,
                    holidayTransaction = mockk(),
                    holidayDirtyDataSource = HolidayDirtyDataSource(),
                )

            repository.get(countrySet = setOf(HolidayCountry.KOREA)).test {
                awaitItem() shouldBe localHolidayList.map { local -> local.toDomain() }
                awaitComplete()
            }
        }

        test("전체 로컬 공휴일 조회 오류를 동일한 원인으로 전파한다") {
            val failure = TestException(fixtureMonkey.giveMeOne())
            val localDataSource = mockk<HolidayLocalDataSource>()
            every { localDataSource.get(countrySet = setOf(HolidayCountryLocalEntity.KOREA)) } returns flow { throw failure }
            val repository =
                HolidayRepositoryImpl(
                    holidayRemoteDataSource = mockk(),
                    holidayLocalDataSource = localDataSource,
                    holidayTransaction = mockk(),
                    holidayDirtyDataSource = HolidayDirtyDataSource(),
                )

            repository.get(countrySet = setOf(HolidayCountry.KOREA)).test {
                awaitError() shouldBeSameInstanceAs failure
            }
        }

        test("요청한 연도의 로컬 공휴일을 도메인 공휴일로 제공한다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val localHolidayList = listOf(localHoliday(year = year), localHoliday(year = year))
            val localDataSource = mockk<HolidayLocalDataSource>()
            val transaction = mockk<HolidayTransaction>()
            every { localDataSource.get(countrySet = setOf(HolidayCountryLocalEntity.KOREA), year = year) } returns flowOf(localHolidayList)
            val repository =
                HolidayRepositoryImpl(
                    holidayRemoteDataSource = mockk(),
                    holidayLocalDataSource = localDataSource,
                    holidayTransaction = transaction,
                    holidayDirtyDataSource = HolidayDirtyDataSource(),
                )

            repository.get(countrySet = setOf(HolidayCountry.KOREA), year = year).test {
                awaitItem() shouldBe localHolidayList.map { local -> local.toDomain() }
                awaitComplete()
            }
        }

        test("요청한 연도에 저장된 로컬 공휴일이 없으면 빈 목록을 제공한다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val localDataSource = mockk<HolidayLocalDataSource>()
            val transaction = mockk<HolidayTransaction>()
            every { localDataSource.get(countrySet = setOf(HolidayCountryLocalEntity.KOREA), year = year) } returns flowOf(emptyList())
            val repository =
                HolidayRepositoryImpl(
                    holidayRemoteDataSource = mockk(),
                    holidayLocalDataSource = localDataSource,
                    holidayTransaction = transaction,
                    holidayDirtyDataSource = HolidayDirtyDataSource(),
                )

            repository.get(countrySet = setOf(HolidayCountry.KOREA), year = year).test {
                awaitItem() shouldBe emptyList()
                awaitComplete()
            }
        }
        test("TC-HOLIDAY-FETCH-DATA-011 요청한 국가와 연도의 공휴일을 원격에 요청한다") {
            mapOf(
                HolidayCountry.KOREA to HolidayCountryRemoteEntity.KOREA,
                HolidayCountry.UNITED_STATES to HolidayCountryRemoteEntity.UNITED_STATES,
            ).forEach { (country, remoteCountry) ->
                val year = fixtureMonkey.giveMeOne<Int>()
                val remoteDataSource = mockk<HolidayRemoteDataSource>()
                coEvery { remoteDataSource.get(country = any(), year = any()) } returns emptyList()
                val transaction = mockk<HolidayTransaction>()
                coEvery { transaction.upsert(country = any(), year = any(), holidayList = any()) } just Runs
                val repository =
                    HolidayRepositoryImpl(
                        holidayRemoteDataSource = remoteDataSource,
                        holidayLocalDataSource = mockk(),
                        holidayTransaction = transaction,
                        holidayDirtyDataSource = HolidayDirtyDataSource(),
                    )

                repository.fetch(country = country, year = year)

                coVerify(exactly = 1) { remoteDataSource.get(country = remoteCountry, year = year) }
                coVerify(exactly = 1) { remoteDataSource.get(country = any(), year = any()) }
            }
        }

        test("TC-HOLIDAY-FETCH-DATA-012 한 국가를 교체해도 같은 연도의 다른 국가 캐시는 유지한다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val remoteHolidayList = listOf(remoteHoliday(), remoteHoliday())
            val expectedHolidayList = remoteHolidayList.map { remote -> remote.toLocal(country = HolidayCountry.KOREA, year = year) }
            val remoteDataSource = mockk<HolidayRemoteDataSource>()
            coEvery { remoteDataSource.get(country = HolidayCountryRemoteEntity.KOREA, year = year) } returns remoteHolidayList
            val transaction = mockk<HolidayTransaction>()
            coEvery { transaction.upsert(country = any(), year = any(), holidayList = any()) } just Runs
            val repository =
                HolidayRepositoryImpl(
                    holidayRemoteDataSource = remoteDataSource,
                    holidayLocalDataSource = mockk(),
                    holidayTransaction = transaction,
                    holidayDirtyDataSource = HolidayDirtyDataSource(),
                )

            repository.fetch(country = HolidayCountry.KOREA, year = year)

            coVerify(exactly = 1) {
                transaction.upsert(
                    country = HolidayCountryLocalEntity.KOREA,
                    year = year,
                    holidayList = expectedHolidayList,
                )
            }
            coVerify(exactly = 0) {
                transaction.upsert(
                    country = HolidayCountryLocalEntity.UNITED_STATES,
                    year = any(),
                    holidayList = any(),
                )
            }
        }

        test("TC-HOLIDAY-FETCH-DATA-013 한 국가의 동기화 이력은 같은 연도의 다른 국가 동기화를 막지 않는다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val remoteHolidayList = listOf(remoteHoliday())
            val remoteDataSource = mockk<HolidayRemoteDataSource>()
            coEvery { remoteDataSource.get(country = any(), year = year) } returns remoteHolidayList
            val transaction = mockk<HolidayTransaction>()
            coEvery { transaction.upsert(country = any(), year = any(), holidayList = any()) } just Runs
            val repository =
                HolidayRepositoryImpl(
                    holidayRemoteDataSource = remoteDataSource,
                    holidayLocalDataSource = mockk(),
                    holidayTransaction = transaction,
                    holidayDirtyDataSource = HolidayDirtyDataSource(),
                )

            repository.fetch(country = HolidayCountry.KOREA, year = year)
            repository.fetch(country = HolidayCountry.UNITED_STATES, year = year) shouldBe
                remoteHolidayList.map { remote -> remote.toLocal(country = HolidayCountry.UNITED_STATES, year = year).toDomain() }

            coVerify(exactly = 1) { remoteDataSource.get(country = HolidayCountryRemoteEntity.UNITED_STATES, year = year) }
            coVerify(exactly = 1) {
                transaction.upsert(
                    country = HolidayCountryLocalEntity.UNITED_STATES,
                    year = year,
                    holidayList = remoteHolidayList.map { remote -> remote.toLocal(country = HolidayCountry.UNITED_STATES, year = year) },
                )
            }
        }

        test("요청한 국가 집합을 로컬 국가 집합으로 바꿔 조회한다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val localHolidayList =
                listOf(
                    localHoliday(year = year, country = HolidayCountryLocalEntity.KOREA),
                    localHoliday(year = year, country = HolidayCountryLocalEntity.UNITED_STATES),
                )
            val localCountrySet = setOf(HolidayCountryLocalEntity.KOREA, HolidayCountryLocalEntity.UNITED_STATES)
            val localDataSource = mockk<HolidayLocalDataSource>()
            every { localDataSource.get(countrySet = localCountrySet, year = year) } returns flowOf(localHolidayList)
            every { localDataSource.get(countrySet = localCountrySet) } returns flowOf(localHolidayList)
            val repository =
                HolidayRepositoryImpl(
                    holidayRemoteDataSource = mockk(),
                    holidayLocalDataSource = localDataSource,
                    holidayTransaction = mockk(),
                    holidayDirtyDataSource = HolidayDirtyDataSource(),
                )
            val countrySet = setOf(HolidayCountry.KOREA, HolidayCountry.UNITED_STATES)

            repository.get(countrySet = countrySet, year = year).test {
                awaitItem() shouldBe localHolidayList.map { local -> local.toDomain() }
                awaitComplete()
            }
            repository.get(countrySet = countrySet).test {
                awaitItem() shouldBe localHolidayList.map { local -> local.toDomain() }
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

private fun localHoliday(
    year: Int,
    country: HolidayCountryLocalEntity = HolidayCountryLocalEntity.KOREA,
): HolidayLocalEntity {
    val start = randomDate()

    return HolidayLocalEntity(
        country = country,
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
