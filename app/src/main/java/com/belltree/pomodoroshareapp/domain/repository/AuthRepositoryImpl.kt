package com.belltree.pomodoroshareapp.domain.repository

import android.util.Log
import com.belltree.pomodoroshareapp.domain.models.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.firestore

class AuthRepositoryImpl : AuthRepository{

    private val firebaseAuth = FirebaseAuth.getInstance()
    // 現在のユーザーを取得する関数
    override fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    // Googleサインインを行う関数
    override fun signInWithGoogle(
        idToken: String,
        onResult: (success: Boolean, isNewUser: Boolean, error: String?) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val isNew = task.result?.additionalUserInfo?.isNewUser == true
                    onResult(true, isNew, null)
                } else {
                    onResult(false, false, task.exception?.message)
                }
            }
    }

    // 匿名サインインを行う関数
    override fun signInAnonymously(onResult: (Boolean, String?) -> Unit) {
        firebaseAuth.signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) onResult(true, null)
                else onResult(false, task.exception?.message)
            }
    }
    // サインアウトを行う関数
    override fun signOut() {
        firebaseAuth.signOut()
    }
}
