package com.example.k4rt

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MainActivity : AppCompatActivity() {

    private lateinit var rvItems: RecyclerView
    private lateinit var tvNoItems: TextView
    private lateinit var itemAdapter: ItemAdapter
    private lateinit var itemList: MutableList<Item>
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.statusBarColor = Color.TRANSPARENT

        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)

        if (!isUserLoggedIn()) {
            navigateToWelcome()
            return
        }

        rvItems = findViewById(R.id.rvItems)
        tvNoItems = findViewById(R.id.tvNoItems)
        val fabAdd: ImageView = findViewById(R.id.fabAdd)
        val ivLogout: ImageView = findViewById(R.id.btnLogout)
        databaseHelper = DatabaseHelper(this)

        itemList = databaseHelper.getAllItems().toMutableList()
        itemAdapter = ItemAdapter(itemList, this, object : ItemAdapter.OnItemActionListener {
            override fun onEdit(item: Item, position: Int) {
                val dialog = AddItemDialog(this@MainActivity, object : AddItemDialog.OnSaveListener {
                    override fun onSave(updatedItem: Item) {
                        val updatedItemWithId = updatedItem.copy(id = item.id)
                        val result = databaseHelper.updateItem(updatedItemWithId)
                        if (result > 0) {
                            itemList[position] = updatedItemWithId
                            itemAdapter.notifyItemChanged(position)
                            Toast.makeText(
                                this@MainActivity,
                                "Item berhasil diperbarui",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "Gagal memperbarui item",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                })
                dialog.setOnShowListener { dialog.prefillData(item) }
                dialog.show()
            }

            override fun onDelete(position: Int) {
                showDeleteConfirmationDialog(position)
            }
        })

        rvItems.layoutManager = LinearLayoutManager(this)
        rvItems.adapter = itemAdapter

        updateEmptyState()

        fabAdd.setOnClickListener {
            val dialog = AddItemDialog(this, object : AddItemDialog.OnSaveListener {
                override fun onSave(item: Item) {
                    val id = databaseHelper.addItem(item)
                    if (id != -1L) {
                        val newItem = Item(id.toInt().toString(), item.name, item.quantity, item.price)
                        itemList.add(newItem)
                        itemAdapter.notifyItemInserted(itemList.size - 1)
                        updateEmptyState()
                        Toast.makeText(
                            this@MainActivity,
                            "Item berhasil ditambahkan",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Gagal menambahkan item",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })

            dialog.setOnShowListener {
                val btnCancel = dialog.findViewById<TextView>(R.id.btnCancel)
                btnCancel.setOnClickListener {
                    dialog.dismiss()
                }
            }

            dialog.show()
        }

        ivLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun updateEmptyState() {
        if (itemList.isEmpty()) {
            tvNoItems.visibility = View.VISIBLE
            rvItems.visibility = View.GONE
        } else {
            tvNoItems.visibility = View.GONE
            rvItems.visibility = View.VISIBLE
        }
    }

    private fun showDeleteConfirmationDialog(position: Int) {
        if (position in itemList.indices) {
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.dialog_delete_confirmation)
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog.window?.setBackgroundDrawable(null)

            val btnCancel = dialog.findViewById<TextView>(R.id.btnCancel)
            val btnDelete = dialog.findViewById<TextView>(R.id.btnDelete)

            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            btnDelete.setOnClickListener {
                val id = itemList[position].id
                val result = databaseHelper.deleteItem(id)
                if (result > 0) {
                    itemList.removeAt(position)
                    itemAdapter.notifyItemRemoved(position)
                    itemAdapter.notifyItemRangeChanged(position, itemList.size - position)
                    updateEmptyState()
                    Toast.makeText(this, "Item berhasil dihapus", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Gagal menghapus item", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }

            dialog.show()
        } else {
            Toast.makeText(this, "Posisi tidak valid", Toast.LENGTH_SHORT).show()
        }
    }


    private fun showLogoutConfirmationDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_logout_confirmation)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(null)

        val btnCancel = dialog.findViewById<TextView>(R.id.btnCancel)
        val btnLogout = dialog.findViewById<TextView>(R.id.btnLogout)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnLogout.setOnClickListener {
            logoutUser()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("isLoggedIn", false)
    }

    private fun logoutUser() {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", false)
        editor.apply()
        Toast.makeText(this, "Anda Telah Logout", Toast.LENGTH_SHORT).show()
        navigateToWelcome()
    }

    private fun navigateToWelcome() {
        val intent = Intent(this, WelcomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}