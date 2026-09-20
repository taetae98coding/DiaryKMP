package io.github.taetae98coding.diary.core.testing.tag

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.network.api.tag.entity.TagRemoteEntity

public fun FixtureMonkey.tag(
    isFinished: Boolean,
    isDeleted: Boolean,
): Tag =
    giveMeKotlinBuilder<Tag>()
        .setExp(Tag::isFinished, isFinished)
        .setExp(Tag::isDeleted, isDeleted)
        .sample()

public fun FixtureMonkey.localTag(
    isFinished: Boolean,
    isDeleted: Boolean,
): TagLocalEntity =
    giveMeKotlinBuilder<TagLocalEntity>()
        .setExp(TagLocalEntity::isFinished, isFinished)
        .setExp(TagLocalEntity::isDeleted, isDeleted)
        .sample()

public fun FixtureMonkey.remoteTag(
    isFinished: Boolean,
    isDeleted: Boolean,
): TagRemoteEntity =
    giveMeKotlinBuilder<TagRemoteEntity>()
        .setExp(TagRemoteEntity::isFinished, isFinished)
        .setExp(TagRemoteEntity::isDeleted, isDeleted)
        .sample()
