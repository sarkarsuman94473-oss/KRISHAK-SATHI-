package com.example.ui.auth

import android.content.Context
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.local.session.UserSession
import com.example.data.local.session.UserSessionManager
import com.example.data.model.AppLanguage
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.AgriTextPrimary
import com.example.ui.theme.AgriTextSecondary
import com.example.util.findActivity
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "AuthScreen"

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AuthGateScreen(
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Language Selection Chips (adaptive wrap, >= 48dp height touch targets)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    AppLanguage.values().forEach { lang ->
                        val isSel = currentLanguage == lang
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSel) AgriGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                .clickable { onLanguageChange(lang) }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = lang.nativeName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else AgriTextPrimary
                                )
                            }
                        }
                    }
                }

                // App Branding Icon - Official Krishak Sathi Emblem with Royal Gold Ring
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .clip(CircleShape)
                        .background(com.example.ui.theme.RoyalGreenSecondaryBg)
                        .border(2.dp, com.example.ui.theme.RoyalGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_krishak_sathi_logo),
                        contentDescription = "Krishak Sathi Official Logo",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "কৃষক সাথী"
                        AppLanguage.HINDI -> "कृषक साथी"
                        AppLanguage.ENGLISH -> "Krishak Sathi"
                    },
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = com.example.ui.theme.RoyalSoftGold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "Krishak Sathi • কৃষকের নির্ভরযোগ্য সহচর"
                        AppLanguage.HINDI -> "Krishak Sathi • किसान का विश्वसनीय साथी"
                        AppLanguage.ENGLISH -> "Krishak Sathi • Smart Farming Companion"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = AgriTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(22.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, com.example.ui.theme.RoyalBorder),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "কৃষক লগইন / প্রবেশ"
                                AppLanguage.HINDI -> "किसान लॉगिन / प्रवेश"
                                AppLanguage.ENGLISH -> "Farmer Sign-In"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = AgriTextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "একটি ট্যাপেই আপনার ফসলের হিসাব ও সাহায্য শুরু করুন"
                                AppLanguage.HINDI -> "एक टैप में अपनी फसलों का हिसाब व सहायता शुरू करें"
                                AppLanguage.ENGLISH -> "Sign in securely to protect your crops and sales records"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = AgriTextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 12.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        Button(
                            onClick = {
                                isLoading = true
                                errorMessage = null
                                onGoogleSignInClicked(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onAuthSuccess = {
                                        isLoading = false
                                        onAuthSuccess()
                                    },
                                    onAuthError = { err ->
                                        isLoading = false
                                        errorMessage = err
                                    },
                                    onAuthCancelled = {
                                        isLoading = false
                                    },
                                    scope = coroutineScope,
                                    currentLanguage = currentLanguage
                                )
                            },
                            enabled = !isLoading,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .testTag("google_sign_in_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "Google দিয়ে প্রবেশ করুন"
                                            AppLanguage.HINDI -> "Google से प्रवेश करें"
                                            AppLanguage.ENGLISH -> "Sign in with Google"
                                        },
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> " অথবা "
                                    AppLanguage.HINDI -> " अथवा "
                                    AppLanguage.ENGLISH -> " OR "
                                },
                                fontSize = 12.sp,
                                color = AgriTextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Box(modifier = Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Direct Farmer Quick Login (Explicit Local Mode)
                        OutlinedButton(
                            onClick = {
                                val defaultFarmerSession = UserSession(
                                    userId = "farmer_suman_sarkar",
                                    userName = "Suman Sarkar",
                                    phone = "",
                                    role = UserSession.ROLE_FARMER,
                                    authProvider = UserSession.PROVIDER_FARMER_DIRECT
                                )
                                UserSessionManager.saveSession(context, defaultFarmerSession)
                                UserSessionManager.recordPrivacyConsent(context)
                                onAuthSuccess()
                            },
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .testTag("farmer_direct_sign_in_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "🌾 কৃষক হিসেবে সরাসরি প্রবেশ (Quick Login)"
                                    AppLanguage.HINDI -> "🌾 किसान के रूप में सीधा प्रवेश (Quick Login)"
                                    AppLanguage.ENGLISH -> "🌾 Continue as Farmer (Quick Login)"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    onAuthCancelled: () -> Unit = {},
    scope: CoroutineScope,
    currentLanguage: AppLanguage = AppLanguage.ENGLISH
) {
    val clientId = try {
        context.getString(R.string.default_web_client_id).trim()
    } catch (_: Exception) {
        ""
    }
    if (clientId.isBlank()) {
        onAuthError(
            when (currentLanguage) {
                AppLanguage.BENGALI -> "Google সাইন-ইন কনফিগারেশন পাওয়া যায়নি। নিচের Quick Login ব্যবহার করুন বা পরে চেষ্টা করুন।"
                AppLanguage.HINDI -> "Google साइन-इन कॉन्फ़िगरेशन उपलब्ध नहीं है। नीचे Quick Login का उपयोग करें या पुनः प्रयास करें।"
                AppLanguage.ENGLISH -> "Google Sign-In configuration is unavailable. Use Quick Login below or retry."
            }
        )
        return
    }

    val hostActivity = context.findActivity()
    if (hostActivity == null) {
        onAuthError(
            when (currentLanguage) {
                AppLanguage.BENGALI -> "অ্যাক্টিভিটি কনটেক্সট না পাওয়ায় Google লগইন ব্যর্থ হয়েছে।"
                AppLanguage.HINDI -> "एक्टिविटी संदर्भ उपलब्ध न होने से Google लॉगिन विफल हुआ।"
                AppLanguage.ENGLISH -> "Unable to launch Google Sign-In without an active screen context."
            }
        )
        return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(hostActivity, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = Firebase.auth.signInWithCredential(authCredential).await()
                val user = authResult.user
                val uid = user?.uid?.trim().orEmpty()
                if (uid.isBlank()) {
                    onAuthError(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "যাচাইকৃত ব্যবহারকারীর তথ্য পাওয়া যায়নি।"
                            AppLanguage.HINDI -> "सत्यापित उपयोगकर्ता पहचान प्राप्त नहीं हुई।"
                            AppLanguage.ENGLISH -> "Verified Firebase user account was not returned."
                        }
                    )
                    return@launch
                }

                val assignedRole = if (user?.email?.trim()?.equals(UserSessionManager.ADMIN_EMAIL, ignoreCase = true) == true) {
                    UserSession.ROLE_ADMIN
                } else {
                    UserSession.ROLE_FARMER
                }
                UserSessionManager.saveSession(
                    context,
                    UserSession(
                        userId = uid,
                        userName = user?.displayName?.takeIf { it.isNotBlank() } ?: "Farmer",
                        email = user?.email,
                        phone = user?.phoneNumber,
                        role = assignedRole,
                        authProvider = UserSession.PROVIDER_GOOGLE
                    )
                )
                UserSessionManager.recordPrivacyConsent(context)
                onAuthSuccess()
            } else {
                onAuthError(
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "অপ্রত্যাশিত ক্রেডেনশিয়াল ধরন পাওয়া গেছে।"
                        AppLanguage.HINDI -> "अप्रत्याशित क्रेडेंशियल प्रकार प्राप्त हुआ।"
                        AppLanguage.ENGLISH -> "Unexpected credential type returned by Google."
                    }
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG, "Google Sign-In cancelled: ${e.message}", e)
            onAuthCancelled()
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In failed", e)
            onAuthError(
                when (currentLanguage) {
                    AppLanguage.BENGALI -> "Google লগইন সম্পন্ন হয়নি। ইন্টারনেট সংযোগ যাচাই করে আবার চেষ্টা করুন অথবা Quick Login ব্যবহার করুন।"
                    AppLanguage.HINDI -> "Google लॉगिन पूरा नहीं हो सका। इंटरनेट जांचें और पुनः प्रयास करें या Quick Login उपयोग करें।"
                    AppLanguage.ENGLISH -> "Google Sign-In failed (${e.localizedMessage ?: "error"}). Check network or use Quick Login."
                }
            )
        }
    }
}

fun signOutFarmer(
    context: Context,
    credentialManager: CredentialManager,
    onComplete: () -> Unit,
    scope: CoroutineScope
) {
    com.example.data.local.session.UserSessionManager.clearSession(context)
    try {
        Firebase.auth.signOut()
    } catch (e: Exception) {
        Log.w(TAG, "Firebase signOut warning: ${e.message}")
    }
    scope.launch {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e(TAG, "Failed clearing credential state", e)
        } finally {
            onComplete()
        }
    }
}
