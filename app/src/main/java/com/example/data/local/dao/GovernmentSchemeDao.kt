package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.GovernmentSchemeBookmarkEntity
import com.example.data.local.entity.GovernmentSchemeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GovernmentSchemeDao {

    @Query(
        "SELECT * FROM cached_government_schemes " +
            "ORDER BY discontinued ASC, northBengalPriority DESC, nameEn ASC"
    )
    fun observeCachedSchemes(): Flow<List<GovernmentSchemeEntity>>

    @Query("SELECT MAX(cachedAtMillis) FROM cached_government_schemes")
    fun observeLastCachedAtMillis(): Flow<Long?>

    @Query("SELECT * FROM cached_government_schemes WHERE id = :id LIMIT 1")
    suspend fun getCachedSchemeById(id: String): GovernmentSchemeEntity?

    @Query("SELECT * FROM cached_government_schemes")
    suspend fun getAllCachedSchemesSync(): List<GovernmentSchemeEntity>

    @Query("SELECT COUNT(*) FROM cached_government_schemes")
    suspend fun getSchemeCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSchemes(schemes: List<GovernmentSchemeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertScheme(scheme: GovernmentSchemeEntity)

    @Query("DELETE FROM cached_government_schemes")
    suspend fun clearAllCachedSchemes()

    @Query(
        "SELECT schemeId FROM government_scheme_bookmarks " +
            "WHERE userId = :userId ORDER BY bookmarkedAtMillis DESC"
    )
    fun observeBookmarkedIds(userId: String): Flow<List<String>>

    @Query(
        "SELECT cached_government_schemes.* FROM cached_government_schemes " +
            "INNER JOIN government_scheme_bookmarks " +
            "ON cached_government_schemes.id = government_scheme_bookmarks.schemeId " +
            "WHERE government_scheme_bookmarks.userId = :userId " +
            "ORDER BY government_scheme_bookmarks.bookmarkedAtMillis DESC"
    )
    fun observeBookmarkedSchemes(userId: String): Flow<List<GovernmentSchemeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: GovernmentSchemeBookmarkEntity)

    @Query(
        "DELETE FROM government_scheme_bookmarks " +
            "WHERE userId = :userId AND schemeId = :schemeId"
    )
    suspend fun deleteBookmark(userId: String, schemeId: String)

    @Query(
        "SELECT EXISTS(SELECT 1 FROM government_scheme_bookmarks " +
            "WHERE userId = :userId AND schemeId = :schemeId)"
    )
    suspend fun isBookmarkedSync(userId: String, schemeId: String): Boolean

    @Query("DELETE FROM government_scheme_bookmarks WHERE userId = :userId")
    suspend fun clearBookmarksForUser(userId: String)
}
