package io.github.taetae98coding.diary.work.musicdownload.proxy

import io.github.taetae98coding.diary.work.musicdownload.di.MusicDownloadDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface

internal fun interface NetworkAddressSource {
    suspend fun findAddressList(): List<InetAddress>
}

@Factory
internal class NetworkInterfaceAddressSource(
    @param:MusicDownloadDispatcher private val dispatcher: CoroutineDispatcher,
) : NetworkAddressSource {
    override suspend fun findAddressList(): List<InetAddress> =
        withContext(dispatcher) {
            NetworkInterface
                .getNetworkInterfaces()
                ?.toList()
                .orEmpty()
                .filter { networkInterface -> networkInterface.isUp && !networkInterface.isLoopback }
                .flatMap { networkInterface -> networkInterface.inetAddresses.toList() }
        }
}

internal fun List<InetAddress>.toMusicDownloadProxyAddressList(port: Int): List<String> =
    filterIsInstance<Inet4Address>()
        .filter { address -> address.isSiteLocalAddress }
        .map { address -> "http://${address.hostAddress}:$port" }
