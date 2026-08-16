package io.github.taetae98coding.diary.feature.search.ui.home

import io.github.taetae98coding.diary.feature.search.api.SearchHomeType
import io.github.taetae98coding.diary.feature.search.ui.Res
import io.github.taetae98coding.diary.feature.search.ui.search_home_memo_tab_label
import io.github.taetae98coding.diary.feature.search.ui.search_home_place_tab_label
import io.github.taetae98coding.diary.feature.search.ui.search_home_tag_tab_label
import io.github.taetae98coding.diary.feature.search.ui.search_home_web_tab_label
import org.jetbrains.compose.resources.StringResource

internal val SearchHomeType.labelResource: StringResource
    get() =
        when (this) {
            SearchHomeType.MEMO -> Res.string.search_home_memo_tab_label
            SearchHomeType.TAG -> Res.string.search_home_tag_tab_label
            SearchHomeType.PLACE -> Res.string.search_home_place_tab_label
            SearchHomeType.WEB -> Res.string.search_home_web_tab_label
        }
