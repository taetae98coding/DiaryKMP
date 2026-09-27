package io.github.taetae98coding.diary.feature.search.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreProvider
import io.github.taetae98coding.diary.compose.core.effect.RequestFocusEffect
import io.github.taetae98coding.diary.compose.core.snackbar.DismissUndoSnackbarEffect
import io.github.taetae98coding.diary.feature.search.api.SearchHomeType
import io.github.taetae98coding.diary.feature.search.ui.home.memo.SearchHomeMemoContent
import io.github.taetae98coding.diary.feature.search.ui.home.place.SearchHomePlaceContent
import io.github.taetae98coding.diary.feature.search.ui.home.tag.SearchHomeTagContent
import io.github.taetae98coding.diary.feature.search.ui.home.web.SearchHomeWebContent
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
    val snackbarHostState = remember { SnackbarHostState() }

    RequestFocusEffect(focusRequester = state.focusRequester)
    DismissUndoSnackbarEffect(keyProvider = { state.type }, hostState = snackbarHostState)

    SearchHomeScaffold(
        onEvent = { event ->
            when (event) {
                is SearchHomeScaffoldEvent.ClickNavigateUp -> navigateUp()
            }
        },
        modifier = modifier,
        state = state,
        snackbarHostState = snackbarHostState,
    ) { type ->
        when (type) {
            SearchHomeType.MEMO ->
                SearchHomeMemoContent(
                    queryState = state.queryState,
                    viewModelStoreProvider = viewModelStoreProvider,
                    snackbarHostState = snackbarHostState,
                    navigateToDetail = navigateToMemoDetail,
                    modifier = Modifier.fillMaxSize(),
                )

            SearchHomeType.TAG ->
                SearchHomeTagContent(
                    queryState = state.queryState,
                    viewModelStoreProvider = viewModelStoreProvider,
                    snackbarHostState = snackbarHostState,
                    navigateToDetail = navigateToTagDetail,
                    modifier = Modifier.fillMaxSize(),
                )

            SearchHomeType.PLACE ->
                SearchHomePlaceContent(
                    queryState = state.queryState,
                    viewModelStoreProvider = viewModelStoreProvider,
                    snackbarHostState = snackbarHostState,
                    navigateToDetail = navigateToPlaceDetail,
                    modifier = Modifier.fillMaxSize(),
                )

            SearchHomeType.WEB ->
                SearchHomeWebContent(
                    queryState = state.queryState,
                    viewModelStoreProvider = viewModelStoreProvider,
                    snackbarHostState = snackbarHostState,
                    navigateToDetail = navigateToWebDetail,
                    modifier = Modifier.fillMaxSize(),
                )
        }
    }
}
