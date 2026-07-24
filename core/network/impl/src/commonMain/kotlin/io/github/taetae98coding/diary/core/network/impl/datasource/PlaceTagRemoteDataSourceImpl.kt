package io.github.taetae98coding.diary.core.network.impl.datasource

import io.github.taetae98coding.diary.core.network.api.placetag.datasource.PlaceTagRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.placetag.entity.PlaceTagPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.placetag.entity.PlaceTagRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.entity.PlaceTagPullResponseRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.entity.PlaceTagPushRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.entity.PullRequestRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.supabase.api.invoke
import io.ktor.client.call.body
import org.koin.core.annotation.Factory

@Factory
internal class PlaceTagRemoteDataSourceImpl(
    private val supabaseFunction: SupabaseFunction,
) : PlaceTagRemoteDataSource {
    override suspend fun push(placeTagList: List<PlaceTagRemoteEntity>) {
        supabaseFunction(
            function = PUSH_PLACE_TAG_FUNCTION,
            body = PlaceTagPushRequestRemoteEntity(placeTagList = placeTagList),
        )
    }

    override suspend fun pull(usn: Long): List<PlaceTagPullRemoteEntity> =
        supabaseFunction(
            function = PULL_PLACE_TAG_FUNCTION,
            body = PullRequestRemoteEntity(usn = usn),
        ).body<PlaceTagPullResponseRemoteEntity>().placeTagList

    private companion object {
        const val PUSH_PLACE_TAG_FUNCTION: String = "v1-sync-push-place-tag"
        const val PULL_PLACE_TAG_FUNCTION: String = "v1-sync-pull-place-tag"
    }
}
