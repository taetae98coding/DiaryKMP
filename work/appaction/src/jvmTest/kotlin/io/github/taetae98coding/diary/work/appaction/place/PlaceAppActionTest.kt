package io.github.taetae98coding.diary.work.appaction.place

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.location.CoordinateBounds
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.testing.memo.memo
import io.github.taetae98coding.diary.core.testing.place.place
import io.github.taetae98coding.diary.core.testing.place.placeDetail
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.domain.memo.usecase.PageFinishedPlaceMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PagePlaceMemoUseCase
import io.github.taetae98coding.diary.domain.place.exception.PlaceCoordinateInvalidException
import io.github.taetae98coding.diary.domain.place.usecase.AddPlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.DeletePlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.FindPlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.GetPlaceListUseCase
import io.github.taetae98coding.diary.domain.place.usecase.PagePlaceHomeUseCase
import io.github.taetae98coding.diary.domain.place.usecase.RestorePlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.UpdatePlaceUseCase
import io.github.taetae98coding.diary.domain.tag.EntityTagKind
import io.github.taetae98coding.diary.domain.tag.usecase.AddEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.GetEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RemoveEntityTagUseCase
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.work.appaction.core.AppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.AppActionDone
import io.github.taetae98coding.diary.work.appaction.core.AppActionException
import io.github.taetae98coding.diary.work.appaction.core.AppActionId
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.memo.AppActionMemo
import io.github.taetae98coding.diary.work.appaction.memo.AppActionMemoDateTime
import io.github.taetae98coding.diary.work.appaction.memo.memoListAppActionOf
import io.github.taetae98coding.diary.work.appaction.tag.AppActionEntityTagKind
import io.github.taetae98coding.diary.work.appaction.tag.AppActionTag
import io.github.taetae98coding.diary.work.appaction.tag.entityTagAppActionOf
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.numericDouble
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.uuid.Uuid

