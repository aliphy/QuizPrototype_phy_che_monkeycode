package com.example.quizprototype_phy_che_deepseek

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogoTopAppBar(
    title: String,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    headerHeight: Dp = 140.dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(headerHeight)
    ) {
        Image(
            painter = painterResource(R.drawable.lo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // تدرج شفاف لضمان وضوح العناوين والأزرار فوق الصورة
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x99000000),
                            Color(0x4D000000),
                            Color(0x99000000)
                        )
                    )
                )
        )

        TopAppBar(
            title = { Text(title) },
            navigationIcon = navigationIcon,
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White,
                actionIconContentColor = Color.White
            )
        )
    }
}

//@Composable
//fun AppIconButton(
//    imageVector: ImageVector,
//    contentDescription: String?,
//    tint: Color,
//    badgeSize: Dp = 42.dp,
//    iconSize: Dp = 22.dp,
//    onClick: () -> Unit
//) {
//    IconButton(
//        onClick = onClick,
//        modifier = Modifier.size(badgeSize)
//    ) {
//        Box(
//            modifier = Modifier
//                .size(badgeSize)
//                .clip(CircleShape)
//                .background(tint.copy(alpha = 0.18f))
//                .border(width = 1.5.dp, color = tint, shape = CircleShape),
//            contentAlignment = Alignment.Center
//        ) {
//            Icon(
//                imageVector = imageVector,
//                contentDescription = contentDescription,
//                tint = tint,
//                modifier = Modifier.size(iconSize)
//            )
//        }
//    }
//}
/////////////
@Composable
fun AppIconButton(
    imageVector: ImageVector,
    contentDescription: String?,
    tint: Color,
    badgeSize: Dp = 42.dp,
    iconSize: Dp = 22.dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(badgeSize)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.40f))
            .border(
                width = 1.5.dp,
                color = tint,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}