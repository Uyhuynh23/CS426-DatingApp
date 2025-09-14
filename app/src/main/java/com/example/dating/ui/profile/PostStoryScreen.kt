package com.example.dating.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.dating.data.model.ImageTransform
import com.example.dating.data.model.Resource
import com.example.dating.data.model.Story
import com.example.dating.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostStoryScreen(
    navController: NavController,
    postState: Resource<List<Story>>?,
    onPost: (caption: String?, uris: List<Uri>, transforms: List<ImageTransform>) -> Unit,
    onClearState: () -> Unit
) {
    val context = LocalContext.current
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var currentImageIndex by remember { mutableStateOf(0) }
    var caption by remember { mutableStateOf("") }

    val scales = remember { mutableStateListOf<Float>() }
    val offsetsX = remember { mutableStateListOf<Float>() }
    val offsetsY = remember { mutableStateListOf<Float>() }
    val rotations = remember { mutableStateListOf<Float>() }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        selectedImageUris = uris
        currentImageIndex = 0
        scales.clear(); offsetsX.clear(); offsetsY.clear(); rotations.clear()
        repeat(uris.size) {
            scales.add(1f); offsetsX.add(0f); offsetsY.add(0f); rotations.add(0f)
        }
    }

    LaunchedEffect(postState) {
        if (postState is Resource.Success) {
            onClearState()
            navController.popBackStack()
        }
    }

    val backgroundBrush = Brush.linearGradient(
        colors = listOf(AppColors.MainBackground11, AppColors.MainBackground12),
        start = Offset(0f, 0f),
        end = Offset(0f, 1000f)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // --- Top Bar ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    onClearState()
                    navController.popBackStack()
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
                }

                Button(
                    onClick = {
                        val transforms = selectedImageUris.mapIndexed { idx, _ ->
                            ImageTransform(
                                scaleX = scales.getOrElse(idx) { 1f },
                                scaleY = scales.getOrElse(idx) { 1f },
                                translationX = offsetsX.getOrElse(idx) { 0f },
                                translationY = offsetsY.getOrElse(idx) { 0f },
                                rotationZ = rotations.getOrElse(idx) { 0f }
                            )
                        }
                        onPost(caption.takeIf { it.isNotBlank() }, selectedImageUris, transforms)
                    },
                    enabled = selectedImageUris.isNotEmpty() && postState !is Resource.Loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Text_Pink,
                        contentColor = Color.White,
                        disabledContainerColor = Color.LightGray.copy(alpha = 0.5f),
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Share", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(8.dp))

            // --- Image Display Area ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(backgroundBrush)
                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUris.isNotEmpty()) {
                    val uri = selectedImageUris[currentImageIndex]
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(currentImageIndex) {
                                detectTransformGestures { _, pan, zoom, rot ->
                                    scales[currentImageIndex] = (scales[currentImageIndex] * zoom).coerceIn(0.5f, 4f)
                                    offsetsX[currentImageIndex] += pan.x
                                    offsetsY[currentImageIndex] += pan.y
                                    rotations[currentImageIndex] += rot
                                }
                            }
                            .graphicsLayer(
                                scaleX = scales[currentImageIndex],
                                scaleY = scales[currentImageIndex],
                                translationX = offsetsX[currentImageIndex],
                                translationY = offsetsY[currentImageIndex],
                                rotationZ = rotations[currentImageIndex]
                            )
                    )
                } else {
                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Text_Pink),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Select Images", color = Color.White)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // --- Thumbnails ---
            if (selectedImageUris.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedImageUris.forEachIndexed { idx, uri ->
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    2.dp,
                                    if (idx == currentImageIndex) AppColors.Text_Pink else Color.White.copy(alpha = 0.3f),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { currentImageIndex = idx }
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // --- Transform Controls ---
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            scales[currentImageIndex] = 1f
                            offsetsX[currentImageIndex] = 0f
                            offsetsY[currentImageIndex] = 0f
                            rotations[currentImageIndex] = 0f
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                    ) {
                        Text("Reset")
                    }

                    OutlinedButton(
                        onClick = { rotations[currentImageIndex] += 90f },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.RotateRight, contentDescription = "Rotate")
                        Spacer(Modifier.width(4.dp))
                        Text("Rotate")
                    }

                    OutlinedButton(
                        onClick = { /* TODO: implement crop */ },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Crop, contentDescription = "Crop")
                        Spacer(Modifier.width(4.dp))
                        Text("Crop")
                    }
                }

                Spacer(Modifier.height(12.dp))
            }

            // --- Caption Field ---
            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                placeholder = {
                    Text(
                        text = "Add a caption...",
                        color = Color.Black.copy(alpha = 0.6f)
                    )
                },
                maxLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColors.MainBackground12.copy(alpha = 0.5f)), // Light pink background
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Text_Pink,
                    unfocusedBorderColor = Color.LightGray,
                    cursorColor = AppColors.Text_Pink,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedLabelColor = AppColors.Text_Pink,
                    unfocusedLabelColor = Color.Black
                )
            )
            // --- Post State Feedback ---
            if (postState is Resource.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 12.dp),
                    color = AppColors.Text_Pink
                )
            }

            if (postState is Resource.Failure) {
                Text(
                    text = postState.exception.localizedMessage ?: "Failed to post story.",
                    color = Color.Red,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun PostStoryScreenPreview() {
    PostStoryScreen(
        navController = NavController(LocalContext.current),
        postState = null,
        onPost = { _, _, _ -> },
        onClearState = {}
    )
}