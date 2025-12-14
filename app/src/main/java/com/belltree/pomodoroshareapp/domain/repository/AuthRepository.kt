package com.belltree.pomodoroshareapp.domain.repository

import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
     fun getCurrentUser(): FirebaseUser?
     fun signInWithGoogle(
          idToken: String,
          onResult: (success: Boolean, isNewUser: Boolean, error: String?) -> Unit
     )
     fun signInAnonymously(onResult: (Boolean, String?) -> Unit)
     fun signOut()
}