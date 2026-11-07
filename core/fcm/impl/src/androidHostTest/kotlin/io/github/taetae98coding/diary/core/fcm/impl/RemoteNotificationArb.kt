package io.github.taetae98coding.diary.core.fcm.impl

import io.github.taetae98coding.diary.core.fcm.impl.notification.RemoteNotification
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.string

internal fun Arb.Companion.remoteNotification(): Arb<RemoteNotification> =
    arbitrary {
        RemoteNotification(
            title = Arb.string().bind(),
            body = Arb.string().bind(),
            channelId = Arb.string().bind(),
            tag = Arb.string().bind(),
            deepLink = Arb.string().bind(),
        )
    }
