package com.example.quizprototype_phy_che_deepseek

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherToolsScreen(
    googleAccount: com.google.android.gms.auth.api.signin.GoogleSignInAccount?,
    onLogout: () -> Unit,
    onEnterApp: () -> Unit
) {
    val context = LocalContext.current
    var studentId by remember { mutableStateOf("") }
    var generatedKey by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("أدوات الأستاذ - توليد الأكواد") },
                navigationIcon = {
                    AppIconButton(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "إغلاق التطبيق",
                        tint = Color.White
                    ) { (context as? android.app.Activity)?.finish() }
                },
                actions = {
                    AppIconButton(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "تسجيل الخروج",
                        tint = Color.White,
                        onClick = onLogout
                    )
                    AppIconButton(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "دخول التطبيق",
                        tint = Color.White,
                        onClick = onEnterApp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF455A64), titleContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color(0xFF455A64))
            
            Text(text = "توليد كود التفعيل للتلاميذ", style = MaterialTheme.typography.titleLarge)
            
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = studentId,
                onValueChange = { studentId = it.uppercase() },
                label = { Text("أدخل كود الهاتف الذي أرسله التلميذ") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (studentId.length >= 6) {
                        generatedKey = SecurityManager.generateActivationKey(studentId)
                    } else {
                        Toast.makeText(context, "الكود المدخل قصير جداً", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("توليد كود التفعيل")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onEnterApp,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("دخول التطبيق كأستاذ")
            }

            if (generatedKey.isNotEmpty()) {
                Spacer(modifier = Modifier.height(40.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFEBE9))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "كود التفعيل للتلميذ:")
                        Text(
                            text = generatedKey,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFC62828)
                        )
                        
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Activation Key", generatedKey))
                                Toast.makeText(context, "تم نسخ الكود لإرساله للتلميذ", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("نسخ الكود")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            
            // لوحة معلومات المعرفات للمواسم الدراسية
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("معلومات تقنية للمواسم القادمة:", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    Text(
                        text = "هذا هو المعرف الخاص بالحساب الحالي. ضعه في ملف build.gradle.kts لربط نسخة التلاميذ بهذا الحساب:",
                        style = MaterialTheme.typography.labelSmall
                    )
                    SelectionContainer {
                        Text(
                            text = googleAccount?.id ?: "يرجى تسجيل الدخول",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Blue,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    Text(
                        text = "ملاحظة: هذا المعرف ثابت لحسابك ولا يتغير عند إنشاء فصول جديدة.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

// أداة مساعدة للسماح بنسخ النص (المعرف) من الواجهة
@Composable
fun SelectionContainer(content: @Composable () -> Unit) {
    androidx.compose.foundation.text.selection.SelectionContainer(content = content)
}
