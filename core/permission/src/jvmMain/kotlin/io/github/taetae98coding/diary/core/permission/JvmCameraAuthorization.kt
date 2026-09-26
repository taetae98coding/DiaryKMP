package io.github.taetae98coding.diary.core.permission

import io.github.taetae98coding.diary.library.avfoundation.AVAuthorizationStatus
import io.github.taetae98coding.diary.library.avfoundation.AVCaptureVideoAuthorization

internal interface JvmCameraAuthorization {
    fun status(): AVAuthorizationStatus

    suspend fun requestAccess(): Boolean
}

internal object AVFoundationCameraAuthorization : JvmCameraAuthorization {
    override fun status(): AVAuthorizationStatus = AVCaptureVideoAuthorization.status()

    override suspend fun requestAccess(): Boolean = AVCaptureVideoAuthorization.requestAccess()
}
