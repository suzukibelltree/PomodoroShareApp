package com.belltree.pomodoroshareapp.login

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.belltree.pomodoroshareapp.BuildConfig
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

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
				.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Pomodoro Share",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(32.dp))

            Button(enabled = !isLoading && !launching, onClick = {
                if (activity == null) return@Button
                launching = true
                oneTapClient.beginSignIn(signInRequest)
                    .addOnSuccessListener(activity) { result ->
                        val request =
                            IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
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
            }) {
                Text("Googleでサインイン")
            }
            Spacer(Modifier.height(12.dp))
            Button(enabled = !isLoading, onClick = { viewModel.signInAnonymously() }) {
                Text("ゲストで続行")
            }

            if (isLoading || launching) {
                Spacer(Modifier.height(24.dp))
                CircularProgressIndicator()
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