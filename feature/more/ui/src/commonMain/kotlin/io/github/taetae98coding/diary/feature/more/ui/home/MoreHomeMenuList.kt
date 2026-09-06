package io.github.taetae98coding.diary.feature.more.ui.home

/**
 * 화면 표시 순서를 소유한다. enum 선언 순서에 기대지 않고, 한국어 이름의 가나다순으로 두며
 * 한글이 아닌 이름의 항목은 그 앞에 둔다.
 */
internal val moreHomeMenuList: List<MoreHomeMenu> =
    listOf(
        MoreHomeMenu.QR,
        MoreHomeMenu.SEARCH,
        MoreHomeMenu.D_DAY,
        MoreHomeMenu.CONTACT,
        MoreHomeMenu.WEB,
        MoreHomeMenu.PLACE,
        MoreHomeMenu.CHECKLIST,
        MoreHomeMenu.FILE,
        MoreHomeMenu.PLAYLIST,
        MoreHomeMenu.HOLIDAY,
    )
