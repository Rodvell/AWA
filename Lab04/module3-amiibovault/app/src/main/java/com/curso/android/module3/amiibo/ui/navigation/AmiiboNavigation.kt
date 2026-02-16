package com.curso.android.module3.amiibo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.curso.android.module3.amiibo.ui.screens.AmiiboDetailScreen
import com.curso.android.module3.amiibo.ui.screens.AmiiboListScreen
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Rutas de navegación de la app.
 */
object AmiiboRoutes {
    const val LIST = "amiibo_list"
    const val DETAIL = "amiibo_detail/{amiiboName}"

    fun detailRoute(amiiboName: String): String {
        val encodedName = URLEncoder.encode(amiiboName, StandardCharsets.UTF_8.toString())
        return "amiibo_detail/$encodedName"
    }
}

/**
 * Navegación principal de la app.
 */
@Composable
fun AmiiboNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = AmiiboRoutes.LIST
    ) {
        composable(AmiiboRoutes.LIST) {
            AmiiboListScreen(
                onAmiiboClick = { amiiboName ->
                    navController.navigate(AmiiboRoutes.detailRoute(amiiboName))
                }
            )
        }

        composable(AmiiboRoutes.DETAIL) { backStackEntry ->
            val encodedName = backStackEntry.arguments?.getString("amiiboName") ?: ""
            val amiiboName = URLDecoder.decode(encodedName, StandardCharsets.UTF_8.toString())
            AmiiboDetailScreen(
                amiiboName = amiiboName,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
