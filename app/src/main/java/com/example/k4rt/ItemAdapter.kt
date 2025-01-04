    package com.example.k4rt

    import android.content.Context
    import android.graphics.Paint
    import android.text.SpannableString
    import android.text.style.ForegroundColorSpan
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.CheckBox
    import android.widget.ImageView
    import android.widget.PopupMenu
    import android.widget.TextView
    import androidx.core.content.ContextCompat
    import androidx.recyclerview.widget.RecyclerView
    import java.text.DecimalFormat
    import java.text.DecimalFormatSymbols
    import java.util.Locale

    class ItemAdapter(
        private val itemList: MutableList<Item>,
        private val context: Context,
        private val listener: OnItemActionListener
    ) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

        interface OnItemActionListener {
            fun onEdit(item: Item, position: Int)
            fun onDelete(position: Int)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_list, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = itemList[position]
            val formattedPrice = formatCurrency(item.price)

            holder.tvName.text = "Item: ${item.name}"
            holder.tvQuantity.text = "Jumlah: ${item.quantity}"
            holder.tvPrice.text = "Harga: Rp.$formattedPrice"

            val isChecked = holder.cbSelectItem.isChecked
            applyStrikeThrough(holder, isChecked)

            holder.cbSelectItem.setOnCheckedChangeListener { _, isChecked ->
                applyStrikeThrough(holder, isChecked)
                holder.itemView.setBackgroundResource(
                    if (isChecked) R.drawable.selected_background else R.drawable.rounded_background
                )
            }

            holder.btnMenu.setOnClickListener {
                val popupMenu = PopupMenu(context, holder.btnMenu, 0, 0, R.style.PopupMenuStyle)
                popupMenu.menuInflater.inflate(R.menu.item_menu, popupMenu.menu)

                for (i in 0 until popupMenu.menu.size()) {
                    val menuItem = popupMenu.menu.getItem(i)
                    val spannableTitle = SpannableString(menuItem.title)
                    spannableTitle.setSpan(
                        ForegroundColorSpan(ContextCompat.getColor(context, R.color.white)),
                        0,
                        spannableTitle.length,
                        0
                    )
                    menuItem.title = spannableTitle
                }

                popupMenu.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_edit -> {
                            listener.onEdit(item, position)
                            true
                        }
                        R.id.action_delete -> {
                            listener.onDelete(position)
                            true
                        }
                        else -> false
                    }
                }
                popupMenu.show()
            }
        }

        override fun getItemCount(): Int {
            return itemList.size
        }

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var tvName: TextView = itemView.findViewById(R.id.tvName)
            var tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
            var tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
            var btnMenu: ImageView = itemView.findViewById(R.id.btnMenu)
            var cbSelectItem: CheckBox = itemView.findViewById(R.id.cbSelectItem)
        }

        private fun formatCurrency(price: Double): String {
            val symbols = DecimalFormatSymbols(Locale.getDefault()).apply {
                groupingSeparator = '.'
                decimalSeparator = ','
            }
            val decimalFormat = DecimalFormat("#,###", symbols)
            return decimalFormat.format(price)
        }

        private fun applyStrikeThrough(holder: ViewHolder, isStruckThrough: Boolean) {
            val strikeThroughFlag = if (isStruckThrough) Paint.STRIKE_THRU_TEXT_FLAG else 0
            val textColor = if (isStruckThrough) ContextCompat.getColor(context, R.color.white)
            else ContextCompat.getColor(context, R.color.default_text_color)

            holder.tvName.paintFlags = holder.tvName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.tvName.paintFlags = holder.tvName.paintFlags or strikeThroughFlag
            holder.tvQuantity.paintFlags = holder.tvQuantity.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.tvQuantity.paintFlags = holder.tvQuantity.paintFlags or strikeThroughFlag
            holder.tvPrice.paintFlags = holder.tvPrice.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.tvPrice.paintFlags = holder.tvPrice.paintFlags or strikeThroughFlag

            holder.tvName.setTextColor(textColor)
            holder.tvQuantity.setTextColor(textColor)
            holder.tvPrice.setTextColor(textColor)
        }


    }
