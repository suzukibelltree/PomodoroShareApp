package com.belltree.pomodoroshareapp.login

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults // ButtonDefaultsをインポート
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.belltree.pomodoroshareapp.BuildConfig
import com.belltree.pomodoroshareapp.R
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes

/**
 * Googleサインイン + 匿名ログイン UI
 */
@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onSignedIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage
    val isNewUser by viewModel.isNewUser

    val context = LocalContext.current
    val activity = context as? Activity
    val snackbarHostState = remember { SnackbarHostState() }

    // Google One Tap Client
    val oneTapClient = remember { Identity.getSignInClient(context) }//ワンタップログインのオブジェクト
    val signInRequest = remember {
        BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)//IDトークンを使うログイン
                    // Firebaseコンソール -> 認証 -> サインイン方法 -> Google -> WebクライアントID を指定
                    .setServerClientId(BuildConfig.WEB_CLIENT_ID)//firebaseプロジェクトであることを示す
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(false).build()
    }

    var launching by remember { mutableStateOf(false) }


    //ログイン結果を受け取る仕組み
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()//別画面から結果を受け取る
    ) { result ->
        launching = false
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(result.data)//サインイン結果を取り出す
                val idToken = credential.googleIdToken//Googleトークンの取得
                if (idToken != null) {
                    viewModel.signInWithGoogle(idToken)
                } else {
                    viewModel.onLoginFailed("ID Tokenが取得できませんでした")
                }
            } catch (e: ApiException) {
                viewModel.onLoginFailed("Googleサインインエラー: ${e.statusCode}")
            }
        } else {
            // ユーザーキャンセル
        }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) snackbarHostState.showSnackbar(errorMessage!!)
    }

    // 既にログイン済なら遷移
    LaunchedEffect(currentUser) {
        if (currentUser != null) onSignedIn()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterVertically)
        ) {

            Image(
                painter = painterResource(id = R.drawable.icon_transparent),
                contentDescription = "image description",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .width(267.dp)
                    .height(267.dp)
            )


            Text(
                text = "Pomodoro Shareにようこそ",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight(500),
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center,
                ),
                modifier = Modifier
                    .width(313.dp)
                    .height(29.dp)
            )

            Spacer(Modifier.height(22.dp))
            // 横線を追加
            Column(
                verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .width(320.dp) // 幅を320dpに設定
                    .height(1.00003.dp) // 高さを1.00003dpに設定
                    .padding(start = 16.dp) // 左パディングを16dpに設定
            ) {
                Box(
                    modifier = Modifier
                        .padding(0.dp)
                        .width(304.dp)
                        .height(1.dp)
                        .background(color = Color(0xFFC4C4C4))
                )
            }
            Spacer(Modifier.height(24.dp))

            Text(
                text = "Please log in to continue",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center,
                ),
                modifier = Modifier
                    .width(352.dp)
                    .height(40.dp)
            )

            Spacer(Modifier.height(0.dp))

            // Googleサインインボタン
            Button(
                enabled = !isLoading && !launching,
                onClick = {
                    if (activity == null) return@Button
                    launching = true
                    oneTapClient.beginSignIn(signInRequest)
                        .addOnSuccessListener(activity) { result ->
                            val request = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                            launcher.launch(request)
                        }
                        .addOnFailureListener(activity) { e ->
                            launching = false
                            val apiMsg = if (e is ApiException) {
                                val code = e.statusCode
                                val meaning = when (code) {
                                    CommonStatusCodes.DEVELOPER_ERROR -> "DEVELOPER_ERROR (設定不備)"
                                    CommonStatusCodes.NETWORK_ERROR -> "NETWORK_ERROR"
                                    CommonStatusCodes.INTERNAL_ERROR -> "INTERNAL_ERROR"
                                    CommonStatusCodes.CANCELED -> "CANCELED"
                                    else -> "code=$code"
                                }
                                "One Tap開始失敗: $meaning: ${e.message}"
                            } else {
                                "One Tap開始失敗: ${e.message}"
                            }
                            viewModel.onLoginFailed(apiMsg)
                        }
                },
                modifier = Modifier
                    .width(294.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF48B3D3)),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    disabledElevation = 0.dp
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 0.dp)
                ) {
                    // アイコン
                    Image(
                        painter = painterResource(id = R.drawable.account_circle),
                        contentDescription = "Googleアカウントアイコン",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .width(24.dp)
                            .height(24.dp)
                    )

                    // テキスト
                    Text(
                        text = "Googleアカウントでログイン",
                        style = TextStyle(
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    )
                }
            }

            Spacer(Modifier.height(0.dp))

            // 匿名ログインボタン
            Button(
                onClick = { viewModel.signInAnonymously() },
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFF9C9C9C),
                    disabledContentColor = Color(0xFFB0B0B0)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
                modifier = Modifier
                    .width(161.dp)
                    .height(56.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .width(113.dp) // Rowの幅を113dpに設定
                        .height(24.dp) // Rowの高さを24dpに設定
                ) {
                    Text(
                        text = "匿名でログイン", // テキストを修正
                        style = TextStyle(
                            fontSize = 16.sp, // Variables.StaticTitleMediumSize
                            lineHeight = 24.sp, // Variables.StaticTitleMediumLineHeight
                            fontFamily = FontFamily.Default, // デフォルトフォントを使用
                            fontWeight = FontWeight(500),
                            color = Color(0xFF9C9C9C),
                            letterSpacing = 0.15.sp, // Variables.StaticTitleMediumTracking
                        )
                    )
                }
            }
            Spacer(Modifier.height(120.dp))


            if (isLoading || launching) {
                Spacer(Modifier.height(0.dp))
                CircularProgressIndicator(
                    color =  Color(0xFF48B3D3),
                    modifier = Modifier
                        .width(12.dp)
                        .height(12.dp) ,
                    strokeWidth = 2.dp   // 线条粗细

                )
            }

            if (isNewUser && currentUser != null) {
                Spacer(Modifier.height(16.dp))
                Text("初回登録が完了しました", style = MaterialTheme.typography.bodyMedium)
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

