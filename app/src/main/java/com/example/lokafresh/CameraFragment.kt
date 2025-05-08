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
import com.example.lokafresh.retrofit.Base64Data
import com.example.lokafresh.retrofit.ImageListRequest
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
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
        .setPageLimit(2)
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
                    for (page in pages) {
                        val imageUri = page.getImageUri()
                        Log.d("DocumentScanner", "Scanned Image URI: $imageUri")
                        try {
                            val inputStream = requireContext().contentResolver.openInputStream(imageUri)
                            val bitmap = BitmapFactory.decodeStream(inputStream)

                            val base64String = encodeBitmapToBase64(bitmap)

                            Log.d("DocumentScanner", "Encoded Base64: ${base64String.take(100)}...")

                            sendScanToBackend(base64String)

                            inputStream?.close()
                        } catch (e: Exception) {
                            Log.e("DocumentScanner", "Error loading scanned image: ${e.message}")
                        }
                    }
                }

                scanResult?.getPdf()?.let { pdf ->
                    val pdfUri = pdf.getUri()
                    val pageCount = pdf.getPageCount()
                    Log.d("DocumentScanner", "Scanned PDF URI: $pdfUri, Page Count: $pageCount")
                }

                // ✅ Setelah selesai scanning, balik ke OrderFragment
                moveToOrderFragment()

            } else {
                Log.d("DocumentScanner", "Scanning cancelled or failed")

                // ✅ Kalau gagal/cancel scanning, juga balik ke OrderFragment
                moveToOrderFragment()
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

    private fun sendScanToBackend(base64String: String) {
        val apiService = ApiConfig.getApiService()

        // Ambil username dari SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("fullname", "Pengguna")

        if (username == null) {
            Log.e("DocumentScanner", "Username tidak ditemukan di SharedPreferences")
            return
        }

        val imageListRequest = ImageListRequest(
            imgs = listOf(base64String) // bungkus base64String jadi list
        )

        apiService.uploadScan(imageListRequest)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Log.d("DocumentScanner", "Scan uploaded successfully.")
                    } else {
                        Log.e("DocumentScanner", "Failed to upload scan: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("DocumentScanner", "Error uploading scan: ${t.message}")
                }
            })

    }

    private fun moveToOrderFragment() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, OrderFragment()) // Ganti fragment_container sesuai ID container kamu
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
