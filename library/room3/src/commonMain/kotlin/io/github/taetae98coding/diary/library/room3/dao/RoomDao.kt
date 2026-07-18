package io.github.taetae98coding.diary.library.room3.dao

import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Upsert

public interface RoomDao<T> {
    @Upsert
    public suspend fun upsert(entity: T)

    @Upsert
    public suspend fun upsert(entityList: List<T>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public suspend fun insertIgnore(entity: T)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public suspend fun insertIgnore(entityList: List<T>)
}
