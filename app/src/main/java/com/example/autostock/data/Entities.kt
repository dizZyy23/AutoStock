package com.example.autostock.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "productos", indices = [Index(value = ["codigo"], unique = true)])
data class Producto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val codigo: String,
    val nombre: String,
    val descripcion: String = "",
    val categoria: String = "Repuestos",
    val marca: String = "Genérica",
    val stock: Int = 0,
    val minimo: Int = 2,
    val objetivo: Int = 10,
    val precio: Double = 0.0,
    val proveedor: String = "Proveedor Demo",
    val ubicacion: String = "Bodega A - Estante 1"
)

@Entity(tableName = "movimientos", foreignKeys = [ForeignKey(entity = Producto::class, parentColumns = ["id"], childColumns = ["productoId"], onDelete = ForeignKey.CASCADE)], indices = [Index("productoId")])
data class Movimiento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productoId: Long,
    val nombreProducto: String,
    val tipo: String,
    val cantidad: Int,
    val anterior: Int,
    val resultante: Int,
    val motivo: String = "",
    val fecha: Long = System.currentTimeMillis()
)

@Dao
interface InventarioDao {
    @Query("SELECT * FROM productos ORDER BY nombre ASC") fun observarProductos(): Flow<List<Producto>>
    @Query("SELECT * FROM movimientos ORDER BY fecha DESC") fun observarMovimientos(): Flow<List<Movimiento>>
    @Query("SELECT * FROM productos WHERE id = :id LIMIT 1") suspend fun obtener(id: Long): Producto?
    @Query("SELECT COUNT(*) FROM productos") suspend fun contar(): Int
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insertarProducto(producto: Producto): Long
    @Update suspend fun actualizarProducto(producto: Producto)
    @Delete suspend fun borrarProducto(producto: Producto)
    @Insert suspend fun insertarMovimiento(movimiento: Movimiento)
}

@Database(entities = [Producto::class, Movimiento::class], version = 1, exportSchema = false)
abstract class InventarioDatabase : RoomDatabase() {
    abstract fun dao(): InventarioDao
}
