package com.example.passwordmanager

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.passwordmanager.modal.PasswordData
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.example.passwordmanager.modal.User
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView


class DashboardActivity: AppCompatActivity() {
    private lateinit var passwordRecyclerView: RecyclerView
    private lateinit var dbHelper: DbConnection
    private lateinit var toolbar: Toolbar
    private lateinit var passwordList: MutableList<PasswordData>
    private lateinit var etSearch: EditText

    private lateinit var passwordAdapter: PasswordAdapter

    // layout
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarToggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard)
        // EditText
        etSearch = findViewById(R.id.search_et)

        passwordRecyclerView = findViewById(R.id.passwordrecyclerView)

        val btnFabAdd = findViewById<FloatingActionButton>(R.id.fabAddbtn)
        btnFabAdd.setOnClickListener {
            val intent = Intent(this@DashboardActivity, AddPassword::class.java)
            startActivity(intent)
            finish()
        }

        dbHelper = DbConnection(this)

        toolbar = findViewById(R.id.toolBar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val userId = getUserId()
        if(userId!=-1){
            displayPasswords(userId)
            setUpSearch(userId)
        }else{
            Toast.makeText(this@DashboardActivity, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        navBar()
        getUsersFromDatabase()
    }

    private fun logoutUser() {
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear()  // Clear the session
            apply()
        }
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }


    private fun getUserId(): Int {
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        return sharedPref.getInt("user_id", -1)
    }

    private fun displayPasswords(userId:Int) {
        try {
            val conn = dbHelper.readableDatabase
            val query = conn.rawQuery("SELECT * FROM password_data WHERE user_id = ?", arrayOf(userId.toString()))
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
                    Log.d("PasswordList", "ID: ${passwordData.id}, Username: ${passwordData.username}, Password: ${passwordData.password}, Sitename: ${passwordData.sitename}, Note: ${passwordData.note}, Timestamp: ${passwordData.timestamp}")
                }
                setupRecyclerView(passwordList)
            }

        } catch (e: Exception) {
            println(e)
        }
    }

    private fun setupRecyclerView(passwordList: MutableList<PasswordData>) {
        passwordAdapter = PasswordAdapter(passwordList, dbHelper)
        passwordRecyclerView.layoutManager = LinearLayoutManager(this)
        passwordRecyclerView.adapter = passwordAdapter
        passwordAdapter.listener = object : PasswordAdapter.OnItemClickListener {
            override fun OnItemClick(position: Int) {
                val selectedPassword = passwordList[position]
                val intent = Intent(this@DashboardActivity, AddPassword::class.java)
                intent.putExtra("id", selectedPassword.id)
                intent.putExtra("username", selectedPassword.username)
                intent.putExtra("password", selectedPassword.password)
                intent.putExtra("sitename", selectedPassword.sitename)
                intent.putExtra("note", selectedPassword.note)
                startActivity(intent)
            }
        }
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
                db.rawQuery("SELECT * FROM password_data WHERE user_id = ?", arrayOf(userId.toString()))
            } else {
                db.rawQuery("SELECT * from password_data WHERE user_id = ? AND (username LIKE ? OR sitename LIKE ?)",
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

    // < Nav bar
    private fun navBar() {
        drawerLayout = findViewById(R.id.drawerLayout)
        actionBarToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close)
        drawerLayout.addDrawerListener(actionBarToggle)
        actionBarToggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navigationView = findViewById(R.id.navigationView)

        navigationView.setNavigationItemSelectedListener { menuitem ->
            when (menuitem.itemId) {
                R.id.action_logout -> {
                    logoutUser()
                }

                R.id.action_deletedpasswords -> {
                    val intent = Intent(this, DeletedPasswordActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                R.id.action_switch_user ->{
                    showSwitchUserDialog()
                }
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (actionBarToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getUsersFromDatabase(): List<User>
    {
        val userList = mutableListOf<User>()
        val db = dbHelper.readableDatabase
        val query = "SELECT user_id, username FROM login"
        val cursor = db.rawQuery(query, null)

        try {
            if (cursor.moveToFirst()) {
                do {
                    val userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"))
                    val username = cursor.getString(cursor.getColumnIndexOrThrow("username"))

                    val icon = R.drawable.account_circle_24px

                    userList.add(User(userId, username, icon))
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor.close()
            db.close()
        }
        return userList
    }

    private fun showSwitchUserDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_switch_user, null)
        val rvUsers = dialogView.findViewById<RecyclerView>(R.id.rvUsers)

        val userList = getUsersFromDatabase()
        val adapter = SwitchUserAdapter(userList) { selectedUser ->
            switchToUser(selectedUser.id)
//            alertDialog.dismiss()
        }
        rvUsers.layoutManager = LinearLayoutManager(this)
        rvUsers.adapter = adapter

        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.show()

    }



    private fun switchToUser(UserId:Int)
    {
        PasswordUtils.saveUserSession(this, UserId)
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }
}
