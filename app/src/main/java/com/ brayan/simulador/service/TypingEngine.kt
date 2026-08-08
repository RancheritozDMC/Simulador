package com.brayan.simulador.service
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityNodeInfo
import com.brayan.simulador.TypingRepository
import com.brayan.simulador.WriterState
class TypingEngine(private val service: HumanTypingService) {
    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null
    private var targetNode: AccessibilityNodeInfo? = null
    private var sessionId: Long = 0
    fun attachNode(node: AccessibilityNodeInfo){
        targetNode?.recycle()
        targetNode = AccessibilityNodeInfo.obtain(node)
        TypingRepository.hasValidField.value = true
        if(TypingRepository.state.value==WriterState.ESPERANDO_CAMPO) TypingRepository.state.value=WriterState.LISTO
    }
    fun detachNode(){
        targetNode?.recycle(); targetNode=null
        TypingRepository.hasValidField.value=false
        if(TypingRepository.state.value==WriterState.ESCRIBIENDO) pause()
    }
    fun start(){
        if(TypingRepository.textToType.value.isEmpty()) return
        if(targetNode==null){ TypingRepository.state.value=WriterState.ESPERANDO_CAMPO; return }
        sessionId=TypingRepository.sessionId
        TypingRepository.state.value=WriterState.ESCRIBIENDO
        scheduleNext()
    }
    fun pause(){ handler.removeCallbacksAndMessages(null); runnable=null; if(TypingRepository.state.value==WriterState.ESCRIBIENDO) TypingRepository.state.value=WriterState.PAUSADO }
    fun stop(){ handler.removeCallbacksAndMessages(null); runnable=null; TypingRepository.state.value=WriterState.LISTO }
    private fun scheduleNext(){
        val delay = TypingRepository.speedMs.value + (0..60).random()
        runnable = Runnable {
            if(sessionId!=TypingRepository.sessionId) return@Runnable
            val node = targetNode ?: return@Runnable
            if(!node.refresh()){ detachNode(); return@Runnable }
            val full = TypingRepository.textToType.value
            val idx = TypingRepository.currentIndex.value
            if(idx>=full.length){ TypingRepository.state.value=WriterState.FINALIZADO; return@Runnable }
            val next = (idx+1).coerceAtMost(full.length)
            val txt = full.substring(0,next)
            val args = Bundle(); args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, txt)
            if(node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)){
                TypingRepository.currentIndex.value=next
                if(next<full.length) scheduleNext() else TypingRepository.state.value=WriterState.FINALIZADO
            } else TypingRepository.state.value=WriterState.PAUSADO
        }
        handler.postDelayed(runnable!!, delay)
    }
}
