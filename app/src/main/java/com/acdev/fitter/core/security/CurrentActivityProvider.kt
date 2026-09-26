package com.acdev.fitter.core.security

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import java.lang.ref.WeakReference

/**
 * Actividad en primer plano.
 *
 * `BiometricPrompt` necesita una actividad viva y el autenticador de red no tiene ninguna a mano,
 * asi que se anota aqui. La referencia es debil para no sostener la actividad tras destruirse, y
 * registrarse cuesta lo mismo que una lambda: nada que retrase el arranque en frio.
 */
class CurrentActivityProvider : Application.ActivityLifecycleCallbacks {

    private var reference: WeakReference<FragmentActivity>? = null

    /** Null cuando la app esta en segundo plano: entonces no se puede pedir ninguna huella. */
    fun current(): FragmentActivity? = reference?.get()

    override fun onActivityResumed(activity: Activity) {
        val fragmentActivity = activity as? FragmentActivity ?: return
        reference = WeakReference(fragmentActivity)
    }

    override fun onActivityPaused(activity: Activity) {
        if (reference?.get() === activity) reference = null
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}
