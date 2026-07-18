package io.github.taetae98coding.diary.core.model.map

public enum class MapProvider {
    NAVER,
    GOOGLE,
    ;

    public companion object {
        public val DEFAULT: MapProvider = NAVER
    }
}
