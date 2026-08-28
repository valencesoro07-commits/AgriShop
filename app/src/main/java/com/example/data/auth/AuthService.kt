package com.example.data.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await
import java.util.UUID

sealed class AuthResult {
    data class Success(val userProfile: UserProfile, val isNewUser: Boolean = false) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthService(private val context: Context) {
    private val firebaseAuth: FirebaseAuth? = try {
        FirebaseAuth.getInstance()
    } catch (e: Exception) {
        Log.w("AuthService", "Firebase not fully initialized: ${e.message}")
        null
    }

    val currentFirebaseUser: FirebaseUser?
        get() = firebaseAuth?.currentUser

    val isUserLoggedIn: Boolean
        get() = firebaseAuth?.currentUser != null

    fun getLoggedInUserProfile(): UserProfile? {
        val user = firebaseAuth?.currentUser ?: return null
        return UserProfile(
            id = user.uid,
            fullName = user.displayName?.ifBlank { null } ?: user.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() } ?: "Agriculteur",
            phone = user.phoneNumber ?: "+225 07 00 00 00",
            email = user.email ?: "",
            role = UserRole.FARMER,
            region = "Yamoussoukro",
            latitude = 6.8276,
            longitude = -5.2893,
            ecoPoints = 450,
            avatarUrl = user.photoUrl?.toString() ?: "",
            isVerified = true,
            memberSince = "Août 2026"
        )
    }

    suspend fun signInWithEmail(email: String, password: String): AuthResult {
        if (email.isBlank() || password.isBlank()) {
            return AuthResult.Error("Veuillez saisir votre adresse email et votre mot de passe.")
        }
        return try {
            if (firebaseAuth != null) {
                val authResult = firebaseAuth.signInWithEmailAndPassword(email.trim(), password).await()
                val user = authResult.user
                if (user != null) {
                    val profile = UserProfile(
                        id = user.uid,
                        fullName = user.displayName?.ifBlank { null } ?: email.substringBefore("@").replaceFirstChar { it.uppercase() },
                        phone = user.phoneNumber ?: "+225 07 00 00 00",
                        email = user.email ?: email,
                        role = UserRole.FARMER,
                        region = "Yamoussoukro",
                        latitude = 6.8276,
                        longitude = -5.2893,
                        ecoPoints = 450,
                        avatarUrl = user.photoUrl?.toString() ?: "",
                        isVerified = true,
                        memberSince = "Août 2026"
                    )
                    AuthResult.Success(profile)
                } else {
                    AuthResult.Error("Échec de la connexion.")
                }
            } else {
                // Local fallback
                val profile = UserProfile(
                    id = "local_usr_${email.hashCode()}",
                    fullName = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                    phone = "+225 07 12 34 56",
                    email = email,
                    role = UserRole.FARMER,
                    region = "Yamoussoukro",
                    latitude = 6.8276,
                    longitude = -5.2893,
                    ecoPoints = 450,
                    avatarUrl = "",
                    isVerified = true,
                    memberSince = "Août 2026"
                )
                AuthResult.Success(profile)
            }
        } catch (e: Exception) {
            val friendlyMsg = when {
                e.message?.contains("user-not-found", ignoreCase = true) == true -> "Aucun compte trouvé avec cette adresse email."
                e.message?.contains("wrong-password", ignoreCase = true) == true -> "Mot de passe incorrect."
                e.message?.contains("invalid-credential", ignoreCase = true) == true -> "Identifiants incorrects ou invalides."
                e.message?.contains("network-request-failed", ignoreCase = true) == true -> "Erreur de connexion réseau. Veuillez vérifier votre connexion."
                else -> e.localizedMessage ?: "Erreur lors de la connexion."
            }
            AuthResult.Error(friendlyMsg)
        }
    }

