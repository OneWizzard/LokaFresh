package com.example.lokafresh

import android.app.Activity.RESULT_OK
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.lokafresh.databinding.FragmentCameraBinding
import com.example.lokafresh.retrofit.ApiConfig
import com.example.lokafresh.retrofit.ImageListRequest
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var scannerLauncher: ActivityResultLauncher<IntentSenderRequest>
    private val options = GmsDocumentScannerOptions.Builder()
        .setGalleryImportAllowed(false)
        .setPageLimit(5)  // Ambil hingga 5 halaman jika diperlukan
        .setResultFormats(
            GmsDocumentScannerOptions.RESULT_FORMAT_JPEG,
            GmsDocumentScannerOptions.RESULT_FORMAT_PDF
        )
        .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
        .build()

    private val scanner by lazy { GmsDocumentScanning.getClient(options) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scannerLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
                scanResult?.getPages()?.let { pages ->
                    val pageBitmaps = mutableListOf<Bitmap>()
                    for (page in pages) {
                        val imageUri = page.getImageUri()
                        Log.d("DocumentScanner", "Scanned Image URI: $imageUri")
                        try {
                            val inputStream = requireContext().contentResolver.openInputStream(imageUri)
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            pageBitmaps.add(bitmap)
                            inputStream?.close()
                        } catch (e: Exception) {
                            Log.e("DocumentScanner", "Error loading scanned image: ${e.message}")
                        }
                    }

                    // Kirim gambar-gambar yang dipindai ke backend
                    sendScanToBackend(pageBitmaps)

                    // Setelah selesai scanning, balik ke OrderFragment
                    moveToOrderFragment(null)

                }
            } else {
                Log.d("DocumentScanner", "Scanning cancelled or failed")
                moveToOrderFragment(null) // Kalau gagal/cancel scanning, juga balik ke OrderFragment
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startDocumentScan()
    }

    private fun startDocumentScan() {
        Log.d("CameraFragment", "startDocumentScan called")
        scanner.getStartScanIntent(requireActivity())
            .addOnSuccessListener { intentSender ->
                val intentSenderRequest = IntentSenderRequest.Builder(intentSender).build()
                scannerLauncher.launch(intentSenderRequest)
            }
            .addOnFailureListener { e ->
                Log.e("DocumentScanner", "Error starting scan intent: ${e.message}")
            }
    }

    private fun encodeBitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun sendScanToBackend(pages: List<Bitmap>) {
        val apiService = ApiConfig.getApiService()

        // Ambil username dari SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("fullname", null)

        if (username == null) {
            Log.e("DocumentScanner", "Username tidak ditemukan di SharedPreferences")
            return
        }

        // Mengonversi setiap halaman gambar menjadi Base64 dan mengumpulkannya dalam list
        val base64Images = pages.map { pageBitmap ->
            encodeBitmapToBase64(pageBitmap)
        }

        // Membungkus list base64 dalam request body
        val imageListRequest = ImageListRequest(
            imgs = base64Images // List base64 string gambar
        )

        apiService.uploadScan(imageListRequest)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()?.string()
                        Log.d("DocumentScanner", "Scan uploaded successfully. Response: $response")

                        try {
                            val json = JSONObject(responseBody ?: "")
                            val customerName = json.getJSONObject("customer").getString("name")
                            moveToOrderFragment(customerName)
                        } catch (e: Exception) {
                            Log.e("DocumentScanner", "Failed to parse response JSON: ${e.message}")
                            moveToOrderFragment(null)
                        }
                    } else {
                        Log.e("DocumentScanner", "Failed to upload scan. Code: ${response.code()}")
                        moveToOrderFragment(null)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("DocumentScanner", "Upload failed: ${t.message}")
                    moveToOrderFragment(null)
                }
            })
    }

    private fun moveToOrderFragment(customerName: String?) {
        val fragment = OrderFragment()
        val bundle = Bundle()
        bundle.putString("customer_name", customerName)
        fragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
