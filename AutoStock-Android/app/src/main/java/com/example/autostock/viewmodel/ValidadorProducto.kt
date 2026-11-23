package com.example.autostock.viewmodel

object ValidadorProducto {
    fun validar(codigo: String, nombre: String, stock: String, minimo: String, objetivo: String, precio: String): Map<String,String> = buildMap {
        if (codigo.isBlank()) put("codigo", "Ingresa un código")
        if (nombre.isBlank()) put("nombre", "Ingresa el nombre")
        if (stock.toIntOrNull() == null || stock.toInt() < 0) put("stock", "Usa un número igual o mayor que cero")
        if (minimo.toIntOrNull() == null || minimo.toInt() < 0) put("minimo", "El mínimo debe ser válido")
        if (objetivo.toIntOrNull() == null || objetivo.toInt() < 0) put("objetivo", "El objetivo debe ser válido")
        if (precio.toDoubleOrNull() == null || precio.toDouble() < 0) put("precio", "Ingresa un precio válido")
        if (minimo.toIntOrNull() != null && objetivo.toIntOrNull() != null && objetivo.toInt() < minimo.toInt()) put("objetivo", "El objetivo debe ser al menos el mínimo")
    }
}
