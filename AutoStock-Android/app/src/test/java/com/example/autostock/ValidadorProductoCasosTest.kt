package com.example.autostock

import com.example.autostock.viewmodel.ValidadorProducto
import org.junit.Assert.*
import org.junit.Test

class ValidadorProductoCasosTest {
    private fun validar(codigo: String="FIL-1",nombre: String="Filtro",stock: String="5",minimo: String="2",objetivo: String="10",precio: String="3500") =
        ValidadorProducto.validar(codigo,nombre,stock,minimo,objetivo,precio)

    @Test fun codigoVacio() { assertTrue(validar(codigo="  ").containsKey("codigo")) }
    @Test fun stockNoNumerico() { assertTrue(validar(stock="abc").containsKey("stock")) }
    @Test fun stockDecimal() { assertTrue(validar(stock="1.5").containsKey("stock")) }
    @Test fun minimoNegativo() { assertTrue(validar(minimo="-3").containsKey("minimo")) }
    @Test fun minimoVacio() { assertTrue(validar(minimo="").containsKey("minimo")) }
    @Test fun objetivoNegativo() { assertTrue(validar(objetivo="-2").containsKey("objetivo")) }
    @Test fun objetivoInvalido() { assertTrue(validar(objetivo="xyz").containsKey("objetivo")) }
    @Test fun precioNegativo() { assertTrue(validar(precio="-100").containsKey("precio")) }
    @Test fun precioInvalido() { assertTrue(validar(precio="abc").containsKey("precio")) }
    @Test fun objetivoIgualAlMinimoEsValido() { assertTrue(validar(minimo="3",objetivo="3").isEmpty()) }
    @Test fun stockCeroEsValido() { assertTrue(validar(stock="0").isEmpty()) }
    @Test fun valoresValidos() { assertTrue(validar().isEmpty()) }
}
