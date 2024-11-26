package com.example.passwordmanager

import android.os.Bundle
import android.widget.TextView
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity

class SignUp : AppCompatActivity() {
    private lateinit var backToLogin: TextView
    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var btnSignIn: Button
    private lateinit var errorView: TextView
    private lateinit var email: EditText

    private val dbConnection by lazy { DbConnection(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signupactivity)

        backToLogin = findViewById(R.id.BackToLoginTextView)
        btnSignIn = findViewById(R.id.signUpButton)
        username = findViewById(R.id.usernameEditText)
        password = findViewById(R.id.passwordEditText)
        confirmPassword = findViewById(R.id.confirmPasswordEditText)
        email = findViewById(R.id.emailEditText)
        errorView = findViewById(R.id.errorTextView)

        backToLogin.setOnClickListener {
            val back = Intent(this, MainActivity::class.java)
            startActivity(back)
            finish()
        }

        btnSignIn.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val usn = username.text.toString().trim()
        val pwd = password.text.toString().trim()
        val confirmPwd = confirmPassword.text.toString().trim()
        val mail = email.text.toString().trim()


        if (usn.isEmpty() || pwd.isEmpty()) {
            errorView.text = "Username and password cannot be empty!"
            errorView.visibility = View.VISIBLE
            return
        }

        if (pwd != confirmPwd) {
            errorView.text = "Passwords do not match!"
            errorView.visibility = View.VISIBLE
            return
        }

        if(mail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(mail).matches())
        {
            errorView.text = "Please enter a valid email address!"
            errorView.visibility = View.VISIBLE
            return
        }

        if (isUserExists(usn)) {
            showError("Username or Email already exists!")
            return
        }


        try {
            val query = "INSERT INTO login (username, password) VALUES (?, ?)"
            dbConnection.insertData(query, arrayOf(usn, pwd))

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()

        } catch (e: Exception) {
            e.printStackTrace()
            showError("An error occurred. Please try again.")
        }
    }

    private fun showError(message: String) {
        errorView.text = message
        errorView.visibility = View.VISIBLE
    }

    private fun isUserExists(username: String): Boolean {
        val query = "SELECT * FROM login WHERE username = ?"
        val conn = dbConnection.readableDatabase
        val cursor = conn.rawQuery(query, arrayOf(username))

        val exists = cursor.count > 0
        cursor.close()
        return exists
    }
}
