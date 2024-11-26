package com.example.passwordmanager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor


class MainActivity : AppCompatActivity() {
    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var errorView: TextView
    private lateinit var btn_login: Button
    private lateinit var signUpOption: TextView
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var executor: Executor

    private val dbConnection by lazy { DbConnection(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity)
        btn_login = findViewById(R.id.loginButton)
        signUpOption = findViewById(R.id.signUpTextView)
        btn_login.setOnClickListener{
            login()
        }

        signUpOption.setOnClickListener{
            val open = Intent(this, SignUp::class.java)
            startActivity(open)
            finish()
        }

        setupBiometricAuthentication()
        biometricPrompt.authenticate(promptInfo)
    }

    fun login()
    {
        username = findViewById(R.id.usernameEditText)
        password = findViewById(R.id.passwordEditText)
        errorView = findViewById(R.id.errorTextView)

        val usn = username.text.toString().trim()
        val pwd = password.text.toString().trim()

        if(usn.isEmpty() || pwd.isEmpty()){
            errorView.text = "Invalid username or password!"
            errorView.visibility = View.VISIBLE
            return
        }

        try{
            val db = dbConnection.readableDatabase
            val query = "SELECT user_id FROM login WHERE username = ? AND password = ?"
            val cursor = db.rawQuery(query, arrayOf(usn, pwd))
            if(cursor.moveToFirst()){
                val userId = cursor.getInt(0)
                PasswordUtils.saveUserSession(this, userId)

                navigateToDashboard()
            }
            else{
                errorView.text = "Invalid username or password!"
                errorView.visibility = View.VISIBLE
            }
            cursor.close()
            db.close()
        }catch(e: Exception){
            e.printStackTrace()
        }

    }

//    fun saveUserSession(userId: Int) {
//        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
//        with(sharedPref.edit()) {
//            putInt("user_id", userId)
//            apply()
//        }
//    }

    private fun setupBiometricAuthentication() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
//            BiometricManager.BIOMETRIC_SUCCESS -> {
//                Toast.makeText(this, "Biometric authentication is available.", Toast.LENGTH_SHORT).show()
//            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Toast.makeText(this, "This device doesn't have a biometric sensor.", Toast.LENGTH_SHORT).show()
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Toast.makeText(this, "Biometric sensor is currently unavailable.", Toast.LENGTH_SHORT).show()
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Toast.makeText(this, "No biometric data enrolled. Please set up biometrics in your device's settings.", Toast.LENGTH_SHORT).show()
            }
        }

        // Executor
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(this@MainActivity, "$errString", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Toast.makeText(this@MainActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                navigateToDashboard()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(this@MainActivity, "Login Failed", Toast.LENGTH_SHORT).show()
            }
        })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Password Manager")
            .setSubtitle("Use your fingerprint to continue")
            .setDescription("Can't even remember your passwords huh? LOSER!!")
            .setNegativeButtonText("Cancel")
            .build()
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }

}
