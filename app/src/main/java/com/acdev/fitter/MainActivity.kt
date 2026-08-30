package com.acdev.fitter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.acdev.fitter.ui.FitterApp
import com.acdev.fitter.ui.di.LocalAppContainer

/**
 * Unica actividad de la app.
 *
 * Su trabajo es minimo: activar el modo de borde a borde, publicar el contenedor de dependencias
 * en la composicion y ceder el control a `FitterApp`.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val container = (application as FitterApplication).container

        setContent {
            CompositionLocalProvider(LocalAppContainer provides container) {
                FitterApp()
            }
        }
    }
}
