import android.app.Activity.RESULT_OK
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.example.lokafresh.databinding.FragmentCameraBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.*

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var scannerLauncher: ActivityResultLauncher<IntentSenderRequest>
    private val options = GmsDocumentScannerOptions.Builder()
        .setGalleryImportAllowed(false)
        .setPageLimit(2)
        .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF)
        .setScannerMode(SCANNER_MODE_FULL)
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
                        // Process the image URI here
                        try {
                            Log.d("DocumentScanner", "Opening input stream for URI: $imageUri")
                            val inputStream = requireContext().contentResolver.openInputStream(imageUri)
                            val bitmap = BitmapFactory.decodeStream(inputStream)

                            Log.d("DocumentScanner", "Bitmap decoding berhasil, mulai encode ke Base64")
                            val base64String = encodeBitmapToBase64(bitmap)

                            Log.d("DocumentScanner", "Encode ke Base64 berhasil, hasilnya (terpotong): ${base64String.take(100)}...")

                            // TODO: Gunakan base64String sesuai kebutuhanmu

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
                    // Process the PDF URI here
                }
            } else {
                Log.d("DocumentScanner", "Scanning cancelled or failed")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
                Log.d("CameraFragment", "Launching scan intent")
                val intentSenderRequest = IntentSenderRequest.Builder(intentSender).build()
                scannerLauncher.launch(intentSenderRequest)
            }
            .addOnFailureListener { e ->
                Log.e("DocumentScanner", "Error starting scan intent: ${e.message}")
            }
    }

    // Function to encode Bitmap to Base64
    private fun encodeBitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
