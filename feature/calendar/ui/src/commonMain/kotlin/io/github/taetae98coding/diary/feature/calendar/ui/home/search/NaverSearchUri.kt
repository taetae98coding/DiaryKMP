package io.github.taetae98coding.diary.feature.calendar.ui.home.search

import io.github.taetae98coding.diary.library.kotlin.text.encodeUriComponent

private const val NAVER_SEARCH_URI = "https://search.naver.com/search.naver?query="

internal fun naverSearchUri(query: String): String = "$NAVER_SEARCH_URI${query.encodeUriComponent()}"
