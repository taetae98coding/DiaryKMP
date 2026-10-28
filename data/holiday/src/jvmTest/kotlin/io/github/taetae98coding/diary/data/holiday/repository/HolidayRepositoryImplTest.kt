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
import io.github.taetae98coding.diary.data.holiday.cache.HolidayFetchedKeySet
import io.github.taetae98coding.diary.data.holiday.mapper.toDomain
import io.github.taetae98coding.diary.data.holiday.mapper.toLocal
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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class HolidayRepositoryImplTest :
    FunSpec({
        test("TC-596be503-c5c1-4e43-833c-3b2b695cc42a 원격 공휴일로 요청한 연도의 캐시를 교체한다") {
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
                    holidayFetchedKeySet = HolidayFetchedKeySet(),
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

        test("TC-60c83dda-3b12-42e2-99a9-218819e7bc94 원격이 공휴일을 제공하지 않으면 요청한 연도의 캐시를 제거한다") {
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
                    holidayFetchedKeySet = HolidayFetchedKeySet(),
                )

            repository.fetch(country = HolidayCountry.KOREA, year = year) shouldBe emptyList()

            coVerify(exactly = 1) { transaction.upsert(country = HolidayCountryLocalEntity.KOREA, year = year, holidayList = emptyList()) }
        }

        test("TC-228f1b11-042c-4817-bac9-89bda80056fa 공휴일을 제공하지 않은 연도도 다시 요청하면 원격 조회를 생략한다") {
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
                    holidayFetchedKeySet = HolidayFetchedKeySet(),
                )

            repository.fetch(country = HolidayCountry.KOREA, year = year)
            repository.fetch(country = HolidayCountry.KOREA, year = year) shouldBe emptyList()

            coVerify(exactly = 1) { remoteDataSource.get(country = HolidayCountryRemoteEntity.KOREA, year = year) }
            coVerify(exactly = 1) { transaction.upsert(country = HolidayCountryLocalEntity.KOREA, year = year, holidayList = emptyList()) }
        }

        test("TC-9020b622-07c7-43c0-a4e5-b86767c3a41a 재조회를 생략한 연도는 로컬 캐시에 저장된 공휴일을 결과로 제공한다") {
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
                    holidayFetchedKeySet = HolidayFetchedKeySet(),
                )

            repository.fetch(country = HolidayCountry.KOREA, year = year)

            repository.fetch(country = HolidayCountry.KOREA, year = year) shouldBe localHolidayList.map { local -> local.toDomain() }
            coVerify(exactly = 1) { remoteDataSource.get(country = HolidayCountryRemoteEntity.KOREA, year = year) }
        }

        test("TC-fbbbe2cd-e57a-48ec-9679-fce9280196a8 원격 조회가 실패하면 기존 캐시를 유지한다") {
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
                    holidayFetchedKeySet = HolidayFetchedKeySet(),
                )

            val actual =
                shouldThrowExactly<TestException> {
                    repository.fetch(country = HolidayCountry.KOREA, year = year)
                }

            actual shouldBeSameInstanceAs failure
            coVerify(exactly = 0) { transaction.upsert(country = any(), year = any(), holidayList = any()) }
        }

        test("TC-9a6535b9-36fd-4fc3-9b63-3aa541fe2e08 기기 저장이 실패하면 동기화가 실패로 끝난다") {
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
                    holidayFetchedKeySet = HolidayFetchedKeySet(),
                )

            val actual =
                shouldThrowExactly<TestException> {
                    repository.fetch(country = HolidayCountry.KOREA, year = year)
                }

            actual shouldBeSameInstanceAs failure
        }

        test("TC-6e944960-6e21-4bb6-a0b6-ad8c1b8c8bcb 이미 동기화에 성공한 연도는 다시 원격 조회하지 않는다") {
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
                    holidayFetchedKeySet = HolidayFetchedKeySet(),
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

        test("TC-41421bc4-3c43-483f-9ae3-cf4d82d6c1bd 앱 프로세스를 새로 시작하면 성공했던 연도도 다시 원격 조회한다") {
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
            val previousRepository =
                HolidayRepositoryImpl(
                    holidayRemoteDataSource = remoteDataSource,
                    holidayLocalDataSource = localDataSource,
                    holidayTransaction = transaction,
                    holidayFetchedKeySet = HolidayFetchedKeySet(),
                )
            previousRepository.fetch(country = HolidayCountry.KOREA, year = year)

            // 저장소를 새로 만들어 빈 동기화 이력으로 시작하는 새 프로세스를 흉내 낸다.
            val repository =
                HolidayRepositoryImpl(
                    holidayRemoteDataSource = remoteDataSource,
                    holidayLocalDataSource = localDataSource,
                    holidayTransaction = transaction,
                    holidayFetchedKeySet = HolidayFetchedKeySet(),
                )
            repository.fetch(country = HolidayCountry.KOREA, year = year)

            coVerify(exactly = 2) { remoteDataSource.get(country = HolidayCountryRemoteEntity.KOREA, year = year) }
        }

        test("TC-944f146e-ab41-4e58-854e-d864d70267da 원격 조회 실패 후 다시 요청하면 다시 원격 조회한다") {
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
                    holidayFetchedKeySet = HolidayFetchedKeySet(),
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

        test("TC-944f146e-ab41-4e58-854e-d864d70267da 로컬 캐시 교체 실패 후 다시 요청하면 다시 원격 조회한다") {
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
                    holidayFetchedKeySet = HolidayFetchedKeySet(),
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

        test("TC-0f3cd63d-cdca-4c37-b0bf-8db797ea540c 한 연도의 동기화 이력은 다른 연도의 동기화를 막지 않는다") {
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
                    holidayFetchedKeySet = HolidayFetchedKeySet(),
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
            val fetchedKeySet = HolidayFetchedKeySet()

            HolidayRepositoryImpl(
                holidayRemoteDataSource = remoteDataSource,
                holidayLocalDataSource = localDataSource,
                holidayTransaction = transaction,
                holidayFetchedKeySet = fetchedKeySet,
            ).fetch(country = HolidayCountry.KOREA, year = year)
            HolidayRepositoryImpl(
                holidayRemoteDataSource = remoteDataSource,
                holidayLocalDataSource = localDataSource,
                holidayTransaction = transaction,
                holidayFetchedKeySet = fetchedKeySet,
            ).fetch(country = HolidayCountry.KOREA, year = year)

            coVerify(exactly = 1) { remoteDataSource.get(country = HolidayCountryRemoteEntity.KOREA, year = year) }
        }

        test("TC-0da603fd-884c-4461-8ee5-ea53853d0af6 같은 연도의 동기화가 진행 중이면 새로 원격 조회하지 않고 그 결과를 함께 기다린다") {
            runTest {
                val year = fixtureMonkey.giveMeOne<Int>()
                val remoteHolidayList = listOf(remoteHoliday())
                val localHolidayList = remoteHolidayList.map { remote -> remote.toLocal(country = HolidayCountry.KOREA, year = year) }
                val response = CompletableDeferred<List<HolidayRemoteEntity>>()
                val remoteDataSource = mockk<HolidayRemoteDataSource>()
                coEvery { remoteDataSource.get(country = HolidayCountryRemoteEntity.KOREA, year = year) } coAnswers { response.await() }
                val localDataSource = mockk<HolidayLocalDataSource>()
                every { localDataSource.get(countrySet = setOf(HolidayCountryLocalEntity.KOREA), year = year) } returns flowOf(localHolidayList)
                val transaction = mockk<HolidayTransaction>()
                coEvery { transaction.upsert(country = HolidayCountryLocalEntity.KOREA, year = year, holidayList = localHolidayList) } just Runs
                val repository =
                    HolidayRepositoryImpl(
                        holidayRemoteDataSource = remoteDataSource,
                        holidayLocalDataSource = localDataSource,
                        holidayTransaction = transaction,
                        holidayFetchedKeySet = HolidayFetchedKeySet(),
                    )

                val first = async { repository.fetch(country = HolidayCountry.KOREA, year = year) }
                runCurrent()
                val second = async { repository.fetch(country = HolidayCountry.KOREA, year = year) }
                runCurrent()

                second.isCompleted shouldBe false

                response.complete(remoteHolidayList)
                first.await()

                second.await() shouldBe localHolidayList.map { local -> local.toDomain() }
                coVerify(exactly = 1) { remoteDataSource.get(country = HolidayCountryRemoteEntity.KOREA, year = year) }
            }
        }

        test("TC-9a8e88b8-2229-4368-8555-dbe88ee8678d 진행 중이던 동기화가 실패하면 함께 기다린 요청이 다시 원격 조회한다") {
            runTest {
                val year = fixtureMonkey.giveMeOne<Int>()
                val remoteHolidayList = listOf(remoteHoliday())
                val localHolidayList = remoteHolidayList.map { remote -> remote.toLocal(country = HolidayCountry.KOREA, year = year) }
                val response = CompletableDeferred<List<HolidayRemoteEntity>>()
                val remoteDataSource = mockk<HolidayRemoteDataSource>()
                coEvery { remoteDataSource.get(country = HolidayCountryRemoteEntity.KOREA, year = year) } coAnswers { response.await() } andThen remoteHolidayList
                val transaction = mockk<HolidayTransaction>()
                coEvery { transaction.upsert(country = HolidayCountryLocalEntity.KOREA, year = year, holidayList = localHolidayList) } just Runs
                val repository =
                    HolidayRepositoryImpl(
                        holidayRemoteDataSource = remoteDataSource,
                        holidayLocalDataSource = mockk(),
                        holidayTransaction = transaction,
                        holidayFetchedKeySet = HolidayFetchedKeySet(),
                    )

                val first = async { runCatching { repository.fetch(country = HolidayCountry.KOREA, year = year) } }
                runCurrent()
                val second = async { repository.fetch(country = HolidayCountry.KOREA, year = year) }
                runCurrent()

                response.completeExceptionally(IllegalStateException(fixtureMonkey.giveMeOne<String>()))

                first.await().shouldBeFailure<IllegalStateException>()
                second.await() shouldBe localHolidayList.map { local -> local.toDomain() }
                coVerify(exactly = 2) { remoteDataSource.get(country = HolidayCountryRemoteEntity.KOREA, year = year) }
            }
        }

        test("TC-d5cb9bf4-52c0-4c6c-952e-fe28bd4a1db7 다른 연도의 동기화는 진행 중인 동기화를 기다리지 않는다") {
            runTest {
                val year = fixtureMonkey.giveMeOne<Int>()
                val otherYear = generateSequence { fixtureMonkey.giveMeOne<Int>() }.first { candidate -> candidate != year }
                val remoteHolidayList = listOf(remoteHoliday())
                val response = CompletableDeferred<List<HolidayRemoteEntity>>()
                val remoteDataSource = mockk<HolidayRemoteDataSource>()
                coEvery { remoteDataSource.get(country = HolidayCountryRemoteEntity.KOREA, year = year) } coAnswers { response.await() }
                coEvery { remoteDataSource.get(country = HolidayCountryRemoteEntity.KOREA, year = otherYear) } returns remoteHolidayList
                val transaction = mockk<HolidayTransaction>()
                coEvery { transaction.upsert(country = any(), year = any(), holidayList = any()) } just Runs
                val repository =
                    HolidayRepositoryImpl(
                        holidayRemoteDataSource = remoteDataSource,
                        holidayLocalDataSource = mockk(),
                        holidayTransaction = transaction,
                        holidayFetchedKeySet = HolidayFetchedKeySet(),
                    )

                val first = async { repository.fetch(country = HolidayCountry.KOREA, year = year) }
                runCurrent()

                repository.fetch(country = HolidayCountry.KOREA, year = otherYear) shouldBe
                    remoteHolidayList.map { remote -> remote.toLocal(country = HolidayCountry.KOREA, year = otherYear).toDomain() }
                first.isCompleted shouldBe false

                response.complete(emptyList())
                first.await()
            }
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
                    holidayFetchedKeySet = HolidayFetchedKeySet(),
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
                    holidayFetchedKeySet = HolidayFetchedKeySet(),
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
                    holidayFetchedKeySet = HolidayFetchedKeySet(),
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
                    holidayFetchedKeySet = HolidayFetchedKeySet(),
                )

            repository.get(countrySet = setOf(HolidayCountry.KOREA), year = year).test {
                awaitItem() shouldBe emptyList()
                awaitComplete()
            }
        }
        test("TC-c7ebdd34-cba3-4362-8c4b-3e4b55960d5a 요청한 국가와 연도의 공휴일을 원격에 요청한다") {
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
                        holidayFetchedKeySet = HolidayFetchedKeySet(),
                    )

                repository.fetch(country = country, year = year)

                coVerify(exactly = 1) { remoteDataSource.get(country = remoteCountry, year = year) }
                coVerify(exactly = 1) { remoteDataSource.get(country = any(), year = any()) }
            }
        }

        test("TC-a5369c65-33ef-4a70-89cf-d04dfccafa8c 한 국가를 교체해도 같은 연도의 다른 국가 캐시는 유지한다") {
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
                    holidayFetchedKeySet = HolidayFetchedKeySet(),
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

        test("TC-d94f0abd-05e0-4c3c-859c-3e31a0af14d3 한 국가의 동기화 이력은 같은 연도의 다른 국가 동기화를 막지 않는다") {
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
                    holidayFetchedKeySet = HolidayFetchedKeySet(),
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
                    holidayFetchedKeySet = HolidayFetchedKeySet(),
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
