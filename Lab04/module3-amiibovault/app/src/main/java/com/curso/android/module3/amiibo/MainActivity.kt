package com.curso.android.module3.amiibo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.curso.android.module3.amiibo.ui.navigation.AmiiboNavHost
import com.curso.android.module3.amiibo.ui.theme.AmiiboVaultTheme

/**
 * ============================================================================
 * MAIN ACTIVITY - Punto de Entrada de la UI
 * ============================================================================
 *
 * MainActivity es la primera pantalla que ve el usuario.
 * En esta app con Jetpack Compose:
 * - Extiende ComponentActivity (no AppCompatActivity)
 * - Usa setContent {} en lugar de setContentView()
 * - Toda la UI se define en composables
 *
 * ARQUITECTURA DE UNA APP COMPOSE:
 * --------------------------------
 *
 *   ┌────────────────────────────────────────────────────────────────┐
 *   │                        ACTIVITY                                │
 *   │  ┌──────────────────────────────────────────────────────────┐ │
 *   │  │                    setContent { }                         │ │
 *   │  │  ┌────────────────────────────────────────────────────┐  │ │
 *   │  │  │                  THEME                              │  │ │
 *   │  │  │  ┌──────────────────────────────────────────────┐  │  │ │
 *   │  │  │  │              SURFACE                          │  │  │ │
 *   │  │  │  │  ┌────────────────────────────────────────┐  │  │  │ │
 *   │  │  │  │  │            SCREEN                      │  │  │  │ │
 *   │  │  │  │  │       (AmiiboListScreen)               │  │  │  │ │
 *   │  │  │  │  │                                        │  │  │  │ │
 *   │  │  │  │  └────────────────────────────────────────┘  │  │  │ │
 *   │  │  │  └──────────────────────────────────────────────┘  │  │ │
 *   │  │  └────────────────────────────────────────────────────┘  │ │
 *   │  └──────────────────────────────────────────────────────────┘ │
 *   └────────────────────────────────────────────────────────────────┘
 *
 * NOTA: En apps más complejas, Navigation Compose manejaría múltiples screens.
 *
 * ============================================================================
 */
class MainActivity : ComponentActivity() {

    /**
     * =========================================================================
     * CREACIÓN DE LA ACTIVITY
     * =========================================================================
     *
     * onCreate() es el punto de entrada del lifecycle de la Activity.
     * Aquí configuramos:
     * 1. Edge-to-edge display (contenido bajo la barra de estado)
     * 2. El contenido Compose de la pantalla
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * enableEdgeToEdge():
         * - Permite que el contenido se dibuje bajo la barra de estado y navegación
         * - La app se ve más inmersiva
         * - Los componentes deben manejar padding para evitar solapamiento
         * - Scaffold maneja esto automáticamente con su paddingValues
         */
        enableEdgeToEdge()

        /**
         * =====================================================================
         * setContent { } - ENTRADA A JETPACK COMPOSE
         * =====================================================================
         *
         * setContent {} reemplaza a setContentView() de las vistas XML.
         * Todo lo que va dentro es código Compose (Composables).
         *
         * Estructura típica:
         * 1. Theme: Provee colores, tipografía, shapes
         * 2. Surface: Fondo con color del tema
         * 3. Screen/Navigation: Contenido principal
         *
         * IMPORTANTE:
         * - setContent {} solo debe llamarse UNA vez
         * - Los composables se recomponen automáticamente cuando el estado cambia
         * - NO es necesario llamar setContent {} de nuevo para actualizar la UI
         */
        setContent {
            /**
             * AmiiboVaultTheme:
             * - Wrapper que provee MaterialTheme
             * - Configura colores (light/dark/dynamic)
             * - Aplica tipografía y shapes de Material 3
             *
             * Todo composable dentro hereda estos valores via LocalComposition.
             */
            AmiiboVaultTheme {
                /**
                 * AmiiboNavHost:
                 * - Configura la navegación entre pantallas
                 * - Maneja la lista y el detalle de Amiibos
                 */
                AmiiboNavHost()
            }
        }
    }
}

/**
 * ============================================================================
 * NOTAS ADICIONALES SOBRE ACTIVITIES CON COMPOSE
 * ============================================================================
 *
 * 1. ComponentActivity vs AppCompatActivity:
 *    - ComponentActivity: Base mínima para Compose
 *    - AppCompatActivity: Incluye soporte para Views XML, Fragments, ActionBar
 *    - Para apps 100% Compose, usar ComponentActivity
 *
 * 2. NAVIGATION COMPOSE (para múltiples pantallas):
 *    ```kotlin
 *    setContent {
 *        val navController = rememberNavController()
 *        NavHost(navController, startDestination = "list") {
 *            composable("list") { AmiiboListScreen(navController) }
 *            composable("detail/{id}") { backStackEntry ->
 *                val id = backStackEntry.arguments?.getString("id")
 *                AmiiboDetailScreen(id)
 *            }
 *        }
 *    }
 *    ```
 *
 * 3. HANDLING SYSTEM BACK:
 *    ```kotlin
 *    BackHandler(enabled = showDialog) {
 *        showDialog = false
 *    }
 *    ```
 *
 * 4. LIFECYCLE EN COMPOSE:
 *    ```kotlin
 *    val lifecycleOwner = LocalLifecycleOwner.current
 *    DisposableEffect(lifecycleOwner) {
 *        val observer = LifecycleEventObserver { _, event ->
 *            when (event) {
 *                Lifecycle.Event.ON_RESUME -> { /* ... */ }
 *                Lifecycle.Event.ON_PAUSE -> { /* ... */ }
 *                else -> {}
 *            }
 *        }
 *        lifecycleOwner.lifecycle.addObserver(observer)
 *        onDispose {
 *            lifecycleOwner.lifecycle.removeObserver(observer)
 *        }
 *    }
 *    ```
 *
 * 5. CONFIGURATION CHANGES:
 *    - Compose maneja automáticamente rotación y cambios de configuración
 *    - El ViewModel sobrevive a estos cambios
 *    - remember {} preserva estado durante recomposición
 *    - rememberSaveable {} preserva estado incluso con process death
 *
 * ============================================================================
 */
