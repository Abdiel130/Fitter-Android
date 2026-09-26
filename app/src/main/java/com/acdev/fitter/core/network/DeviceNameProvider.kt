package com.acdev.fitter.core.network

import android.os.Build

/**
 * Nombre con el que el servidor identifica esta sesion en la lista de dispositivos del usuario.
 */
fun interface DeviceNameProvider {
    fun deviceName(): String
}

class AndroidDeviceNameProvider(private val platformLabel: String) : DeviceNameProvider {

    override fun deviceName(): String =
        "${Build.MANUFACTURER} ${Build.MODEL} - $platformLabel ${Build.VERSION.RELEASE}"
}
