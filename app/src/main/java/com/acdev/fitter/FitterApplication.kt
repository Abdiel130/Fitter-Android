package com.acdev.fitter

import android.app.Application
import com.acdev.fitter.core.di.AppContainer

/**
 * Punto de entrada del proceso. Construye el contenedor de dependencias y registra el observador de
 * actividades que la biometria necesita: cualquier trabajo mas pesado retrasaria el arranque en
 * frio, y todo lo demas dentro del contenedor es perezoso.
 */
class FitterApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        registerActivityLifecycleCallbacks(container.activityProvider)
    }
}
