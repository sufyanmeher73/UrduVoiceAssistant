package com.edusphere.urduassistant

import android.os.Bundle
import android.speech.RecognitionListener

class SimpleRecognitionListener(
    private val onResult: (String) -> Unit,
    private val onError: () -> Unit,
    private val onBegin: () -> Unit,
    private val onEnd: () -> Unit
) : RecognitionListener {
    override fun onReadyForSpeech(params: Bundle?) = onBegin()
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() = onEnd()
    override fun onError(error: Int) = onError()
    override fun onResults(results: Bundle?) {
        val list = results?.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)
        val text = list?.firstOrNull()
        if (text.isNullOrBlank()) onError() else onResult(text)
    }
    override fun onPartialResults(partialResults: Bundle?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}
    override fun onLanguageDetection(results: Bundle) {}
}
