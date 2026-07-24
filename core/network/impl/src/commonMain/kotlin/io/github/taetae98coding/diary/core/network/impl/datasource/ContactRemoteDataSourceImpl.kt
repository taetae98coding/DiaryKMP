package io.github.taetae98coding.diary.core.network.impl.datasource

import io.github.taetae98coding.diary.core.network.api.contact.datasource.ContactRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.contact.entity.ContactPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.contact.entity.ContactRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.entity.ContactPullResponseRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.entity.ContactPushRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.entity.PullRequestRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.supabase.api.invoke
import io.ktor.client.call.body
import org.koin.core.annotation.Factory

@Factory
internal class ContactRemoteDataSourceImpl(
    private val supabaseFunction: SupabaseFunction,
) : ContactRemoteDataSource {
    override suspend fun push(contactList: List<ContactRemoteEntity>) {
        supabaseFunction(
            function = PUSH_CONTACT_FUNCTION,
            body = ContactPushRequestRemoteEntity(contactList = contactList),
        )
    }

    override suspend fun pull(usn: Long): List<ContactPullRemoteEntity> =
        supabaseFunction(
            function = PULL_CONTACT_FUNCTION,
            body = PullRequestRemoteEntity(usn = usn),
        ).body<ContactPullResponseRemoteEntity>().contactList

    private companion object {
        const val PUSH_CONTACT_FUNCTION: String = "v1-sync-push-contact"
        const val PULL_CONTACT_FUNCTION: String = "v1-sync-pull-contact"
    }
}
