package com.belltree.pomodoroshareapp.Setting

import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import com.belltree.pomodoroshareapp.R
import com.belltree.pomodoroshareapp.notification.NoonAlarmScheduler
import com.belltree.pomodoroshareapp.ui.components.AppTopBar
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.TextUnit
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage
import coil.request.ImageRequest


object Variables {
    val StaticLabelLargeLineHeight: TextUnit = 20.sp
}

@Composable
fun SettingScreen(
    modifier: Modifier = Modifier,
    settingViewModel: SettingViewModel,
    onSignOut: () -> Unit,
    onNavigateHome: () -> Unit = {}
) {

    val context = LocalContext.current
    var goalStudyTimeInput by remember { mutableStateOf("") }
    val ownerPhotoUrl by settingViewModel.ownerPhotoUrl.collectAsState()
    val isUploadingImage by settingViewModel.isUploadingImage.collectAsState()
    
    // ダークモード判定
    val isDarkMode = MaterialTheme.colorScheme.background.run {
        val rgb = this.red + this.green + this.blue
        rgb < 1.5f // 背景が暗い場合
    }
    val textColor = if (isDarkMode) Color(0xFFE6E0E9) else Color(0xFF234121)
    val bgColor = if (isDarkMode) Color(0xFF1C1B1F) else Color(0xFFFFFBFE)
    val cardBgColor = if (isDarkMode) Color(0xFF2B2930) else Color.White
    
    // 画像クロッパー用のランチャー
    val imageCropperLauncher = rememberLauncherForActivityResult(
        contract = CropImageContract()
    ) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { uri ->
                settingViewModel.uploadProfileImage(uri, context)
            }
        }
    }
    
    LaunchedEffect(Unit) {
        settingViewModel.loadCurrentUserGoal()
        settingViewModel.loadOwner()
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "設定",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onNavigateHome,
            )
        },
        containerColor = bgColor
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding() + 12.dp)
        ) {

            Image(
                painter = painterResource(id = R.drawable.rectangle6),
                contentDescription = "background image",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .width(371.dp)
                    .height(700.dp)
                    .align(Alignment.TopCenter)
                    .alpha(0.7f) // 这里指定透明度，范围 0f ~ 1f
            )

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(2.dp)
                    .width(371.dp)
                    .height(456.dp) ,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(ownerPhotoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                    )
                    
                    if (isUploadingImage) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(100.dp)
                                .align(Alignment.Center),
                            color = if (isDarkMode) Color(0xFF8ECFE3) else Color(0xFF446E36)
                        )
                    }
                }

                Button(
                    onClick = { 
                        // 画像クロッパーを起動（円形クロップ設定）
                        val cropOptions = CropImageContractOptions(
                            uri = null,
                            cropImageOptions = CropImageOptions(
                                guidelines = CropImageView.Guidelines.ON,
                                cropShape = CropImageView.CropShape.OVAL, // 円形クロップ
                                aspectRatioX = 1, // 1:1のアスペクト比
                                aspectRatioY = 1,
                                fixAspectRatio = true, // アスペクト比を固定
                                allowRotation = true, // 回転を許可
                                allowFlipping = true, // 反転を許可
                                imageSourceIncludeGallery = true,
                                imageSourceIncludeCamera = true
                            )
                        )
                        imageCropperLauncher.launch(cropOptions)
                    },
                    enabled = !isUploadingImage,
                    modifier = Modifier
                        .width(180.dp)
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkMode) Color(0xFF48B3D3) else Color(0xFF48B3D3),
                        disabledContainerColor = if (isDarkMode) Color(0xFF49454E) else Color(0xFFCCCCCC)
                    )
                ){
                    Text(
                        text = if (isUploadingImage) "アップロード中..." else "画像をアップロード",
                        fontSize = 14.sp,
                        color = if (isUploadingImage) (if (isDarkMode) Color(0xFFCAC7D0) else Color.Gray) else Color.White,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "一週間の目標勉強時間を決めよう\n毎週土曜日にリセットされるよ！",
                    fontSize = 16.sp,
                    color = textColor,
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = goalStudyTimeInput,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() } && newValue.length <= 2) {
                            goalStudyTimeInput = newValue
                        }
                    },
                    label = { Text("目標時間") },
                    placeholder = { Text("例：5") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isDarkMode) Color(0xFF8ECFE3) else Color(0xFF446E36),
                        unfocusedBorderColor = if (isDarkMode) Color(0xFF49454E) else Color(0xFF446E36),
                        cursorColor = if (isDarkMode) Color(0xFF8ECFE3) else Color(0xFF446E36),
                        focusedLabelColor = if (isDarkMode) Color(0xFF8ECFE3) else Color(0xFF263F1F),
                        unfocusedLabelColor = if (isDarkMode) Color(0xFFCAC7D0) else Color(0xFF263F1F),
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor
                    ),
                    modifier = Modifier
                        .width(300.dp)
                        .padding(horizontal = 16.dp)
                )

                Button(
                    onClick = {
                        if (goalStudyTimeInput.isNotBlank()) {
                            settingViewModel.viewModelScope.launch {
                                settingViewModel.updateUserProfiles(
                                    goalStudyTime = goalStudyTimeInput
                                )
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    val alarmManager =
                                        context.getSystemService(AlarmManager::class.java)
                                    if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                                        Toast.makeText(
                                            context,
                                            "正確なアラームの許可を設定画面で有効にしてください",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        val intent =
                                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(intent)
                                    }
                                }
                                NoonAlarmScheduler.cancelDailyNoon(context)
                                NoonAlarmScheduler.scheduleDailyNoon(context)
                            }
                        }
                    },
                    modifier = Modifier
                        .width(160.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkMode) Color(0xFF7CB342) else Color(0xFFA6C242)
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "目標を設定する",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }


                Spacer(modifier = Modifier.height(4.dp))



                val currentGoal = settingViewModel.goalStudyTime.value
                if (currentGoal != null) {
                    SelectionContainer {
                        Text(
                            text = "目標時間：　${currentGoal}　(h)",
                            style = TextStyle(
                                fontSize = 24.sp,
                                lineHeight = Variables.StaticLabelLargeLineHeight,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                textAlign = TextAlign.Center,
                            ),
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .fillMaxWidth()
                        )
                    }
                }

                Text(
                    text = "勉強するほどたまっていくよ\nたまるほどフレームが豪華になっていくよ！\n毎週土曜日にリセットされるよ",
                    fontSize = 16.sp,
                    color = textColor,
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                )

                val totalStudyPoint = settingViewModel.totalStudyPoint.value
                SelectionContainer {
                    Text(
                        text = "勉強ポイント：　${totalStudyPoint}　(P)",
                        style = TextStyle(
                            fontSize = 24.sp,
                            lineHeight = Variables.StaticLabelLargeLineHeight,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            textAlign = TextAlign.Center,
                        ),
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .fillMaxWidth()
                    )
                }

                Button(
                    onClick = onSignOut,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .width(129.dp)
                        .height(56.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .width(81.dp)
                            .height(24.dp)
                    ) {
                        Text(
                            text = "ログアウト",
                            fontSize = 16.sp,
                            style = TextStyle(
//
                                fontWeight = FontWeight(500),
                                color = Color(0xFFE76D48),
                            )
                        )
                    }


                }
            }
        }
    }
}