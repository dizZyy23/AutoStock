package com.example.autostock

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.autostock.data.Producto
import com.example.autostock.viewmodel.InventarioViewModel
import com.example.autostock.viewmodel.ValidadorProducto
import com.example.autostock.qr.QrScannerScreen
import com.example.autostock.qr.QrProductParser
import com.example.autostock.remote.ServidorScreen
import com.example.autostock.qr.QrProductData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

private val Azul = Color(0xFF165C93)
private val Fondo = Color(0xFFF4F7FA)
private val Naranja = Color(0xFFAA5700)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = Azul, background = Fondo)) {
                AutoStockApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoStockApp(vm: InventarioViewModel = viewModel()) {
    var inicioSesion by rememberSaveable { mutableStateOf(false) }
    var usuario by rememberSaveable { mutableStateOf("") }
    var clave by rememberSaveable { mutableStateOf("") }
    var errorLogin by remember { mutableStateOf(false) }
    var ruta by rememberSaveable { mutableStateOf("Inicio") }
    var buscando by rememberSaveable { mutableStateOf("") }
    var seleccionado by remember { mutableStateOf<Producto?>(null) }
    var confirmEliminar by remember { mutableStateOf<Producto?>(null) }
    var codigoEscaneado by remember { mutableStateOf("") }
    val productos by vm.productos.collectAsStateWithLifecycle()
    val movimientos by vm.movimientos.collectAsStateWithLifecycle()
    val mensaje by vm.mensaje.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current

    var qrData by remember { mutableStateOf<QrProductData?>(null) }
    var qrError by remember { mutableStateOf<String?>(null) }
    var qrCameraGranted by remember { mutableStateOf(
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    ) }
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        qrCameraGranted = granted
    }
    fun procesarQr(valor: String) {
        when (val resultado = QrProductParser.parse(valor)) {
            is QrProductParser.Result.Success -> {
                val existe = productos.any { it.codigo.equals(resultado.producto.codigo, ignoreCase = true) }
                if (existe) {
                    qrError = "El código ${resultado.producto.codigo} ya existe. Puedes buscarlo en Productos."
                } else {
                    qrData = resultado.producto
                    seleccionado = null
                    ruta = "Formulario"
                }
            }
            is QrProductParser.Result.Error -> qrError = resultado.message
        }
    }

    LaunchedEffect(mensaje) {
        if (mensaje != null) {
            snackbar.showSnackbar(mensaje!!)
            vm.limpiarMensaje()
        }
    }

    if (!inicioSesion) {
        Column(Modifier.fillMaxSize().background(Fondo).padding(28.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Inventory2, null, tint = Azul, modifier = Modifier.size(76.dp))
            Spacer(Modifier.height(12.dp))
            Text("AutoStock", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = Azul)
            Text("Inventario para taller automotriz", color = Color.DarkGray)
            Spacer(Modifier.height(30.dp))
            OutlinedTextField(usuario, { usuario = it; errorLogin = false }, label = { Text("Usuario") }, leadingIcon = { Icon(Icons.Default.Person, null) }, singleLine = true, modifier = Modifier.fillMaxWidth(), isError=errorLogin)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(clave, { clave = it; errorLogin = false }, label = { Text("PIN") }, leadingIcon = { Icon(Icons.Default.Lock, null) }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), singleLine = true, modifier = Modifier.fillMaxWidth(), isError=errorLogin)
            AnimatedVisibility(errorLogin) { Text("Usuario o PIN incorrecto", color = MaterialTheme.colorScheme.error) }
            Spacer(Modifier.height(18.dp))
            Button(onClick = { if (usuario.trim() == "admin" && clave == "1234") inicioSesion = true else errorLogin = true }, modifier = Modifier.fillMaxWidth()) { Text("Ingresar") }
            Text("Cuenta de demostración: admin / 1234", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(title = { Text(if (ruta == "Inicio") "AutoStock" else ruta, fontWeight = FontWeight.Bold) },
                navigationIcon = { if (ruta != "Inicio") IconButton(onClick = { ruta = "Inicio"; seleccionado = null }) { Icon(Icons.Default.ArrowBack, "Volver") } },
                actions = {
                    if (ruta == "Inicio") IconButton(onClick = { ruta = "Servidor" }) { Icon(Icons.Default.Cloud, "Abrir servidor REST") }
                    if (ruta == "Productos") IconButton(onClick = { qrError = null; ruta = "Escanear QR" }) { Icon(Icons.Default.QrCodeScanner, "Agregar producto por QR") }
                    if (ruta == "Inicio") IconButton(onClick = { inicioSesion = false; clave = "" }) { Icon(Icons.Default.Logout, "Cerrar sesión") }
                })
        },
        bottomBar = {
            NavigationBar {
                listOf(Triple("Inicio", Icons.Default.Home, "Inicio"), Triple("Productos", Icons.Default.Inventory2, "Productos"), Triple("Movimientos", Icons.Default.SwapHoriz, "Movimientos"), Triple("Reposición", Icons.Default.ShoppingCart, "Reposición")).forEach { (nombre, icono, etiqueta) ->
                    NavigationBarItem(selected = ruta == nombre, onClick = { ruta = nombre; seleccionado = null }, icon = { Icon(icono, etiqueta) }, label = { Text(etiqueta, maxLines = 1) })
                }
            }
        },
        floatingActionButton = {
            if (ruta == "Productos") FloatingActionButton(onClick = { seleccionado = null; ruta = "Formulario" }) { Icon(Icons.Default.Add, "Agregar producto") }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (ruta) {
                "Inicio" -> InicioScreen(productos, movimientos.size, ir = { ruta = it })
                "Servidor" -> ServidorScreen()
                "Productos" -> ProductosScreen(productos, buscando, { buscando = it }, onEdit = { seleccionado = it; ruta = "Formulario" }, onDelete = { confirmEliminar = it }, onMovimiento = { seleccionado = it; ruta = "Registrar movimiento" }, codigoEscaneado = codigoEscaneado)
                "Formulario" -> FormularioScreen(seleccionado, qrData, onGuardar = { vm.guardar(it) { ruta = "Productos"; seleccionado = null; qrData = null } }, onCancelar = { ruta = "Productos"; qrData = null })
                "Escanear QR" -> QrScannerScreen(
                    hasCameraPermission = qrCameraGranted,
                    onRequestPermission = { cameraPermission.launch(Manifest.permission.CAMERA) },
                    onResult = { procesarQr(it) },
                    error = qrError,
                    onRetry = { qrError = null },
                    onCancel = { ruta = "Productos" }
                )
                "Movimientos" -> MovimientosScreen(movimientos, productos, onAgregar = { seleccionado = it; ruta = "Registrar movimiento" })
                "Registrar movimiento" -> MovimientoFormScreen(seleccionado, onGuardar = { tipo, cantidad, motivo -> vm.registrar(seleccionado!!.id,tipo,cantidad,motivo) { ruta = "Movimientos" } }, onCancelar = { ruta = "Movimientos" })
                "Reposición" -> ReposicionScreen(productos) { proveedor, body ->
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("cotizaciones@ejemplo.invalid"))
                        putExtra(Intent.EXTRA_SUBJECT, "Solicitud de reposición - $proveedor")
                        putExtra(Intent.EXTRA_TEXT, body)
                    }
                    try { context.startActivity(intent) } catch (_: ActivityNotFoundException) {
                        android.widget.Toast.makeText(context, "No hay una app de correo instalada", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    if (confirmEliminar != null) AlertDialog(onDismissRequest = { confirmEliminar = null }, title = { Text("Eliminar producto") }, text = { Text("¿Eliminar ${confirmEliminar!!.nombre}? También se borrará su historial. Esta acción no se puede deshacer.") }, confirmButton = { TextButton(onClick = { vm.borrar(confirmEliminar!!); confirmEliminar = null }) { Text("Eliminar") } }, dismissButton = { TextButton(onClick = { confirmEliminar = null }) { Text("Cancelar") } })
}

@Composable
private fun InicioScreen(productos: List<Producto>, movimientos: Int, ir: (String) -> Unit) {
    val bajos = productos.count { it.stock <= it.minimo }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("Resumen del taller", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
        item { Text("Controla el inventario sin depender de anotaciones.", color = Color.Gray) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ResumenCard("Productos", productos.size.toString(), Icons.Default.Inventory2, Modifier.weight(1f))
                ResumenCard("Por reponer", bajos.toString(), Icons.Default.WarningAmber, Modifier.weight(1f))
            }
        }
        item { ResumenCard("Movimientos registrados", movimientos.toString(), Icons.Default.History, Modifier.fillMaxWidth()) }
        item { Text("Accesos rápidos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        item { Button(onClick = { ir("Productos") }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Inventory2, null); Spacer(Modifier.width(8.dp)); Text("Ver productos") } }
        item { OutlinedButton(onClick = { ir("Movimientos") }, modifier = Modifier.fillMaxWidth()) { Text("Registrar entrada o salida") } }
        item { OutlinedButton(onClick = { ir("Reposición") }, modifier = Modifier.fillMaxWidth()) { Text("Consultar reposición") } }
        item { Text("Datos de demostración: proveedores y productos ficticios.", style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
    }
}

@Composable
private fun ResumenCard(titulo: String, numero: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier) {
    ElevatedCard(modifier.animateContentSize(), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(18.dp)) {
            Icon(icon, null, tint = Azul)
            Spacer(Modifier.height(12.dp))
            Text(numero, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(titulo, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ProductosScreen(productos: List<Producto>, busqueda: String, cambiarBusqueda: (String) -> Unit, onEdit: (Producto) -> Unit, onDelete: (Producto) -> Unit, onMovimiento: (Producto) -> Unit, codigoEscaneado: String) {
    var filtro by remember { mutableStateOf("Todos") }
    val categorias = listOf("Todos") + productos.map { it.categoria }.distinct()
    val filtrados = productos.filter {
        (filtro == "Todos" || it.categoria == filtro) && (it.nombre.contains(busqueda, true) || it.codigo.contains(busqueda, true) || it.proveedor.contains(busqueda,true))
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(busqueda, cambiarBusqueda, label = { Text("Buscar nombre, código o proveedor") }, leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            categorias.forEach { cat -> FilterChip(selected = filtro == cat, onClick = { filtro = cat }, label = { Text(cat) }) }
        }
        Text("${filtrados.size} producto(s)", style = MaterialTheme.typography.bodySmall, color=Color.Gray)
        LazyColumn(contentPadding = PaddingValues(vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filtrados, key = { it.id }) { producto ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(producto.nombre, fontWeight = FontWeight.Bold, style=MaterialTheme.typography.titleMedium)
                                Text("${producto.codigo} · ${producto.categoria}", style=MaterialTheme.typography.bodySmall, color=Color.Gray)
                            }
                            val estado = when { producto.stock == 0 -> "Agotado"; producto.stock <= producto.minimo -> "Bajo mínimo"; else -> "Disponible" }
                            AssistChip(onClick={}, label={Text(estado)}, leadingIcon={Icon(if (producto.stock <= producto.minimo) Icons.Default.WarningAmber else Icons.Default.CheckCircle, null, modifier=Modifier.size(16.dp))})
                        }
                        Text("Stock: ${producto.stock}  ·  Mínimo: ${producto.minimo}")
                        Text("Ubicación: ${producto.ubicacion}", style=MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            TextButton(onClick = { onEdit(producto) }) { Text("Editar") }
                            TextButton(onClick = { onMovimiento(producto) }) { Text("Movimiento") }
                            TextButton(onClick = { onDelete(producto) }) { Icon(Icons.Default.Delete, "Eliminar", tint=MaterialTheme.colorScheme.error) }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(72.dp)) }
        }
    }
}

@Composable
private fun FormularioScreen(original: Producto?, qr: QrProductData? = null, onGuardar: (Producto) -> Unit, onCancelar: () -> Unit) {
    var codigo by remember(original?.id, qr) { mutableStateOf(original?.codigo ?: qr?.codigo ?: "") }
    var nombre by remember(original?.id, qr) { mutableStateOf(original?.nombre ?: qr?.nombre ?: "") }
    var descripcion by remember(original?.id, qr) { mutableStateOf(original?.descripcion ?: qr?.descripcion ?: "") }
    var categoria by remember(original?.id, qr) { mutableStateOf(original?.categoria ?: qr?.categoria ?: "Repuestos") }
    var marca by remember(original?.id, qr) { mutableStateOf(original?.marca ?: qr?.marca ?: "") }
    var stock by remember(original?.id, qr) { mutableStateOf(original?.stock?.toString() ?: qr?.stock?.toString() ?: "0") }
    var minimo by remember(original?.id, qr) { mutableStateOf(original?.minimo?.toString() ?: qr?.minimo?.toString() ?: "2") }
    var objetivo by remember(original?.id, qr) { mutableStateOf(original?.objetivo?.toString() ?: qr?.objetivo?.toString() ?: "10") }
    var precio by remember(original?.id, qr) { mutableStateOf(original?.precio?.toString() ?: qr?.precio?.toString() ?: "0") }
    var proveedor by remember(original?.id, qr) { mutableStateOf(original?.proveedor ?: qr?.proveedor ?: "Proveedor Demo") }
    var ubicacion by remember(original?.id, qr) { mutableStateOf(original?.ubicacion ?: qr?.ubicacion ?: "Bodega A - Estante 1") }
    var errores by remember { mutableStateOf<Map<String,String>>(emptyMap()) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text(if (original == null) "Nuevo producto" else "Editar producto", style=MaterialTheme.typography.headlineSmall, fontWeight=FontWeight.Bold) }
        if (qr != null) item { Text("Datos obtenidos del QR. Revísalos antes de guardar.", color = MaterialTheme.colorScheme.primary) }
        item { Campo("Código *", codigo, {codigo=it}, errores["codigo"]) }
        item { Campo("Nombre *", nombre, {nombre=it}, errores["nombre"]) }
        item { Campo("Descripción", descripcion, {descripcion=it}) }
        item { Campo("Categoría", categoria, {categoria=it}) }
        item { Campo("Marca", marca, {marca=it}) }
        item { Campo("Stock actual *", stock, {stock=it}, errores["stock"], KeyboardType.Number) }
        item { Campo("Stock mínimo *", minimo, {minimo=it}, errores["minimo"], KeyboardType.Number) }
        item { Campo("Stock objetivo *", objetivo, {objetivo=it}, errores["objetivo"], KeyboardType.Number) }
        item { Campo("Precio referencial *", precio, {precio=it}, errores["precio"], KeyboardType.Decimal) }
        item { Campo("Proveedor ficticio", proveedor, {proveedor=it}) }
        item { Campo("Ubicación (zona / estante / repisa)", ubicacion, {ubicacion=it}) }
        item {
            Button(onClick = {
                errores = ValidadorProducto.validar(codigo,nombre,stock,minimo,objetivo,precio)
                if (errores.isEmpty()) onGuardar(Producto(id=original?.id ?: 0, codigo=codigo.trim(), nombre=nombre.trim(), descripcion=descripcion.trim(), categoria=categoria.trim().ifBlank {"Otros"}, marca=marca.trim(), stock=stock.toInt(), minimo=minimo.toInt(), objetivo=objetivo.toInt(), precio=precio.toDouble(), proveedor=proveedor.trim(), ubicacion=ubicacion.trim()))
            }, modifier=Modifier.fillMaxWidth()) { Text("Guardar producto") }
        }
        item { OutlinedButton(onClick=onCancelar, modifier=Modifier.fillMaxWidth()) { Text("Cancelar") } }
    }
}

@Composable
private fun Campo(label: String, valor: String, onChange: (String) -> Unit, error: String? = null, teclado: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(value=valor, onValueChange=onChange, label={Text(label)}, isError=error!=null, supportingText=if (error != null) { { Text(error) } } else null, trailingIcon=if (error != null) { { Icon(Icons.Default.Error, "Error") } } else null, keyboardOptions=KeyboardOptions(keyboardType=teclado), singleLine=true, modifier=Modifier.fillMaxWidth())
}

@Composable
private fun MovimientosScreen(movimientos: List<com.example.autostock.data.Movimiento>, productos: List<Producto>, onAgregar: (Producto) -> Unit) {
    LazyColumn(contentPadding=PaddingValues(16.dp), verticalArrangement=Arrangement.spacedBy(10.dp)) {
        item { Text("Historial de movimientos", style=MaterialTheme.typography.headlineSmall, fontWeight=FontWeight.Bold) }
        item { Text("Selecciona un producto para registrar una entrada o salida.", style=MaterialTheme.typography.bodySmall) }
        items(productos, key={"p${it.id}"}) { p -> OutlinedButton(onClick={onAgregar(p)}, modifier=Modifier.fillMaxWidth()) { Text("Registrar: ${p.nombre}", maxLines=1, overflow=TextOverflow.Ellipsis) } }
        item { HorizontalDivider(); Text("Últimos movimientos", style=MaterialTheme.typography.titleMedium, fontWeight=FontWeight.Bold) }
        if(movimientos.isEmpty()) item { Text("Todavía no hay movimientos registrados.") }
        items(movimientos, key={"m${it.id}"}) { m ->
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text("${m.tipo}: ${m.nombreProducto}", fontWeight=FontWeight.Bold)
                    Text("Cantidad: ${m.cantidad}  ·  ${m.anterior} → ${m.resultante}")
                    Text(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es","CL")).format(Date(m.fecha)), style=MaterialTheme.typography.bodySmall, color=Color.Gray)
                    if (m.motivo.isNotBlank()) Text(m.motivo, style=MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun MovimientoFormScreen(producto: Producto?, onGuardar: (String,Int,String) -> Unit, onCancelar: () -> Unit) {
    if(producto == null) { Text("Selecciona un producto"); return }
    var tipo by remember { mutableStateOf("Venta") }
    var cantidad by remember { mutableStateOf("1") }
    var motivo by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text(producto.nombre, style=MaterialTheme.typography.headlineSmall, fontWeight=FontWeight.Bold)
        Text("Existencia actual: ${producto.stock}")
        Text("Tipo de movimiento", fontWeight=FontWeight.Bold)
        listOf("Ingreso","Venta","Consumo en servicio","Ajuste","Devolución").forEach { opcion ->
            Row(verticalAlignment=Alignment.CenterVertically, modifier=Modifier.fillMaxWidth().clickable {tipo=opcion}) {
                RadioButton(selected=tipo==opcion, onClick={tipo=opcion}); Text(opcion)
            }
        }
        Campo("Cantidad",cantidad,{cantidad=it},if(error.isNotEmpty())error else null,KeyboardType.Number)
        Campo("Motivo u observación",motivo,{motivo=it})
        Text("Nota: Ajuste descuenta unidades. Devolución suma unidades.", style=MaterialTheme.typography.bodySmall, color=Color.Gray)
        Button(onClick={
            val n = cantidad.toIntOrNull()
            error = when { n == null || n <= 0 -> "Ingresa una cantidad mayor que cero"; tipo !in listOf("Ingreso","Devolución") && n > producto.stock -> "No hay stock suficiente"; else -> "" }
            if(error.isEmpty()) onGuardar(tipo,n!!,motivo)
        }, modifier=Modifier.fillMaxWidth()) { Text("Confirmar movimiento") }
        OutlinedButton(onClick=onCancelar, modifier=Modifier.fillMaxWidth()) { Text("Cancelar") }
    }
}

@Composable
private fun ReposicionScreen(productos: List<Producto>, enviar: (String,String) -> Unit) {
    val reponer = productos.filter { it.stock <= it.minimo }
    val grupos = reponer.groupBy { it.proveedor.ifBlank { "Sin proveedor" } }
    LazyColumn(contentPadding=PaddingValues(16.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item { Text("Reposición sugerida", style=MaterialTheme.typography.headlineSmall, fontWeight=FontWeight.Bold) }
        item { Text("Productos con existencia igual o menor al mínimo. El envío de correos es manual.") }
        if (reponer.isEmpty()) item { Text("No hay productos pendientes de reposición.") }
        grupos.forEach { (proveedor, lista) ->
            item {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement=Arrangement.spacedBy(8.dp)) {
                        Text(proveedor, style=MaterialTheme.typography.titleMedium, fontWeight=FontWeight.Bold)
                        lista.forEach { p -> Text("• ${p.nombre}: ${p.stock} disponibles · sugerido ${max(0,p.objetivo-p.stock)}") }
                        val cuerpo = "Estimado proveedor $proveedor:\n\nSolicitamos cotizar los siguientes productos:\n" + lista.joinToString("\n") { "- ${it.nombre} (${it.codigo}): ${max(0,it.objetivo-it.stock)} unidades" } + "\n\nQuedamos atentos a disponibilidad y valores.\nTaller AutoStock (datos ficticios)."
                        Button(onClick={ enviar(proveedor, cuerpo) }, modifier=Modifier.fillMaxWidth()) { Icon(Icons.Default.Email,null); Spacer(Modifier.width(8.dp)); Text("Preparar correo") }
                    }
                }
            }
        }
    }
}
