package com.example.autostock.qr

/**
 * Formato de demostración: AUTOSTOCK|codigo|nombre|categoria|marca|stock|minimo|objetivo|precio|proveedor|ubicacion|descripcion
 * También acepta AUTOSTOCK|codigo|nombre (los demás campos toman valores seguros por defecto).
 * No se guardan datos automáticamente: el usuario revisa el formulario antes de confirmar.
 */
data class QrProductData(
    val codigo: String,
    val nombre: String,
    val categoria: String = "Repuestos",
    val marca: String = "",
    val stock: Int = 0,
    val minimo: Int = 2,
    val objetivo: Int = 10,
    val precio: Double = 0.0,
    val proveedor: String = "Proveedor Demo",
    val ubicacion: String = "Bodega A - Estante 1",
    val descripcion: String = ""
)

object QrProductParser {
    sealed interface Result {
        data class Success(val producto: QrProductData) : Result
        data class Error(val message: String) : Result
    }
    fun parse(raw: String): Result {
        if (raw.length > 1200) return Result.Error("El QR es demasiado largo")
        val p = raw.split('|')
        if (p.size !in 3..12 || p[0] != "AUTOSTOCK") {
            return Result.Error("QR no válido. Debe comenzar con AUTOSTOCK|codigo|nombre")
        }
        val codigo = p[1].trim()
        val nombre = p[2].trim()
        if (codigo.isEmpty() || nombre.isEmpty() || codigo.length > 40 || nombre.length > 100) {
            return Result.Error("El QR debe incluir código y nombre válidos")
        }
        fun field(index: Int, default: String) = p.getOrNull(index)?.trim()?.takeIf { it.isNotEmpty() } ?: default
        val stock = field(5,"0").toIntOrNull()
        val minimo = field(6,"2").toIntOrNull()
        val objetivo = field(7,"10").toIntOrNull()
        val precio = field(8,"0").toDoubleOrNull()
        if (stock == null || minimo == null || objetivo == null || precio == null ||
            stock < 0 || minimo < 0 || objetivo < minimo || precio < 0 || !precio.isFinite()) {
            return Result.Error("El QR contiene stock, mínimo, objetivo o precio inválidos")
        }
        return Result.Success(QrProductData(
            codigo, nombre, field(3,"Repuestos"), field(4,""), stock, minimo,
            objetivo, precio, field(9,"Proveedor Demo"),
            field(10,"Bodega A - Estante 1"), field(11,"")
        ))
    }
}
