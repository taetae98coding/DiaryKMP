@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.core.location.impl

import android.content.Context
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import io.github.taetae98coding.diary.core.location.api.Location
import io.github.taetae98coding.diary.core.location.api.LocationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import org.koin.core.annotation.Factory

@Factory
internal class AndroidLocationProvider(
    private val context: Context,
) : LocationProvider {
    override suspend fun getCurrentLocation(): Location? {
        val location =
            try {
                val client = LocationServices.getFusedLocationProviderClient(context)

                client.lastLocation.await() ?: run {
                    val cancellationTokenSource = CancellationTokenSource()

                    client
                        .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancellationTokenSource.token)
                        .await(cancellationTokenSource)
                }
            } catch (_: ApiException) {
                // Google Play 서비스가 위치를 제공하지 못하는 환경이면 FusedLocationProviderClient가 ApiException을 던진다.
                null
            } catch (_: SecurityException) {
                // 위치 권한이 없거나 확인 중 권한이 회수되면 FusedLocationProviderClient가 SecurityException을 던진다.
                null
            }

        return location?.let { Location(latitude = it.latitude, longitude = it.longitude) }
    }
}
