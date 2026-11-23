package com.example.autostock.qr

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Escanea QR con permiso solicitado en MainActivity, y entrega contenido al formulario. */
@Composable
fun QrScannerScreen(
    hasCameraPermission: Boolean,
    onRequestPermission: () -> Unit,
    onResult: (String) -> Unit,
    error: String?,
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    var scanning by remember { mutableStateOf(true) }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Agregar producto mediante QR", style = MaterialTheme.typography.titleLarge)
        Text("Escanea un QR AutoStock. Revisarás los datos antes de guardarlos.")
        if (!hasCameraPermission) {
            Text("Se necesita permiso de cámara para escanear.")
            Button(onClick = onRequestPermission) { Text("Permitir cámara") }
        } else if (scanning && error == null) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                QrScanner(
                    modifier = Modifier.fillMaxSize(),
                    onQrCodeScanned = { value ->
                        scanning = false
                        onResult(value)
                    }
                )
            }
        }
        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error)
            Button(onClick = { scanning = true; onRetry() }) { Text("Volver a escanear") }
        }
        OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
    }
}
