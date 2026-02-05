package com.example.englab2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.englab2.ui.theme.EngLab2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewmdoel = MainViewModel()

        enableEdgeToEdge()
        setContent {
            EngLab2Theme {
                val state by viewmdoel.state.collectAsState()
                val AiRes by viewmdoel.aiRes.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    Column(modifier = Modifier.padding(innerPadding)) {
                        state.forEach { device  ->
                            Row() {
                                Text(text = "${device.key}: ${device.value}")
                                Switch(checked = (device.value == "on" || device.value == "open"), onCheckedChange = { viewmdoel.onUpdate(device.key, it) })
                            }
                        }
                        STT(onRes = {viewmdoel.onSpeech(it)})

                        Text(text = AiRes)

                    }
                }
            }
        }
    }
}

@Composable
fun STT(onRes: (String) -> Unit){
    val ctx = LocalContext.current
    val speachRec = SpeechRecognizer.createOnDeviceSpeechRecognizer(ctx)

    var button_text by remember { mutableStateOf("start speach rec") }

    speachRec.setRecognitionListener(myListenre(
        onStart = { button_text = "start talking" },
        onEnd = {button_text = "processing speach" },
        onRes = {
            button_text = "start speach rec"
            onRes(it)
        }
    ))

    val recIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

    val launcherFine = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {isGranted ->
        if (isGranted) {
            speachRec.startListening(recIntent)
            Log.e("Permission", "Location permission granted")
        } else {
            Log.e("Permission", "Location permission denied")
        }
    }

    Button(onClick = {
        if( ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){
            speachRec.startListening(recIntent)
        }else {
            launcherFine.launch(Manifest.permission.RECORD_AUDIO)
        }
    }) {
        Text(text = button_text)
    }
}


class myListenre(val onStart: ()->Unit, val onEnd: ()->Unit, val onRes: (String)->Unit): RecognitionListener{

    override fun onBeginningOfSpeech() {
    }

    override fun onBufferReceived(p0: ByteArray?) {
    }

    override fun onEndOfSpeech() {
        onEnd()
    }

    override fun onError(p0: Int) {
        Log.e("ERROR", p0.toString())
    }

    override fun onEvent(p0: Int, p1: Bundle?) {
    }

    override fun onPartialResults(p0: Bundle?) {
    }

    override fun onReadyForSpeech(p0: Bundle?) {
        onStart()
    }

    override fun onResults(results: Bundle?) {
        val data: ArrayList<String>? = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        Log.d("SpeechRecognizer", "Speech recognition results received: ${data?.get(0)}")
        onRes(data?.get(0) ?: "")
    }

    override fun onRmsChanged(p0: Float) {
    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}
