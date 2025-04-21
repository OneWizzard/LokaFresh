package com.example.lokafresh

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.coroutines.withContext

@Serializable
data class ChatbotRequest(val prompt: String)
@Serializable
data class ChatbotResponse(val response: String)
@Serializable
data class Message(val text: String, val isUser: Boolean)

class ChatbotFragment : Fragment() {
    private var navListener: NavigationVisibilityListener? = null
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatInput: EditText
    private lateinit var sendButton: ImageButton
    private val messageList = mutableListOf<Message>()
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var backButton: ImageButton
    private lateinit var backProgressBar: ProgressBar

    private val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true }) // Konfigurasi JSON
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chatbot, container, false)
        chatRecyclerView = view.findViewById(R.id.chat_recycler_view)
        chatInput = view.findViewById(R.id.chat_input)
        sendButton = view.findViewById(R.id.send_button)
        backButton = view.findViewById(R.id.back_button)
        backProgressBar = view.findViewById(R.id.back_progress_bar)
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
        setupBackButton()
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
                addMessage(messageText, true)
                chatInput.text.clear()

                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(chatInput.windowToken, 0)

                sendPromptToAPI(messageText)
            }
        }
    }

    private fun sendPromptToAPI(prompt: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val requestData = ChatbotRequest(prompt = prompt)
                val response: ChatbotResponse = httpClient.post(API_URL) {
                    contentType(ContentType.Application.Json)
                    setBody(requestData)
                }.body()

                withContext(Dispatchers.Main) {
                    handleBotResponse(response.response)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    addMessage("Terjadi kesalahan saat menghubungi chatbot.", false)
                    e.printStackTrace()
                }
            }
        }
    }

    private fun handleBotResponse(responseText: String) {
        view?.postDelayed({
            addMessage(responseText, false)
        }, 1000)
    }

    private fun addMessage(text: String, isUser: Boolean) {
        messageList.add(Message(text, isUser))
        chatAdapter.notifyItemInserted(messageList.size - 1)
        chatRecyclerView.scrollToPosition(messageList.size - 1)
    }

    private fun setupBackButton() {
        backButton.setOnClickListener {
            backProgressBar.visibility = View.VISIBLE
            (activity as? MainActivity)?.hideNavigationElements()
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }, 500)
        }
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

    companion object {
        private const val API_URL = "http://34.143.173.201:8502/webhook/receiveprompt"
    }
}