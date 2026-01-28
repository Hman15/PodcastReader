package com.hman.podcastreader.data.repository

import com.hman.podcastreader.data.dataSource.local.dao.ArticleDao
import com.hman.podcastreader.data.dataSource.remote.scraper.DantriScraper
import com.hman.podcastreader.data.dataSource.remote.scraper.VnExpressScraper
import com.hman.podcastreader.data.mapper.toDomain
import com.hman.podcastreader.data.mapper.toEntity
import com.hman.podcastreader.domain.model.Article
import com.hman.podcastreader.domain.repository.ArticleRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArticleRepositoryImpl
@Inject
constructor(
    private val articleDao: ArticleDao,
    private val vnExpressScraper: VnExpressScraper,
    private val dantriScraper: DantriScraper
) : ArticleRepository {
    override suspend fun fetchArticles(): Result<List<Article>> {
        return try {
            val vnExpressResult = vnExpressScraper.scrapeArticles()
            val dantriResult = dantriScraper.scrapeArticles()

            val allArticles = mutableListOf<Article>()

            vnExpressResult.getOrNull()?.let { allArticles.addAll(it) }
            dantriResult.getOrNull()?.let { allArticles.addAll(it) }

            if (allArticles.isEmpty()) {
                // Both scrapers failed
                val error = vnExpressResult.exceptionOrNull() ?: dantriResult.exceptionOrNull()
                return Result.failure(error ?: Exception("Failed to fetch articles"))
            }

            // Remove duplicates based on articleUrl
            val uniqueArticles = allArticles.distinctBy { it.articleUrl }

            // Save the articles to database
            articleDao.insertArticles(uniqueArticles.map { it.toEntity() })

            Result.success(uniqueArticles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getArticleById(id: String): Result<Article?> {
        return try {
            val article = articleDao.getArticleById(id)?.toDomain()
            Result.success(article)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
