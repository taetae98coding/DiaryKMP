package io.github.taetae98coding.diary.feature.search.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreProvider
import io.github.taetae98coding.diary.compose.core.effect.RequestFocusEffect
import io.github.taetae98coding.diary.feature.search.api.SearchHomeType
import kotlin.uuid.Uuid

@Composable
internal fun SearchHomeScreen(
    navigateUp: () -> Unit,
    navigateToMemoDetail: (Uuid) -> Unit,
    navigateToTagDetail: (Uuid) -> Unit,
    navigateToPlaceDetail: (Uuid) -> Unit,
    navigateToWebDetail: (Uuid) -> Unit,
    initialType: SearchHomeType,
    modifier: Modifier = Modifier,
) {
    val state = rememberSearchHomeScaffoldState(initialType = initialType)

    val viewModelStoreProvider = rememberViewModelStoreProvider()

    RequestFocusEffect(focusRequester = state.focusRequester)

    SearchHomeScaffold(
        onEvent = { event ->
            when (event) {
                is SearchHomeScaffoldEvent.ClickNavigateUp -> navigateUp()
            }
        },
        modifier = modifier,
        state = state,
    ) { type ->
        when (type) {
            SearchHomeType.MEMO ->
                SearchHomeMemoContent(
                    queryState = state.queryState,
                    viewModelStoreProvider = viewModelStoreProvider,
                    navigateToDetail = navigateToMemoDetail,
                    modifier = Modifier.fillMaxSize(),
                )

            SearchHomeType.TAG ->
                SearchHomeTagContent(
                    queryState = state.queryState,
                    viewModelStoreProvider = viewModelStoreProvider,
                    navigateToDetail = navigateToTagDetail,
                    modifier = Modifier.fillMaxSize(),
                )

            SearchHomeType.PLACE ->
                SearchHomePlaceContent(
                    queryState = state.queryState,
                    viewModelStoreProvider = viewModelStoreProvider,
                    navigateToDetail = navigateToPlaceDetail,
                    modifier = Modifier.fillMaxSize(),
                )

            SearchHomeType.WEB ->
                SearchHomeWebContent(
                    queryState = state.queryState,
                    viewModelStoreProvider = viewModelStoreProvider,
                    navigateToDetail = navigateToWebDetail,
                    modifier = Modifier.fillMaxSize(),
                )
        }
    }
}
