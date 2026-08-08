package com.brayan.simulador
import kotlinx.coroutines.flow.MutableStateFlow
enum class WriterState { OFF, LISTO, ESCRIBIENDO, PAUSADO, FINALIZADO, ESPERANDO_CAMPO }
object TypingRepository {
    val textToType = MutableStateFlow("Hola, ¿cómo estás?")
    val speedMs = MutableStateFlow(120L)
    val state = MutableStateFlow(WriterState.OFF)
    val currentIndex = MutableStateFlow(0)
    val isServiceConnected = MutableStateFlow(false)
    val hasValidField = MutableStateFlow(false)
    var sessionId = 0L
    fun reset(){ currentIndex.value=0; sessionId++ }
}
