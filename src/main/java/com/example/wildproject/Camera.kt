package com.example.wildproject

import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable.Orientation
import android.media.MediaScannerConnection
import android.media.VolumeShaper.Configuration
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.wildproject.LoginActivity.Companion.useremail
import com.example.wildproject.databinding.ActivityCameraBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.ktx.storageMetadata
import java.io.File
import java.io.IOError
import java.io.IOException

import kotlin.math.max
import kotlin.math.abs


import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Camera : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding

    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK //SELECCCIONAR CAMARA TRASERA
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    private lateinit var dataRun: String
    private lateinit var startTimeRun: String

    private lateinit var metadata: StorageMetadata
    private var preview: Preview? = null

    companion object{
        private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)
        private val REQUEST_CODE: Int = 10

        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var bundle = intent.extras

        dataRun = bundle?.getString("dataRun").toString()
        startTimeRun = bundle?.getString("startTimeRun").toString()

        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.cameraCaptureButton.setOnClickListener{
            takePicture()
        }
        binding.cameraSwitchButton.setOnClickListener {
            lensFacing = if(lensFacing == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_FRONT
            bindCamera()
        }

        if(allPermissionGranted())startCamera()
        else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE)
        }
    }

    private fun allPermissionGranted() = REQUIRED_PERMISSIONS.all{
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
    private fun startCamera():Unit{
        val cameraProviderFinally = ProcessCameraProvider.getInstance(this)
        cameraProviderFinally.addListener(Runnable {
                cameraProvider = cameraProviderFinally.get()
            lensFacing = when{
                hasBackCamera()-> CameraSelector.LENS_FACING_BACK
                hasFromCamera()-> CameraSelector.LENS_FACING_FRONT
                else -> throw IllegalStateException("No hay cámaras")
            }
            manageSwitchButton()
            bindCamera()
        }, ContextCompat.getMainExecutor(this))

    }
    private var FILENAME: String = ""
    private fun manageSwitchButton():Unit{
        try {
            binding.cameraSwitchButton.isEnabled = hasBackCamera() && hasFromCamera()
        }catch (e:java.lang.Exception){
            binding.cameraSwitchButton.isEnabled = false
        }
    }
    private fun hasBackCamera():Boolean{
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
    } private fun hasFromCamera():Boolean{
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == REQUEST_CODE){
            if(allPermissionGranted()) startCamera()
            else {
                Toast.makeText(this, "No se otorgaron los permisos necesarios", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }


    private fun bindCamera():Unit{
        val metrics= DisplayMetrics().also {
            binding.viewFinder.display.getRealMetrics(it)
        }
        val screenAspectRatio = aspectRadio(metrics.widthPixels, metrics.heightPixels)
        val rotation = binding.viewFinder.display.rotation

        val cameraProvider = cameraProvider ?: throw java.lang.IllegalStateException("Fallo al iniciar")

        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        preview = Preview.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()

        cameraProvider.unbindAll()

        try {
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            preview?.setSurfaceProvider(binding.viewFinder.surfaceProvider)
        }catch (e: java.lang.Exception){
            Log.e("cameraRunning", "error al iniciar la camera")
        }

    }

    private fun aspectRadio(width: Int, height: Int): Int{
        val previewRatio = max(width, height).toDouble() / kotlin.math.min(width, height)

        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)){
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9


    }
    private fun getOutputDirectory():File{
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, "wildRunning").apply {
                mkdirs()
            }
        }
        return if (mediaDir!=null && mediaDir.exists()) mediaDir else filesDir
    }
    private fun takePicture():Unit{
        FILENAME = getString(R.string.app_name) + useremail + startTimeRun
        FILENAME = FILENAME.replace(":", "")
        FILENAME = FILENAME.replace("/", "")


        if(getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE){
            metadata = storageMetadata{
                contentType = "image/jpg"
                setCustomMetadata("orientation", "horizontal")
                setCustomMetadata("autor", "gerson")
            }
        }else{
            metadata = storageMetadata{
                contentType = "image/jpg"
                setCustomMetadata("orientation", "vertical")
                setCustomMetadata("autor", "gerson")
            }
        }




        val photoFile = File (outputDirectory, FILENAME + ".jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture?.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this ),
            object: ImageCapture.OnImageSavedCallback{
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

                    val savedUri = Uri.fromFile(photoFile)

                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                        setGalleryThumbail(savedUri)
                    }
                    val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(savedUri.toFile().extension)
                    MediaScannerConnection.scanFile(
                        baseContext,
                        arrayOf(
                            savedUri.toFile().absolutePath
                        ),
                        arrayOf(mimeType)
                    ){ s: String, uri: Uri ->

                    }

                    Snackbar.make(binding.root, "Error al tomar al fotografía", Snackbar.LENGTH_SHORT)
                        .setAction("Ok"){
                            binding.clMain.setBackgroundColor(Color.BLUE)
                        }
                        .show()
                    uploadFile(photoFile)
                }

                override fun onError(exception: ImageCaptureException) {
                    Snackbar.make(binding.root, "Error al tomar al fotografía", Snackbar.LENGTH_SHORT)
                        .setAction("Ok"){
                            binding.clMain.setBackgroundColor(Color.RED)
                        }
                        .show()
                }

            }
        )
    }
    private var lastImage = ""
    private fun uploadFile(file: File):Unit{
        var dirname = dataRun+startTimeRun
        dirname= dirname.replace(":", "")
        dirname= dirname.replace("/", "")
        var fileName = dirname+"-"+(0..100).random()

        var storageReferences = FirebaseStorage.getInstance().getReference("images/$useremail/$dirname/$fileName")
        storageReferences.putFile(Uri.fromFile(file))
            .addOnCompleteListener {
                lastImage= "images/$useremail/$dirname/$fileName"
                val myFile = File(file.absolutePath)



                val metaRef = FirebaseStorage.getInstance().getReference("images/$useremail/$dirname/$fileName")
                metaRef.updateMetadata(metadata)




                Toast.makeText(this, "Se a subido y eliminado en el" +
                        "dispostivo correctamente", Toast.LENGTH_SHORT).show()

                myFile.delete()
            }.addOnFailureListener{
                throw (IOException("Ocurrío un error al subir el archivo $it"))
            }
    }
    private fun setGalleryThumbail(uri: Uri):Unit{
        var thumbail = binding.photoViewButton
        thumbail.post {
            Glide.with(thumbail)
                .load(uri)
                .apply(RequestOptions.circleCropTransform())
                .into(thumbail)
        }
    }
}