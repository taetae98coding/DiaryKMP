package io.github.taetae98coding.diary.feature.more.ui.home

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import io.github.taetae98coding.diary.feature.more.ui.home.refresh.MoreHomeRefreshViewModel

@Composable
internal fun MoreHomeScreenEffect(refreshViewModel: MoreHomeRefreshViewModel) {
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        refreshViewModel.refresh()
    }
}
