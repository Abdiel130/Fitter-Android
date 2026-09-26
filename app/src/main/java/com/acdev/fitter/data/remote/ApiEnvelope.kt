package com.acdev.fitter.data.remote

import kotlinx.serialization.Serializable

/**
 * Sobre comun de todas las respuestas de la API.
 *
 * Los valores por defecto hacen que una respuesta incompleta se lea igual en lugar de lanzar: si
 * el servidor omite `data` en un error, el sobre sigue siendo valido.
 */
@Serializable
data class ApiEnvelope<T>(
    val success: Boolean = false,
    val message: String? = null,
    val data: T? = null,
    val errors: Map<String, List<String>>? = null
)
