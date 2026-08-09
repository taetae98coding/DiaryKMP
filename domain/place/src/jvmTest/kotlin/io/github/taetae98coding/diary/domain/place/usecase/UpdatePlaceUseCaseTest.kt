package io.github.taetae98coding.diary.domain.place.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.place.exception.PlaceCoordinateInvalidException
import io.github.taetae98coding.diary.domain.place.repository.AccountPlaceRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class UpdatePlaceUseCaseTest :
    BehaviorSpec({
        Given("현재 계정과 저장된 장소가 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val savedTitle = "saved-title-${fixtureMonkey.giveMeOne<String>()}"
            val id = Uuid.random()
            val detailSlot = slot<PlaceDetail>()
            val updatedAtSlot = slot<Instant>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val findPlaceUseCase = mockk<FindPlaceUseCase>()
            every { findPlaceUseCase(id) } returns flowOf(Result.success(place(id = id, title = savedTitle)))
            val accountPlaceRepository = mockk<AccountPlaceRepository>()
            coEvery {
                accountPlaceRepository.updateDetail(
                    account = account,
                    placeId = id,
                    detail = capture(detailSlot),
                    updatedAt = capture(updatedAtSlot),
                )
            } returns 1
            val now = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
            val clock = mockk<Clock>()
            every { clock.now() } returns now
            val requestSyncUseCase = requestSyncUseCase()
            val useCase =
                UpdatePlaceUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    findPlaceUseCase = findPlaceUseCase,
                    accountPlaceRepository = accountPlaceRepository,
                    clock = clock,
                )

            When("공백이 아닌 제목과 유효한 좌표로 수정한다") {
                Then("TC-PLACE-DETAIL-DATA-008 수정을 서버와 맞추기 위한 동기화를 요청한다") {
                    useCase(parameter = UpdatePlaceUseCase.Parameter(id = id, detail = detail())).shouldBeSuccess(1)

                    coVerify(atLeast = 1) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }

                Then("TC-PLACE-DETAIL-DOMAIN-004 입력한 내용과 수정 시각으로 수정한다") {
                    val expected = detail()

                    useCase(parameter = UpdatePlaceUseCase.Parameter(id = id, detail = expected)).shouldBeSuccess(1)

                    detailSlot.captured shouldBe expected
                    updatedAtSlot.captured shouldBe now
                }

                Then("TC-PLACE-DETAIL-DOMAIN-006 설명이 비어 있으면 빈 설명으로 수정한다") {
                    useCase(
                        parameter = UpdatePlaceUseCase.Parameter(id = id, detail = detail(description = "")),
                    ).shouldBeSuccess(1)

                    detailSlot.captured.description shouldBe ""
                }
            }

            listOf(
                "" to "빈 제목",
                "   " to "공백 문자로만 이루어진 제목",
            ).forEach { (blankTitle, label) ->
                When("$label 과 유효한 좌표로 수정한다") {
                    Then("TC-PLACE-DETAIL-DOMAIN-005 저장된 기존 제목으로 수정한다") {
                        useCase(
                            parameter = UpdatePlaceUseCase.Parameter(id = id, detail = detail(title = blankTitle)),
                        ).shouldBeSuccess(1)

                        detailSlot.captured.title shouldBe savedTitle
                    }
                }
            }

            listOf(
                Coordinate(latitude = 90.0, longitude = 180.0),
                Coordinate(latitude = -90.0, longitude = -180.0),
                Coordinate(latitude = 0.0, longitude = 0.0),
            ).forEach { coordinate ->
                When("유효 범위 경계 좌표 $coordinate 로 수정한다") {
                    Then("TC-PLACE-DETAIL-DOMAIN-008 장소를 수정한다") {
                        useCase(
                            parameter = UpdatePlaceUseCase.Parameter(id = id, detail = detail(coordinate = coordinate)),
                        ).shouldBeSuccess(1)

                        detailSlot.captured.coordinate shouldBe coordinate
                    }
                }
            }
        }

        listOf(
            Coordinate(latitude = Double.NaN, longitude = Double.NaN),
            Coordinate(latitude = Double.NaN, longitude = 127.0),
            Coordinate(latitude = 37.5, longitude = Double.NaN),
            Coordinate(latitude = Double.POSITIVE_INFINITY, longitude = 127.0),
            Coordinate(latitude = 90.000001, longitude = 127.0),
            Coordinate(latitude = -90.000001, longitude = 127.0),
            Coordinate(latitude = 37.5, longitude = 180.000001),
            Coordinate(latitude = 37.5, longitude = -180.000001),
        ).forEach { coordinate ->
            Given("유효 범위 안의 숫자가 아닌 좌표 $coordinate 로 수정하려 한다") {
                val getAccountUseCase = mockk<GetAccountUseCase>()
                val findPlaceUseCase = mockk<FindPlaceUseCase>()
                val accountPlaceRepository = mockk<AccountPlaceRepository>(relaxed = true)
                val useCase =
                    UpdatePlaceUseCase(
                        getAccountUseCase = getAccountUseCase,
                        requestSyncUseCase = requestSyncUseCase(),
                        findPlaceUseCase = findPlaceUseCase,
                        accountPlaceRepository = accountPlaceRepository,
                        clock = Clock.System,
                    )

                When("공백이 아닌 제목으로 수정한다") {
                    Then("TC-PLACE-DETAIL-DOMAIN-007 좌표 오류로 실패하고 아무 내용도 수정하지 않는다") {
                        val result =
                            useCase(
                                parameter =
                                    UpdatePlaceUseCase.Parameter(
                                        id = Uuid.random(),
                                        detail = detail(coordinate = coordinate),
                                    ),
                            )

                        result.shouldBeFailure().shouldBeInstanceOf<PlaceCoordinateInvalidException>()
                        coVerify(exactly = 0) {
                            accountPlaceRepository.updateDetail(
                                account = any(),
                                placeId = any(),
                                detail = any(),
                                updatedAt = any(),
                            )
                        }
                    }
                }

                When("제목을 비운 상태로 수정한다") {
                    Then("TC-PLACE-DETAIL-DOMAIN-009 좌표 오류를 알리고 아무 내용도 수정하지 않는다") {
                        val result =
                            useCase(
                                parameter =
                                    UpdatePlaceUseCase.Parameter(
                                        id = Uuid.random(),
                                        detail = detail(title = "", coordinate = coordinate),
                                    ),
                            )

                        result.shouldBeFailure().shouldBeInstanceOf<PlaceCoordinateInvalidException>()
                        coVerify(exactly = 0) {
                            accountPlaceRepository.updateDetail(
                                account = any(),
                                placeId = any(),
                                detail = any(),
                                updatedAt = any(),
                            )
                        }
                    }
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val findPlaceUseCase = mockk<FindPlaceUseCase>()
            val accountPlaceRepository = mockk<AccountPlaceRepository>(relaxed = true)
            val useCase =
                UpdatePlaceUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase(),
                    findPlaceUseCase = findPlaceUseCase,
                    accountPlaceRepository = accountPlaceRepository,
                    clock = Clock.System,
                )

            When("공백이 아닌 제목과 유효한 좌표로 수정한다") {
                Then("계정 조회 실패를 전달하고 아무 내용도 수정하지 않는다") {
                    val result = useCase(parameter = UpdatePlaceUseCase.Parameter(id = Uuid.random(), detail = detail()))

                    result.shouldBeFailure() shouldBeSameInstanceAs throwable
                    coVerify(exactly = 0) {
                        accountPlaceRepository.updateDetail(
                            account = any(),
                            placeId = any(),
                            detail = any(),
                            updatedAt = any(),
                        )
                    }
                }
            }
        }

        Given("장소 수정은 성공하지만 서버 반영이 실패하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val id = Uuid.random()
            val now = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
            val detailSlot = slot<PlaceDetail>()
            val updatedAtSlot = slot<Instant>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val findPlaceUseCase = mockk<FindPlaceUseCase>()
            val accountPlaceRepository = mockk<AccountPlaceRepository>()
            coEvery {
                accountPlaceRepository.updateDetail(
                    account = account,
                    placeId = id,
                    detail = capture(detailSlot),
                    updatedAt = capture(updatedAtSlot),
                )
            } returns 1
            val clock = mockk<Clock>()
            every { clock.now() } returns now
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns
                Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
            val useCase =
                UpdatePlaceUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    findPlaceUseCase = findPlaceUseCase,
                    accountPlaceRepository = accountPlaceRepository,
                    clock = clock,
                )

            When("공백이 아닌 제목과 유효한 좌표로 수정한다") {
                Then("TC-PLACE-DETAIL-DATA-010 수정은 성공으로 전달되고 기기에 반영한 내용을 되돌리지 않는다") {
                    val expected = detail()

                    useCase(parameter = UpdatePlaceUseCase.Parameter(id = id, detail = expected)).shouldBeSuccess(1)

                    detailSlot.captured shouldBe expected
                    updatedAtSlot.captured shouldBe now
                    coVerify(exactly = 1) {
                        accountPlaceRepository.updateDetail(
                            account = account,
                            placeId = id,
                            detail = any(),
                            updatedAt = any(),
                        )
                    }
                    coVerify(exactly = 0) {
                        accountPlaceRepository.updateDeleted(
                            account = any(),
                            placeId = any(),
                            isDeleted = any(),
                            updatedAt = any(),
                        )
                    }
                }
            }
        }

        Given("장소 수정 저장이 실패하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val findPlaceUseCase = mockk<FindPlaceUseCase>()
            val accountPlaceRepository = mockk<AccountPlaceRepository>()
            coEvery {
                accountPlaceRepository.updateDetail(
                    account = account,
                    placeId = any(),
                    detail = any(),
                    updatedAt = any(),
                )
            } throws throwable
            val useCase =
                UpdatePlaceUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase(),
                    findPlaceUseCase = findPlaceUseCase,
                    accountPlaceRepository = accountPlaceRepository,
                    clock = Clock.System,
                )

            When("공백이 아닌 제목과 유효한 좌표로 수정한다") {
                Then("TC-PLACE-DETAIL-DATA-005 저장 실패를 그대로 전달한다") {
                    val result = useCase(parameter = UpdatePlaceUseCase.Parameter(id = Uuid.random(), detail = detail()))

                    result.shouldBeFailure() shouldBeSameInstanceAs throwable
                }
            }
        }
    }) {
    private companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun requestSyncUseCase(): RequestSyncUseCase = mockk(relaxed = true)

        private fun detail(
            title: String = "title-${fixtureMonkey.giveMeOne<String>()}",
            description: String = fixtureMonkey.giveMeOne<String>(),
            color: Long = fixtureMonkey.giveMeOne<Long>(),
            coordinate: Coordinate = Coordinate(latitude = 37.5, longitude = 127.0),
            address: String = "address-${fixtureMonkey.giveMeOne<String>()}",
        ): PlaceDetail =
            PlaceDetail(
                title = title,
                description = description,
                color = color,
                coordinate = coordinate,
                address = address,
            )

        private fun place(
            id: Uuid,
            title: String,
        ): Place =
            Place(
                id = id,
                detail = detail(title = title),
                isDeleted = false,
                updatedAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
                createdAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
            )
    }
}
