package com.acdev.fitter.core.util

import java.util.UUID

/**
 * Generador de identificadores.
 *
 * El id lo crea siempre el cliente, nunca el servidor. Asi una fila tiene identidad desde que se
 * escribe en local y la sincronizacion posterior no tiene que reconciliar identificadores.
 */
fun interface IdGenerator {
    fun newId(): String
}

/** Implementacion por defecto: UUID v4 en texto, compatible con la columna uuid del servidor. */
object UuidGenerator : IdGenerator {
    override fun newId(): String = UUID.randomUUID().toString()
}
