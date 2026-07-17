package com.sou56.vikunjabubble

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(items: List<ChatItem>) :
    RecyclerView.Adapter<ChatAdapter.VH>() {

    // val → var に変更して updateItems() で差し替え可能にする
    private val items: MutableList<ChatItem> = items.toMutableList()

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.textMessage)
    }

    override fun getItemViewType(position: Int) =
        if (items[position].isUser) 0 else 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val layout = if (viewType == 0) R.layout.item_chat_user else R.layout.item_chat_bot
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.text.text = items[position].text
    }

    override fun getItemCount() = items.size

    /** リストを差し替えて再描画する。adapter自体は再生成しない。 */
    fun updateItems(newItems: List<ChatItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
