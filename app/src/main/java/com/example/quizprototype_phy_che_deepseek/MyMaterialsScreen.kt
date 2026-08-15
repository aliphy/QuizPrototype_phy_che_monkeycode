package com.example.quizprototype_phy_che_deepseek

import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyMaterialsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val materialRepository = remember { MaterialRepository(context) }
    
    var viewingPdf by remember { mutableStateOf<ClassroomMaterial?>(null) }
    var viewingImage by remember { mutableStateOf<ClassroomMaterial?>(null) }
    var viewingAiUrl by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showAiMenu by remember { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    if (viewingPdf != null) {
        PdfViewerScreen(
            filePath = viewingPdf!!.localFilePath ?: "",
            title = viewingPdf!!.title,
            onBack = { viewingPdf = null }
        )
        return
    }

    if (viewingImage != null) {
        ImageViewerScreen(
            filePath = viewingImage!!.localFilePath ?: "",
            title = viewingImage!!.title,
            onBack = { viewingImage = null }
        )
        return
    }

    if (viewingAiUrl != null) {
        WebViewScreen(
            url = viewingAiUrl!!.second,
            title = viewingAiUrl!!.first,
            onBack = { viewingAiUrl = null }
        )
        return
    }

    // دالة لفتح الملفات مع التعرف الذكي والصارم على النوع
    val openFile = { material: ClassroomMaterial ->
        val filePath = material.localFilePath ?: ""
        val file = File(filePath)
        val titleLower = material.title.lowercase()
        val mimeLower = material.mimeType?.lowercase() ?: ""
        
        if (!file.exists()) {
            Toast.makeText(context, "الملف غير موجود", Toast.LENGTH_SHORT).show()
        } else if (titleLower.endsWith(".pdf") || mimeLower.contains("pdf")) {
            // فتح باستخدام العارض المدمج
            viewingPdf = material
        } else if (titleLower.endsWith(".png") || titleLower.endsWith(".jpg") || 
                   titleLower.endsWith(".jpeg") || mimeLower.contains("image")) {
            // فتح الصور باستخدام العارض المدمج لحمايتها
            viewingImage = material
        } else {
            // فتح الملفات الأخرى (docx, xlsx) باستخدام التطبيقات الخارجية مؤقتاً
            // ملاحظة: FileProvider يمنح وصولاً مؤقتاً فقط للملف المحدد ولا يكشف المجلد
            try {
                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                
                val mimeType = material.mimeType ?: getMimeTypeFromExtension(material.title)
                
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, mimeType)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                
                val chooser = Intent.createChooser(intent, "فتح الملف باستخدام")
                context.startActivity(chooser)
            } catch (e: Exception) {
                Toast.makeText(context, "فشل في فتح الملف: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val materialsByTopic by materialRepository.getMaterialsGroupedByTopicFlow()
        .collectAsState(initial = emptyList())
        
    var selectedFilter by remember { mutableStateOf<MaterialType?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        LogoTopAppBar(
            title = "دروسي وتماريني",
            navigationIcon = {
                AppIconButton(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = "خروج",
                    tint = Color.White
                ) { (context as? android.app.Activity)?.finish() }
            },
            actions = {
                // زر الذكاء الاصطناعي
                AppIconButton(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI",
                    tint = Color.Yellow
                ) { showAiMenu = true }

                DropdownMenu(
                    expanded = showAiMenu,
                    onDismissRequest = { showAiMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Google Gemini") },
                        onClick = { viewingAiUrl = "Gemini" to "https://gemini.google.com/"; showAiMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("ChatGPT") },
                        onClick = { viewingAiUrl = "ChatGPT" to "https://chatgpt.com/"; showAiMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Qwen AI") },
                        onClick = { viewingAiUrl = "Qwen AI" to "https://chat.qwenlm.ai/"; showAiMenu = false }
                    )
                }

                if (materialsByTopic.isNotEmpty()) {
                    AppIconButton(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "حذف الكل",
                        tint = Color.White
                    ) { showDeleteAllDialog = true }
                }
            }
        )

        if (showDeleteAllDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteAllDialog = false },
                title = { Text("حذف جميع المواد") },
                text = { Text("هل أنت متأكد من حذف جميع الدروس والتمارين المحملة؟") },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            materialRepository.deleteAllMaterialsExceptQuizzes()
                            showDeleteAllDialog = false
                        }
                    }) { Text("حذف الكل", color = Color.Red) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteAllDialog = false }) { Text("إلغاء") }
                }
            )
        }

        FilterChipsRow(
            selectedFilter = selectedFilter,
            onFilterSelected = { selectedFilter = it }
        )

        if (materialsByTopic.isEmpty()) {
            EmptyContent(message = "لم تقم بتحميل أي مواد بعد")
        } else {
            val filteredTopics = if (selectedFilter != null) {
                materialsByTopic.map { topic ->
                    topic.copy(materials = topic.materials.filter { it.type == selectedFilter })
                }.filter { it.materials.isNotEmpty() }
            } else {
                materialsByTopic
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredTopics) { topic ->
                    DownloadedTopicContent(
                        topic = topic,
                        onItemClick = openFile,
                        onDelete = { material ->
                            scope.launch {
                                materialRepository.deleteMaterial(material.id)
                            }
                        }
                    )
                }
            }
        }
    }
}

