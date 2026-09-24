package io.github.taetae98coding.diary.core.fcm.impl

import com.google.firebase.messaging.FirebaseMessaging
import io.github.taetae98coding.diary.core.fcm.api.FcmTokenProvider
import kotlinx.coroutines.tasks.await
import org.koin.core.annotation.Factory

@Factory
internal class AndroidFcmTokenProvider : FcmTokenProvider {
    override suspend fun getToken(): String? = FirebaseMessaging.getInstance().token.await()
}
