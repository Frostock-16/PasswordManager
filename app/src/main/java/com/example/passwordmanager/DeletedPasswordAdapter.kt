package com.example.passwordmanager

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.passwordmanager.modal.PasswordData
import org.w3c.dom.Text

class DeletedPasswordAdapter(private val passwordList: MutableList<PasswordData>, private val dbHelper:DbConnection) :
    RecyclerView.Adapter<DeletedPasswordAdapter.DeletedPasswordViewHolder>() {

    interface OnItemClickListener{
        fun OnItemClick(position: Int)
    }


    var listener:OnItemClickListener? = null
    val passwordData:PasswordData = PasswordData()

    class DeletedPasswordViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val tvSitename: TextView = itemView.findViewById(R.id.sitename_tv)
        val tvUsername: TextView = itemView.findViewById(R.id.username_tv)
        val btnDelete: ImageButton = itemView.findViewById(R.id.delete_btn)
        val btnCopy: ImageButton = itemView.findViewById(R.id.copy_btn)
        val btnRestore: ImageButton = itemView.findViewById(R.id.restore_btn)
    }

    override fun onCreateViewHolder(passwordViewGroup: ViewGroup, viewType: Int): DeletedPasswordViewHolder {
        val itemView = LayoutInflater.from(passwordViewGroup.context).inflate(R.layout.deleteditem_password, passwordViewGroup, false)
        return DeletedPasswordViewHolder(itemView)
    }

    override fun onBindViewHolder(deletedPasswordHolder: DeletedPasswordViewHolder, position: Int) {
        val selectPassword = passwordList[position]
        deletedPasswordHolder.tvSitename.text = selectPassword.sitename
        deletedPasswordHolder.tvUsername.text = selectPassword.username

        // Delete password
        deletedPasswordHolder.btnDelete.setOnClickListener{
            showDeleteConfirm(deletedPasswordHolder.itemView.context, position, selectPassword.id)
        }

        // To get the position of passwords in recycler view
        deletedPasswordHolder.itemView.setOnClickListener{
            listener?.OnItemClick(position)
        }

        // Copy password
        deletedPasswordHolder.btnCopy.setOnClickListener{
            val textCopy = deletedPasswordHolder.tvSitename.text.toString()

            val clipboard = deletedPasswordHolder.itemView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied Text", textCopy)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(deletedPasswordHolder.itemView.context, "Text copied to clipboard!", Toast.LENGTH_SHORT).show()
        }

        // Restore password
        deletedPasswordHolder.btnRestore.setOnClickListener{
            showRestoreConfirm(deletedPasswordHolder.itemView.context, position)
        }
    }

    override fun getItemCount(): Int {
        return passwordList.size
    }

    private fun deletePassword(position:Int, passwordId:Int)
    {
        dbHelper.deletePasswordById_Deleted(passwordId)
        passwordList.removeAt(position)
        notifyItemRemoved(position)
//        notifyItemRangeChanged(position, passwordList.size)
    }

    private fun showDeleteConfirm(context: Context, position:Int, passwordId:Int)
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
            deletePassword(position, passwordId)
            dialog.dismiss()
        }
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun showRestoreConfirm(context:Context, position:Int)
    {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.restorealertbox, null)
        val btnCancel = view.findViewById<Button>(R.id.cancel_btn)
        val btnYes= view.findViewById<Button>(R.id.yes_btn)
        view.findViewById<TextView>(R.id.restoreconfirm_tv)
        builder.setView(view)
        val dialog = builder.create()
        btnYes.setOnClickListener{
            val selectedPassword = passwordList[position]
            val userId = getUserIdFromSession(context)
            dbHelper.insertPasswordData(
                userId,selectedPassword.username, selectedPassword.password, selectedPassword.sitename, selectedPassword.note)
            deletePassword(position, selectedPassword.id)
            dialog.dismiss()
        }
        btnCancel.setOnClickListener{
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