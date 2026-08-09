package io.github.taetae98coding.diary.compose.map

internal object DiaryMapProviderTestFixture {
    const val DEFAULT_NAVER_LABEL: String = "Naver"
    const val DEFAULT_GOOGLE_LABEL: String = "Google"
    const val DEFAULT_PROVIDER_CONTENT_DESCRIPTION: String = "Map provider"

    const val KOREAN_NAVER_LABEL: String = "네이버"
    const val KOREAN_GOOGLE_LABEL: String = "Google"
    const val KOREAN_PROVIDER_CONTENT_DESCRIPTION: String = "지도 제공자"

    const val MAP_TEST_TAG: String = "diaryMapContent"
    const val LAYOUT_TEST_TAG: String = "diaryMapLayout"

    fun label(provider: DiaryMapProvider): String =
        when (provider) {
            DiaryMapProvider.NAVER -> DEFAULT_NAVER_LABEL
            DiaryMapProvider.GOOGLE -> DEFAULT_GOOGLE_LABEL
        }
}
