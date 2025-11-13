package com.example.edgeviewer

object NativeBridge {
    // native library loaded in MainActivity
    external fun processFrame(argbBytes: ByteArray, width: Int, height: Int): ByteArray
}
