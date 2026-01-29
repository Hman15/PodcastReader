package com.hman.podcastreader.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.hman.podcastreader.presentation.articledetail.ArticleDetailScreen
import com.hman.podcastreader.presentation.articlelist.ArticleListScreen
import com.hman.podcastreader.presentation.audiolist.AudioListScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Audio : Screen("audio")
    data object ArticleDetail : Screen("article_detail/{articleId}") {
        fun createRoute(articleId: String) = "article_detail/$articleId"
    }
}

@Composable
fun NavGraph(
        navController: NavHostController,
        startDestination: String = Screen.Home.route,
        modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier
    ) {
        composable(Screen.Home.route) {
            ArticleListScreen(
                    onArticleClick = { article ->
                        navController.navigate(Screen.ArticleDetail.createRoute(article.id))
                    }
            )
        }

        composable(Screen.Audio.route) { AudioListScreen() }

        composable(
                route = Screen.ArticleDetail.route,
                arguments = listOf(navArgument("articleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val articleId = backStackEntry.arguments?.getString("articleId") ?: return@composable
            ArticleDetailScreen(
                    articleId = articleId,
                    onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
