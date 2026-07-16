package com.sou56.vikunjabubble

data class ChatItem(val text: String, val isUser: Boolean)

object ChatMessage {
    private val history   = mutableListOf<ChatItem>()
    private val listeners = mutableListOf<() -> Unit>()

    fun add(msg: ChatItem) {
        history.add(msg)
        listeners.forEach { it() }
    }

    fun getHistory(): List<ChatItem> = history.toList()

    fun addListener(listener: () -> Unit)    { listeners.add(listener) }
    fun removeListener(listener: () -> Unit) { listeners.remove(listener) }
}
