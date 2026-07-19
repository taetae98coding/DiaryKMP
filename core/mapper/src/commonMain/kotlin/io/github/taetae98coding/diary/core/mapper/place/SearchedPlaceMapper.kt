package io.github.taetae98coding.diary.core.mapper.place

import io.github.taetae98coding.diary.core.google.network.api.entity.GooglePlaceRemoteEntity
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.place.SearchedPlace
import io.github.taetae98coding.diary.core.naver.network.api.entity.NaverPlaceRemoteEntity
import kotlin.uuid.Uuid

public fun NaverPlaceRemoteEntity.toDomain(id: Uuid): SearchedPlace =
    SearchedPlace(
        id = id,
        name = title.removeHighlightMark(),
        address = roadAddress.ifBlank { address },
        coordinate =
            Coordinate(
                latitude = mapy.toCoordinateDegree(),
                longitude = mapx.toCoordinateDegree(),
            ),
    )

public fun GooglePlaceRemoteEntity.toDomain(id: Uuid): SearchedPlace =
    SearchedPlace(
        id = id,
        name = displayName.text,
        address = formattedAddress.ifBlank { shortFormattedAddress },
        coordinate =
            Coordinate(
                latitude = location.latitude,
                longitude = location.longitude,
            ),
    )

private fun String.removeHighlightMark(): String = replace(HIGHLIGHT_MARK_REGEX, "")

/**
 * 네이버 지역 검색은 좌표를 WGS84 도 단위에 [COORDINATE_SCALE]을 곱한 정수 문자열로 돌려준다.
 */
private fun String.toCoordinateDegree(): Double = (toDoubleOrNull() ?: return Double.NaN) / COORDINATE_SCALE

private val HIGHLIGHT_MARK_REGEX = Regex(pattern = "</?b>", option = RegexOption.IGNORE_CASE)
private const val COORDINATE_SCALE = 10_000_000.0
