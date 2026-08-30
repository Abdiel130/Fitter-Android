package com.acdev.fitter

import android.app.Application
import com.acdev.fitter.core.di.AppContainer

/**
 * Punto de entrada del proceso. Solo construye el contenedor de dependencias: cualquier trabajo
 * pesado en `onCreate` retrasaria el arranque en frio.
 */
class FitterApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
