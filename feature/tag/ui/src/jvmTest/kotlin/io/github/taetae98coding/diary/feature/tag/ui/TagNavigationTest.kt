package io.github.taetae98coding.diary.feature.tag.ui

import androidx.navigation3.runtime.NavBackStack
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceAddNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagHomeNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.web.api.WebAddNavKey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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

        test("TC-TAG-MEMO-FINISHED-LIST-DETAIL-FEATURE-003 메모를 선택하면 목록을 유지한 채 그 메모의 상세가 놓인다") {
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val memoId = fixtureMonkey.giveMeOne<Uuid>()
            val backStack = tagMemoFinishedListBackStack(tagId = tagId, finishedDetailKeyList = emptyList())

            backStack.navigateToMemoDetailFromTagMemoFinishedList(memoId)

            backStack.toList() shouldContainExactly
                listOf(
                    TagHomeNavKey,
                    TagDetailNavKey(id = tagId),
                    TagMemoFinishedListNavKey(tagId = tagId),
                    MemoDetailNavKey(id = memoId),
                )
        }

        test("TC-TAG-MEMO-FINISHED-LIST-DETAIL-FEATURE-004 다른 메모를 선택하면 이전 상세를 쌓지 않고 교체한다") {
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val selectedId = fixtureMonkey.giveMeOne<Uuid>()
            val backStack =
                tagMemoFinishedListBackStack(
                    tagId = tagId,
                    finishedDetailKeyList = listOf(MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())),
                )

            backStack.navigateToMemoDetailFromTagMemoFinishedList(selectedId)

            backStack.toList() shouldContainExactly
                listOf(
                    TagHomeNavKey,
                    TagDetailNavKey(id = tagId),
                    TagMemoFinishedListNavKey(tagId = tagId),
                    MemoDetailNavKey(id = selectedId),
                )
        }

        test("TC-TAG-MEMO-FINISHED-LIST-DETAIL-FEATURE-009 상세가 선택된 상태에서 뒤로가면 목록을 유지하고 선택 전 상태가 된다") {
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val openCases =
                listOf(
                    listOf(fixtureMonkey.giveMeOne<Uuid>()),
                    listOf(fixtureMonkey.giveMeOne<Uuid>(), fixtureMonkey.giveMeOne<Uuid>()),
                )

            openCases.forEach { memoIdList ->
                val backStack = tagMemoFinishedListBackStack(tagId = tagId, finishedDetailKeyList = emptyList())
                memoIdList.forEach { memoId -> backStack.navigateToMemoDetailFromTagMemoFinishedList(memoId) }

                backStack.removeLastOrNull()

                backStack.toList() shouldContainExactly
                    listOf(
                        TagHomeNavKey,
                        TagDetailNavKey(id = tagId),
                        TagMemoFinishedListNavKey(tagId = tagId),
                    )
            }
        }

        test("TC-TAG-DETAIL-WEB-FEATURE-007 웹 탭에서 시작한 웹 추가는 대상 태그를 초기 태그로 넘긴다") {
            val tagId = Uuid.random()
            val backStack = NavBackStack<ScreenNavKey>(TagHomeNavKey, TagDetailNavKey(id = tagId))

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
            val backStack = NavBackStack<ScreenNavKey>(TagHomeNavKey, TagDetailNavKey(id = tagId))

            backStack.navigateToPlaceAddFromTagDetail(tagId = tagId, coordinate = null)

            val key = backStack.last().shouldBeInstanceOf<PlaceAddNavKey>()
            key.initialTagId shouldBe tagId
            key.latitude.shouldBeNull()
            key.longitude.shouldBeNull()
        }

        test("TC-TAG-DETAIL-PLACE-FEATURE-031 지도 모드에서 시작한 장소 추가는 보고 있던 지도 위치를 넘긴다") {
            val tagId = Uuid.random()
            val coordinate = Coordinate(latitude = fixtureMonkey.giveMeOne<Double>(), longitude = fixtureMonkey.giveMeOne<Double>())
            val backStack = NavBackStack<ScreenNavKey>(TagHomeNavKey, TagDetailNavKey(id = tagId))

            backStack.navigateToPlaceAddFromTagDetail(tagId = tagId, coordinate = coordinate)

            val key = backStack.last().shouldBeInstanceOf<PlaceAddNavKey>()
            key.initialTagId shouldBe tagId
            key.latitude shouldBe coordinate.latitude
            key.longitude shouldBe coordinate.longitude
        }

        test("TC-TAG-DETAIL-FEATURE-054 다른 태그의 상세에서 이동해 온 상세 화면은 뒤로가기 동작을 제공하고 이전 태그의 상세로 돌아간다") {
            val fromKey = TagDetailNavKey(id = Uuid.random())
            val toKey = TagDetailNavKey(id = Uuid.random())
            val backStack = NavBackStack<ScreenNavKey>(TagHomeNavKey, fromKey, toKey)

            backStack.isNavigatedFromTagDetail(toKey).shouldBeTrue()

            backStack.removeLastOrNull()

            backStack shouldContainExactly listOf(TagHomeNavKey, fromKey)
        }

        test("TC-TAG-DETAIL-FEATURE-055 목록에서 선택해 진입한 상세 화면은 뒤로가기 동작을 제공하지 않는다") {
            val selectedId = Uuid.random()
            val pathCases =
                listOf(
                    emptyList(),
                    listOf(TagAddNavKey()),
                    listOf(TagDetailNavKey(id = Uuid.random())),
                    listOf(TagDetailNavKey(id = Uuid.random()), TagDetailNavKey(id = Uuid.random())),
                )

            pathCases.forEach { pathKeyList ->
                val backStack = NavBackStack<ScreenNavKey>(TagHomeNavKey, *pathKeyList.toTypedArray())

                backStack.navigateToTagDetail(selectedId)

                backStack.isNavigatedFromTagDetail(TagDetailNavKey(id = selectedId)).shouldBeFalse()
            }
        }

        test("TC-TAG-DETAIL-FEATURE-056 상세 영역의 태그 추가에서 연 상세 화면은 뒤로가기 동작을 제공하지 않는다") {
            val linkedKey = TagDetailNavKey(id = Uuid.random())
            val backStack =
                NavBackStack<ScreenNavKey>(
                    TagHomeNavKey,
                    TagDetailNavKey(id = Uuid.random()),
                    TagAddNavKey(requestKey = Uuid.random()),
                    linkedKey,
                )

            backStack.isNavigatedFromTagDetail(linkedKey).shouldBeFalse()
        }

        test("TC-TAG-LIST-DETAIL-FEATURE-017 목록에서 태그를 고르면 상세 영역에 쌓인 화면을 모두 정리하고 뒤로가면 상세를 고르기 전으로 돌아간다") {
            val selectedId = Uuid.random()
            val pathCases =
                listOf(
                    listOf(TagDetailNavKey(id = Uuid.random())),
                    listOf(TagDetailNavKey(id = Uuid.random()), TagDetailNavKey(id = Uuid.random())),
                    listOf(TagDetailNavKey(id = Uuid.random()), TagAddNavKey(requestKey = Uuid.random()), TagDetailNavKey(id = Uuid.random())),
                    listOf(TagAddNavKey(), TagDetailNavKey(id = Uuid.random()), TagDetailNavKey(id = Uuid.random())),
                )

            pathCases.forEach { pathKeyList ->
                val backStack = NavBackStack<ScreenNavKey>(TagHomeNavKey, *pathKeyList.toTypedArray())

                backStack.navigateToTagDetail(selectedId)

                backStack shouldContainExactly listOf(TagHomeNavKey, TagDetailNavKey(id = selectedId))

                backStack.removeLastOrNull()

                backStack shouldContainExactly listOf(TagHomeNavKey)
            }
        }

        test("TC-TAG-LIST-DETAIL-FEATURE-019 태그 상세가 놓인 동안 목록에서 태그 추가를 실행하면 그 위에 태그 추가가 놓이고 뒤로가면 이전 태그 상세로 돌아온다") {
            val detailKey = TagDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
            val backStack = NavBackStack<ScreenNavKey>(TagHomeNavKey, detailKey)
            val detailContentKey = backStack.tagDetailContentKey(detailKey)

            backStack.navigateToTagAdd()

            backStack shouldContainExactly listOf(TagHomeNavKey, detailKey, TagAddNavKey())

            backStack.removeLastOrNull()

            backStack shouldContainExactly listOf(TagHomeNavKey, detailKey)
            backStack.tagDetailContentKey(detailKey) shouldBe detailContentKey
        }

        test("목록에서 고른 상세는 대상이 바뀌어도 같은 화면으로 이어지고, 다른 경로로 연 상세는 대상마다 따로 둔다") {
            val firstKey = TagDetailNavKey(id = Uuid.random())
            val secondKey = TagDetailNavKey(id = Uuid.random())
            val linkedKey = TagDetailNavKey(id = Uuid.random())
            val backStack = NavBackStack<ScreenNavKey>(TagHomeNavKey, firstKey, linkedKey)
            val firstContentKey = backStack.tagDetailContentKey(firstKey)
            val linkedContentKey = backStack.tagDetailContentKey(linkedKey)

            backStack.navigateToTagDetail(secondKey.id)

            backStack.tagDetailContentKey(secondKey) shouldBe firstContentKey
            linkedContentKey shouldNotBe firstContentKey
            NavBackStack<ScreenNavKey>(MemoDetailNavKey(id = Uuid.random()), linkedKey).tagDetailContentKey(linkedKey) shouldNotBe firstContentKey
        }

        test("연결이 순환해 같은 태그의 상세가 두 번 놓여도 두 상세는 서로 다른 화면이고, 앞선 상세는 뒤로 돌아올 때까지 같은 화면으로 남는다") {
            val tagId = Uuid.random()
            val listSelectedKey = TagDetailNavKey(id = tagId)
            val linkedKey = TagDetailNavKey(id = Uuid.random())
            val revisitedKey = TagDetailNavKey(id = tagId)
            val backStack = NavBackStack<ScreenNavKey>(TagHomeNavKey, listSelectedKey, linkedKey)
            val listSelectedContentKey = backStack.tagDetailContentKey(listSelectedKey)
            val linkedContentKey = backStack.tagDetailContentKey(linkedKey)

            backStack.add(revisitedKey)

            backStack.tagDetailContentKey(listSelectedKey) shouldBe listSelectedContentKey
            backStack.tagDetailContentKey(linkedKey) shouldBe linkedContentKey
            backStack.tagDetailContentKey(revisitedKey) shouldNotBe listSelectedContentKey
            backStack.tagDetailContentKey(revisitedKey) shouldNotBe linkedContentKey
            backStack.isNavigatedFromTagDetail(listSelectedKey).shouldBeFalse()
            backStack.isNavigatedFromTagDetail(revisitedKey).shouldBeTrue()

            backStack.removeLastOrNull()
            backStack.removeLastOrNull()

            backStack shouldContainExactly listOf(TagHomeNavKey, listSelectedKey)
            backStack.tagDetailContentKey(listSelectedKey) shouldBe listSelectedContentKey
        }

        test("전환 이력에 없는 상세 화면과 첫 화면은 다른 태그의 상세에서 이동해 온 것으로 보지 않는다") {
            val detailKey = TagDetailNavKey(id = Uuid.random())
            val backStack = NavBackStack<ScreenNavKey>(detailKey)

            backStack.isNavigatedFromTagDetail(detailKey).shouldBeFalse()
            backStack.isNavigatedFromTagDetail(TagDetailNavKey(id = Uuid.random())).shouldBeFalse()
        }

        test("완료된 메모 목록이 없는 전환 이력에서는 완료된 메모 목록 뒤로가기 동작이 전환 이력을 바꾸지 않는다") {
            val tagId = Uuid.random()
            val backStack =
                NavBackStack<ScreenNavKey>(
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
    finishedDetailKeyList: List<ScreenNavKey>,
): NavBackStack<ScreenNavKey> =
    NavBackStack(
        TagHomeNavKey,
        TagDetailNavKey(id = tagId),
        TagMemoFinishedListNavKey(tagId = tagId),
        *finishedDetailKeyList.toTypedArray(),
    )
