package com.belltree.pomodoroshareapp.Login

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.IntentSenderRequest
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.belltree.pomodoroshareapp.BuildConfig
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
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

	// Google Play services availability (avoid initializing Identity client when unavailable)
	val hasPlayServices = remember {
		GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
	}
	val signInRequest = remember {
		BeginSignInRequest.Builder()
			.setGoogleIdTokenRequestOptions(
				BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
					.setSupported(true)
					// Firebaseコンソール -> 認証 -> サインイン方法 -> Google -> WebクライアントID を指定
					.setServerClientId(BuildConfig.WEB_CLIENT_ID)
					.setFilterByAuthorizedAccounts(false)
					.build()
			)
			.setAutoSelectEnabled(false)			.build()
	}

	var launching by remember { mutableStateOf(false) }

	val launcher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.StartIntentSenderForResult()
	) { result ->
		launching = false
		if (result.resultCode == Activity.RESULT_OK) {
			try {
				val credential = Identity.getSignInClient(context).getSignInCredentialFromIntent(result.data)
				val idToken = credential.googleIdToken
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

			Button(enabled = !isLoading && !launching && hasPlayServices, onClick = {
				if (activity == null) return@Button
				launching = true
				// Initialize client only when needed and when Play services are available
				val oneTapClient = Identity.getSignInClient(context)
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
			}) {
				Text("Googleでサインイン")
			}
			if (!hasPlayServices) {
				Spacer(Modifier.height(8.dp))
				Text("Google Play 開発者サービスが利用できない端末です", style = MaterialTheme.typography.bodySmall)
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
		SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
	}
}