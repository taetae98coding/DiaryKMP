package io.github.taetae98coding.diary.domain.place.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.place.exception.PlaceCoordinateInvalidException
import io.github.taetae98coding.diary.domain.place.exception.PlaceTitleBlankException
import io.github.taetae98coding.diary.domain.place.repository.AccountPlaceRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AddPlaceUseCaseTest :
    BehaviorSpec({
        listOf(
            "" to "빈 제목",
            "   " to "공백 문자로만 이루어진 제목",
        ).forEach { (blankTitle, label) ->
            Given("$label 이 입력되어 있다") {
                val getAccountUseCase = mockk<GetAccountUseCase>()
                val accountPlaceRepository = mockk<AccountPlaceRepository>(relaxed = true)
                val useCase =
                    AddPlaceUseCase(
                        getAccountUseCase = getAccountUseCase,
                        requestSyncUseCase = requestSyncUseCase(),
                        accountPlaceRepository = accountPlaceRepository,
                        clock = Clock.System,
                    )

                When("장소를 추가한다") {
                    Then("TC-77a9c001-3d4c-4ec9-9885-4253fc2aa141 제목 공백 예외로 실패하고 장소를 저장하지 않는다") {
                        val result = useCase(parameter = AddPlaceUseCase.Parameter(detail = detail(title = blankTitle), tagIdSet = emptySet()))

                        result.shouldBeFailure().shouldBeInstanceOf<PlaceTitleBlankException>()
                        verify(exactly = 0) { getAccountUseCase(parameter = Unit) }
                        coVerify(exactly = 0) { accountPlaceRepository.upsert(account = any(), place = any(), tagIdSet = any()) }
                    }

                    Then("TC-c4b0f2ff-8c5f-4401-815d-3920814a94d7 좌표도 성립하지 않으면 제목 공백 예외를 먼저 알린다") {
                        val outOfRange = Coordinate(latitude = 90.000001, longitude = 127.0)

                        val result = useCase(parameter = AddPlaceUseCase.Parameter(detail = detail(title = blankTitle, coordinate = outOfRange), tagIdSet = emptySet()))

                        result.shouldBeFailure().shouldBeInstanceOf<PlaceTitleBlankException>()
                        coVerify(exactly = 0) { accountPlaceRepository.upsert(account = any(), place = any(), tagIdSet = any()) }
                    }
                }
            }
        }

        listOf(
            Coordinate(latitude = Double.NaN, longitude = Double.NaN),
            Coordinate(latitude = Double.NaN, longitude = 127.0),
            Coordinate(latitude = 37.5, longitude = Double.NaN),
            Coordinate(latitude = Double.POSITIVE_INFINITY, longitude = 127.0),
            Coordinate(latitude = 37.5, longitude = Double.NEGATIVE_INFINITY),
            Coordinate(latitude = 90.000001, longitude = 127.0),
            Coordinate(latitude = -90.000001, longitude = 127.0),
            Coordinate(latitude = 37.5, longitude = 180.000001),
            Coordinate(latitude = 37.5, longitude = -180.000001),
        ).forEach { coordinate ->
            Given("유효 범위 안의 숫자가 아닌 좌표 $coordinate 가 입력되어 있다") {
                val getAccountUseCase = mockk<GetAccountUseCase>()
                val accountPlaceRepository = mockk<AccountPlaceRepository>(relaxed = true)
                val useCase =
                    AddPlaceUseCase(
                        getAccountUseCase = getAccountUseCase,
                        requestSyncUseCase = requestSyncUseCase(),
                        accountPlaceRepository = accountPlaceRepository,
                        clock = Clock.System,
                    )

                When("공백이 아닌 제목으로 장소를 추가한다") {
                    Then("TC-ffd25e46-ad1c-46db-97db-dbe73dd1b53b 좌표 오류로 실패하고 장소를 저장하지 않는다") {
                        val result = useCase(parameter = AddPlaceUseCase.Parameter(detail = detail(coordinate = coordinate), tagIdSet = emptySet()))

                        result.shouldBeFailure().shouldBeInstanceOf<PlaceCoordinateInvalidException>()
                        verify(exactly = 0) { getAccountUseCase(parameter = Unit) }
                        coVerify(exactly = 0) { accountPlaceRepository.upsert(account = any(), place = any(), tagIdSet = any()) }
                    }
                }
            }
        }

        Given("현재 계정이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val placeSlot = slot<Place>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountPlaceRepository = mockk<AccountPlaceRepository>()
            coEvery { accountPlaceRepository.upsert(account = account, place = capture(placeSlot), tagIdSet = any()) } just Runs
            val now = fixtureMonkey.giveMeOne<Instant>()
            val clock = mockk<Clock>()
            every { clock.now() } returns now
            val useCase =
                AddPlaceUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase(),
                    accountPlaceRepository = accountPlaceRepository,
                    clock = clock,
                )

            When("공백이 아닌 제목과 유효한 좌표로 장소를 추가한다") {
                Then("TC-71726817-3873-40c5-8081-8856b2290f1c 현재 계정과 연결된 고유한 장소로 저장한다") {
                    val firstId = useCase(parameter = AddPlaceUseCase.Parameter(detail = detail(), tagIdSet = emptySet())).shouldBeSuccess()
                    val secondId = useCase(parameter = AddPlaceUseCase.Parameter(detail = detail(), tagIdSet = emptySet())).shouldBeSuccess()

                    firstId shouldNotBe Uuid.NIL
                    secondId shouldNotBe Uuid.NIL
                    firstId shouldNotBe secondId
                    coVerify(exactly = 2) { accountPlaceRepository.upsert(account = account, place = any(), tagIdSet = any()) }
                }

                Then("TC-064634f6-5b7f-472c-86b4-753a09051180 입력한 제목, 설명, 컬러, 좌표를 그대로 저장한다") {
                    val expected = detail()

                    val result = useCase(parameter = AddPlaceUseCase.Parameter(detail = expected, tagIdSet = emptySet()))

                    result.shouldBeSuccess(placeSlot.captured.id)
                    placeSlot.captured.detail shouldBe expected
                }

                Then("TC-5a548f34-e80b-480e-a5d6-4d14e63aea09 미삭제 상태와 빈 주소, 추가 시각을 저장한다") {
                    useCase(parameter = AddPlaceUseCase.Parameter(detail = detail(address = ""), tagIdSet = emptySet())).shouldBeSuccess()

                    placeSlot.captured.isDeleted shouldBe false
                    placeSlot.captured.detail.address shouldBe ""
                    placeSlot.captured.createdAt shouldBe now
                    placeSlot.captured.updatedAt shouldBe now
                }

                Then("TC-66005473-cd1d-4382-b628-b20b1358da5c 설명과 주소가 비어 있어도 장소를 저장한다") {
                    useCase(parameter = AddPlaceUseCase.Parameter(detail = detail(description = "", address = ""), tagIdSet = emptySet())).shouldBeSuccess()

                    placeSlot.captured.detail.description shouldBe ""
                    placeSlot.captured.detail.address shouldBe ""
                }
            }

            When("연결할 태그를 골라 장소를 추가한다") {
                Then("TC-91a28ed2-a761-49ec-8fa6-a51061cfa4b2 고른 태그를 그대로 새 장소의 연결로 넘긴다") {
                    val tagIdSet = List(2) { fixtureMonkey.giveMeOne<Uuid>() }.toSet()
                    val repository = mockk<AccountPlaceRepository>(relaxed = true)
                    val taggedUseCase =
                        AddPlaceUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase(),
                            accountPlaceRepository = repository,
                            clock = clock,
                        )

                    taggedUseCase(parameter = AddPlaceUseCase.Parameter(detail = detail(), tagIdSet = tagIdSet)).shouldBeSuccess()

                    coVerify(exactly = 1) {
                        repository.upsert(account = account, place = any(), tagIdSet = tagIdSet)
                    }
                }

                Then("TC-7516bee1-7866-415e-b087-d81a5add7e76 태그를 하나도 고르지 않아도 장소를 추가하고 연결을 만들지 않는다") {
                    val repository = mockk<AccountPlaceRepository>(relaxed = true)
                    val tagLessUseCase =
                        AddPlaceUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase(),
                            accountPlaceRepository = repository,
                            clock = clock,
                        )

                    tagLessUseCase(parameter = AddPlaceUseCase.Parameter(detail = detail(), tagIdSet = emptySet())).shouldBeSuccess()

                    coVerify(exactly = 1) {
                        repository.upsert(account = account, place = any(), tagIdSet = emptySet())
                    }
                }

                Then("TC-caa32a5b-7036-4636-b1ea-40b0241735ff 태그를 연결해도 장소의 컬러는 바뀌지 않는다") {
                    val expected = detail(color = PLACE_COLOR)

                    useCase(
                        parameter =
                            AddPlaceUseCase.Parameter(
                                detail = expected,
                                tagIdSet = setOf(fixtureMonkey.giveMeOne<Uuid>()),
                            ),
                    ).shouldBeSuccess()

                    placeSlot.captured.detail.color shouldBe PLACE_COLOR
                }
            }

            listOf(
                Coordinate(latitude = 90.0, longitude = 180.0),
                Coordinate(latitude = -90.0, longitude = -180.0),
                Coordinate(latitude = 0.0, longitude = 0.0),
            ).forEach { coordinate ->
                When("유효 범위 경계 좌표 $coordinate 로 장소를 추가한다") {
                    Then("TC-6c27a194-36aa-42e3-847a-2f81f3823028 장소를 저장한다") {
                        useCase(parameter = AddPlaceUseCase.Parameter(detail = detail(coordinate = coordinate), tagIdSet = emptySet())).shouldBeSuccess()

                        placeSlot.captured.detail.coordinate shouldBe coordinate
                    }
                }
            }

            listOf(
                Coordinate(latitude = 37.1234567, longitude = 127.1234564) to Coordinate(latitude = 37.123457, longitude = 127.123456),
                Coordinate(latitude = 90.0000004, longitude = -180.0000004) to Coordinate(latitude = 90.0, longitude = -180.0),
            ).forEach { (coordinate, expected) ->
                When("소수 여섯째 자리보다 긴 좌표 $coordinate 로 장소를 추가한다") {
                    Then("TC-0fd02978-91a4-462a-9a0b-ce752e85ff1c 여섯째 자리로 반올림한 좌표로 저장한다") {
                        useCase(parameter = AddPlaceUseCase.Parameter(detail = detail(coordinate = coordinate), tagIdSet = emptySet())).shouldBeSuccess()

                        placeSlot.captured.detail.coordinate shouldBe expected
                    }
                }
            }
        }

        Given("장소를 추가할 수 있는 현재 계정이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountPlaceRepository = mockk<AccountPlaceRepository>(relaxed = true)
            val requestSyncUseCase = requestSyncUseCase()
            val useCase =
                AddPlaceUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountPlaceRepository = accountPlaceRepository,
                    clock = Clock.System,
                )

            When("장소 추가에 성공한다") {
                Then("TC-541e8b7e-a72b-4768-ae1e-8a46e49afb9c TC-90ca32d1-b285-4415-a2cf-d8baba8cc6b7 추가한 장소를 서버와 맞추기 위한 동기화를 요청한다") {
                    useCase(parameter = AddPlaceUseCase.Parameter(detail = detail(), tagIdSet = emptySet())).shouldBeSuccess()

                    coVerify(exactly = 1) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }
        }

        Given("장소 저장은 성공하지만 서버 반영이 실패하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountPlaceRepository = mockk<AccountPlaceRepository>(relaxed = true)
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns
                Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
            val useCase =
                AddPlaceUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountPlaceRepository = accountPlaceRepository,
                    clock = Clock.System,
                )

            When("공백이 아닌 제목과 유효한 좌표로 장소를 추가한다") {
                Then("TC-75ecc45b-34b8-4f8c-a2a9-bb1d501733fa 추가는 성공으로 전달되고 저장한 장소를 되돌리지 않는다") {
                    val placeSlot = slot<Place>()
                    coEvery { accountPlaceRepository.upsert(account = account, place = capture(placeSlot), tagIdSet = any()) } just Runs

                    useCase(parameter = AddPlaceUseCase.Parameter(detail = detail(), tagIdSet = emptySet())).shouldBeSuccess()

                    coVerify(exactly = 1) { accountPlaceRepository.upsert(account = account, place = any(), tagIdSet = any()) }
                    coVerify(exactly = 0) {
                        accountPlaceRepository.updateDeleted(
                            account = any(),
                            placeId = any(),
                            isDeleted = any(),
                            updatedAt = any(),
                        )
                    }
                    placeSlot.captured.isDeleted.shouldBeFalse()
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val accountPlaceRepository = mockk<AccountPlaceRepository>(relaxed = true)
            val useCase =
                AddPlaceUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase(),
                    accountPlaceRepository = accountPlaceRepository,
                    clock = Clock.System,
                )

            When("공백이 아닌 제목과 유효한 좌표로 장소를 추가한다") {
                Then("TC-94b0ff38-c433-4e83-9b99-fd3146d38db8 계정 조회 실패를 전달하고 장소를 저장하지 않는다") {
                    val result = useCase(parameter = AddPlaceUseCase.Parameter(detail = detail(), tagIdSet = emptySet()))

                    result.shouldBeFailure() shouldBeSameInstanceAs throwable
                    coVerify(exactly = 0) { accountPlaceRepository.upsert(account = any(), place = any(), tagIdSet = any()) }
                }
            }
        }

        Given("장소 저장이 실패하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountPlaceRepository = mockk<AccountPlaceRepository>()
            coEvery { accountPlaceRepository.upsert(account = account, place = any(), tagIdSet = any()) } throws throwable
            val useCase =
                AddPlaceUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase(),
                    accountPlaceRepository = accountPlaceRepository,
                    clock = Clock.System,
                )

            When("공백이 아닌 제목과 유효한 좌표로 장소를 추가한다") {
                Then("TC-1c64c262-6223-452d-b72b-6ff5c108b7a5 추가를 성공으로 다루지 않고 저장 실패를 전달한다") {
                    val result = useCase(parameter = AddPlaceUseCase.Parameter(detail = detail(), tagIdSet = emptySet()))

                    result.shouldBeFailure() shouldBeSameInstanceAs throwable
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private const val PLACE_COLOR: Long = 0xFF3A7BD5

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
    }
}
