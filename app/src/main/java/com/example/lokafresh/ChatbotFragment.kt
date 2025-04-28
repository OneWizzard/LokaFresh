package com.example.lokafresh

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lokafresh.databinding.FragmentChatbotBinding
import com.example.lokafresh.response.ChatbotRequest
import com.example.lokafresh.response.ChatbotResponse
import com.example.lokafresh.response.Message
import com.example.lokafresh.retrofit.ApiConfig
import com.example.lokafresh.retrofit.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatbotFragment : Fragment() {

    private lateinit var apiService: ApiService
    private var _binding: FragmentChatbotBinding? = null
    private val binding get() = _binding!!
    private lateinit var chatInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private var messageList = mutableListOf<Message>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatbotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views using binding
        val chatInput = binding.chatInput
        val sendButton = binding.sendButton
        val progressBar = binding.backProgressBar
        val chatRecyclerView = binding.chatRecyclerView
        val backButton = binding.backButton

        // Set up RecyclerView and Adapter
        chatAdapter = ChatAdapter(messageList)
        chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        chatRecyclerView.adapter = chatAdapter
        chatRecyclerView.scrollToPosition(messageList.size - 1)

        // Initialize ApiService
        apiService = ApiConfig.getApiService()

        // Set up listeners
        backButton.setOnClickListener {
            navigateToOrderFragment()
        }

        sendButton.setOnClickListener {
            sendMessageToChatbot(chatInput, progressBar, chatRecyclerView)
        }
    }

    private fun navigateToOrderFragment() {
        binding.backProgressBar.visibility = View.VISIBLE
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, OrderFragment())
            .commitNow() // Gunakan commitNow untuk memastikan transaksi selesai sebelum fragment dihancurkan
        binding.backProgressBar.visibility = View.GONE
    }

    private fun sendMessageToChatbot(
        chatInput: EditText,
        progressBar: ProgressBar,
        chatRecyclerView: RecyclerView
    ) {
        val message = chatInput.text.toString().trim()

        Log.d("ChatbotFragment", "Sending message: $message")

        if (message.isNotBlank()) {
            progressBar.visibility = View.VISIBLE

            val userMessage = Message(text = message, isUser = true)
            messageList.add(userMessage)
            chatAdapter.notifyItemInserted(messageList.size - 1)
            chatRecyclerView.smoothScrollToPosition(messageList.size - 1)
            chatInput.text.clear()

            val sharedPreferences = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
            val username = sharedPreferences.getString("fullname", "Pengguna") ?: "Pengguna"

            Log.d("ChatbotFragment", "Username: $username")

            val request = ChatbotRequest(prompt = message, username = username)
            Log.d("ChatbotFragment", "Request body: prompt = ${request.prompt}, username = ${request.username}")

            apiService.getChatbotResponse(request).enqueue(object : Callback<List<ChatbotResponse>> {
                override fun onResponse(call: Call<List<ChatbotResponse>>, response: Response<List<ChatbotResponse>>) {
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        Log.d("ChatbotFragment", "API Response: ${response.body()}")
                        val chatbotResponse = response.body()?.get(0)
                        if (chatbotResponse != null) {
                            val botMessage = Message(text = chatbotResponse.output, isUser = false)
                            messageList.add(botMessage)
                            chatAdapter.notifyItemInserted(messageList.size - 1)
                            chatRecyclerView.smoothScrollToPosition(messageList.size - 1)
                        }
                    } else {
                        Log.e("ChatbotFragment", "Error response: ${response.code()} - ${response.message()}")
                        Toast.makeText(requireContext(), "Failed to get response", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<ChatbotResponse>>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    Log.e("ChatbotFragment", "API Call failed: ${t.message}")
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Log.d("ChatbotFragment", "Message input is empty")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}