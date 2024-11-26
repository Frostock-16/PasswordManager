package com.example.passwordmanager

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.passwordmanager.modal.PasswordData

class DeletedPasswordActivity:AppCompatActivity() {
    private lateinit var dbHelper:DbConnection
    private lateinit var passwordList:MutableList<PasswordData>
    private lateinit var deletedPasswordRecyclerView:RecyclerView
    private lateinit var btnBack:ImageButton
    private lateinit var etSearch:EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.deleted_password)

        dbHelper = DbConnection(this)
        deletedPasswordRecyclerView = findViewById(R.id.deletedpasswordrecyclerView)

        // EditText
        etSearch = findViewById(R.id.search_et)

        // Back button -> To dashboard
        btnBack = findViewById(R.id.back_imgbtn)
        btnBack.setOnClickListener{
            val intent = Intent(this@DeletedPasswordActivity, DashboardActivity::class.java)
            startActivity(intent)
            finish()
        }

        val userId = getUserId()
        if(userId!=-1){
            displayPasswords(userId)
            setUpSearch(userId)
        }else{
            Toast.makeText(this@DeletedPasswordActivity, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun getUserId(): Int {
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        return sharedPref.getInt("user_id", -1)
    }

    private fun displayPasswords(userId:Int) {
        try {
            val conn = dbHelper.readableDatabase
            val query = conn.rawQuery("SELECT * FROM deleted_password_data WHERE user_id = ?", arrayOf(userId.toString()))
            passwordList = mutableListOf()
            if (query.count == 0) {
                return
            }

            while (query.moveToNext()) {
                val id = query.getInt(query.getColumnIndexOrThrow("id"))
                val usn = query.getString(query.getColumnIndexOrThrow("username"))
                val pwd = query.getString(query.getColumnIndexOrThrow("password"))
                val sn = query.getString(query.getColumnIndexOrThrow("sitename"))
                val nt = query.getString(query.getColumnIndexOrThrow("note"))
                Log.d("DB_LOG", "Sitename: $sn, Note: $nt")
                passwordList.add(PasswordData(id, usn, pwd, sn, nt))
            }
            query.close()
            conn.close()

            if (passwordList.isEmpty()) {
                return
            } else {
                for (passwordData in passwordList) {
                    Log.d("PasswordList", "ID: ${passwordData.id}, Username: ${passwordData.username}, Password: ${passwordData.password}, Sitename: ${passwordData.sitename}, Note: ${passwordData.note}")
                }
                setupRecyclerView(passwordList)
            }

        } catch (e: Exception) {
            println(e)
        }
    }

    private fun setupRecyclerView(passwordList: MutableList<PasswordData>) {
        val deletedPasswordAdapter = DeletedPasswordAdapter(passwordList, dbHelper)
        deletedPasswordAdapter.listener = object: DeletedPasswordAdapter.OnItemClickListener{
            override fun OnItemClick(position: Int) {
                val selectedPassword = passwordList[position]
                val intent = Intent(this@DeletedPasswordActivity, AddPassword::class.java)
                intent.putExtra("id", selectedPassword.id)
                intent.putExtra("username", selectedPassword.username)
                intent.putExtra("password", selectedPassword.password)
                intent.putExtra("sitename", selectedPassword.sitename)
                intent.putExtra("note", selectedPassword.note)
                startActivity(intent)
            }
        }
        deletedPasswordRecyclerView.layoutManager = LinearLayoutManager(this)
        deletedPasswordRecyclerView.adapter = deletedPasswordAdapter
    }

    // < Search Bar
    private fun setUpSearch(userId: Int) {
        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // .....
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString().trim()
                searchPassword(searchText, userId)
            }

            override fun afterTextChanged(s: Editable?) {
                // ......
            }
        })
    }

    private fun searchPassword(searchText: String, userId:Int) {
        try {
            val db = dbHelper.readableDatabase

            val query: Cursor = if (searchText.isEmpty()) {
                db.rawQuery("SELECT * FROM deleted_password_data WHERE user_id = ?", arrayOf(userId.toString()))
            } else {
                db.rawQuery("SELECT * from deleted_password_data WHERE user_id = ? AND (username LIKE ? OR sitename LIKE ?)",
                    arrayOf(userId.toString(), "%$searchText%", "%$searchText%"))
            }

            passwordList.clear()
            while (query.moveToNext()) {
                val id = query.getInt(query.getColumnIndexOrThrow("id"))
                val usn = query.getString(query.getColumnIndexOrThrow("username"))
                val pwd = query.getString(query.getColumnIndexOrThrow("password"))
                val sn = query.getString(query.getColumnIndexOrThrow("sitename"))
                val nt = query.getString(query.getColumnIndexOrThrow("note"))
                passwordList.add(PasswordData(id, usn, pwd, sn, nt))
            }
            query.close()
            db.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (passwordList.isEmpty()) {
            return
        } else {
            setupRecyclerView(passwordList)
        }
    }
    // Search Bar >
}