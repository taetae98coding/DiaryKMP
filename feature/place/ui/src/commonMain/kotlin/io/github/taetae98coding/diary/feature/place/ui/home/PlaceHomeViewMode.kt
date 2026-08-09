package io.github.taetae98coding.diary.feature.place.ui.home

internal enum class PlaceHomeViewMode {
    MAP,
    LIST,
    ;

    fun toggled(): PlaceHomeViewMode =
        when (this) {
            MAP -> LIST
            LIST -> MAP
        }
}
