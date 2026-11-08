package io.github.taetae98coding.diary.work.appaction.place

import io.github.taetae98coding.diary.work.appaction.tag.AppActionTag

public data class AppActionPlaceDetail(
    val place: AppActionPlace,
    val tags: List<AppActionTag>,
)
