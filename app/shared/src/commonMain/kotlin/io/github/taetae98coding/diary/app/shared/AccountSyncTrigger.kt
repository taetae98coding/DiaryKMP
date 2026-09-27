package io.github.taetae98coding.diary.app.shared

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.uuid.Uuid

// 확인한 계정을 수집 단위로 기억하므로, 앱이 다시 활성 상태가 되거나 화면이 다시 만들어져 새로 수집하면 처음 확인으로 다시 센다.
internal fun Flow<Account>.toSyncTrigger(): Flow<SyncTrigger> =
    flow {
        var confirmedAccountId: Uuid? = null

        collect { account ->
            val trigger =
                when (account) {
                    is Account.Guest -> {
                        confirmedAccountId = null
                        SyncTrigger.ACCOUNT_CONFIRMED
                    }

                    is Account.User -> {
                        if (account.id == confirmedAccountId) {
                            SyncTrigger.ACCOUNT_UPDATED
                        } else {
                            if (account.isSessionValid) confirmedAccountId = account.id
                            SyncTrigger.ACCOUNT_CONFIRMED
                        }
                    }
                }

            emit(trigger)
        }
    }
