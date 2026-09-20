package io.github.taetae98coding.diary.compose.list.sort

import io.github.taetae98coding.diary.core.model.list.ListSort

// 선택 목록에 놓이는 순서는 디자인이 정하므로 enum 선언 순서에 기대지 않고 이 목록이 소유한다.
public val listSortList: List<ListSort> =
    listOf(
        ListSort.TITLE,
        ListSort.RECENTLY_UPDATED,
    )

// 항목을 가리키는 값을 이름이라 부르는 목록은 제목순 자리에 이름순을 둔다.
public val nameListSortList: List<ListSort> =
    listOf(
        ListSort.NAME,
        ListSort.RECENTLY_UPDATED,
    )

// 메모 목록만 제목순과 다른 기본순을 가지므로 기본순을 첫 줄에 두는 목록을 따로 둔다.
public val memoListSortList: List<ListSort> =
    listOf(
        ListSort.DEFAULT,
        ListSort.TITLE,
        ListSort.RECENTLY_UPDATED,
    )
