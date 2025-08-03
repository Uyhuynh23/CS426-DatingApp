package com.example.dating.ui.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dating.R
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

data class OnboardingPage(
    val image: Int,
    val title: String,
    val description: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(navController: NavController) {

    val pages = listOf(
        OnboardingPage(
            image = R.drawable.onboard1,
            title = "Algorithm",
            description = "Users going through a vetting process to ensure you never match with bots."
        ),
        OnboardingPage(
            image = R.drawable.onboard2,
            title = "Matches",
            description = "We match you with people that have a large array of similar interests."
        ),
        OnboardingPage(
            image = R.drawable.onboard3,
            title = "Premium",
            description = "Sign up today and enjoy the first month of premium benefits on us."
        )
    )

    val actualPageCount = pages.size
    val virtualPageCount = Int.MAX_VALUE
    val startPage = virtualPageCount / 2
    val pagerState = rememberPagerState(
        initialPage = startPage,
        pageCount = { virtualPageCount }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.onPrimary),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // HorizontalPager với hiệu ứng carousel và preview
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 80.dp), // Tăng padding để hiển thị preview
            pageSpacing = 16.dp
        ) { virtualPage ->
            val actualPage = ((virtualPage - startPage) % actualPageCount + actualPageCount) % actualPageCount
            val pageOffset = (pagerState.currentPage - virtualPage) + pagerState.currentPageOffsetFraction

            // Tính toán scale và alpha dựa trên vị trí
            val scale = when {
                pageOffset.absoluteValue < 0.1f -> 1f // Trang chính giữa
                pageOffset.absoluteValue < 1.1f -> 0.7f // Trang bên cạnh
                else -> 0.5f // Trang xa hơn
            }

            val alpha = when {
                pageOffset.absoluteValue < 0.1f -> 1f // Trang chính giữa
                pageOffset.absoluteValue < 1.1f -> 0.6f // Trang bên cạnh
                else -> 0.3f // Trang xa hơn
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
            ) {
                Image(
                    painter = painterResource(id = pages[actualPage].image),
                    contentDescription = pages[actualPage].title,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .fillMaxWidth()
                        .aspectRatio(0.7f)
                )

                // Chỉ hiển thị text cho trang chính giữa
                if (pageOffset.absoluteValue < 0.1f) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = pages[actualPage].title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = pages[actualPage].description,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        }

        // Indicator
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(vertical = 16.dp)
                .fillMaxWidth()
        ) {
            repeat(actualPageCount) { index ->
                val currentActualPage = ((pagerState.currentPage - startPage) % actualPageCount + actualPageCount) % actualPageCount
                val isSelected = currentActualPage == index
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(if (isSelected) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) MaterialTheme.colorScheme.onSurface else Color.LightGray)
                )
            }
        }

        // Button Create Account
        Button(
            onClick = { navController.navigate("register") },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF8EAFB)),
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Create an account",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Text Sign In
        TextButton(
            onClick = { navController.navigate("login") }
        ) {
            Row {
                Text(text = "Already have an account? ", color = Color.Gray)
                Text(text = "Sign In", color = Color.Magenta)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}