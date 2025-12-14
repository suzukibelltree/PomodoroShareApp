package com.belltree.pomodoroshareapp.infra.api
import android.annotation.SuppressLint
import com.belltree.pomodoroshareapp.AppContextHolder
import com.belltree.pomodoroshareapp.auth.TokenManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object ApiEndpoints {
    const val BASE = ""
    object Auth {
        const val LOGIN = "$BASE/"
        const val REGISTER = "$BASE/"
        const val GOOGLE = "$BASE/"
        const val LOGOUT = "$BASE/"
        const val VERIFY = ""
    }
}

object ApiClient {
    private val client = OkHttpClient()
    var token: String? = null
    private val gson = Gson()
    @SuppressLint("StaticFieldLeak")
    private val tokenManager= TokenManager(AppContextHolder.appContext)

    suspend fun request(
        url: String,
        method: String = "GET",
        body: Map<String, Any>? = null
    ): String? = withContext(Dispatchers.IO) {

        token=tokenManager.getToken()
        val builder = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .apply { token?.let { addHeader("Authorization", "Bearer $it") } }

        val requestBody = body?.let { gson.toJson(it).toRequestBody("application/json".toMediaType()) }
        println(token)
        builder.method(method, if (method.uppercase() == "GET") null else requestBody)
        println(requestBody.toString())
        val response = client.newCall(builder.build()).execute()

        response.body.string()

    }
}