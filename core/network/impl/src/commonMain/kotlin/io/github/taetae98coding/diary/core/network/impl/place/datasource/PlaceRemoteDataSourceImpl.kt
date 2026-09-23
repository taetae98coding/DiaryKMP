package io.github.taetae98coding.diary.core.network.impl.place.datasource

import io.github.taetae98coding.diary.core.network.api.place.datasource.PlaceRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.place.entity.PlacePullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.place.entity.PlaceRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.place.entity.PlacePullResponseRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.place.entity.PlacePushRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.sync.entity.PullRequestRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.supabase.api.invoke
import io.ktor.client.call.body
import org.koin.core.annotation.Factory

@Factory
internal class PlaceRemoteDataSourceImpl(
    private val supabaseFunction: SupabaseFunction,
) : PlaceRemoteDataSource {
    override suspend fun push(placeList: List<PlaceRemoteEntity>) {
        supabaseFunction(
            function = PUSH_PLACE_FUNCTION,
            body = PlacePushRequestRemoteEntity(placeList = placeList),
        )
    }

    override suspend fun pull(usn: Long): List<PlacePullRemoteEntity> =
        supabaseFunction(
            function = PULL_PLACE_FUNCTION,
            body = PullRequestRemoteEntity(usn = usn),
        ).body<PlacePullResponseRemoteEntity>().placeList

    private companion object {
        const val PUSH_PLACE_FUNCTION: String = "v1-sync-push-place"
        const val PULL_PLACE_FUNCTION: String = "v1-sync-pull-place"
    }
}
