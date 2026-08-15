package com.example.quizprototype_phy_che_deepseek

//package com.yourapp.physicschem.ui.screens
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.School
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.google.android.gms.auth.api.signin.GoogleSignInAccount
//
//@Composable
//fun LoginScreen(
//    onLoginSuccess: (GoogleSignInAccount) -> Unit
//) {
//    var isLoading by remember { mutableStateOf(false) }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(24.dp),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//        // شعار التطبيق
//        Icon(
//            imageVector = Icons.Default.School,
//            contentDescription = null,
//            modifier = Modifier.size(120.dp),
//            tint = Color(0xFF1565C0)
//        )
//
//        Spacer(modifier = Modifier.height(32.dp))
//
//        Text(
//            text = "فيزياء وكيمياء",
//            fontSize = 32.sp,
//            fontWeight = FontWeight.Bold,
//            color = Color(0xFF1565C0)
//        )
//
//        Text(
//            text = "الثالثة ثانوي",
//            fontSize = 20.sp,
//            color = Color.Gray
//        )
//
//        Spacer(modifier = Modifier.height(48.dp))
//
//        // زر تسجيل الدخول
//        Button(
//            onClick = {
//                isLoading = true
//                // هنا سيتم استدعاء Google Sign-In
//            },
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(56.dp),
//            enabled = !isLoading,
//            colors = ButtonDefaults.buttonColors(
//                containerColor = Color(0xFF1565C0)
//            )
//        ) {
//            if (isLoading) {
//                CircularProgressIndicator(
//                    modifier = Modifier.size(24.dp),
//                    color = Color.White
//                )
//            } else {
//                Text(
//                    text = "تسجيل الدخول بحساب Google",
//                    fontSize = 16.sp
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Text(
//            text = "استخدم حسابك الدراسي للدخول",
//            style = MaterialTheme.typography.bodySmall,
//            color = Color.Gray
//        )
//    }
//}
/////////////////////////////2
//package com.yourapp.physicschem.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onLoginClick: () -> Unit,
    onClearError: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // شعار التطبيق
        Icon(
            imageVector = Icons.Default.School,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = Color(0xFF1565C0)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // اسم التطبيق
        Text(
            text = "فيزياء وكيمياء",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1565C0),
            textAlign = TextAlign.Center
        )

        Text(
            text = "الثالثة ثانوي",
            fontSize = 20.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "تطبيق تعليمي متكامل لمتابعة الدروس وحل التمارين",
            fontSize = 14.sp,
            color = Color(0xFF757575),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // زر تسجيل الدخول
        Button(
            onClick = {
                if (!isLoading) {
                    onLoginClick()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1565C0),
                disabledContainerColor = Color(0xFF90CAF9)
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            if (isLoading) {
                // عرض مؤشر التحميل
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "جاري تسجيل الدخول...",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            } else {
                // عرض زر تسجيل الدخول
                Text(
                    text = "تسجيل الدخول بحساب Google",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // رسالة الخطأ
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3E0)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "تنبيه",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = errorMessage,
                            color = Color(0xFFBF360C),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // معلومات إضافية
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "استخدم حسابك الدراسي",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "للوصول إلى موادك التعليمية",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}