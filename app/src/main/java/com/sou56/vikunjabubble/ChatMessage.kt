package com.sou56.vikunjabubble

data class ChatMessage(val text: String, val isUser: Boolean)

object ChatMessage {
    private val history = mutableListOf<ChatMessage>()
    private val listeners = mutableListOf<() -> Unit>()

    fun add(msg: ChatMessage) {
        history.add(msg)
        listeners.forEach { it() }
    }

    fun getHistory(): List<ChatMessage> = history.toList()

    fun addListener(listener: () -> Unit) { listeners.add(listener) }
    fun removeListener(listener: () -> Unit) { listeners.remove(listener) }
}
