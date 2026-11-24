package io.github.taetae98coding.diary.core.fcm.impl

import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.fcm.api.FcmTokenProvider
import kotlinx.coroutines.tasks.await

@Inject
@ContributesBinding(AppScope::class)
internal class AndroidFcmTokenProvider : FcmTokenProvider {
    override suspend fun getInstallationId(): String {
        FirebaseMessaging.getInstance().register().await()

        return FirebaseInstallations.getInstance().id.await()
    }
}
