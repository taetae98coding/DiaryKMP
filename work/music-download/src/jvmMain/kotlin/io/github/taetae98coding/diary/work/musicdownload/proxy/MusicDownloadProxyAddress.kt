package io.github.taetae98coding.diary.work.musicdownload.proxy

import org.koin.core.annotation.Factory
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface

internal fun interface NetworkAddressSource {
    fun findAddressList(): List<InetAddress>
}

@Factory
internal class NetworkInterfaceAddressSource : NetworkAddressSource {
    override fun findAddressList(): List<InetAddress> =
        NetworkInterface
            .getNetworkInterfaces()
            ?.toList()
            .orEmpty()
            .filter { networkInterface -> networkInterface.isUp && !networkInterface.isLoopback }
            .flatMap { networkInterface -> networkInterface.inetAddresses.toList() }
}

internal fun List<InetAddress>.toMusicDownloadProxyAddressList(port: Int): List<String> =
    filterIsInstance<Inet4Address>()
        .filter { address -> address.isSiteLocalAddress }
        .map { address -> "http://${address.hostAddress}:$port" }
