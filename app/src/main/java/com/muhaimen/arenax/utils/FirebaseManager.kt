package com.muhaimen.arenax.utils

import android.app.Activity
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.muhaimen.arenax.dataClasses.UserData

object FirebaseManager {


    private const val verificationTimeout = 120_000L // 2 minutes in milliseconds
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val database: DatabaseReference by lazy { FirebaseDatabase.getInstance().getReference("userData") }

    // Sign up a new user with email and password
    fun signUpUser(email: String, password: String, user: UserData, callback: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                    // Set the userId in the UserData object
                    val newUser = user.copy(userId = userId)

                    // Save user data under the parent node "userData"
                    database.child(userId).setValue(newUser)
                        .addOnSuccessListener {
                            sendVerificationEmail { success, error ->
                                if (success) {
                                    callback(true, null) // Indicate successful registration
                                } else {
                                    callback(false, error) // Handle the error from sending email
                                }
                            }
                        }
                        .addOnFailureListener { callback(false, it.message) }
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }

    fun updateUserEmailVerificationStatus(callback: (Boolean, String?) -> Unit) {
        val userId = getCurrentUserId() // Fetch the current user ID
        if (userId != null) {
            // Reference to the specific user node in Firebase
            val userRef = database.child(userId)

            // Directly update the accountVerified field to true
            userRef.child("accountVerified").setValue(true)
                .addOnSuccessListener {
                    // If the update is successful, invoke the callback with success
                    callback(true, null)
                }
                .addOnFailureListener { e ->
                    // If there's a failure, invoke the callback with an error message
                    callback(false, e.message)
                }
        } else {
            // If userId is null, callback with an error
            callback(false, "User ID not found.")
        }
    }

    // Send email verification to the user
    fun sendVerificationEmail(callback: (Boolean, String?) -> Unit) {
        val user = auth.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(true, null) // Email sent successfully
            } else {
                Log.e("FirebaseManager", "Error sending verification email: ${task.exception?.message}")
                callback(false, task.exception?.message) // Pass the error message to the callback
            }
        } ?: run {
            callback(false, "User is not signed in") // User is not signed in
        }
    }

    // Check if the user's email is verified
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

    // Sign in with email and password
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

    // Save user data to the Firebase Realtime Database
    fun saveUserData(user: UserData, callback: (Boolean, String?) -> Unit) {
        val userId = user.userId
        database.child(userId).setValue(user)
            .addOnSuccessListener { callback(true, null) }
            .addOnFailureListener { callback(false, it.message) }
    }

    // Sign out the current user
    fun signOutUser() {
        auth.signOut()
    }

    // Get the current user's ID
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    // Check if the user is logged in
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    // Get the current Firebase user object
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    // Retrieve user data by user ID
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

    // Delete user data from Firebase Realtime Database
    fun deleteUserData(callback: (Boolean, String?) -> Unit = { _, _ -> }) {
        // Get the current user ID
        val userId = getCurrentUserId() ?: run {
            callback(false, "User ID not found")
            return
        }

        // Remove user data from the database
        database.child(userId).removeValue() // Fixed to use userId directly
            .addOnSuccessListener {
                // After removing user data from the database, delete the user from Firebase Auth
                auth.currentUser?.delete()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // If user deletion is successful, return success in the callback
                        callback(true, null)
                    } else {
                        // If there is an error while deleting the user, return the error message
                        callback(false, task.exception?.message ?: "Unknown error occurred while deleting user.")
                    }
                }
            }
            .addOnFailureListener {
                // If there is an error while removing user data from the database, return the error message
                callback(false, it.message ?: "Error occurred while deleting user data.")
            }
    }

    fun checkIfEmailExists(email: String, callback: (Boolean, Boolean?, String?) -> Unit) {
        database.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Email exists
                    for (childSnapshot in snapshot.children) {
                        val userData = childSnapshot.getValue(UserData::class.java)
                        if (userData != null) {
                            // Check if the user is verified
                            val isVerified = auth.currentUser?.isEmailVerified ?: false
                            callback(true, isVerified, null)
                            return
                        }
                    }
                    callback(false, null, "User data not found.")
                } else {
                    // Email does not exist
                    callback(false, null, null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, null, error.message)
            }
        })
    }
}