package io.github.taetae98coding.diary.data.tag.mapper

import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import io.github.taetae98coding.diary.core.model.tag.TagScope

public fun TagScope.toLocal(): TagScopeLocalEntity =
    when (this) {
        TagScope.SELF -> TagScopeLocalEntity.SELF
        TagScope.CHILD -> TagScopeLocalEntity.CHILD
        TagScope.DESCENDANT -> TagScopeLocalEntity.DESCENDANT
    }

internal fun TagScopeLocalEntity.toDomain(): TagScope =
    when (this) {
        TagScopeLocalEntity.SELF -> TagScope.SELF
        TagScopeLocalEntity.CHILD -> TagScope.CHILD
        TagScopeLocalEntity.DESCENDANT -> TagScope.DESCENDANT
    }
