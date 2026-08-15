package com.example.quizprototype_phy_che_deepseek

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ActivationScreen(onActivated: () -> Unit, onLogout: () -> Unit) {
    val context = LocalContext.current
    val deviceId = remember { SecurityManager.getDeviceId(context) }
    val (remainingDays, totalDays) = remember { SecurityManager.getTrialInfo(context) }
    var inputKey by remember { mutableStateOf("") }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // أزرار في الأعلى
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { (context as? android.app.Activity)?.finish() }) {
                Icon(Icons.Default.PowerSettingsNew, contentDescription = "إغلاق", tint = Color.Gray)
            }
            
            // زر جديد لتبديل الحساب
            TextButton(onClick = onLogout) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("تبديل الحساب", fontSize = 12.sp)
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp).padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        // 1. الصورة الترحيبية (تأكد من وجود صورة في الـ drawables أو استخدم أيقونة مؤقتاً)
        Icon(
            painter = painterResource(id = android.R.drawable.star_big_on), // استبدلها بصورتك التعليمية
            contentDescription = null,
            modifier = Modifier.size(120.dp).padding(16.dp),
            tint = Color(0xFF1565C0)
        )

        Text(
            text = "مرحباً بكم في تطبيق الفيزياء والكيمياء",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. معلومات الفترة التجريبية
        Card(
            colors = CardDefaults.cardColors(containerColor = if (remainingDays > 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE))
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "الفترة التجريبية: $remainingDays يوم متبقي من $totalDays",
                    color = if (remainingDays > 0) Color(0xFF2E7D32) else Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. معرف الهاتف مع أزرار النسخ والإرسال
        Text(text = "كود هاتفك الفريد:", style = MaterialTheme.typography.labelLarge)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(
                text = deviceId,
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF1565C0),
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Device ID", deviceId))
                Toast.makeText(context, "تم نسخ الكود", Toast.LENGTH_SHORT).show()
            }) {
                Icon(Icons.Default.ContentCopy, contentDescription = "نسخ")
            }
        }

        Button(
            onClick = {
                val message = "أستاذي، هذا هو كود هاتفي لتفعيل تطبيق الفيزياء: $deviceId"
                val url = "https://api.whatsapp.com/send?text=${Uri.encode(message)}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "واتساب غير مثبت", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
        ) {
            Icon(Icons.Default.Send, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("إرسال الكود للأستاذ عبر واتساب")
        }

        Spacer(modifier = Modifier.weight(1f))

        // 4. خانة إدخال التفعيل
        OutlinedTextField(
            value = inputKey,
            onValueChange = { inputKey = it.uppercase() },
            label = { Text("أدخل كود التفعيل الذي أرسله لك الأستاذ") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val correctKey = SecurityManager.generateActivationKey(deviceId)
                if (inputKey == correctKey) {
                    SecurityManager.setActivated(context)
                    onActivated()
                    Toast.makeText(context, "تم التفعيل بنجاح! شكراً لك", Toast.LENGTH_LONG).show()
                } else if (remainingDays > 0) {
                    onActivated() // السماح بالدخول في الفترة التجريبية
                    Toast.makeText(context, "دخول مؤقت (فترة تجريبية)", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "كود التفعيل غير صحيح", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("تشغيل التطبيق")
        }
        }
    }
}
