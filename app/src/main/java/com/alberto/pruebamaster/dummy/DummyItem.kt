package com.alberto.pruebamaster.dummy

/**
 * Esta clase la utilizamos para almacenar los posts
 * En el constructor le pasamos el id, titulo y resumen
 */
data class DummyItem(val id: String, val title: String, val resumen: String) {
    // personalizamos toString para que nos devuelva el titulo
    override fun toString(): String = title
}