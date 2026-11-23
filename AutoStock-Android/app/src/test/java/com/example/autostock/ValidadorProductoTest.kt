package com.example.autostock
import com.example.autostock.viewmodel.ValidadorProducto
import org.junit.Assert.*
import org.junit.Test
class ValidadorProductoTest {
    @Test fun aceptaFormularioCorrecto() {
        assertTrue(ValidadorProducto.validar("FIL-1", "Filtro", "5", "2", "10", "3500").isEmpty())
    }
    @Test fun rechazaNombreVacio() {
        assertTrue(ValidadorProducto.validar("1", "", "5", "2", "10", "3500").containsKey("nombre"))
    }
    @Test fun rechazaStockNegativo() {
        assertTrue(ValidadorProducto.validar("1", "Filtro", "-1", "2", "10", "3500").containsKey("stock"))
    }
    @Test fun rechazaObjetivoMenorAlMinimo() {
        assertTrue(ValidadorProducto.validar("1", "Filtro", "5", "9", "3", "3500").containsKey("objetivo"))
    }
}
