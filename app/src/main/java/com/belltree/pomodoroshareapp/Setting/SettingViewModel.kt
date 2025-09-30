package com.belltree.pomodoroshareapp.Setting

import android.content.Context
import android.net.Uri
import android.util.Log
import android.util.Base64
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belltree.pomodoroshareapp.BuildConfig
import com.belltree.pomodoroshareapp.domain.models.RewardState
import com.belltree.pomodoroshareapp.domain.models.User
import com.belltree.pomodoroshareapp.domain.repository.AuthRepository
import com.belltree.pomodoroshareapp.domain.repository.UserRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository,
    private val supabaseClient: SupabaseClient
) : ViewModel(
) {
    // DI 未導入のため内部生成（将来 Hilt へ移行予定）
    // 現在ログインしているユーザー

    val userId: String = auth.currentUser?.uid ?: "Unknown"
    private val _currentUser = mutableStateOf<FirebaseUser?>(authRepository.getCurrentUser())
    val currentUser: State<FirebaseUser?> = _currentUser

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _isNewUser = mutableStateOf(false)
    val isNewUser: State<Boolean> = _isNewUser

    private val _goalStudyTime = mutableStateOf<Long?>(null)
    val goalStudyTime: State<Long?> = _goalStudyTime

    private val _ownerPhotoUrl = MutableStateFlow<String>("")
    val ownerPhotoUrl: StateFlow<String> = _ownerPhotoUrl

    private val _isUploadingImage = MutableStateFlow(false)
    val isUploadingImage: StateFlow<Boolean> = _isUploadingImage

    // サインアウトを行う関数
    fun signOut() {
        authRepository.signOut()
        _currentUser.value = null
        _isNewUser.value = false
        _errorMessage.value = null
    }
    suspend fun updateUserProfiles(
        goalStudyTime: String
    ){
        _isLoading.value = true
        val uid = _currentUser.value?.uid
        val goal = goalStudyTime.toLongOrNull()

        if (uid == null || goal == null) {
            _errorMessage.value = "ユーザーIDまたは目標時間が不正です"
            _isLoading.value = false
            return
        }

        userRepository.updateUserToFirestore(
            userId = uid,
            updates = mapOf("goalStudyTime" to goal)
        )
        _goalStudyTime.value = goal
        _isLoading.value = false
    }

    fun loadCurrentUserGoal() {
        val uid = _currentUser.value?.uid ?: return
        viewModelScope.launch {
            val user = userRepository.getUserById(uid)
            _goalStudyTime.value = user?.goalStudyTime
        }
    }

    fun loadOwner() {
        viewModelScope.launch {
            val u = userRepository.getUserById(userId)
            _ownerPhotoUrl.value = u?.photoUrl ?: ""
        }
    }

    fun uploadProfileImage(uri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                _isUploadingImage.value = true
                val uid = auth.currentUser?.uid ?: return@launch
                
                // Firebase ID トークンを取得
                val token = Firebase.auth.currentUser?.getIdToken(false)
                    ?.await()?.token
                
                if (token == null) {
                    Log.e("SettingViewModel", "Failed to get Firebase ID token")
                    _errorMessage.value = "認証トークンの取得に失敗しました"
                    _isUploadingImage.value = false
                    return@launch
                }
                
                // URIからバイト配列を読み込む
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                
                if (bytes == null) {
                    _errorMessage.value = "画像の読み込みに失敗しました"
                    _isUploadingImage.value = false
                    return@launch
                }
                
                withContext(Dispatchers.IO) {
                    try {
                        // Base64エンコード（Data URI形式）
                        val base64Image = "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
                        
                        // Edge Functionを呼び出してアップロード（既存のupload-profileを使用）
                        val publicUrl = callUploadImageEdgeFunction(
                            endpoint = "https://jaemimxpboicrxbpaycq.functions.supabase.co/upload-profile",
                            userId = uid,
                            imageBase64 = base64Image,
                            token = token
                        )
                        
                        if (publicUrl == null) {
                            withContext(Dispatchers.Main) {
                                _errorMessage.value = "画像のアップロードに失敗しました"
                            }
                            return@withContext
                        }
                        
                        val urlWithVersion = "$publicUrl?v=${System.currentTimeMillis()}"
                        Log.i("SettingViewModel", "Uploaded image to: $urlWithVersion")
                        
                        // Firestoreを更新
                        userRepository.updateUserToFirestore(
                            userId = uid,
                            updates = mapOf("photoUrl" to urlWithVersion)
                        )
                        
                        // Firebase Authenticationのプロフィールを更新
                        auth.currentUser?.let { user ->
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setPhotoUri(Uri.parse(urlWithVersion))
                                .build()
                            user.updateProfile(profileUpdates).await()
                        }
                        
                        // UIを更新
                        withContext(Dispatchers.Main) {
                            _ownerPhotoUrl.value = urlWithVersion
                        }
                        
                    } catch (e: Exception) {
                        Log.e("SettingViewModel", "Failed to upload image", e)
                        withContext(Dispatchers.Main) {
                            _errorMessage.value = "画像のアップロードに失敗しました: ${e.message}"
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SettingViewModel", "Error reading image", e)
                _errorMessage.value = "画像の読み込みに失敗しました"
            } finally {
                _isUploadingImage.value = false
            }
        }
    }
    
    private suspend fun callUploadImageEdgeFunction(
        endpoint: String,
        userId: String,
        imageBase64: String,
        token: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            // Edge FunctionはimageUrlパラメータでData URIを受け取る
            val jsonBody = """{"userId":"$userId","imageUrl":"$imageBase64"}"""
            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val body = RequestBody.create(mediaType, jsonBody)
            val request = Request.Builder()
                .url(endpoint)
                .post(body)
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "application/json")
                .build()

            val client = OkHttpClient.Builder()
                .connectTimeout(java.time.Duration.ofSeconds(30))
                .readTimeout(java.time.Duration.ofSeconds(30))
                .writeTimeout(java.time.Duration.ofSeconds(30))
                .build()
                
            val response = client.newCall(request).execute()
            response.use { resp ->
                if (!resp.isSuccessful) {
                    Log.w("SettingViewModel", "Edge function http ${resp.code}")
                    return@withContext null
                }
                val responseStr = resp.body?.string() ?: return@withContext null
                try {
                    val obj = org.json.JSONObject(responseStr)
                    obj.optString("publicUrl", null)
                } catch (e: Exception) {
                    Log.w("SettingViewModel", "Edge function parse error", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("SettingViewModel", "Edge function call failed", e)
            null
        }
    }
}
