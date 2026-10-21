@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.qr.ui

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.result.LocalResultEventBus
import io.github.taetae98coding.diary.compose.core.scene.LIST_DETAIL_PANE_WIDTH_FRACTION
import io.github.taetae98coding.diary.compose.core.scene.ListDetailPlaceholderStateProvider
import io.github.taetae98coding.diary.compose.core.scene.isPaneVisible
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrAddNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrDetailNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrHomeNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrScanNavKey
import io.github.taetae98coding.diary.feature.qr.api.isQrAddOnDetailPane
import io.github.taetae98coding.diary.feature.qr.api.isQrListDetailPane
import io.github.taetae98coding.diary.feature.qr.ui.add.QrAddScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.qr.ui.add.QrAddScreen
import io.github.taetae98coding.diary.feature.qr.ui.detail.QrDetailScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.qr.ui.detail.QrDetailScreen
import io.github.taetae98coding.diary.feature.qr.ui.home.QrHomeScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.qr.ui.home.QrHomeScreen
import io.github.taetae98coding.diary.feature.qr.ui.scan.QrScanScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

public fun EntryProviderScope<ScreenNavKey>.qrEntry(backStack: NavBackStack<ScreenNavKey>) {
    qrHomeEntry(backStack = backStack)
    qrAddEntry(backStack = backStack)
    qrDetailEntry(backStack = backStack)
    qrScanEntry(backStack = backStack)
}

private fun EntryProviderScope<ScreenNavKey>.qrHomeEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<QrHomeNavKey>(
        clazzContentKey = { QR_HOME_CONTENT_KEY },
        metadata = qrHomeListPaneMetadata(backStack = backStack),
    ) {
        val isDetailPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.Detail)

        QrHomeScreen(
            navigateUp = backStack::navigateUpFromQrHome,
            navigateToAdd = { backStack.add(QrAddNavKey) },
            navigateToDetail = { id -> backStack.navigateToQrDetail(id = id) },
            componentVisibleProvider = { QrHomeScaffoldComponentVisible(isAddButtonVisible = !isDetailPaneVisible || !backStack.isQrAddOnDetailPane()) },
            qrViewModel = koinViewModel(),
            syncViewModel = koinViewModel(),
        )
    }
}

internal const val QR_HOME_CONTENT_KEY: String = "QrHomeNavKey"

internal fun qrHomeListPaneMetadata(backStack: NavBackStack<ScreenNavKey>): Map<String, Any> =
    ListDetailSceneStrategy.listPane(
        sceneKey = QrHomeNavKey,
        detailPlaceholder = {
            ListDetailPlaceholderStateProvider(listContentKey = QR_HOME_CONTENT_KEY) {
                QrAddScreen(
                    navigateUp = {},
                    navigateToScan = { backStack.add(QrScanNavKey) },
                    permissionManager = rememberPermissionManager(),
                    resultEventBus = LocalResultEventBus.current,
                    componentVisibleProvider = { QrAddScaffoldComponentVisible(isNavigateUpButtonVisible = false) },
                    viewModel = koinViewModel(),
                )
            }
        },
    ) + ListDetailSceneStrategy.preferredPaneSize(width = LIST_DETAIL_PANE_WIDTH_FRACTION)

private fun EntryProviderScope<ScreenNavKey>.qrAddEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<QrAddNavKey>(
        metadata = { key -> backStack.qrListDetailPaneMetadata(key) },
    ) {
        val isListPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.List)

        QrAddScreen(
            navigateUp = backStack::removeLastOrNull,
            navigateToScan = { backStack.add(QrScanNavKey) },
            permissionManager = rememberPermissionManager(),
            resultEventBus = LocalResultEventBus.current,
            componentVisibleProvider = { QrAddScaffoldComponentVisible(isNavigateUpButtonVisible = !isListPaneVisible) },
            viewModel = koinViewModel(),
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.qrDetailEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<QrDetailNavKey>(
        metadata = { key -> backStack.qrListDetailPaneMetadata(key) },
    ) { key ->
        val isListPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.List)

        QrDetailScreen(
            navigateUp = backStack::removeLastOrNull,
            navigateToScan = { backStack.add(QrScanNavKey) },
            permissionManager = rememberPermissionManager(),
            resultEventBus = LocalResultEventBus.current,
            componentVisibleProvider = { QrDetailScaffoldComponentVisible(isNavigateUpButtonVisible = !isListPaneVisible) },
            viewModel = koinViewModel { parametersOf(key.id) },
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.qrScanEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<QrScanNavKey> {
        val resultEventBus = LocalResultEventBus.current

        QrScanScreen(
            navigateUp = backStack::removeLastOrNull,
            navigateUpWithValue = { value ->
                backStack.navigateUpWithQrScannedValue(
                    resultEventBus = resultEventBus,
                    value = value,
                )
            },
        )
    }
}

private fun NavBackStack<ScreenNavKey>.qrListDetailPaneMetadata(key: ScreenNavKey): Map<String, Any> =
    if (isQrListDetailPane(key)) {
        ListDetailSceneStrategy.detailPane(sceneKey = QrHomeNavKey) + ListDetailSceneStrategy.preferredPaneSize(width = LIST_DETAIL_PANE_WIDTH_FRACTION)
    } else {
        emptyMap()
    }