// دالة مساعدة لجلب الـ MimeType بناءً على الاسم أو الامتداد
fun getMimeTypeFromExtension(fileName: String): String {
    val extension = fileName.substringAfterLast('.', "").lowercase()
    return when (extension) {
        "pdf" -> "application/pdf"
        "doc", "docx" -> "application/msword"
        "xls", "xlsx" -> "application/vnd.ms-excel"
        "ppt", "pptx" -> "application/vnd.ms-powerpoint"
        "jpg", "jpeg", "png", "webp" -> "image/*"
        "zip", "rar" -> "application/zip"
        else -> "*/*"
    }
}

// دالة لاختيار الأيقونة واللون بناءً على الهوية الحقيقية للملف
@Composable
fun getFileDisplayInfo(material: ClassroomMaterial): Pair<ImageVector, Color> {
    val title = material.title.lowercase()
    val mime = material.mimeType?.lowercase() ?: ""
    
    return when {
        title.endsWith(".pdf") || mime.contains("pdf") -> Icons.Default.PictureAsPdf to Color(0xFFE53935) // أحمر PDF
        title.endsWith(".doc") || title.endsWith(".docx") || mime.contains("word") -> Icons.Default.Description to Color(0xFF1E88E5) // أزرق Word
        title.endsWith(".xls") || title.endsWith(".xlsx") || mime.contains("excel") || mime.contains("sheet") -> Icons.Default.TableChart to Color(0xFF2E7D32) // أخضر Excel
        title.endsWith(".png") || title.endsWith(".jpg") || title.endsWith(".jpeg") || mime.contains("image") -> Icons.Default.Image to Color(0xFF43A047) // أخضر صور
        title.endsWith(".zip") || title.endsWith(".rar") || mime.contains("zip") || mime.contains("compressed") -> Icons.Default.FolderZip to Color(0xFFFBC02D) // أصفر Zip
        else -> Icons.Default.InsertDriveFile to Color(0xFF757575) // رمادي ملفات أخرى
    }
}

@Composable
fun DownloadedMaterialItem(
    material: ClassroomMaterial,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val (icon, color) = getFileDisplayInfo(material)
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("حذف المادة") },
            text = { Text("هل أنت متأكد من حذف '${material.title}'؟") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteConfirm = false
                }) { Text("حذف", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("إلغاء") }
            }
        )
    }
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // أيقونة كبيرة ملونة
            Surface(
                modifier = Modifier.size(45.dp),
                shape = MaterialTheme.shapes.small,
                color = color.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp),
                    tint = color
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = material.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Text(
                    text = if (material.mimeType != null) "ملف موثق" else "تم التحقق من الامتداد",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Gray, modifier = Modifier.size(20.dp))
            }

            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChipsRow(selectedFilter: MaterialType?, onFilterSelected: (MaterialType?) -> Unit) {
    LazyRow(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item { FilterChip(selected = selectedFilter == null, onClick = { onFilterSelected(null) }, label = { Text("الكل") }) }
        items(MaterialType.values()) { type ->
            if (type != MaterialType.QUIZ && type != MaterialType.OTHER) {
                FilterChip(selected = selectedFilter == type, onClick = { onFilterSelected(if (selectedFilter == type) null else type) }, label = { Text(type.arabicName) })
            }
        }
    }
}

@Composable
fun DownloadedTopicContent(
    topic: CourseTopic,
    onItemClick: (ClassroomMaterial) -> Unit,
    onDelete: (ClassroomMaterial) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = topic.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
            Spacer(modifier = Modifier.height(12.dp))
            topic.materials.groupBy { it.type }.forEach { (type, materials) ->
                if (type != MaterialType.QUIZ) {
                    Column {
                        Text(text = type.arabicNamePlural, style = MaterialTheme.typography.labelMedium, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                        materials.forEach { material ->
                            DownloadedMaterialItem(
                                material = material,
                                onClick = { onItemClick(material) },
                                onDelete = { onDelete(material) }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}
