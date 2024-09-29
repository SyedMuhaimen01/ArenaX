package com.muhaimen.arenax.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import com.muhaimen.arenax.dataClasses.UserData
import com.muhaimen.arenax.R
import java.util.*

object FirebaseManager {

    private const val verificationTimeout = 120_000L // 2 minutes in milliseconds
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val database: DatabaseReference by lazy { FirebaseDatabase.getInstance().getReference("userData") }

    @SuppressLint("StaticFieldLeak")
    private lateinit var googleSignInClient: GoogleSignInClient

    fun configureGoogleSignIn(activity: Activity) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(activity, gso)
    }

    fun getGoogleSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    fun handleGoogleSignInResult(data: Intent?, activity: Activity, callback: (Boolean, GoogleSignInAccount?, String?) -> Unit) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            callback(true, account, null)
        } catch (e: ApiException) {
            callback(false, null, "Google Sign-In error: ${e.message}")
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String, callback: (Boolean, String?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }

    fun signUpWithEmail(email: String, password: String, user: UserData, callback: (Boolean, String?) -> Unit) {
        val hashedPassword = securityUtils.hashPassword(password)

        auth.createUserWithEmailAndPassword(email, hashedPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val newUser = user.copy(userId = userId, password = hashedPassword)

                    database.child(userId).setValue(newUser)
                        .addOnSuccessListener {
                            sendVerificationEmail {
                                // No need to check email verification immediately here
                                callback(true, null) // Indicate successful registration
                            }
                        }
                        .addOnFailureListener { callback(false, it.message) }
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }

    fun sendVerificationEmail(callback: () -> Unit) {
        val user = auth.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback()
            } else {
                callback() // Handle failure appropriately if needed
            }
        }
    }

    fun checkEmailVerification(callback: (Boolean, String?) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            user.reload().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(user.isEmailVerified, null)
                } else {
                    callback(false, task.exception?.message)
                }
            }
        } else {
            callback(false, "User not signed in")
        }
    }

    fun deleteUserData(userId: String, callback: (Boolean, String?) -> Unit = { _, _ -> }) {
        database.child(userId).removeValue()
            .addOnSuccessListener {
                auth.currentUser?.delete()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        callback(true, null)
                    } else {
                        callback(false, task.exception?.message)
                    }
                }
            }
            .addOnFailureListener { callback(false, it.message) }
    }

    fun signInWithEmail(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        val hashedPassword = securityUtils.hashPassword(password)

        auth.signInWithEmailAndPassword(email, hashedPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun saveUserData(user: UserData, callback: (Boolean, String?) -> Unit) {
        val userId = user.userId
        database.child(userId).setValue(user)
            .addOnSuccessListener { callback(true, null) }
            .addOnFailureListener { callback(false, it.message) }
    }

    fun logOut() {
        auth.signOut()
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun getUserById(userId: String, callback: (UserData?, String?) -> Unit) {
        database.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UserData::class.java)
                callback(user, null)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null, error.message)
            }
        })
    }

}
