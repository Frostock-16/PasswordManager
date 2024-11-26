package com.example.passwordmanager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.content.ClipboardManager
import android.content.ClipData
import android.text.InputType
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.passwordmanager.modal.PasswordData
import com.google.android.material.navigation.NavigationView
import java.util.UUID

class AddPassword:AppCompatActivity() {
    private lateinit var toolbar:Toolbar
    private lateinit var btnBack: ImageButton
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var etNote: EditText
    private lateinit var btnEdit:Button
    private lateinit var btnSave:Button
    private lateinit var btnDelete:Button
    private lateinit var btnSitenameEdit:ImageButton
    private lateinit var btnCopyUsn:ImageButton
    private lateinit var btnCopyPwd:ImageButton
    private lateinit var btntoggle:ImageButton
    private lateinit var ivWebsiteLogo: ImageView
    private lateinit var tvError:TextView
    private lateinit var tvSitename:TextView
    private lateinit var tvTimestamp:TextView
    private lateinit var dbHelper:DbConnection
    private lateinit var passwordData:PasswordData
    private var isPasswordVisible = false


    private var passwordId: Int? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.addpassword)

        dbHelper = DbConnection(this)

        //EditText
        etUsername = findViewById(R.id.usernameEditText)
        etPassword  = findViewById(R.id.passwordEditText)
        etNote = findViewById(R.id.noteEditText)

        etUsername.isEnabled = false
        etPassword.isEnabled = false
        etNote.isEnabled = false

        //Button
        btnEdit = findViewById(R.id.editBtn)
        btnSave = findViewById(R.id.saveBtn)
        btnDelete = findViewById(R.id.deleteBtn_AP)
        btnSitenameEdit = findViewById(R.id.sitenameEditBtn)
        btnCopyUsn = findViewById(R.id.btnCopyUsername)
        btnCopyPwd = findViewById(R.id.btnCopyPassword)
        btntoggle = findViewById(R.id.btnToggle)
        btntoggle.setImageResource(R.drawable.visibility_off_24px)
        btnSave.visibility = View.INVISIBLE

        //Textview
        tvError = findViewById(R.id.errorTextView)
//        tvTimestamp = findViewById(R.id.timestamp_tv)
        tvError.visibility = View.INVISIBLE
        tvSitename = findViewById(R.id.tvSiteName)

        // Display toolbar
        toolbar = findViewById(R.id.toolBar)
        PasswordUtils.passwordMenuBar(this, toolbar)

        //Back button
        btnBack = findViewById(R.id.imageBackButton)
        btnBack.setOnClickListener{
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()
        }

        // On edit button click
        btnEdit.setOnClickListener{
            etUsername.isEnabled = true
            etPassword.isEnabled = true
            etNote.isEnabled = true
            etUsername.requestFocus()
            btnSave.visibility = View.VISIBLE
        }


        // ImageView for website logo
//        ivWebsiteLogo = findViewById(R.id.websitelogo_iv)
        // On Edit sitename click
        btnSitenameEdit.setOnClickListener {
            showEditPopup(this, passwordData, dbHelper)
        }

        /*tvSitename.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val updatedSiteName = tvSitename.text.toString().trim()
                fetchAndDisplayWebsiteLogo(updatedSiteName)
            }
        }*/