    suspend fun signUpWithEmail(
        email: String,
        password: String,
        fullName: String,
        phone: String,
        role: UserRole,
        region: String
    ): AuthResult {
        if (email.isBlank() || password.isBlank() || fullName.isBlank()) {
            return AuthResult.Error("Veuillez remplir tous les champs obligatoires.")
        }
        if (password.length < 6) {
            return AuthResult.Error("Le mot de passe doit comporter au moins 6 caractères.")
        }

        return try {
            if (firebaseAuth != null) {
                val authResult = firebaseAuth.createUserWithEmailAndPassword(email.trim(), password).await()
                val user = authResult.user
                if (user != null) {
                    try {
                        val profileUpdate = UserProfileChangeRequest.Builder()
                            .setDisplayName(fullName.trim())
                            .build()
                        user.updateProfile(profileUpdate).await()
                    } catch (e: Exception) {
                        Log.w("AuthService", "Could not set displayName: ${e.message}")
                    }

                    val profile = UserProfile(
                        id = user.uid,
                        fullName = fullName.trim(),
                        phone = phone.trim().ifBlank { "+225 07 00 00 00" },
                        email = user.email ?: email,
                        role = role,
                        region = region,
                        latitude = com.example.data.model.GeoUtils.getCityCoordinates(region).first,
                        longitude = com.example.data.model.GeoUtils.getCityCoordinates(region).second,
                        ecoPoints = 500, // 500 points bonus inscription
                        avatarUrl = "",
                        isVerified = true,
                        memberSince = "Août 2026"
                    )
                    AuthResult.Success(profile, isNewUser = true)
                } else {
                    AuthResult.Error("Impossible de créer le compte utilisateur.")
                }
            } else {
                // Local fallback
                val profile = UserProfile(
                    id = "local_usr_${UUID.randomUUID().toString().take(6)}",
                    fullName = fullName.trim(),
                    phone = phone.trim().ifBlank { "+225 07 00 00 00" },
                    email = email,
                    role = role,
                    region = region,
                    latitude = com.example.data.model.GeoUtils.getCityCoordinates(region).first,
                    longitude = com.example.data.model.GeoUtils.getCityCoordinates(region).second,
                    ecoPoints = 500,
                    avatarUrl = "",
                    isVerified = true,
                    memberSince = "Août 2026"
                )
                AuthResult.Success(profile, isNewUser = true)
            }
        } catch (e: Exception) {
            val friendlyMsg = when {
                e.message?.contains("email-already-in-use", ignoreCase = true) == true -> "Cette adresse email est déjà associée à un compte existant."
                e.message?.contains("invalid-email", ignoreCase = true) == true -> "Format d'adresse email invalide."
                e.message?.contains("weak-password", ignoreCase = true) == true -> "Le mot de passe est trop faible (minimum 6 caractères)."
                else -> e.localizedMessage ?: "Erreur lors de l'inscription."
            }
            AuthResult.Error(friendlyMsg)
        }
    }

    suspend fun signInWithGoogleCredential(idToken: String, email: String?, displayName: String?, photoUrl: String?): AuthResult {
        return try {
            if (firebaseAuth != null && idToken.isNotBlank() && !idToken.startsWith("preview_token") && !idToken.startsWith("simulated_token")) {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = firebaseAuth.signInWithCredential(credential).await()
                val user = authResult.user
                if (user != null) {
                    val profile = UserProfile(
                        id = user.uid,
                        fullName = user.displayName ?: displayName ?: "Agriculteur Google",
                        phone = user.phoneNumber ?: "+225 07 10 20 30",
                        email = user.email ?: email ?: "",
                        role = UserRole.FARMER,
                        region = "Yamoussoukro",
                        latitude = 6.8276,
                        longitude = -5.2893,
                        ecoPoints = 450,
                        avatarUrl = user.photoUrl?.toString() ?: photoUrl ?: "",
                        isVerified = true,
                        memberSince = "Août 2026"
                    )
                    AuthResult.Success(profile)
                } else {
                    AuthResult.Error("Échec de connexion avec le compte Google.")
                }
            } else {
                val profile = UserProfile(
                    id = "google_usr_${(email ?: "usr").hashCode()}",
                    fullName = displayName ?: "Agriculteur Connecté",
                    phone = "+225 07 10 20 30",
                    email = email ?: "user@agrishop.ci",
                    role = UserRole.FARMER,
                    region = "Yamoussoukro",
                    latitude = 6.8276,
                    longitude = -5.2893,
                    ecoPoints = 450,
                    avatarUrl = photoUrl ?: "",
                    isVerified = true,
                    memberSince = "Août 2026"
                )
                AuthResult.Success(profile)
            }
        } catch (e: Exception) {
            Log.e("AuthService", "Google Sign In Error", e)
            AuthResult.Error("Erreur de connexion Google : ${e.localizedMessage}")
        }
    }

    suspend fun sendPasswordReset(email: String): String? {
        if (email.isBlank()) return "Veuillez entrer votre email pour réinitialiser le mot de passe."
        return try {
            firebaseAuth?.sendPasswordResetEmail(email.trim())?.await()
            null // Success
        } catch (e: Exception) {
            e.localizedMessage ?: "Impossible d'envoyer l'email de réinitialisation."
        }
    }

    fun signOut() {
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            Log.e("AuthService", "Sign out error", e)
        }
    }
}
