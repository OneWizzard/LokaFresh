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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var scannerLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var loadingDialog: LoadingDialog

    private val options = GmsDocumentScannerOptions.Builder()
        .setGalleryImportAllowed(false)
        .setPageLimit(5)
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
                        val imageUri = page.imageUri
                        Log.d("DocumentScanner", "Scanned Image URI: $imageUri")
                        try {
                            val inputStream = requireContext().contentResolver.openInputStream(imageUri)
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            if (bitmap != null) pageBitmaps.add(bitmap)
                            inputStream?.close()
                        } catch (e: Exception) {
                            Log.e("DocumentScanner", "Error loading scanned image: ${e.message}")
                        }
                    }

                    sendScanToBackend(pageBitmaps)
                }
            } else {
                Log.d("DocumentScanner", "Scanning cancelled or failed")
                loadingDialog.dismiss()
                moveToOrderFragment()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        loadingDialog = LoadingDialog(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startDocumentScan()
    }

    private fun startDocumentScan() {
        Log.d("CameraFragment", "startDocumentScan called")
        loadingDialog.show("Memulai pemindaian...")

        scanner.getStartScanIntent(requireActivity())
            .addOnSuccessListener { intentSender ->
                loadingDialog.dismiss()
                val intentSenderRequest = IntentSenderRequest.Builder(intentSender).build()
                scannerLauncher.launch(intentSenderRequest)
            }
            .addOnFailureListener { e ->
                loadingDialog.dismiss()
                Log.e("DocumentScanner", "Error starting scan intent: ${e.message}")
                moveToOrderFragment()
            }
    }

    private fun encodeBitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun sendScanToBackend(pages: List<Bitmap>) {
        loadingDialog.show("Mengirim data hasil scan...")

        val apiService = ApiConfig.getApiService()

        val sharedPreferences = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("fullname", null)

        if (username == null) {
            loadingDialog.dismiss()
            Log.e("DocumentScanner", "Username tidak ditemukan di SharedPreferences")
            return
        }

        val base64Images = pages.map { encodeBitmapToBase64(it) }

        val imageListRequest = ImageListRequest(imgs = base64Images)

        apiService.uploadScan(imageListRequest)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    loadingDialog.dismiss()
                    if (response.isSuccessful) {
                        val responseBody = response.body()?.string()
                        val returFragment = ReturFragment().apply {
                            arguments = Bundle().apply {
                                putString("scan_response", responseBody)
                            }
                        }
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, returFragment)
                            .commit()
                        Log.d("DocumentScanner", "Scan uploaded successfully. Response: $responseBody")
                    } else {
                        Log.e("DocumentScanner", "Failed to upload scan: ${response.errorBody()?.string()}")
                        Log.e("DocumentScanner", "Response Code: ${response.code()}")
                        moveToOrderFragment()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    loadingDialog.dismiss()
                    Log.e("DocumentScanner", "Error uploading scan: ${t.message}", t)
                    moveToOrderFragment()
                }
            })
    }

    private fun moveToReturFragment() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ReturFragment())
            .commit()
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
