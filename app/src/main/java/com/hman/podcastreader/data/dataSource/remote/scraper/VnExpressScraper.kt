package com.hman.podcastreader.data.dataSource.remote.scraper

import com.hman.podcastreader.domain.model.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import javax.inject.Inject

class VnExpressScraper @Inject constructor(private val okHttpClient: OkHttpClient) {
    private val baseUrl = "https://vnexpress.net/vne-go/podcast"

    suspend fun scrapeArticles(): Result<List<Article>> =
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(baseUrl).build()
                val response = okHttpClient.newCall(request).execute()

                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("Failed to fetch VnExpress: ${response.code}")
                    )
                }

                val html = response.body.string()
                val document = Jsoup.parse(html)

                val articles = parseVnExpressArticles(document)
                Result.success(articles)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun parseVnExpressArticles(document: Document): List<Article> {
        val articles = mutableListOf<Article>()

        val articleElements = document.select("article.item-ev")

        articleElements.forEach { element ->
            try {
                val playerDataJson = element.attr("data-player")
                val titleElement = element.select("h3.title-ev a").first()

                if (playerDataJson.isNotEmpty() && titleElement != null) {
                    val jsonObject = JSONObject(playerDataJson)
                    val playlist = jsonObject.optJSONArray("playlist")
                    val firstItem = playlist?.optJSONObject(0)

                    val title = titleElement.text().trim()
                    val articleUrl =
                        titleElement.attr("href").let {
                            if (it.startsWith("http")) it else "https://vnexpress.net$it"
                        }

                    // 3. Extract data directly from the JSON object
                    val audioUrl = firstItem?.optString("src")
                    val description =
                        firstItem?.optString("lead") // VnExpress calls description 'lead'
                    val thumbnailUrl = firstItem?.optString("thumbnail")

                    articles.add(
                        Article(
                            id = articleUrl.hashCode().toString(),
                            title = title,
                            description = description,
                            thumbnailUrl = thumbnailUrl,
                            articleUrl = articleUrl,
                            audioUrl = audioUrl,
                            source = "VnExpress"
                        )
                    )
                }
            } catch (_: Exception) {
            }
        }

        return articles
    }
}
