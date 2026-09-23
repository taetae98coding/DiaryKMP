package io.github.taetae98coding.diary.feature.tag.ui.detail.place

internal enum class TagDetailPlaceViewMode {
    LIST,
    MAP,
    ;

    fun toggled(): TagDetailPlaceViewMode =
        when (this) {
            LIST -> MAP
            MAP -> LIST
        }
}
