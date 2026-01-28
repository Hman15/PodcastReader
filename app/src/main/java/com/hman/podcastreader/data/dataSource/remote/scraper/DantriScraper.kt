package com.hman.podcastreader.data.dataSource.remote.scraper

import com.hman.podcastreader.domain.model.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import javax.inject.Inject

class DantriScraper @Inject constructor(private val okHttpClient: OkHttpClient) {
    private val baseUrl = "https://dantri.com.vn/event/podcast-4193.htm"

    suspend fun scrapeArticles(): Result<List<Article>> =
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(baseUrl).build()
                val response = okHttpClient.newCall(request).execute()

                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("Failed to fetch Dantri: ${response.code}")
                    )
                }

                val html = response.body.string()
                val document = Jsoup.parse(html)

                val articles = parseDantriArticles(document)
                Result.success(articles)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun parseDantriArticles(document: Document): List<Article> {
        val articles = mutableListOf<Article>()

        val articleElements = document.select("article, .article-item, .news-item")

        articleElements.forEach { element ->
            try {
                val titleElement = element.select("h3 a, h2 a, .article-title a").first()
                val title = titleElement?.text()?.trim() ?: return@forEach
                val articleUrl =
                    titleElement.attr("href").let {
                        if (it.startsWith("http")) it else "https://dantri.com.vn$it"
                    }

                val description =
                    element.select("p, .article-excerpt, .sapo").text().trim().ifEmpty { null }

                val thumbnailUrl =
                    element.select("img")
                        .first()
                        ?.let { it.attr("data-src").ifEmpty { it.attr("src") } }
                        ?.let {
                            if (it.startsWith("http")) it else "https://dantri.com.vn$it"
                        }

                // Generate ID from URL
                val id = articleUrl.hashCode().toString()

                articles.add(
                    Article(
                        id = id,
                        title = title,
                        description = description,
                        thumbnailUrl = thumbnailUrl,
                        articleUrl = articleUrl,
                        audioUrl = null,
                        source = "Dantri"
                    )
                )
            } catch (e: Exception) {
                // Skip malformed articles
            }
        }

        return articles
    }
}
