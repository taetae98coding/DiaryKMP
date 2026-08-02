package io.github.taetae98coding.diary.feature.tag.ui

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceAddNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagHomeNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.web.api.WebAddNavKey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.uuid.Uuid

class TagNavigationTest :
    FunSpec({
        test("TC-TAG-MEMO-FINISHED-LIST-DETAIL-FEATURE-017 완료된 메모 목록의 뒤로가기 동작은 상세 표시 여부와 관계없이 TagDetail로 돌아간다") {
            val tagId = Uuid.random()
            val detailCases =
                listOf(
                    emptyList(),
                    listOf(MemoDetailNavKey(id = Uuid.random())),
                    listOf(MemoDetailNavKey(id = Uuid.random()), MemoDetailNavKey(id = Uuid.random())),
                )

            detailCases.forEach { detailKeyList ->
                val backStack = tagMemoFinishedListBackStack(tagId = tagId, finishedDetailKeyList = detailKeyList)

                backStack.navigateUpFromTagMemoFinishedList()

                backStack shouldContainExactly listOf(TagHomeNavKey, TagDetailNavKey(id = tagId))
            }
        }

        test("TC-TAG-DETAIL-WEB-FEATURE-007 웹 탭에서 시작한 웹 추가는 대상 태그를 초기 태그로 넘긴다") {
            val tagId = Uuid.random()
            val backStack = NavBackStack<NavKey>(TagHomeNavKey, TagDetailNavKey(id = tagId))

            backStack.navigateToWebAddFromTagDetail(tagId = tagId)

            backStack shouldContainExactly
                listOf(
                    TagHomeNavKey,
                    TagDetailNavKey(id = tagId),
                    WebAddNavKey(initialTagId = tagId),
                )
        }

        test("TC-TAG-DETAIL-PLACE-FEATURE-019 목록 모드에서 시작한 장소 추가는 지도 위치를 넘기지 않는다") {
            val tagId = Uuid.random()
            val backStack = NavBackStack<NavKey>(TagHomeNavKey, TagDetailNavKey(id = tagId))

            backStack.navigateToPlaceAddFromTagDetail(tagId = tagId, coordinate = null)

            val key = backStack.last().shouldBeInstanceOf<PlaceAddNavKey>()
            key.initialTagId shouldBe tagId
            key.latitude.shouldBeNull()
            key.longitude.shouldBeNull()
        }

        test("TC-TAG-DETAIL-PLACE-FEATURE-031 지도 모드에서 시작한 장소 추가는 보고 있던 지도 위치를 넘긴다") {
            val tagId = Uuid.random()
            val coordinate = Coordinate(latitude = fixtureMonkey.giveMeOne<Double>(), longitude = fixtureMonkey.giveMeOne<Double>())
            val backStack = NavBackStack<NavKey>(TagHomeNavKey, TagDetailNavKey(id = tagId))

            backStack.navigateToPlaceAddFromTagDetail(tagId = tagId, coordinate = coordinate)

            val key = backStack.last().shouldBeInstanceOf<PlaceAddNavKey>()
            key.initialTagId shouldBe tagId
            key.latitude shouldBe coordinate.latitude
            key.longitude shouldBe coordinate.longitude
        }

        test("완료된 메모 목록이 없는 전환 이력에서는 완료된 메모 목록 뒤로가기 동작이 전환 이력을 바꾸지 않는다") {
            val tagId = Uuid.random()
            val backStack =
                NavBackStack<NavKey>(
                    TagHomeNavKey,
                    TagDetailNavKey(id = tagId),
                )
            val expected = backStack.toList()

            backStack.navigateUpFromTagMemoFinishedList()

            backStack shouldContainExactly expected
        }
    })

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

private fun tagMemoFinishedListBackStack(
    tagId: Uuid,
    finishedDetailKeyList: List<NavKey>,
): NavBackStack<NavKey> =
    NavBackStack(
        TagHomeNavKey,
        TagDetailNavKey(id = tagId),
        TagMemoFinishedListNavKey(tagId = tagId),
        *finishedDetailKeyList.toTypedArray(),
    )
