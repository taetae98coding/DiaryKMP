package io.github.taetae98coding.diary.core.testing

// Boolean 플래그는 값이 둘뿐이라 다른 플래그와 뒤바뀌어 매핑돼도 우연히 통과할 수 있으므로 네 조합을 모두 확인한다.
public val finishedAndDeletedCaseList: List<Pair<Boolean, Boolean>> =
    listOf(
        true to true,
        true to false,
        false to true,
        false to false,
    )

public val favoriteAndDeletedCaseList: List<Pair<Boolean, Boolean>> =
    listOf(
        true to true,
        true to false,
        false to true,
        false to false,
    )

public val isDeletedCaseList: List<Boolean> = listOf(true, false)
