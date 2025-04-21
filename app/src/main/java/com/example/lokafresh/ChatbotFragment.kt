package com.example.lokafresh

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

data class Message(val text: String, val isUser: Boolean)

class ChatbotFragment : Fragment() {
    private var navListener: NavigationVisibilityListener? = null
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatInput: EditText
    private lateinit var sendButton: ImageButton
    private val messageList = mutableListOf<Message>()
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chatbot, container, false)
        chatRecyclerView = view.findViewById(R.id.chat_recycler_view)
        chatInput = view.findViewById(R.id.chat_input)
        sendButton = view.findViewById(R.id.send_button)
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is NavigationVisibilityListener) {
            navListener = context
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSendMessage()
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(messageList)
        chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        chatRecyclerView.adapter = chatAdapter
    }

    private fun setupSendMessage() {
        sendButton.setOnClickListener {
            val messageText = chatInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                addMessage(messageText, true) // Tambahkan pesan pengguna
                chatInput.text.clear()

                // Sembunyikan keyboard setelah mengirim pesan
                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(chatInput.windowToken, 0)

                // Di sini Anda bisa menambahkan logika untuk mengirim pesan ke chatbot
                // dan menerima jawabannya, lalu menambahkan jawaban ke RecyclerView
                // Contoh simulasi jawaban chatbot setelah 1 detik:
                view?.postDelayed({
                    addMessage("Ini adalah jawaban dari chatbot untuk pesan: $messageText", false)
                }, 1000)
            }
        }
    }

    private fun addMessage(text: String, isUser: Boolean) {
        messageList.add(Message(text, isUser))
        chatAdapter.notifyItemInserted(messageList.size - 1)
        chatRecyclerView.scrollToPosition(messageList.size - 1) // Scroll ke pesan terbaru
    }

    override fun onResume() {
        super.onResume()
        navListener?.setNavigationVisibility(false)
    }

    override fun onPause() {
        super.onPause()
        navListener?.setNavigationVisibility(true)
    }

    override fun onDetach() {
        navListener = null
        super.onDetach()
    }
}