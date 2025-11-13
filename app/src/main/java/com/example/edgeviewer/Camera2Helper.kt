package com.example.edgeviewer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.util.Size
import android.view.Surface
import android.util.Log

object Camera2Helper {
    private var cameraDevice: CameraDevice? = null
    private var session: CameraCaptureSession? = null
    private const val TAG = "Camera2Helper"

    @SuppressLint("MissingPermission")
    fun openCamera(activity: Activity, surfaceTexture: SurfaceTexture, width: Int, height: Int) {
        val manager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val cameraId = manager.cameraIdList.first { id ->
                val char = manager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING)
                char == CameraCharacteristics.LENS_FACING_BACK
            }
            manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    try {
                        surfaceTexture.setDefaultBufferSize(width, height)
                        val surface = Surface(surfaceTexture)
                        val previewRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                        previewRequestBuilder.addTarget(surface)

                        camera.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
                            override fun onConfigured(session: CameraCaptureSession) {
                                this@Camera2Helper.session = session
                                previewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
                                val request = previewRequestBuilder.build()
                                session.setRepeatingRequest(request, null, null)
                            }
                            override fun onConfigureFailed(session: CameraCaptureSession) {}
                        }, null)
                    } catch (e: Exception) { Log.e(TAG, "openCamera error: ${e.localizedMessage}") }
                }
                override fun onDisconnected(camera: CameraDevice) { camera.close(); cameraDevice = null }
                override fun onError(camera: CameraDevice, error: Int) { camera.close(); cameraDevice = null }
            }, null)
        } catch (e: Exception) { Log.e(TAG, "openCamera exception: ${e.localizedMessage}") }
    }

    fun closeCamera() {
        session?.close(); session = null
        cameraDevice?.close(); cameraDevice = null
    }
}