class PlaceAppActionTest :
    FunSpec({
        test("목록은 정렬을 담지 않으면 제목순으로, 담으면 그 정렬로 조회해 MCP와 같은 항목을 돌려준다") {
            runTest {
                val placeList = Arb.list(Arb.place(), 1..3).next()
                val pagePlaceHomeUseCase = mockk<PagePlaceHomeUseCase>()
                every { pagePlaceHomeUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(placeList)))
                val action = placeAppActionOf(pagePlaceHomeUseCase)

                action.list(sort = null, cursor = null) shouldBe AppActionPage(items = placeList.map { place -> place.expected() }, nextCursor = null)
                action.list(sort = "recently_updated", cursor = null).items shouldBe placeList.map { place -> place.expected() }

                verify(exactly = 1) { pagePlaceHomeUseCase(parameter = ListSort.TITLE) }
                verify(exactly = 1) { pagePlaceHomeUseCase(parameter = ListSort.RECENTLY_UPDATED) }
            }
        }

        test("목록이 제공하지 않는 정렬은 고를 수 있는 정렬을 알리며 실패한다") {
            runTest {
                val pagePlaceHomeUseCase = mockk<PagePlaceHomeUseCase>()
                val action = placeAppActionOf(pagePlaceHomeUseCase)

                shouldThrow<AppActionException.InvalidArgument> { action.list(sort = "default", cursor = null) }.message shouldBe "'sort' must be one of title, recently_updated."
                verify(exactly = 0) { pagePlaceHomeUseCase(parameter = any()) }
            }
        }

        test("목록은 100개씩 나눠 다음 페이지 위치를 알린다") {
            runTest {
                val placeList = Arb.list(Arb.place(), 101..150).next()
                val pagePlaceHomeUseCase = mockk<PagePlaceHomeUseCase>()
                every { pagePlaceHomeUseCase(parameter = any()) } answers { flowOf(Result.success(PagingData.from(placeList))) }
                val action = placeAppActionOf(pagePlaceHomeUseCase)

                val firstPage = action.list(sort = null, cursor = null)
                val secondPage = action.list(sort = null, cursor = firstPage.nextCursor)

                firstPage shouldBe AppActionPage(items = placeList.take(100).map { place -> place.expected() }, nextCursor = "100")
                secondPage shouldBe AppActionPage(items = placeList.drop(100).map { place -> place.expected() }, nextCursor = null)
            }
        }

        test("좌표가 없는 장소는 위도와 경도를 null로 돌려준다") {
            runTest {
                val place =
                    Arb
                        .place()
                        .next()
                        .copy(isDeleted = false)
                        .let { place -> place.copy(detail = place.detail.copy(coordinate = Coordinate(latitude = Double.NaN, longitude = Double.NaN))) }
                val pagePlaceHomeUseCase = mockk<PagePlaceHomeUseCase>()
                every { pagePlaceHomeUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(listOf(place))))
                val action = placeAppActionOf(pagePlaceHomeUseCase)

                val item = action.list(sort = null, cursor = null).items.single()

                item.latitude shouldBe null
                item.longitude shouldBe null
            }
        }

        test("지역 안 목록은 받은 영역과 정렬로 조회해 모두 돌려준다") {
            runTest {
                val placeList = Arb.list(Arb.place(), 0..3).next()
                val south = Arb.numericDouble(min = -90.0, max = 0.0).next().toCoordinateText()
                val north = Arb.numericDouble(min = 0.0, max = 90.0).next().toCoordinateText()
                val west = Arb.numericDouble(min = -180.0, max = 180.0).next().toCoordinateText()
                val east = Arb.numericDouble(min = -180.0, max = 180.0).next().toCoordinateText()
                val getPlaceListUseCase = mockk<GetPlaceListUseCase>()
                every { getPlaceListUseCase(parameter = any()) } returns flowOf(Result.success(placeList))
                val action = placeAppActionOf(getPlaceListUseCase)

                action.listInArea(south = south, north = north, west = west, east = east, sort = "recently_updated") shouldBe
                    placeList.map { place -> place.expected() }
                verify(exactly = 1) {
                    getPlaceListUseCase(
                        parameter = GetPlaceListUseCase.Parameter(bounds = CoordinateBounds(south = south.toDouble(), north = north.toDouble(), west = west.toDouble(), east = east.toDouble()), sort = ListSort.RECENTLY_UPDATED),
                    )
                }
            }
        }

        test("지역이 범위를 벗어나거나 북쪽이 남쪽보다 아래면 앱 기능을 실행하지 않고 MCP와 같은 메시지로 실패한다") {
            runTest {
                val getPlaceListUseCase = mockk<GetPlaceListUseCase>()
                val action = placeAppActionOf(getPlaceListUseCase)
                val caseList =
                    listOf(
                        listOf("-91", "0", "0", "0") to "'south' and 'north' must be latitudes between -90 and 90.",
                        listOf("0", "north", "0", "0") to "'south' and 'north' must be latitudes between -90 and 90.",
                        listOf("0", "0", "181", "0") to "'west' and 'east' must be longitudes between -180 and 180.",
                        listOf("0", "0", "0", "-180.5") to "'west' and 'east' must be longitudes between -180 and 180.",
                        listOf("10", "9.5", "0", "0") to "'north' must not be below 'south'.",
                    )

                caseList.forEach { (bounds, message) ->
                    shouldThrow<AppActionException.InvalidArgument> {
                        action.listInArea(south = bounds[0], north = bounds[1], west = bounds[2], east = bounds[3], sort = null)
                    }.message shouldBe message
                }
                verify(exactly = 0) { getPlaceListUseCase(parameter = any()) }
            }
        }

        test("TC-4ca7faaf-f740-4b6a-9c4b-33ac9b779292 장소 하나를 연결된 태그와 함께 돌려준다") {
            runTest {
                val place = Arb.place().next().copy(isDeleted = false)
                val tagList = Arb.list(Arb.tag(), 1..3).next()
                val findPlaceUseCase = mockk<FindPlaceUseCase>()
                val getEntityTagUseCase = mockk<GetEntityTagUseCase>()
                every { findPlaceUseCase(parameter = place.id) } returns flowOf(Result.success(place))
                every { getEntityTagUseCase(parameter = GetEntityTagUseCase.Parameter(kind = EntityTagKind.PLACE, entityId = place.id)) } returns flowOf(Result.success(tagList))
                val action = placeAppActionOf(findPlaceUseCase, getEntityTagUseCase)

                action.get(placeId = place.id.toString()) shouldBe AppActionPlaceDetail(place = place.expected(), tags = tagList.map { tag -> tag.expected() })
            }
        }

        test("없는 장소를 조회하면 null을 돌려준다") {
            runTest {
                val placeId = Arb.kotlinUuid().next()
                val findPlaceUseCase = mockk<FindPlaceUseCase>()
                val getEntityTagUseCase = mockk<GetEntityTagUseCase>()
                every { findPlaceUseCase(parameter = placeId) } returns flowOf(Result.success(null))
                val action = placeAppActionOf(findPlaceUseCase, getEntityTagUseCase)

                action.get(placeId = placeId.toString()) shouldBe null
                verify(exactly = 0) { getEntityTagUseCase(parameter = any()) }
            }
        }

        test("장소 id가 id 형식이 아니면 앱 기능을 실행하지 않고 실패를 알린다") {
            runTest {
                val findPlaceUseCase = mockk<FindPlaceUseCase>()
                val deletePlaceUseCase = mockk<DeletePlaceUseCase>()
                val pagePlaceMemoUseCase = mockk<PagePlaceMemoUseCase>()
                val action = placeAppActionOf(findPlaceUseCase, deletePlaceUseCase)
                val memoListAction = memoListAppActionOf(pagePlaceMemoUseCase)

                shouldThrow<AppActionException.InvalidArgument> { action.get(placeId = "not-id") }.message shouldBe "'placeId' must be an id."
                shouldThrow<AppActionException.InvalidArgument> { action.delete(placeId = "not-id") }.message shouldBe "'placeId' must be an id."
                shouldThrow<AppActionException.InvalidArgument> { memoListAction.placeList(placeId = "not-id", sort = null, cursor = null) }.message shouldBe "'placeId' must be an id."
                verify(exactly = 0) { findPlaceUseCase(parameter = any()) }
                verify(exactly = 0) { pagePlaceMemoUseCase(parameter = any()) }
                coVerify(exactly = 0) { deletePlaceUseCase(parameter = any()) }
            }
        }

        test("장소의 메모 목록과 완료 메모 목록은 장소와 메모 정렬로 조회한다") {
            runTest {
                val placeId = Arb.kotlinUuid().next()
                val memoList = Arb.list(Arb.memo(), 1..3).next()
                val pagePlaceMemoUseCase = mockk<PagePlaceMemoUseCase>()
                val pageFinishedPlaceMemoUseCase = mockk<PageFinishedPlaceMemoUseCase>()
                every { pagePlaceMemoUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(memoList)))
                every { pageFinishedPlaceMemoUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(memoList)))
                val action = memoListAppActionOf(pagePlaceMemoUseCase, pageFinishedPlaceMemoUseCase)
                val expected = AppActionPage(items = memoList.map { memo -> memo.expected() }, nextCursor = null)

                action.placeList(placeId = placeId.toString(), sort = null, cursor = null) shouldBe expected
                action.placeFinishedList(placeId = placeId.toString(), sort = "title", cursor = null) shouldBe expected

                verify(exactly = 1) { pagePlaceMemoUseCase(parameter = PagePlaceMemoUseCase.Parameter(placeId = placeId, sort = ListSort.DEFAULT)) }
                verify(exactly = 1) { pageFinishedPlaceMemoUseCase(parameter = PageFinishedPlaceMemoUseCase.Parameter(placeId = placeId, sort = ListSort.TITLE)) }
            }
        }

        test("장소를 추가하면 받은 값으로 추가하고 추가한 장소의 id를 돌려준다") {
            runTest {
                val detail = Arb.placeDetail().next()
                val latitude = Arb.numericDouble(min = -90.0, max = 90.0).next().toCoordinateText()
                val longitude = Arb.numericDouble(min = -180.0, max = 180.0).next().toCoordinateText()
                val tagIdList = Arb.list(Arb.kotlinUuid(), 0..3).next()
                val addedId = Arb.kotlinUuid().next()
                val parameter = slot<AddPlaceUseCase.Parameter>()
                val addPlaceUseCase = mockk<AddPlaceUseCase>()
                coEvery { addPlaceUseCase(parameter = capture(parameter)) } returns Result.success(addedId)
                val action = placeAppActionOf(addPlaceUseCase)

                val result =
                    action.add(
                        title = detail.title,
                        latitude = latitude,
                        longitude = longitude,
                        address = detail.address,
                        description = detail.description,
                        color = detail.color.expectedHex(),
                        tagIds = tagIdList.map(Uuid::toString),
                    )

                result shouldBe AppActionId(id = addedId.toString())
                parameter.captured shouldBe
                    AddPlaceUseCase.Parameter(
                        detail = detail.copy(color = (OPAQUE or (detail.color and RGB)).toInt().toLong(), coordinate = Coordinate(latitude = latitude.toDouble(), longitude = longitude.toDouble())),
                        tagIdSet = tagIdList.toSet(),
                    )
            }
        }

        test("TC-edf2f510-aac0-4a31-afdb-2f9368763906 추가 요청에 담은 태그를 함께 연결한다") {
            runTest {
                val tagIdSet = setOf(Arb.kotlinUuid().next(), Arb.kotlinUuid().next())
                val addedId = Arb.kotlinUuid().next()
                val parameter = slot<AddPlaceUseCase.Parameter>()
                val addPlaceUseCase = mockk<AddPlaceUseCase>()
                coEvery { addPlaceUseCase(parameter = capture(parameter)) } returns Result.success(addedId)
                val action = placeAppActionOf(addPlaceUseCase)

                val result =
                    action.add(
                        title = Arb.nonBlankString().next(),
                        latitude = Arb.numericDouble(min = -90.0, max = 90.0).next().toCoordinateText(),
                        longitude = Arb.numericDouble(min = -180.0, max = 180.0).next().toCoordinateText(),
                        address = null,
                        description = null,
                        color = null,
                        tagIds = tagIdSet.map { tagId -> tagId.toString() },
                    )

                result shouldBe AppActionId(id = addedId.toString())
                parameter.captured.tagIdSet shouldBe tagIdSet
            }
        }

        test("주소, 설명, 색을 담지 않으면 빈 주소와 설명, 불투명한 무작위 색으로 추가한다") {
            runTest {
                val parameter = slot<AddPlaceUseCase.Parameter>()
                val addPlaceUseCase = mockk<AddPlaceUseCase>()
                coEvery { addPlaceUseCase(parameter = capture(parameter)) } returns Result.success(Arb.kotlinUuid().next())
                val action = placeAppActionOf(addPlaceUseCase)

                action.add(title = Arb.nonBlankString().next(), latitude = Arb.numericDouble(min = -90.0, max = 90.0).next().toCoordinateText(), longitude = Arb.numericDouble(min = -180.0, max = 180.0).next().toCoordinateText(), address = null, description = null, color = null, tagIds = emptyList())

                parameter.captured.detail.address shouldBe ""
                parameter.captured.detail.description shouldBe ""
                (parameter.captured.detail.color and OPAQUE) shouldBe OPAQUE
                parameter.captured.tagIdSet shouldBe emptySet()
            }
        }

        test("숫자가 아닌 좌표는 앱 기능에 올바르지 않은 좌표로 넘겨 MCP와 같은 메시지로 실패한다") {
            runTest {
                val parameter = slot<AddPlaceUseCase.Parameter>()
                val addPlaceUseCase = mockk<AddPlaceUseCase>()
                coEvery { addPlaceUseCase(parameter = capture(parameter)) } returns Result.failure(PlaceCoordinateInvalidException())
                val action = placeAppActionOf(addPlaceUseCase)

                shouldThrow<AppActionException.Failure> {
                    action.add(title = Arb.nonBlankString().next(), latitude = "north", longitude = "1e3", address = null, description = null, color = null, tagIds = emptyList())
                }.message shouldBe "PlaceCoordinateInvalidException"
                parameter.captured.detail.coordinate.latitude
                    .isNaN() shouldBe true
                parameter.captured.detail.coordinate.longitude
                    .isNaN() shouldBe true
            }
        }

        test("잘못된 색과 태그 id는 앱 기능을 실행하지 않고 MCP와 같은 메시지로 실패한다") {
            runTest {
                val addPlaceUseCase = mockk<AddPlaceUseCase>()
                val action = placeAppActionOf(addPlaceUseCase)

                shouldThrow<AppActionException.InvalidArgument> {
                    action.add(title = Arb.nonBlankString().next(), latitude = "0", longitude = "0", address = null, description = null, color = "blue", tagIds = emptyList())
                }.message shouldBe "'color' must be a hex color like #4285F4."
                shouldThrow<AppActionException.InvalidArgument> {
                    action.add(title = Arb.nonBlankString().next(), latitude = "0", longitude = "0", address = null, description = null, color = null, tagIds = listOf("tag"))
                }.message shouldBe "'tagIds' must be an id."
                coVerify(exactly = 0) { addPlaceUseCase(parameter = any()) }
            }
        }

        test("고칠 값을 담지 않은 항목은 지금 값을 유지하고 고친 수를 돌려준다") {
            runTest {
                val place = Arb.place().next().copy(isDeleted = false)
                val title = Arb.nonBlankString().next()
                val latitude = Arb.numericDouble(min = -90.0, max = 90.0).next().toCoordinateText()
                val changedCount = Arb.int(0..1).next()
                val findPlaceUseCase = mockk<FindPlaceUseCase>()
                val updatePlaceUseCase = mockk<UpdatePlaceUseCase>()
                every { findPlaceUseCase(parameter = place.id) } returns flowOf(Result.success(place))
                coEvery { updatePlaceUseCase(parameter = any()) } returns Result.success(changedCount)
                val action = placeAppActionOf(findPlaceUseCase, updatePlaceUseCase)

                action.update(placeId = place.id.toString(), title = title, description = null, color = null, address = null, latitude = latitude, longitude = null) shouldBe
                    AppActionChangedCount(changedCount = changedCount)
                coVerify(exactly = 1) {
                    updatePlaceUseCase(
                        parameter =
                            UpdatePlaceUseCase.Parameter(
                                id = place.id,
                                detail = place.detail.copy(title = title, coordinate = place.detail.coordinate.copy(latitude = latitude.toDouble())),
                            ),
                    )
                }
            }
        }

        test("없는 장소를 고치면 고친 수 0을 돌려준다") {
            runTest {
                val placeId = Arb.kotlinUuid().next()
                val findPlaceUseCase = mockk<FindPlaceUseCase>()
                val updatePlaceUseCase = mockk<UpdatePlaceUseCase>()
                every { findPlaceUseCase(parameter = placeId) } returns flowOf(Result.success(null))
                val action = placeAppActionOf(findPlaceUseCase, updatePlaceUseCase)

                action.update(placeId = placeId.toString(), title = null, description = null, color = null, address = null, latitude = null, longitude = null) shouldBe
                    AppActionChangedCount(changedCount = 0)
                coVerify(exactly = 0) { updatePlaceUseCase(parameter = any()) }
            }
        }

        test("삭제와 되돌리기는 바뀐 수를 돌려준다") {
            runTest {
                val placeId = Arb.kotlinUuid().next()
                val changedCount = Arb.int(0..1).next()
                val deletePlaceUseCase = mockk<DeletePlaceUseCase>()
                val restorePlaceUseCase = mockk<RestorePlaceUseCase>()
                coEvery { deletePlaceUseCase(parameter = placeId) } returns Result.success(changedCount)
                coEvery { restorePlaceUseCase(parameter = placeId) } returns Result.success(changedCount)
                val action = placeAppActionOf(deletePlaceUseCase, restorePlaceUseCase)

                action.delete(placeId = placeId.toString()) shouldBe AppActionChangedCount(changedCount = changedCount)
                action.restore(placeId = placeId.toString()) shouldBe AppActionChangedCount(changedCount = changedCount)
            }
        }

        test("TC-173de82e-308b-45d4-bc9b-14a00eb1c2fc 태그 연결과 해제는 장소 종류로 요청하고 완료를 돌려준다") {
            runTest {
                val placeId = Arb.kotlinUuid().next()
                val tagId = Arb.kotlinUuid().next()
                val addEntityTagUseCase = mockk<AddEntityTagUseCase>()
                val removeEntityTagUseCase = mockk<RemoveEntityTagUseCase>()
                coEvery { addEntityTagUseCase(parameter = any()) } returns Result.success(Unit)
                coEvery { removeEntityTagUseCase(parameter = any()) } returns Result.success(Unit)
                val action = entityTagAppActionOf(addEntityTagUseCase, removeEntityTagUseCase)

                action.add(kind = AppActionEntityTagKind.PLACE, entityId = placeId.toString(), tagId = tagId.toString()) shouldBe AppActionDone(done = true)
                action.remove(kind = AppActionEntityTagKind.PLACE, entityId = placeId.toString(), tagId = tagId.toString()) shouldBe AppActionDone(done = true)

                coVerify(exactly = 1) { addEntityTagUseCase(parameter = AddEntityTagUseCase.Parameter(kind = EntityTagKind.PLACE, entityId = placeId, tagId = tagId)) }
                coVerify(exactly = 1) { removeEntityTagUseCase(parameter = RemoveEntityTagUseCase.Parameter(kind = EntityTagKind.PLACE, entityId = placeId, tagId = tagId)) }
            }
        }

        test("태그 id가 id 형식이 아니면 연결하지 않고, 연결이 실패하면 MCP와 같은 메시지로 실패한다") {
            runTest {
                val message = Arb.nonBlankString().next()
                val addEntityTagUseCase = mockk<AddEntityTagUseCase>()
                val removeEntityTagUseCase = mockk<RemoveEntityTagUseCase>()
                coEvery { removeEntityTagUseCase(parameter = any()) } returns Result.failure(IllegalStateException(message))
                val action = entityTagAppActionOf(addEntityTagUseCase, removeEntityTagUseCase)

                shouldThrow<AppActionException.InvalidArgument> { action.add(kind = AppActionEntityTagKind.PLACE, entityId = Arb.kotlinUuid().next().toString(), tagId = "tag") }.message shouldBe "'tagId' must be an id."
                shouldThrow<AppActionException.Failure> {
                    action.remove(kind = AppActionEntityTagKind.PLACE, entityId = Arb.kotlinUuid().next().toString(), tagId = Arb.kotlinUuid().next().toString())
                }.message shouldBe "IllegalStateException: $message"
                coVerify(exactly = 0) { addEntityTagUseCase(parameter = any()) }
            }
        }

        test("TC-e17787d4-7a35-4675-8dbd-0b61b2795a73 삭제된 장소의 상세는 없는 장소와 같이 null을 돌려준다") {
            runTest {
                val place = Arb.place().next().copy(isDeleted = true)
                val findPlaceUseCase = mockk<FindPlaceUseCase>()
                every { findPlaceUseCase(parameter = place.id) } returns flowOf(Result.success(place))
                val action = placeAppActionOf(findPlaceUseCase)

                action.get(placeId = place.id.toString()) shouldBe null
            }
        }

        test("삭제된 장소도 포함하는 조회는 삭제된 장소를 돌려준다") {
            runTest {
                val place = Arb.place().next().copy(isDeleted = true)
                val findPlaceUseCase = mockk<FindPlaceUseCase>()
                val getEntityTagUseCase = mockk<GetEntityTagUseCase>()
                every { findPlaceUseCase(parameter = place.id) } returns flowOf(Result.success(place))
                every { getEntityTagUseCase(parameter = GetEntityTagUseCase.Parameter(kind = EntityTagKind.PLACE, entityId = place.id)) } returns flowOf(Result.success(emptyList()))
                val action = placeAppActionOf(findPlaceUseCase, getEntityTagUseCase)

                action.getIncludingDeleted(placeId = place.id.toString())?.place?.id shouldBe place.id.toString()
            }
        }
    })

