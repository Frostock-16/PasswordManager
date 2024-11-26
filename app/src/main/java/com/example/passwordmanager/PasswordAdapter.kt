package com.example.passwordmanager

import android.view.LayoutInflater
import android.view.View
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Context
import android.text.format.DateUtils
import android.widget.Toast
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.passwordmanager.modal.PasswordData
import android.os.Handler
import androidx.core.content.ContextCompat

class PasswordAdapter(private val passwordList: MutableList<PasswordData>, private val dbHelper:DbConnection):RecyclerView.Adapter<PasswordAdapter.PasswordViewHolder>() {

    interface OnItemClickListener{
        fun OnItemClick(position: Int)
    }

    var listener:OnItemClickListener? = null

    class PasswordViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val tvSiteNameView:TextView = itemView.findViewById(R.id.tvSiteName)
        val tvUsernameView:TextView = itemView.findViewById(R.id.tvUsername)
        val tvTimestamp:TextView = itemView.findViewById(R.id.timestamp_tv)
        val btnDeleteView: ImageButton = itemView.findViewById(R.id.deleteBtn)
        val btnCopyView:ImageButton = itemView.findViewById(R.id.btnCopy)
        val btnStarred:ImageButton = itemView.findViewById(R.id.starred_btn)
        var isStarredClicked = false
    }

    override fun onCreateViewHolder(passwordViewGroup: ViewGroup, viewType: Int): PasswordViewHolder {
        val itemView = LayoutInflater.from(passwordViewGroup.context).inflate(R.layout.item_password, passwordViewGroup, false)
        return PasswordViewHolder(itemView)
    }

    override fun onBindViewHolder(passwordHolder: PasswordViewHolder, position: Int) {
        val selectPassword = passwordList[position]
        passwordHolder.tvSiteNameView.text = selectPassword.sitename
        passwordHolder.tvUsernameView.text = selectPassword.username


        passwordHolder.btnDeleteView.setOnClickListener{
            showDeleteConfirm(passwordHolder.itemView.context, position, selectPassword.id)
        }

        passwordHolder.itemView.setOnClickListener{
            listener?.OnItemClick(position)
        }

        passwordHolder.btnCopyView.setOnClickListener{
            val textCopy = passwordHolder.tvSiteNameView.text.toString()

            val clipboard = passwordHolder.itemView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied Text", textCopy)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(passwordHolder.itemView.context, "Text copied to clipboard!", Toast.LENGTH_SHORT).show()
        }

        passwordHolder.btnStarred.setOnClickListener{
            if(passwordHolder.isStarredClicked){
                passwordHolder.btnStarred.setColorFilter(
                    ContextCompat.getColor(passwordHolder.itemView.context, R.color.OnClickStar)
                )
                Toast.makeText(passwordHolder.itemView.context, "Starred ${selectPassword.sitename}", Toast.LENGTH_SHORT).show()
            }else{
                passwordHolder.btnStarred.clearColorFilter()
                Toast.makeText(passwordHolder.itemView.context, "Removed ${selectPassword.sitename} from starred", Toast.LENGTH_SHORT).show()
            }

            passwordHolder.isStarredClicked = !passwordHolder.isStarredClicked
        }
    }

    override fun getItemCount(): Int {
        return passwordList.size
    }

    private fun deletePassword(context: Context, position:Int, passwordId:Int)
    {
        val selectedPassword = passwordList[position]
        val userId = getUserIdFromSession(context)
        dbHelper.insertDeletedPasswordData(userId, selectedPassword.username, selectedPassword.password, selectedPassword.sitename, selectedPassword.note)
        dbHelper.deletePasswordById(passwordId)
        passwordList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, passwordList.size)
    }

    fun showDeleteConfirm(context: Context, position:Int, passwordId:Int)
    {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.deletealertbox, null)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val btnYes = view.findViewById<Button>(R.id.btnYes)
        view.findViewById<TextView>(R.id.tvDeleteConfirm)
        builder.setView(view)
        val dialog = builder.create()
        btnYes.setOnClickListener {
            deletePassword(context, position, passwordId)
            dialog.dismiss()
        }
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun getUserIdFromSession(context: Context): Int {
        val sharedPref = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        return sharedPref.getInt("user_id", -1)
    }

}