package com.example.autostock.remote

import retrofit2.http.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

// Android Emulator: 10.0.2.2 = equipo anfitrión. En un teléfono real: IP local del computador.
object RemoteConfig {
    const val BASE_URL = "http://10.0.2.2:8080/"
}

data class ProductoRemoto(
    val id: Long? = null,
    val codigo: String = "",
    val nombre: String = "",
    val categoria: String = "Repuestos",
    val marca: String = "Genérica",
    val stock: Int = 0,
    val minimo: Int = 2,
    val objetivo: Int = 10,
    val precio: Double = 0.0,
    val proveedor: String = "Proveedor Demo",
    val ubicacion: String = "Bodega A"
)

data class MovimientoRemoto(
    val id: Long? = null,
    val productoId: Long = 0,
    val tipo: String = "Ingreso",
    val cantidad: Int = 0,
    val motivo: String = "",
    val anterior: Int = 0,
    val resultante: Int = 0,
    val fecha: String? = null
)

data class NuevoMovimiento(val productoId: Long, val tipo: String, val cantidad: Int, val motivo: String = "")

data class ProveedorExterno(val id: Int, val name: String, val email: String, val phone: String?)

interface AutoStockApi {
    @GET("api/productos") suspend fun productos(): List<ProductoRemoto>
    @POST("api/productos") suspend fun crear(@Body producto: ProductoRemoto): ProductoRemoto
    @PUT("api/productos/{id}") suspend fun editar(@Path("id") id: Long, @Body producto: ProductoRemoto): ProductoRemoto
    @DELETE("api/productos/{id}") suspend fun eliminar(@Path("id") id: Long)
    @GET("api/movimientos") suspend fun movimientos(): List<MovimientoRemoto>
    @POST("api/movimientos") suspend fun registrar(@Body movimiento: NuevoMovimiento): MovimientoRemoto
    @GET("api/reposicion") suspend fun reposicion(): List<ProductoRemoto>
}

interface ProveedoresApi {
    // API externa pública, datos de demostración, no proveedores comerciales reales.
    @GET("users") suspend fun proveedores(): List<ProveedorExterno>
}

object ApiFactory {
    private val http = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()
    val inventario: AutoStockApi = Retrofit.Builder().baseUrl(RemoteConfig.BASE_URL)
        .client(http).addConverterFactory(GsonConverterFactory.create()).build().create(AutoStockApi::class.java)
    val proveedores: ProveedoresApi = Retrofit.Builder().baseUrl("https://jsonplaceholder.typicode.com/")
        .client(http).addConverterFactory(GsonConverterFactory.create()).build().create(ProveedoresApi::class.java)
}
