package com.example.dating.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.dating.ui.theme.AppColors
import com.example.dating.data.model.Resource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostStoryScreen(
    navController: NavController,
    postState: Resource<List<com.example.dating.data.model.Story>>?,
    onPost: (caption: String?, uris: List<Uri>) -> Unit,
    onClearState: () -> Unit
) {
    // Multiple image support
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var currentImageIndex by remember { mutableStateOf(0) }
    var caption by remember { mutableStateOf("") }

    // Adjustment states per image
    val scales = remember { mutableStateListOf<Float>() }
    val offsetsX = remember { mutableStateListOf<Float>() }
    val offsetsY = remember { mutableStateListOf<Float>() }
    val rotations = remember { mutableStateListOf<Float>() }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedImageUris = uris
        currentImageIndex = 0
        scales.clear(); offsetsX.clear(); offsetsY.clear(); rotations.clear()
        repeat(uris.size) {
            scales.add(1f)
            offsetsX.add(0f)
            offsetsY.add(0f)
            rotations.add(0f)
        }
    }

    LaunchedEffect(postState) {
        if (postState is Resource.Success) {
            onClearState()
            navController.popBackStack()
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        color = Color.Black
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 0.dp, vertical = 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 8.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        onClearState()
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                    Button(
                        onClick = {
                            if (selectedImageUris.isNotEmpty()) {
                                onPost(caption, selectedImageUris)
                            } else {
                                onPost(caption, emptyList())
                            }
                        },
                        enabled = (selectedImageUris.isNotEmpty()) && postState !is Resource.Loading,
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Text_Pink)
                    ) {
                        Text("Share", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Image frame (fills most of the screen)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.Black)
                        .border(2.dp, AppColors.Text_Pink, RoundedCornerShape(0.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUris.isNotEmpty()) {
                        val uri = selectedImageUris.getOrNull(currentImageIndex)
                        if (uri != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(currentImageIndex) {
                                        detectTransformGestures { _, pan, zoom, rot ->
                                            scales[currentImageIndex] = (scales[currentImageIndex] * zoom).coerceIn(0.5f, 4f)
                                            offsetsX[currentImageIndex] += pan.x
                                            offsetsY[currentImageIndex] += pan.y
                                            rotations[currentImageIndex] += rot
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(uri),
                                    contentDescription = "Story Image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight()
                                        .graphicsLayer(
                                            scaleX = scales[currentImageIndex],
                                            scaleY = scales[currentImageIndex],
                                            translationX = offsetsX[currentImageIndex],
                                            translationY = offsetsY[currentImageIndex],
                                            rotationZ = rotations[currentImageIndex]
                                        )
                                        .clip(RoundedCornerShape(0.dp)),
                                    contentScale = ContentScale.Fit
                                )
                            }
                            // Controls: Reset, Rotate, Crop (placeholder)
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        scales[currentImageIndex] = 1f
                                        offsetsX[currentImageIndex] = 0f
                                        offsetsY[currentImageIndex] = 0f
                                        rotations[currentImageIndex] = 0f
                                    },
                                    border = BorderStroke(1.dp, Color.White),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                ) {
                                    Text("Reset")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedButton(
                                    onClick = { rotations[currentImageIndex] += 90f },
                                    border = BorderStroke(1.dp, AppColors.Text_Pink),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Text_Pink)
                                ) {
                                    Icon(Icons.Default.RotateRight, contentDescription = "Rotate")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Rotate")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedButton(
                                    onClick = { /* TODO: Integrate real cropper library */ },
                                    border = BorderStroke(1.dp, AppColors.Text_Pink),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Text_Pink)
                                ) {
                                    Icon(Icons.Default.Crop, contentDescription = "Crop")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Crop")
                                }
                            }
                        }
                        // Thumbnails for switching images
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 8.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            selectedImageUris.forEachIndexed { idx, thumbUri ->
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .padding(horizontal = 4.dp)
                                        .border(
                                            2.dp,
                                            if (idx == currentImageIndex) AppColors.Text_Pink else Color.White,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { currentImageIndex = idx }
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(thumbUri),
                                        contentDescription = "Thumbnail $idx",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    } else {
                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Main_Secondary1)
                        ) {
                            Text("Select Images", color = AppColors.Text_Pink, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Caption input
                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("Add a caption...", color = Color.White) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .padding(horizontal = 16.dp),
                    singleLine = false,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color.LightGray,
                        errorTextColor = Color.Red,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent,
                        cursorColor = AppColors.Text_Pink,
                        focusedBorderColor = AppColors.Text_Pink,
                        unfocusedBorderColor = Color.White,
                        disabledBorderColor = Color.LightGray,
                        errorBorderColor = Color.Red,
                        focusedLabelColor = AppColors.Text_Pink,
                        unfocusedLabelColor = Color.White,
                        disabledLabelColor = Color.LightGray,
                        errorLabelColor = Color.Red
                    )
                )

                if (postState is Resource.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .align(Alignment.CenterHorizontally),
                        color = AppColors.Text_Pink
                    )
                }
                if (postState is Resource.Failure) {
                    Text(
                        text = (postState as Resource.Failure).exception.localizedMessage ?: "Failed to post story.",
                        color = Color.Red,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }
}
