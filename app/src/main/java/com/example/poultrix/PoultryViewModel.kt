package com.example.poultrix

import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

data class Hen(
    val id: String = "",
    val hardwareId: String? = null,
    val ageDays: Int = 0,
    val currentTemp: Double = 0.0,
    val currentActivity: String = "Unknown",
    val isConnected: Boolean = false
)

data class PoultryState(
    val hens: List<Hen> = emptyList(),
    val ambientTemp: Double = 0.0,
    val humidity: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val temperatureHistory: List<Double> = emptyList()
)

class PoultryViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance().reference
    private val _uiState = MutableStateFlow(PoultryState())
    val uiState: StateFlow<PoultryState> = _uiState.asStateFlow()

    init {
        listenToFirebaseData()
    }

    private fun listenToFirebaseData() {
        _uiState.update { it.copy(isLoading = true) }
        
        // Listen for Environmental Data (Global)
        database.child("environment").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val temp = snapshot.child("temperature").getValue(Double::class.java) ?: 0.0
                val hum = snapshot.child("humidity").getValue(Int::class.java) ?: 0
                
                _uiState.update { state ->
                    val newHistory = (state.temperatureHistory + temp).takeLast(20)
                    state.copy(
                        ambientTemp = temp,
                        humidity = hum,
                        temperatureHistory = newHistory,
                        isLoading = false
                    )
                }
            }
            override fun onCancelled(error: DatabaseError) {
                _uiState.update { it.copy(error = error.message, isLoading = false) }
            }
        })

        // Listen for Hens Data
        database.child("hens").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val henList = mutableListOf<Hen>()
                for (henSnapshot in snapshot.children) {
                    val hen = henSnapshot.getValue(Hen::class.java)
                    if (hen != null) {
                        henList.add(hen)
                    }
                }
                _uiState.update { it.copy(hens = henList) }
            }
            override fun onCancelled(error: DatabaseError) {
                _uiState.update { it.copy(error = error.message) }
            }
        })
    }

    fun addHen(hardwareId: String, age: Int) {
        val henId = UUID.randomUUID().toString().take(6).uppercase()
        val newHen = Hen(
            id = henId,
            hardwareId = hardwareId,
            ageDays = age,
            isConnected = false 
        )
        database.child("hens").child(henId).setValue(newHen)
    }

    fun refreshData() {
        listenToFirebaseData()
    }
}
