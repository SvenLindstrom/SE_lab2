package com.example.englab2

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class MainViewModel(): ViewModel() {

    private val valueHandler = onChange()
    val state: StateFlow<Map<String,String>> = valueHandler._state
    lateinit var ref_devices: DatabaseReference
    lateinit var ref_TTS: DatabaseReference
    val aiRes = MutableStateFlow("")


    val model = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel("gemini-2.5-flash")


    init {
        viewModelScope.launch {
            ref_devices = Firebase.database.reference.child("devices")
            ref_TTS = Firebase.database.reference.child("TTS")

            ref_devices.addValueEventListener(valueHandler)
        }
    }

    fun onUpdate(key: String, current: Boolean){
        val value = if (key == "light"){
            if (current) {
                "on"
            } else{
                "off"
            }
        } else {
            if (current) {
                "open"
            } else{
                "closed"
            }
        }
        ref_devices.child(key).setValue(value)
    }

    fun onSpeech(speech: String){
        if (speech != ""){
            viewModelScope.launch {
                ref_TTS.push().setValue(speech)
                askAi(speech)
            }
        }
    }

    suspend fun askAi(speech: String){
        val prompt = "Pleas give a one line respons the the following text:\n"
        val response = model.generateContent(prompt+speech)
        print(response.text)
        aiRes.value = response.text?: ""
    }

    class onChange(): ValueEventListener{
        val _state = MutableStateFlow(mapOf<String, String>())

        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.value is Map<*, *>){

                _state.value = snapshot.value as Map<String, String>
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("AAAAA", error.toString())
        }
    }
}