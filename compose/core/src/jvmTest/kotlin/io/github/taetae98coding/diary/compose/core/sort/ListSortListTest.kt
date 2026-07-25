package io.github.taetae98coding.diary.compose.core.sort

import io.github.taetae98coding.diary.core.model.list.ListSort
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class ListSortListTest :
    FunSpec({
        test("정렬은 제목순, 최근 수정순 순서다") {
            listSortList shouldContainExactly
                listOf(
                    ListSort.TITLE,
                    ListSort.RECENTLY_UPDATED,
                )
        }

        test("이름을 쓰는 목록의 정렬은 이름순, 최근 수정순 순서다") {
            nameListSortList shouldContainExactly
                listOf(
                    ListSort.NAME,
                    ListSort.RECENTLY_UPDATED,
                )
        }

        test("메모 목록의 정렬은 기본순, 제목순, 최근 수정순 순서다") {
            memoListSortList shouldContainExactly
                listOf(
                    ListSort.DEFAULT,
                    ListSort.TITLE,
                    ListSort.RECENTLY_UPDATED,
                )
        }

        test("각 목록은 같은 정렬을 두 번 담지 않는다") {
            listOf(listSortList, nameListSortList, memoListSortList).forEach { sortList ->
                sortList.toSet().size shouldBe sortList.size
            }
        }

        // 목록마다 고를 수 있는 정렬이 달라 한 목록이 모든 정렬을 담지는 않으므로, 어느 목록에도 없는 정렬이 생기지 않는지로 확인한다.
        test("세 목록을 합치면 모든 정렬을 담는다") {
            (listSortList + nameListSortList + memoListSortList).toSet() shouldBe ListSort.entries.toSet()
        }
    })
