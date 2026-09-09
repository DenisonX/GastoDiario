package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.ExpenseCategory
import com.example.domain.ReceiptOcrScanner
import com.example.domain.ReceiptScanResult
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ReceiptOcrDialog(
    isProcessing: Boolean,
    onDismiss: () -> Unit,
    onSelectImageUri: (Uri, (ReceiptScanResult) -> Unit) -> Unit,
    onExpenseConfirmed: (title: String, amount: Double, category: ExpenseCategory, note: String, method: String) -> Unit
) {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var scanResult by remember { mutableStateOf<ReceiptScanResult?>(null) }
    var editedTitle by remember { mutableStateOf("") }
    var editedAmount by remember { mutableStateOf("") }
    var editedCategory by remember { mutableStateOf(ExpenseCategory.SUPERMERCADO) }

    val ptBr = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    // Android Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            onSelectImageUri(uri) { res ->
                scanResult = res
                editedTitle = res.title
                editedAmount = String.format(Locale.ROOT, "%.2f", res.amount).replace(".", ",")
                editedCategory = res.category
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "Escanear Recibo (OCR)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "O leitor inteligente extrai o total pago e a loja automaticamente usando IA on-device gratuita.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action to pick from gallery or take photo
                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("pick_receipt_photo_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Escolher Foto do Comprovante")
                }

                // Sample receipts for instant testing in the emulator
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Ou teste com comprovantes simulados:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                val sampleReceipts = listOf(
                    Triple(
                        "Supermercado Carrefour",
                        "CARREFOUR COMERCIO LTDA\nCUPOM FISCAL ELETRONICO\nARROZ TIO JOAO 5KG R$ 28,90\nFEIJAO CARIOCA 1KG R$ 8,50\nCARNE BOVINA 1.2KG R$ 48,20\nLEITE INTEGRAL 1L R$ 5,90\nSUBTOTAL R$ 91,50\nTOTAL A PAGAR R$ 142,50\nFORMA DE PAGAMENTO: PIX",
                        "Supermercado"
                    ),
                    Triple(
                        "Posto Ipiranga",
                        "AUTO POSTO IPIRANGA\nAV PAULISTA 1000\nGASOLINA COMUM 22,5L\nVALOR UNITARIO R$ 5,89\nTOTAL R$ 132,50\nCARTAO DEBITO VISA",
                        "Transporte"
                    ),
                    Triple(
                        "Restaurante & Grill",
                        "RESTAURANTE SABOR & ARTE\nREFEICAO POR QUILO R$ 42,00\nSUCO NATURAL R$ 10,00\nSOBREMESA PUDIM R$ 8,00\nTOTAL R$ 60,00\nOBRIGADO PELA PREFERENCIA",
                        "Alimentação"
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    sampleReceipts.forEach { (name, sampleText, categoryName) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                .clickable {
                                    val result = ReceiptOcrScanner.parseReceiptText(sampleText)
                                    scanResult = result
                                    editedTitle = result.title
                                    editedAmount = String.format(Locale.ROOT, "%.2f", result.amount).replace(".", ",")
                                    editedCategory = result.category
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = name.split(" ").first(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = categoryName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Loading spinner while ML Kit scans
                if (isProcessing) {
                    Spacer(modifier = Modifier.height(20.dp))
                    CircularProgressIndicator(modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Processando OCR no dispositivo...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Display scan result review
                AnimatedVisibility(visible = scanResult != null && !isProcessing) {
                    scanResult?.let { result ->
                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ocr_result_card"),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = "Dados Extraídos do Recibo:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = editedTitle,
                                    onValueChange = { editedTitle = it },
                                    label = { Text("Estabelecimento / Descrição") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = editedAmount,
                                    onValueChange = { editedAmount = it },
                                    label = { Text("Valor Total Extraído") },
                                    prefix = { Text("R$ ", fontWeight = FontWeight.Bold) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Categoria: ${editedCategory.displayName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = editedCategory.color
                                    )

                                    Text(
                                        text = result.confidenceNotes,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            val amount = editedAmount.replace(".", "").replace(",", ".").toDoubleOrNull() ?: 0.0
            Button(
                onClick = {
                    if (amount > 0 && editedTitle.isNotBlank()) {
                        onExpenseConfirmed(
                            editedTitle,
                            amount,
                            editedCategory,
                            "Comprovante OCR Digitalizado",
                            "OCR"
                        )
                        onDismiss()
                    } else {
                        Toast.makeText(context, "Verifique o valor e o nome do gasto.", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = scanResult != null && amount > 0,
                modifier = Modifier.testTag("confirm_ocr_expense_button")
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Confirmar e Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
