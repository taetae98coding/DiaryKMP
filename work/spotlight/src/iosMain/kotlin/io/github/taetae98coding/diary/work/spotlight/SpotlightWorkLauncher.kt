package io.github.taetae98coding.diary.work.spotlight

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.taetae98coding.diary.work.spotlight.di.SpotlightScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@SingleIn(AppScope::class)
public class SpotlightWorkLauncher
    @Inject
    internal constructor(
        private val work: SpotlightWork,
        @param:SpotlightScope private val scope: CoroutineScope,
    ) {
        private var job: Job? = null

        // 앱이 시작되면 화면과 관계없이 색인을 맞추기 시작하도록 앱 시작 시점에 한 번 호출한다.
        public fun start() {
            if (job?.isActive == true) return

            job = scope.launch { work.doWork() }
        }
    }
