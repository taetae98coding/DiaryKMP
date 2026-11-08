package io.github.taetae98coding.diary

import io.github.taetae98coding.diary.work.appaction.memolink.MemoLinkAppAction

public object DiaryMemoLinkAppAction {
    public suspend fun tagAdd(
        memoId: String,
        tagId: String,
    ): Boolean = memoLinkAppAction().tagAdd(memoId = memoId, tagId = tagId).done

    public suspend fun tagRemove(
        memoId: String,
        tagId: String,
    ): Boolean = memoLinkAppAction().tagRemove(memoId = memoId, tagId = tagId).done

    public suspend fun primaryTagSet(
        memoId: String,
        tagId: String,
    ): Boolean = memoLinkAppAction().primaryTagSet(memoId = memoId, tagId = tagId).done

    public suspend fun primaryTagUnset(memoId: String): Boolean = memoLinkAppAction().primaryTagUnset(memoId = memoId).done

    public suspend fun placeAdd(
        memoId: String,
        placeId: String,
    ): Boolean = memoLinkAppAction().placeAdd(memoId = memoId, placeId = placeId).done

    public suspend fun placeRemove(
        memoId: String,
        placeId: String,
    ): Boolean = memoLinkAppAction().placeRemove(memoId = memoId, placeId = placeId).done

    public suspend fun webAdd(
        memoId: String,
        webId: String,
    ): Boolean = memoLinkAppAction().webAdd(memoId = memoId, webId = webId).done

    public suspend fun webRemove(
        memoId: String,
        webId: String,
    ): Boolean = memoLinkAppAction().webRemove(memoId = memoId, webId = webId).done

    public suspend fun contactAdd(
        memoId: String,
        contactId: String,
    ): Boolean = memoLinkAppAction().contactAdd(memoId = memoId, contactId = contactId).done

    public suspend fun contactRemove(
        memoId: String,
        contactId: String,
    ): Boolean = memoLinkAppAction().contactRemove(memoId = memoId, contactId = contactId).done
}

private fun memoLinkAppAction(): MemoLinkAppAction = diaryAppAction()
