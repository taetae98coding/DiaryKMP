package io.github.taetae98coding.diary.compose.core.input

// 페이지와 탭이 놓이는 순서는 디자인이 정하므로 enum 선언 순서에 기대지 않고 이 목록이 소유한다.
internal val diaryDescriptionInputPageList: List<DiaryDescriptionInputPage> =
    listOf(
        DiaryDescriptionInputPage.Input,
        DiaryDescriptionInputPage.Preview,
    )
