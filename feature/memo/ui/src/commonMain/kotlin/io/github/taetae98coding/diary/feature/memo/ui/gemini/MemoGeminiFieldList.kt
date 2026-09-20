package io.github.taetae98coding.diary.feature.memo.ui.gemini

// 결과가 놓이는 순서는 디자인이 정하므로 enum 선언 순서에 기대지 않고 이 목록이 소유한다.
internal val memoGeminiFieldList: List<MemoGeminiField> =
    listOf(
        MemoGeminiField.TITLE,
        MemoGeminiField.DESCRIPTION,
        MemoGeminiField.DATE_TIME,
    )
