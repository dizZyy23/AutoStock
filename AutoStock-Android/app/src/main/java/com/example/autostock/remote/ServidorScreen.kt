package com.example.autostock.remote

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ServidorScreen(vm: ServidorViewModel = viewModel()) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    var editar by remember { mutableStateOf<ProductoRemoto?>(null) }
    var formulario by remember { mutableStateOf(false) }
    var verProveedores by remember { mutableStateOf(false) }
    var codigo by remember(editar,formulario) { mutableStateOf(editar?.codigo ?: "") }
    var nombre by remember(editar,formulario) { mutableStateOf(editar?.nombre ?: "") }
    var stock by remember(editar,formulario) { mutableStateOf(editar?.stock?.toString() ?: "0") }
    var minimo by remember(editar,formulario) { mutableStateOf(editar?.minimo?.toString() ?: "2") }
    var ubicacion by remember(editar,formulario) { mutableStateOf(editar?.ubicacion ?: "Bodega A") }
    var validacion by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Text("Servidor Spring Boot", style = MaterialTheme.typography.titleLarge)
        Text("Productos remotos y API externa: datos ficticios", style = MaterialTheme.typography.bodySmall)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Button(onClick = vm::actualizar) { Text("Actualizar") }
            OutlinedButton(onClick = { editar = null; formulario = true }) { Text("Nuevo") }
            OutlinedButton(onClick = { verProveedores = !verProveedores; if (verProveedores) vm.cargarProveedores() }) { Text("API externa") }
        }
        if (estado.cargando) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (estado.mensaje.isNotBlank()) Text(estado.mensaje, color = MaterialTheme.colorScheme.error)
        if (verProveedores) {
            Text("Proveedores ficticios desde JSONPlaceholder", style = MaterialTheme.typography.titleMedium)
            LazyColumn { items(estado.proveedores) { p -> ListItem(headlineContent = { Text(p.name) }, supportingContent = { Text(p.email) }) } }
        } else {
            Text("Productos del backend: ${estado.productos.size}", style = MaterialTheme.typography.titleMedium)
            LazyColumn {
                items(estado.productos, key = { it.id ?: it.codigo }) { p ->
                    ElevatedCard(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(Modifier.padding(10.dp)) {
                            Text("${p.nombre} (${p.codigo})", style = MaterialTheme.typography.titleMedium)
                            Text("Stock ${p.stock} · mínimo ${p.minimo} · ${p.ubicacion}")
                            Row {
                                TextButton(onClick = { editar = p; formulario = true }) { Text("Editar") }
                                TextButton(onClick = { p.id?.let { vm.movimiento(it,"Ingreso",1) } }) { Text("+1") }
                                TextButton(onClick = { p.id?.let { vm.movimiento(it,"Venta",1) } }) { Text("-1") }
                                TextButton(onClick = { p.id?.let(vm::borrar) }) { Text("Eliminar") }
                            }
                        }
                    }
                }
                item { Text("Movimientos remotos: ${estado.movimientos.size}") }
                items(estado.movimientos.take(10)) { m -> Text("${m.tipo} · producto ${m.productoId} · ${m.anterior} → ${m.resultante}") }
            }
        }
    }
    if (formulario) AlertDialog(
        onDismissRequest = { formulario = false },
        title = { Text(if (editar == null) "Nuevo producto remoto" else "Editar producto remoto") },
        text = {
            Column {
                OutlinedTextField(codigo, { codigo = it }, label = { Text("Código") }, singleLine = true)
                OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre") }, singleLine = true)
                OutlinedTextField(stock, { stock = it }, label = { Text("Stock") }, singleLine = true)
                OutlinedTextField(minimo, { minimo = it }, label = { Text("Mínimo") }, singleLine = true)
                OutlinedTextField(ubicacion, { ubicacion = it }, label = { Text("Ubicación") }, singleLine = true)
                if (validacion.isNotBlank()) Text(validacion, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = { TextButton(onClick = {
            val s = stock.toIntOrNull(); val m = minimo.toIntOrNull()
            validacion = if (codigo.isBlank() || nombre.isBlank() || s == null || m == null || s < 0 || m < 0) "Completa los campos y usa cantidades válidas" else ""
            if (validacion.isBlank()) {
                vm.guardar((editar ?: ProductoRemoto()).copy(codigo=codigo.trim(), nombre=nombre.trim(), stock=s!!, minimo=m!!, ubicacion=ubicacion))
                formulario = false
            }
        }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = { formulario = false }) { Text("Cancelar") } }
    )
}