private const val OPAQUE = 0xFF000000L
private const val RGB = 0xFFFFFFL

// 지수 표기(1.0E-5)는 좌표 글로 받지 않으므로 소수 표기로 넘긴다.
private fun Double.toCoordinateText(): String = toBigDecimal().toPlainString()

private fun Long.expectedHex(): String = "#%06X".format(this and RGB)

private fun Place.expected(): AppActionPlace =
    AppActionPlace(
        id = id.toString(),
        title = detail.title,
        description = detail.description,
        color = detail.color.expectedHex(),
        address = detail.address,
        latitude = detail.coordinate.latitude.takeIf(Double::isFinite),
        longitude = detail.coordinate.longitude.takeIf(Double::isFinite),
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )

private fun Tag.expected(): AppActionTag =
    AppActionTag(
        id = id.toString(),
        emoji = detail.emoji,
        title = detail.title,
        description = detail.description,
        color = detail.color.expectedHex(),
        isFinished = isFinished,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )

private fun Memo.expected(): AppActionMemo =
    AppActionMemo(
        id = id.toString(),
        title = detail.title,
        description = detail.description,
        color = detail.color.expectedHex(),
        dateTime =
            when (val dateTime = detail.dateTime) {
                is MemoDateTime.AllDay -> AppActionMemoDateTime(allDay = true, start = dateTime.dateRange.start.toString(), endInclusive = dateTime.dateRange.endInclusive.toString())
                is MemoDateTime.DateTime -> AppActionMemoDateTime(allDay = false, start = dateTime.start.toString(), endInclusive = dateTime.endInclusive.toString())
                null -> null
            },
        primaryTagId = primaryTagId?.toString(),
        isFinished = isFinished,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )
