package com.edusphere.urduassistant

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * UI shell only. All command understanding lives in the parser classes
 * (MultiActionCommandEngine / *CommandParser) and all side effects live
 * in the router classes (*Router, CommonAppActions), coordinated by
 * CommandExecutionCoordinator. MainActivity never talks to
 * ContactResolver or builds contact/call intents directly - that keeps
 * this file small and keeps a single source of truth for routing
 * behaviour and for contact-match safety checks.
 */
class MainActivity : Activity(), TextToSpeech.OnInitListener {

    private companion object {
        const val REQ_RECORD_AUDIO = 10
    }

    private lateinit var speech: SpeechRecognizer
    private lateinit var tts: TextToSpeech
    private lateinit var status: TextView
    private lateinit var command: TextView
    private var isListening = false

    /** Holds the last recognized command so it can be retried automatically
     *  once the user grants a permission we had to request mid-command. */
    private var pendingCommand: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        status = findViewById(R.id.status)
        command = findViewById(R.id.command)
        val mic = findViewById<Button>(R.id.micButton)

        tts = TextToSpeech(this, this)
        speech = SpeechRecognizer.createSpeechRecognizer(this)

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), REQ_RECORD_AUDIO)
        }

        mic.setOnClickListener { listen() }
    }

    private fun listen() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), REQ_RECORD_AUDIO)
            speak("مائیک کی اجازت درکار ہے۔ اجازت دینے کے بعد دوبارہ بولیں۔")
            return
        }
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            speak("اس فون پر آواز کی شناخت دستیاب نہیں ہے۔")
            return
        }
        if (isListening) return

        isListening = true
        status.text = "سن رہا ہوں…"
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ur-PK")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ur-PK")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "بولیں")
        }
        speech.setRecognitionListener(SimpleRecognitionListener(
            onResult = { text ->
                isListening = false
                command.text = text
                execute(text.trim())
            },
            onError = {
                isListening = false
                status.text = "آواز سمجھ نہیں آئی"
                speak("معاف کیجیے، دوبارہ بولیں۔")
            },
            onBegin = { status.text = "سن رہا ہوں…" },
            onEnd = {
                isListening = false
                if (status.text == "سن رہا ہوں…") status.text = "کمانڈ مکمل کریں"
            }
        ))
        runCatching { speech.startListening(intent) }
            .onFailure {
                isListening = false
                status.text = "سننا شروع نہیں ہو سکا"
                speak("مائیک شروع نہیں ہو سکا۔ دوبارہ کوشش کریں۔")
            }
    }

    /**
     * Single entry point for a recognized (or retried) command.
     * Order: multi-action plan -> single-action coordinator -> loose
     * built-in fallbacks (time/settings/camera/files/search/app-launch)
     * for phrasing the parsers don't cover yet.
     */
    private fun execute(raw: String) {
        val text = normalize(raw)
        pendingCommand = text

        if (handleMultiActionCommand(text)) {
            onCommandFinished()
            return
        }
        if (isCoordinatorRoutedCommand(text)) {
            val handled = handleRoutedCommand(text)
            onCommandFinished()
            if (handled) pendingCommand = null
            return
        }
        if (handleFallback(text)) {
            onCommandFinished()
            pendingCommand = null
            return
        }

        speak("یہ کمانڈ
