package com.example.edgeviewer

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.view.TextureView
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.*
import java.nio.ByteBuffer
import kotlin.system.measureNanoTime

class MainActivity : AppCompatActivity() {
    private lateinit var textureView: TextureView
    private lateinit var glSurface: GLSurfaceView
    private lateinit var renderer: EdgeRenderer
    private lateinit var btnToggle: Button
    private lateinit var tvFps: TextView

    private var showEdge = true
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
    private var running = false

    private val TAG = "MainActivity"

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startCameraPreview()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Load native library
        System.loadLibrary("native-lib")
        setContentView(R.layout.activity_main)

        textureView = findViewById(R.id.previewTexture)
        glSurface = findViewById(R.id.glSurface)
        btnToggle = findViewById(R.id.btnToggle)
        tvFps = findViewById(R.id.tvFps)

        // Setup GLSurfaceView + renderer
        glSurface.setEGLContextClientVersion(2)
        renderer = EdgeRenderer(this)
        glSurface.setRenderer(renderer)
        glSurface.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

        btnToggle.setOnClickListener {
            showEdge = !showEdge
            renderer.setShowEdge(showEdge)
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            startCameraPreview()
        }
    }

    private fun startCameraPreview() {
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {}
            override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {}
            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean { stopProcessing(); return true }
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                // Start camera using helper that opens camera and streams to the SurfaceTexture.
                Camera2Helper.openCamera(this@MainActivity, surface, width, height)
                startProcessingLoop()
            }
        }
    }

    private fun startProcessingLoop() {
        if (running) return
        running = true
        coroutineScope.launch(Dispatchers.Default) {
            var frames = 0
            var lastTime = System.nanoTime()
            while (running) {
                if (!textureView.isAvailable) { delay(30); continue }
                // capture bitmap from TextureView
                val bmp = textureView.bitmap ?: continue
                val w = bmp.width
                val h = bmp.height

                // convert bitmap ARGB -> byte[]
                val buf = ByteBuffer.allocate(w * h * 4)
                bmp.copyPixelsToBuffer(buf)
                val arr = buf.array()

                // process native (synchronous) and measure time
                var processedBytes: ByteArray
                val t = measureNanoTime {
                    processedBytes = NativeBridge.processFrame(arr, w, h)
                }
                val procMs = t / 1_000_000.0

                // convert back to Bitmap
                val outBmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                outBmp.copyPixelsFromBuffer(ByteBuffer.wrap(processedBytes))

                // give to renderer
                renderer.updateFrame(outBmp)

                frames++
                val now = System.nanoTime()
                if (now - lastTime >= 1_000_000_000L) {
                    val fps = frames
                    withContext(Dispatchers.Main) {
                        tvFps.text = "FPS: $fps  (proc ${"%.1f".format(procMs)} ms)"
                    }
                    frames = 0
                    lastTime = now
                }
                // small delay to avoid overloading CPU; tune for target FPS
                delay(30)
            }
        }
    }

    private fun stopProcessing() {
        running = false
        Camera2Helper.closeCamera()
        coroutineScope.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopProcessing()
    }
}
