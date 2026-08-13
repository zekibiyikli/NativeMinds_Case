package com.zekibiyikli.nativemindscase.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zekibiyikli.nativemindscase.core.analytics.AnalyticsSource
import com.zekibiyikli.nativemindscase.ui.detail.DetailScreen
import com.zekibiyikli.nativemindscase.ui.favorites.FavoritesScreen
import com.zekibiyikli.nativemindscase.ui.home.HomeScreen
import com.zekibiyikli.nativemindscase.ui.premium.PremiumScreen
import com.zekibiyikli.nativemindscase.ui.search.SearchResultsScreen
import com.zekibiyikli.nativemindscase.ui.search.SearchScreen
import com.zekibiyikli.nativemindscase.ui.splash.SplashScreen

@Composable
fun NativeMindsNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = SplashRoute,
        modifier = modifier
    ) {
        composable<SplashRoute> {
            SplashScreen(
                onFinished = {
                    // Splash yiginda kalmamali; geri tusu ona donmesin.
                    navController.navigate(HomeRoute) {
                        popUpTo<SplashRoute> { inclusive = true }
                    }
                }
            )
        }

        composable<HomeRoute> {
            HomeScreen(
                onItemClick = {
                    navController.navigate(DetailRoute(it, source = AnalyticsSource.HOME))
                },
                onSearchClick = { navController.navigate(SearchRoute) },
                onFavoritesClick = { navController.navigate(FavoritesRoute) },
                onPremiumClick = {
                    navController.navigate(PremiumRoute(source = AnalyticsSource.HOME))
                }
            )
        }

        composable<SearchRoute> {
            SearchScreen(
                onSearch = { navController.navigate(SearchResultsRoute(query = it)) },
                onSubjectClick = { navController.navigate(SearchResultsRoute(subjectId = it)) },
                onItemClick = {
                    navController.navigate(DetailRoute(it, source = AnalyticsSource.SEARCH))
                },
                onBackClick = navController::navigateUp
            )
        }

        composable<SearchResultsRoute> {
            SearchResultsScreen(
                onItemClick = {
                    navController.navigate(DetailRoute(it, source = AnalyticsSource.SEARCH_RESULTS))
                },
                onBackClick = navController::navigateUp
            )
        }

        composable<FavoritesRoute> {
            FavoritesScreen(
                onItemClick = {
                    navController.navigate(DetailRoute(it, source = AnalyticsSource.FAVORITES))
                },
                onBackClick = navController::navigateUp
            )
        }

        composable<DetailRoute> {
            DetailScreen(
                onBackClick = navController::navigateUp,
                onQuotaExceeded = {
                    // Detail'i yiginda birakmiyoruz; geri tusu kilitli ekrana donmesin.
                    navController.navigate(PremiumRoute(source = AnalyticsSource.QUOTA_GATE)) {
                        popUpTo<DetailRoute> { inclusive = true }
                    }
                }
            )
        }

        composable<PremiumRoute> {
            PremiumScreen(
                onBackClick = navController::navigateUp,
                onPurchased = navController::navigateUp
            )
        }
    }
}
