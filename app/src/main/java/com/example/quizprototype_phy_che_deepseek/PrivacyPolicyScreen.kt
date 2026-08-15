package com.example.quizprototype_phy_che_deepseek

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PrivacyPolicyScreen(onAccept: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF8F9FA)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.PrivacyTip,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color(0xFF1565C0)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "سياسة الخصوصية والأمان",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "نحن نلتزم بحماية خصوصية تلاميذنا. هذا التطبيق تعليمي 100%:\n\n" +
                                "• لا يجمع أرقام هواتف، ولا صور شخصية، ولا جهات اتصال.\n" +
                                "• لا يطلب صلاحيات الموقع الجغرافي أو الكاميرا.\n" +
                                "• البيانات المستلمة: نستخدم فقط البريد الإلكتروني والاسم القادم من جوجل للتعرف على الفصل الدراسي داخل Google Classroom.\n" +
                                "• تخزين الملفات: المواد المحملة تُخزن في ذاكرة التطبيق المحمية ولا يمكن لأي تطبيق آخر الوصول إليها.\n" +
                                "• المشاركة: لا نقوم بمشاركة أي بيانات مع أي جهات خارجية.\n" +
                                "غايتنا : جعل الهواتف ليست للعب فقط بل لتعلم و التعلم مع متعة ",
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 24.sp,
                        textAlign = TextAlign.Right
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "باستخدامك لهذا التطبيق، فإنك توافق على هذه الشروط لضمان بيئة تعليمية آمنة.",
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onAccept,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
            ) {
                Text("أوافق وأرغب في المتابعة", fontSize = 18.sp)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(onClick = { /* يمكن إضافة خيار الخروج هنا */ }) {
                Text("خروج", color = Color.Red)
            }
        }
    }
}
