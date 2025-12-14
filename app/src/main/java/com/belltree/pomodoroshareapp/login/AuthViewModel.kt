package com.belltree.pomodoroshareapp.login

import android.util.Log
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import com.belltree.pomodoroshareapp.domain.models.User
import com.belltree.pomodoroshareapp.domain.repository.AuthRepository
import com.belltree.pomodoroshareapp.domain.repository.UserRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import jakarta.inject.Inject
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.SupervisorJob
import okhttp3.MediaType.Companion.toMediaTypeOrNull


/**
 * 認証関連の状態と操作を管理するViewModel
 * UI側ではcurrentUserとauthStateを監視し、認証操作を呼び出す
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val supabaseClient: SupabaseClient
) : ViewModel(
) {
    // 長時間のネットワーク処理を UI スコープから分離
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _currentUser = mutableStateOf<FirebaseUser?>(authRepository.getCurrentUser())
    val currentUser: State<FirebaseUser?> = _currentUser

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _isNewUser = mutableStateOf(false)
    val isNewUser: State<Boolean> = _isNewUser

    // 匿名認証を行う関数
    fun signInAnonymously() {
        _isLoading.value = true
        authRepository.signInAnonymously { success, error ->
            _isLoading.value = false
            if (success) {
                _currentUser.value = authRepository.getCurrentUser()
                _currentUser.value?.let { user ->
                    ioScope.launch {
                        try {
                            Log.d("AuthViewModel", "Adding anonymous user to Firestore: ${user.uid}")
                            userRepository.addUserToFirestore(
                                User(
                                    userId = user.uid,
                                    userName = user.displayName ?: "Guest User",
                                    photoUrl = user.photoUrl?.toString() ?: ""
                                )
                            )
                            Log.d("AuthViewModel", "Added user to Firestore successfully: ${user.uid}")
                        } catch (e: Exception) {
                            Log.e("AuthViewModel", "Failed to add anonymous user to Firestore", e)
                            withContext(Dispatchers.Main) {
                                _errorMessage.value = "ユーザー保存に失敗しました: ${e.message}"
                            }
                        }
                    }
                }
            } else {
                _errorMessage.value = "匿名認証に失敗しました: $error"
            }
        }
    }

    fun onLoginSuccess(user: FirebaseUser?) {
        _currentUser.value = user
        _errorMessage.value = null
        val bucket = "pomodoro"
        val path = "users/${user?.uid}/profile.jpg"

        viewModelScope.launch(SupervisorJob() + Dispatchers.IO) {
            if (user == null) {
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "ログインユーザー情報の取得に失敗しました"
                }
                return@launch
            }
            Log.i("AuthViewModel", "onLoginSuccess uid=${user.uid}")
            val googleUrl = getGooglePhotoUrl(user)?.let { normalizeGooglePhotoUrl(it) }
            val urlToSave: String = if (!googleUrl.isNullOrBlank()) {
                Log.i("AuthViewModel", "Calling edge function for profile upload uid=${user.uid}")
                val publicUrlFromEdge = try {
                    withTimeout(5000) {
                        callUploadProfileEdgeFunction(
                            //edge functionURL
                            endpoint = "https://jaemimxpboicrxbpaycq.functions.supabase.co/upload-profile",
                            userId = user.uid,
                            //google画像のURL
                            sourceImageUrl = googleUrl
                        )
                    }
                } catch (e: TimeoutCancellationException) {
                    Log.w("AuthViewModel", "Edge function timeout", e)
                    null
                } catch (e: Exception) {
                    Log.w("AuthViewModel", "Edge function failed", e)
                    null
                }
                val finalUrl = (publicUrlFromEdge ?: googleUrl).trim()
                Log.i("AuthViewModel", "Resolved profile image URL length=${finalUrl.length}")
                // ログインのたびにユニークなURLを生成
                "$finalUrl?v=${System.currentTimeMillis()}"
            } else {
                (user.photoUrl?.toString() ?: "").trim()
            }

            // Firebase Authentication のユーザープロフィールにも反映（photoUrl を設定）
            try {
                val photo = urlToSave.takeIf { it.isNotBlank() }
                if (photo != null && user != null) {
                    val request = UserProfileChangeRequest.Builder()
                        .setPhotoUri(Uri.parse(photo))
                        .build()
                    user.updateProfile(request).await()
                    // ローカルの FirebaseUser を更新
                    Firebase.auth.currentUser?.reload()?.await()
                    Log.i("AuthViewModel", "FirebaseAuth photoUrl updated for uid=${user.uid}")
                }
            } catch (e: Exception) {
                Log.w("AuthViewModel", "Failed to update FirebaseAuth photoUrl", e)
            }

            try {
                Log.i("AuthViewModel", "Adding user to Firestore: ${user.uid}")
                userRepository.addUserToFirestore(
                    User(
                        userId = user.uid,
                        userName = user.displayName ?: "Guest User",
                        photoUrl = urlToSave
                    )
                )
                Log.i("AuthViewModel", "Added user to Firestore successfully: ${user.uid}")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to add user to Firestore", e)
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "ユーザー保存に失敗しました: ${e.message}"
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        ioScope.cancel()
    }

    fun onLoginFailed(message: String) {
        _currentUser.value = null
        _errorMessage.value = message
    }

    // Google サインイン（ID Token を受け取りFirebase認証）
    fun signInWithGoogle(idToken: String) {
        _isLoading.value = true
        authRepository.signInWithGoogle(idToken) { success, isNew, error ->
            _isLoading.value = false
            if (success) {
                _isNewUser.value = isNew
                onLoginSuccess(authRepository.getCurrentUser())
            } else {
                onLoginFailed(error ?: "Googleサインインに失敗しました")
            }
        }
    }

    // サインアウトを行う関数
    fun signOut() {
        authRepository.signOut()
        _currentUser.value = null
        _isNewUser.value = false
        _errorMessage.value = null
    }
}

private fun getGooglePhotoUrl(user: FirebaseUser?): String? {
    user ?: return null
    val googleProvider = user.providerData.firstOrNull { it.providerId == "google.com" }
    return googleProvider?.photoUrl?.toString() ?: user.photoUrl?.toString()
}
//画像の256×256にを指定する関数、googleプロフィール画像は一定のため必要ないかもしれない
private fun normalizeGooglePhotoUrl(original: String): String {
    return try {
        val uri = java.net.URI(original)
        val host = uri.host ?: ""
        if (host.endsWith("googleusercontent.com")) {
            val replaced = original.replace(Regex("=s\\d+(-c)?$"), "=s256-c")
            if (replaced == original) {
                if (original.contains("?")) "$original&s256-c" else "$original=s256-c"
            } else replaced
        } else original
    } catch (_: Exception) {
        original
    }
}

private suspend fun callUploadProfileEdgeFunction(
    endpoint: String,
    userId: String,
    sourceImageUrl: String
): String? = withContext(Dispatchers.IO) {
    // Get Firebase ID token
    val token = Firebase.auth.currentUser?.getIdToken(false)
        ?.addOnFailureListener { Log.e("ID_TOKEN", "failed", it) }
        ?.await()?.token ?: return@withContext null

    //edge functionにリクエストを送る
    val jsonBody = """{"userId":"$userId","imageUrl":"$sourceImageUrl"}"""
    val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
    val body = okhttp3.RequestBody.create(mediaType, jsonBody)
    val request = okhttp3.Request.Builder()
        .url(endpoint)
        .post(body)
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Content-Type", "application/json")
        .build()

    val client = okhttp3.OkHttpClient.Builder()
        .connectTimeout(java.time.Duration.ofSeconds(3))
        .readTimeout(java.time.Duration.ofSeconds(4))
        .writeTimeout(java.time.Duration.ofSeconds(4))
        .build()
    //edge functionにhttpリクエストを送信
    val response = client.newCall(request).execute()
    response.use { resp ->
        if (!resp.isSuccessful) {
            Log.w("AuthViewModel", "Edge function http ${'$'}{resp.code}")
            return@withContext null
        }
        val responseStr = resp.body?.string() ?: return@withContext null
        // parse JSON safely
        try {
            //jsonをstringに変換,publicUrlで包括
            val obj = org.json.JSONObject(responseStr)
            obj.optString("publicUrl", null)
        } catch (e: Exception) {
            Log.w("AuthViewModel", "Edge function parse error", e)
            null
        }
    }
}
