package com.example.quizprototype_phy_che_deepseek

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyQuizzesScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val materialRepository = remember { MaterialRepository(context) }
    
    var viewingAiUrl by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showAiMenu by remember { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    if (viewingAiUrl != null) {
        WebViewScreen(
            url = viewingAiUrl!!.second,
            title = viewingAiUrl!!.first,
            onBack = { viewingAiUrl = null }
        )
        return
    }

    // خادم الويب المحلي
    val quizServer = remember { QuizServer() }

    // استخدام collectAsState للحصول على تحديثات فورية
    val quizzesByTopic by materialRepository.getQuizzesGroupedByTopicFlow()
        .collectAsState(initial = emptyList())
    
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            LogoTopAppBar(
                title = "كويزاتي المحملة",
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

                    if (quizzesByTopic.isNotEmpty()) {
                        AppIconButton(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "حذف الكل",
                            tint = Color.White
                        ) { showDeleteAllDialog = true }
                    }
                }
            )
        }
    ) { padding ->
        if (showDeleteAllDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteAllDialog = false },
                title = { Text("حذف جميع الكويزات") },
                text = { Text("هل أنت متأكد من حذف جميع الكويزات المحملة؟") },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            materialRepository.deleteAllQuizzes()
                            showDeleteAllDialog = false
                        }
                    }) { Text("حذف الكل", color = Color.Red) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteAllDialog = false }) { Text("إلغاء") }
                }
            )
        }
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize()) { CircularProgressIndicator(modifier = Modifier.align(Alignment.Center)) }
        } else if (quizzesByTopic.isEmpty()) {
            EmptyContent(message = "لم تقم بتحميل أي كويزات بعد")
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
                items(quizzesByTopic) { topic ->
                    Text(text = topic.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
                    topic.materials.forEach { quiz ->
                        QuizItemRow(
                            quiz = quiz,
                            onStart = {
                                scope.launch {
                                    // تشغيل الكويز
                                    val quizFolder = quiz.localFilePath ?: ""
                                    if (File(quizFolder, "index.html").exists()) {
                                        // 1. تشغيل الخادم المحلي على المجلد
                                        quizServer.start(quizFolder, 8080)
                                        
                                        // انتظار قصير لضمان جاهزية الخادم
                                        kotlinx.coroutines.delay(800)
                                        
                                        // 2. فتح الكويز باستخدام العنوان الرقمي الصريح
                                        val url = "http://127.0.0.1:8080/index.html"
                                        val intent = CustomTabsIntent.Builder()
                                            .setToolbarColor(android.graphics.Color.parseColor("#673AB7"))
                                            .setShowTitle(true)
                                            .build()
                                        intent.launchUrl(context, Uri.parse(url))
                                    } else {
                                        android.util.Log.e("QUIZ_DEBUG", "index.html not found in: $quizFolder")
                                        android.widget.Toast.makeText(context, "ملف التشغيل index.html مفقود", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onDelete = {
                                scope.launch {
                                    materialRepository.deleteMaterial(quiz.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuizItemRow(
    quiz: ClassroomMaterial,
    onStart: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("حذف الكويز") },
            text = { Text("هل أنت متأكد من حذف '${quiz.title}'؟") },
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

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = quiz.title, style = MaterialTheme.typography.bodyLarge)
                Text(text = "جاهز للبدء", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Gray)
            }

            Button(onClick = onStart) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("بدء")
            }
        }
    }
}
