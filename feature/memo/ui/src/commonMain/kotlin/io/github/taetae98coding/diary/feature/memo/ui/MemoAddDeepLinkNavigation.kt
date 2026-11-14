package io.github.taetae98coding.diary.feature.memo.ui

import androidx.navigation3.runtime.deeplink.DeepLinkUri
import androidx.navigation3.runtime.deeplink.UriDeepLinkMatcher
import io.github.taetae98coding.diary.core.navigation.DeepLinkNavigation
import io.github.taetae98coding.diary.core.navigation.ScreenDeepLink
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.ScreenNavigator
import io.github.taetae98coding.diary.core.navigation.toDeepLinkNavigation
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoHomeNavKey
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Single

@Single(binds = [DeepLinkNavigation::class])
internal class MemoAddDeepLinkNavigation :
    DeepLinkNavigation by UriDeepLinkMatcher(DeepLinkUri(ScreenDeepLink.MEMO_ADD), MemoAddDeepLinkArgs.serializer())
        .toDeepLinkNavigation({ result, isLaunch -> openMemoAdd(addKey = result.key.toNavKey(), isLaunch = isLaunch, listBackStack = topLevelBackStackOf(MemoHomeNavKey)) })

// 화면 키의 기간은 묶음 값이라 주소의 쿼리 하나로 담을 수 없으므로, 주소는 시작과 종료를 따로 받는다.
@Serializable
internal data class MemoAddDeepLinkArgs(
    val initialTitle: String = "",
    val initialDescription: String = "",
    val initialStartDate: LocalDate? = null,
    val initialEndDate: LocalDate? = null,
    val initialStartDateTime: LocalDateTime? = null,
    val initialEndDateTime: LocalDateTime? = null,
)

internal fun MemoAddDeepLinkArgs.toNavKey(): MemoAddNavKey =
    MemoAddNavKey(
        initialTitle = initialTitle,
        initialDescription = initialDescription,
        initialDateRange = initialStartDate?.let { start -> MemoAddNavKey.InitialDateRange(start = start, endInclusive = initialEndDate ?: start) },
        initialDateTimeRange = initialStartDateTime?.let { start -> MemoAddNavKey.InitialDateTimeRange(start = start, endInclusive = initialEndDateTime ?: start) },
        isStandalone = true,
    )

private fun ScreenNavigator.openMemoAdd(
    addKey: ScreenNavKey,
    listBackStack: List<ScreenNavKey>,
    isLaunch: Boolean,
) {
    if (isLaunch) {
        backStack.clear()
        backStack.addAll(listBackStack + addKey)
    } else {
        backStack.add(addKey)
    }
}