//        fetchAndDisplayWebsiteLogo("google.com")


        // Calling the add password function
        val userId = getUserId()
        btnSave.setOnClickListener{
            if(passwordId!=-1 && userId!=-1) updatePassword()
            else {
                if(userId!=-1)
                {
                    addPassword(userId)
                }
                else{
                    Toast.makeText(this@AddPassword, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        }

        // On click delete button
        btnDelete.setOnClickListener{
            deletePassword()
        }

        // On Username Copy button
        btnCopyUsn.setOnClickListener{
            val copyUsn = etUsername.text.toString()
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied Text", copyUsn)
            clipboard.setPrimaryClip(clip)
        }
        //On Password Copy button
        btnCopyPwd.setOnClickListener{
            val copyPwd = etPassword.text.toString()
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied Text", copyPwd)
            clipboard.setPrimaryClip(clip)
        }
        // On password hide/show button
        btntoggle.setOnClickListener{
            if(isPasswordVisible){
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                btntoggle.setImageResource(R.drawable.visibility_off_24px)
            }else {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                btntoggle.setImageResource(R.drawable.visibility_24px)
            }
            etPassword.setSelection(etPassword.text.length)

            isPasswordVisible = !isPasswordVisible
            etPassword.clearFocus()
            etPassword.requestFocus()

        }

        displayPasswordDataFromIntent()
    }

    fun displayPasswordDataFromIntent()
    {
        // Displaying the added password when clicked on recycler view item
        passwordId = intent.getIntExtra("id", -1)
        val username = intent.getStringExtra("username")
        val password = intent.getStringExtra("password")
        val sitename = intent.getStringExtra("sitename")
        val note = intent.getStringExtra("note")

        passwordData = PasswordData(
            passwordId ?: -1,
            username ?: "",
            password ?: "",
            sitename ?: "",
            note ?: ""
        )

        if(!username.isNullOrEmpty()) etUsername.setText(username)
        if(!password.isNullOrEmpty()) etPassword.setText(password)
        if (!sitename.isNullOrEmpty()) tvSitename.setText(sitename)
        if(!note.isNullOrEmpty()) etNote.setText(note)

        if(passwordId != -1) btnSave.text = "Update"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.password_menu, menu)
        return true
    }

    private fun getUserId(): Int {
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        return sharedPref.getInt("user_id", -1)
    }

    // Function to add the password and display it in the recycler view (itempassword.xml)
    private fun addPassword(userId:Int)
    {
        val usn = etUsername.text.toString()
        val pwd = etPassword.text.toString()
        val note = etNote.text.toString()
        val sitename = tvSitename.text.toString()


        if(usn.isEmpty() || pwd.isEmpty()){
            tvError.text = "Username or password cannot be empty!"
            tvError.visibility = View.VISIBLE
        }

        try {
            val result = dbHelper.insertPasswordData(userId, usn, pwd, sitename, note)

            if (result != -1L) {
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                tvError.text = "An error occurred. Please try again."
                tvError.visibility = View.VISIBLE
            }

        } catch (e: Exception) {
            e.printStackTrace()
            tvError.text = "An error occurred. Please try again."
            tvError.visibility = View.VISIBLE
        }
    }

    // Function to update the password
    private fun updatePassword()
    {
        val usn = etUsername.text.toString()
        val pwd = etPassword.text.toString()
        val sitename = tvSitename.text.toString()
        val note = etNote.text.toString()

        if(usn.isEmpty() || pwd.isEmpty()){
            tvError.text = "Username or password cannot be empty!"
            tvError.visibility = View.VISIBLE
        }

        try {
            val result = dbHelper.updatePasswordData(passwordId!!, usn, pwd, sitename, note)

            if (result > 0) {
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                tvError.text = "An error occurred. Please try again."
                tvError.visibility = View.VISIBLE
            }

        } catch (e: Exception) {
            e.printStackTrace()
            tvError.text = "An error occurred. Please try again."
            tvError.visibility = View.VISIBLE
        }
    }

    // Function to delete password
    private fun deletePassword()
    {
        val usn = etUsername.text.toString()
        val pwd = etPassword.text.toString()
        val sitename = tvSitename.text.toString()
        val note = etNote.text.toString()

        try{
            val result = dbHelper.deletePasswordData(passwordId!!, usn, pwd, sitename, note)
            if(result>0){
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
                finish()
            }
            else{
                tvError.text = "An error occurred. Please try again."
                tvError.visibility = View.VISIBLE
            }
        }catch(e:Exception){
            e.printStackTrace()
        }
    }

    // Function to edit the site name
    private fun showEditPopup(context: Context, passwordData:PasswordData, dbHelper:DbConnection)
    {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.sitenamepopup, null)
        val etSitename = view.findViewById<EditText>(R.id.sitenameEt)
        builder.setView(view)
        builder.setPositiveButton("Save"){dialog, _ ->
            val newSitename = etSitename.text.toString().trim()
            if(newSitename.isNotEmpty()){
                dbHelper.updatePasswordData(
                    passwordData.id,
                    username = passwordData.username,
                    password = passwordData.password,
                    sitename = newSitename,
                    note = passwordData.note
                )
                tvSitename.text = newSitename
                dialog.dismiss()
            }
            else{
                etSitename.error = "Site name cannot be empty!"
            }
        }

        builder.setNegativeButton("Cancel"){dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }


    /*// WHY THE FUCK IS THIS NOT WORKING
    fun fetchAndDisplayWebsiteLogo(siteName: String) {
        if (siteName.isBlank()) {
            Log.e("LogoFetch", "Site name is empty, skipping logo fetch.")
            return
        }

        // Sanitize the input
        val sanitizedSiteName = sanitizeSiteName(siteName)

        // Log the sanitized domain for debugging
        Log.d("LogoFetch", "Fetching logo for: $sanitizedSiteName")

        // Fetch and display the logo using the sanitized site name
        val logoUrl = "https://www.google.com/s2/favicons?domain=$sanitizedSiteName"
        Glide.with(this)
            .load(logoUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache the logo for performance
            .error(R.drawable.placeholder_logo) // Placeholder image in case of an error
            .into(ivWebsiteLogo)
    }

    fun sanitizeSiteName(siteName: String): String {
        var domain = siteName.trim()

        // Remove "http://" or "https://" if present
        if (domain.startsWith("http://")) {
            domain = domain.removePrefix("http://")
        } else if (domain.startsWith("https://")) {
            domain = domain.removePrefix("https://")
        }

        // Ensure no trailing slash
        if (domain.endsWith("/")) {
            domain = domain.removeSuffix("/")
        }

        return domain
    }*/


}