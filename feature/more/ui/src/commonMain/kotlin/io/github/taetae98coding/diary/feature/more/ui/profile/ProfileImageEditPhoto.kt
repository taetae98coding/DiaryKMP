package io.github.taetae98coding.diary.feature.more.ui.profile

internal sealed interface ProfileImageEditPhoto {
    data object Empty : ProfileImageEditPhoto

    data object Loading : ProfileImageEditPhoto

    data class Ready(
        val width: Int,
        val height: Int,
    ) : ProfileImageEditPhoto {
        val shortSide: Int get() = minOf(width, height)

        // 남길 영역은 사진의 짧은 변을 확대 배율로 나눈 길이의 정사각형이므로, 사진 전체에 대한 비율은 변마다 다르다.
        fun regionWidth(zoom: Float): Float = shortSide / (width * zoom)

        fun regionHeight(zoom: Float): Float = shortSide / (height * zoom)
    }

    data object Unreadable : ProfileImageEditPhoto
}
