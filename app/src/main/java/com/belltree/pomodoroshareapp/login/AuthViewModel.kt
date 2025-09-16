package com.belltree.pomodoroshareapp.login

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belltree.pomodoroshareapp.domain.models.User
import com.belltree.pomodoroshareapp.domain.repository.AuthRepository
import com.belltree.pomodoroshareapp.domain.repository.UserRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
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
    // DI 未導入のため内部生成（将来 Hilt へ移行予定）
    // 現在ログインしているユーザー
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
                    viewModelScope.launch {
                        userRepository.addUserToFirestore(
                            User(
                                userId = user.uid,
                                userName = user.displayName ?: "Guest User",
                                photoUrl = user.photoUrl?.toString() ?: ""
                            )
                        )
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

        viewModelScope.launch {
            val googleUrl = getGooglePhotoUrl(user)?.let { normalizeGooglePhotoUrl(it) }
            val urlToSave: String = if (!googleUrl.isNullOrBlank()) {
                val publicUrlFromEdge = callUploadProfileEdgeFunction(
                    endpoint = "https://jaemimxpboicrxbpaycq.functions.supabase.co/upload-profile",
                    userId = user?.uid ?: "",
                    sourceImageUrl = googleUrl
                )
                val finalUrl = (publicUrlFromEdge ?: googleUrl).trim()
                // Cache bust to avoid CDN stale content
                "$finalUrl?v=${System.currentTimeMillis()}"
            } else {
                (user?.photoUrl?.toString() ?: "").trim()
            }

            userRepository.addUserToFirestore(
                User(
                    userId = user?.uid ?: "",
                    userName = user?.displayName ?: "Guest User",
                    photoUrl = urlToSave
                )
            )
        }
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

private suspend fun downloadImage(url: String): ByteArray = withContext(Dispatchers.IO) {
    val request = okhttp3.Request.Builder().url(url).build()
    val client = okhttp3.OkHttpClient()
    val response = client.newCall(request).execute()
    response.use { resp ->
        if (!resp.isSuccessful) throw IllegalStateException("Failed to download: ${resp.code}")
        resp.body?.bytes() ?: ByteArray(0)
    }
}

private suspend fun callUploadProfileEdgeFunction(
    endpoint: String,
    userId: String,
    sourceImageUrl: String
): String? = withContext(Dispatchers.IO) {
    // Get Firebase ID token
    val token = Firebase.auth.currentUser?.getIdToken(false)?.addOnSuccessListener { Log.d("ID_TOKEN", it.token ?: "") } ?.addOnFailureListener { Log.e("ID_TOKEN", "failed", it) }
        ?.await()?.token ?: return@withContext null

    val jsonBody = """{"userId":"$userId","imageUrl":"$sourceImageUrl"}"""
    val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
    val body = okhttp3.RequestBody.create(mediaType, jsonBody)
    val request = okhttp3.Request.Builder()
        .url(endpoint)
        .post(body)
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Content-Type", "application/json")
        .build()

    val client = okhttp3.OkHttpClient()
    val response = client.newCall(request).execute()
    response.use { resp ->
        if (!resp.isSuccessful) return@withContext null
        val responseStr = resp.body?.string() ?: return@withContext null
        // parse JSON safely
        try {
            val obj = org.json.JSONObject(responseStr)
            obj.optString("publicUrl", null)
        } catch (e: Exception) {
            null
        }
    }
}
