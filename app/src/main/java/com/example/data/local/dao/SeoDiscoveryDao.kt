package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.SeoArticleEntity
import com.example.data.local.entity.SeoConfigEntity
import com.example.data.local.entity.SeoReferralLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SeoDiscoveryDao {
    @Query("SELECT * FROM seo_articles WHERE status = 'PUBLISHED' ORDER BY updatedAtMillis DESC")
    fun observePublishedArticles(): Flow<List<SeoArticleEntity>>

    @Query("SELECT * FROM seo_articles ORDER BY updatedAtMillis DESC")
    fun observeAllArticles(): Flow<List<SeoArticleEntity>>

    @Query("SELECT * FROM seo_articles WHERE status = 'PUBLISHED'")
    suspend fun getPublishedArticlesOnce(): List<SeoArticleEntity>

    @Query("SELECT * FROM seo_articles")
    suspend fun getAllArticlesOnce(): List<SeoArticleEntity>

    @Query("SELECT * FROM seo_articles WHERE id = :id LIMIT 1")
    suspend fun getArticleById(id: String): SeoArticleEntity?

    @Query("SELECT * FROM seo_articles WHERE slug = :slug LIMIT 1")
    suspend fun getArticleBySlug(slug: String): SeoArticleEntity?

    @Query("SELECT COUNT(*) FROM seo_articles")
    suspend fun countArticles(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertArticle(article: SeoArticleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertArticles(articles: List<SeoArticleEntity>)

    @Query("DELETE FROM seo_articles WHERE id = :id")
    suspend fun deleteArticleById(id: String)

    @Query("SELECT * FROM seo_config WHERE id = 'default' LIMIT 1")
    fun observeConfig(): Flow<SeoConfigEntity?>

    @Query("SELECT * FROM seo_config WHERE id = 'default' LIMIT 1")
    suspend fun getConfigOnce(): SeoConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConfig(config: SeoConfigEntity)

    @Query("SELECT * FROM seo_referral_logs ORDER BY timestampMillis DESC LIMIT 500")
    fun observeReferralLogs(): Flow<List<SeoReferralLogEntity>>

    @Query("SELECT * FROM seo_referral_logs ORDER BY timestampMillis DESC LIMIT 500")
    suspend fun getReferralLogsOnce(): List<SeoReferralLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReferralLog(log: SeoReferralLogEntity)
}
