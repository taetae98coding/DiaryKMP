package io.github.taetae98coding.diary.work.spotlight

import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.navigation.ScreenDeepLink
import kotlin.uuid.Uuid

// 앱 테마의 밝은 모양 주 색(Material3 기본 primary)이다. 밝은 모양과 어두운 모양에서 같은 아이콘을 쓰므로 하나만 둔다.
internal const val APP_PRIMARY_COLOR: Long = 0xFF6750A4

internal fun MemoLocalEntity.toSpotlightItem(): SpotlightItem =
    SpotlightItem(
        identifier = ScreenDeepLink.MEMO_DETAIL.withId(id),
        title = detail.title,
        description = detail.description,
        icon = SpotlightIcon.MEMO,
        iconColor = detail.color,
    )

internal fun TagLocalEntity.toSpotlightItem(): SpotlightItem =
    SpotlightItem(
        identifier = ScreenDeepLink.TAG_DETAIL.withId(id),
        title = listOf(detail.emoji, detail.title).joinNotEmpty(separator = " "),
        description = detail.description,
        icon = SpotlightIcon.TAG,
        iconColor = detail.color,
    )

internal fun PlaceLocalEntity.toSpotlightItem(): SpotlightItem =
    SpotlightItem(
        identifier = ScreenDeepLink.PLACE_DETAIL.withId(id),
        title = detail.title,
        description = listOf(detail.address, detail.description).joinNotEmpty(separator = "\n"),
        icon = SpotlightIcon.PLACE,
        iconColor = detail.color,
    )

internal fun WebLocalEntity.toSpotlightItem(): SpotlightItem =
    SpotlightItem(
        identifier = ScreenDeepLink.WEB_DETAIL.withId(id),
        title = detail.title,
        description = listOf(detail.url, detail.description).joinNotEmpty(separator = "\n"),
        icon = SpotlightIcon.WEB,
        iconColor = APP_PRIMARY_COLOR,
    )

internal fun ContactLocalEntity.toSpotlightItem(): SpotlightItem =
    SpotlightItem(
        identifier = ScreenDeepLink.CONTACT_DETAIL.withId(id),
        title = detail.name,
        description = detail.description,
        icon = SpotlightIcon.CONTACT,
        iconColor = APP_PRIMARY_COLOR,
    )

internal fun ChecklistLocalEntity.toSpotlightItem(): SpotlightItem =
    SpotlightItem(
        identifier = ScreenDeepLink.CHECKLIST_DETAIL.withId(id),
        title = detail.title,
        description = detail.description,
        icon = SpotlightIcon.CHECKLIST,
        iconColor = APP_PRIMARY_COLOR,
    )

internal fun QrLocalEntity.toSpotlightItem(): SpotlightItem =
    SpotlightItem(
        identifier = ScreenDeepLink.QR_DETAIL.withId(id),
        title = detail.title,
        description = detail.description,
        icon = SpotlightIcon.QR,
        iconColor = APP_PRIMARY_COLOR,
    )

internal fun MusicLocalEntity.toSpotlightItem(): SpotlightItem =
    SpotlightItem(
        identifier = ScreenDeepLink.MUSIC_DETAIL.withId(id),
        title = detail.title,
        description = detail.artist,
        icon = SpotlightIcon.MUSIC,
        iconColor = APP_PRIMARY_COLOR,
    )

private fun String.withId(id: Uuid): String = replace("{id}", id.toString())

private fun List<String>.joinNotEmpty(separator: String): String = filter(String::isNotEmpty).joinToString(separator = separator)
