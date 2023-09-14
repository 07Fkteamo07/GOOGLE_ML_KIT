package com.example.codebarml

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.example.codebarml.ui.theme.CodeBarMlTheme
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.io.IOException


class MainActivity : ComponentActivity() {
    private lateinit var img: ImageView
    private val CODES = intArrayOf(50, 100, 200, 250)
    private var FilesPermit = true
    var lista: ArrayList<Array<String>>? = null


    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        img = this.findViewById(R.id.imageView);
        val button = findViewById<Button>(R.id.btnsearchpicture)
        button.setOnClickListener {
            if (FilesPermit) {
                val gallery =
                    Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                gallery.type = "image/*"
                startActivityForResult(gallery, CODES[0])
            } else {
                Toast.makeText(this@MainActivity, "No tienes permiso", Toast.LENGTH_SHORT).show()
            }
        }

        val buttonT = findViewById<Button>(R.id.btntakepicture)
        buttonT.setOnClickListener {
            val cameraPermission = Manifest.permission.CAMERA
            if (ActivityCompat.checkSelfPermission(this, cameraPermission) == PackageManager.PERMISSION_GRANTED
            ) {
                val takePic = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (takePic.resolveActivity(packageManager) != null) {
                    startActivityForResult(takePic, CODES[2])
                } else {
                    showErrorToast("No se pudo abrir la cámara")
                }
            } else {
                showErrorToast("No tienes permiso para acceder a la cámara")
            }
        }


    }

    private fun showErrorToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK || true) {
            //verifica si se ha seleccionado una imagen
            if (requestCode == CODES[0]) {
                showErrorToast("todo bien, codigo: [$requestCode]")
                //obtener imageUri
                val imageUri = data?.data
                if (img != null) {
                    try {
                        img.setImageURI(imageUri)
                        var image: InputImage? = null
                        try {
                            image = InputImage.fromFilePath(this@MainActivity, imageUri!!)
                            identifyQr(image)
                        } catch (e: IOException) {
                            showErrorToast("IOimg: " + e.message)
                        }
                    } catch (ex: Exception) {
                        showErrorToast("ImgSetUri: " + ex.message)
                    }
                } else {
                    showErrorToast("imagen vacia")
                }
            } else if (requestCode == CODES[1]) {
                //obtiene respuesta de la imagen
                showErrorToast("Permiso aceptado")
                FilesPermit = true
            } else if (requestCode == CODES[2]) {
                //obtiene respuesta de la imagen
                val extras = data?.extras
                val imgBitMap = extras!!["data"] as Bitmap?
                if (img != null) {
                    var image: InputImage? = null
                    try {
                        //ubicar imagen en contenedor ImageView
                        img.setImageBitmap(imgBitMap)
                        //obtiene el input imagen a partir de un bitMap
                        image = InputImage.fromBitmap(imgBitMap!!, 0)
                        identifyQr(image)
                    } catch (ex: Exception) {
                        showErrorToast("ImgSetUri: " + ex.message)
                    }



                }
            }
        } else {
            showErrorToast("resultCode: $resultCode =[$requestCode]")
        }
    }

    private fun identifyQr(image: InputImage?) {
        //verifica si se obtuvo el InputImage
        if (image != null) {
            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE, Barcode.FORMAT_AZTEC).build()
            val scanner = BarcodeScanning.getClient();
            val result = scanner.process(image).addOnSuccessListener { barcodes ->

                val lista: ArrayList<Array<String>> = ArrayList()
                for (barcode in barcodes) {
                    val bounds = barcode.boundingBox
                    val corners = barcode.cornerPoints
                    val rawValue = barcode.rawValue
                    showErrorToast("ipipi: ${rawValue}")
                    val textView = findViewById<TextView>(R.id.txtcontenido)
                    textView.setText("Contenido: " + rawValue)
                    val valueType = barcode.valueType
                    when (valueType) {
                        Barcode.TYPE_WIFI -> {
                            val ssid = barcode.wifi!!.ssid
                            val password = barcode.wifi!!.password
                            val type = barcode.wifi!!.encryptionType
                        }
                        Barcode.TYPE_URL -> {
                            val title = barcode.url!!.title
                            val url = barcode.url!!.url
                        }
                    }
                }
            }.addOnFailureListener { }
        } else {
            showErrorToast("Imagen no disponible")
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CodeBarMlTheme {
        Greeting("Android")
    }

}
