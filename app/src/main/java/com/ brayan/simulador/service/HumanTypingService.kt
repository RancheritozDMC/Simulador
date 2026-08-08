package com.brayan.simulador.service
import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.brayan.simulador.TypingRepository
import com.brayan.simulador.WriterState
class HumanTypingService : AccessibilityService() {
    companion object{ var instance: HumanTypingService?=null }
    private lateinit var engine: TypingEngine
    override fun onServiceConnected(){ super.onServiceConnected(); instance=this; engine=TypingEngine(this); TypingRepository.isServiceConnected.value=true; TypingRepository.state.value=WriterState.ESPERANDO_CAMPO }
    override fun onDestroy(){ super.onDestroy(); instance=null; TypingRepository.isServiceConnected.value=false }
    override fun onAccessibilityEvent(event: AccessibilityEvent?){
        if(event==null) return
        val root = rootInActiveWindow ?: return
        val focused = findFocusedEditText(root) ?: return
        engine.attachNode(focused); focused.recycle()
    }
    private fun findFocusedEditText(root: AccessibilityNodeInfo?): AccessibilityNodeInfo?{
        if(root==null) return null
        if(root.isFocused && root.isEditable) return root
        for(i in 0 until root.childCount){
            val c = root.getChild(i) ?: continue
            val r = findFocusedEditText(c); c.recycle(); if(r!=null) return r
        }
        return null
    }
    override fun onInterrupt(){ engine.pause() }
    fun startTyping()=engine.start()
    fun pauseTyping()=engine.pause()
    fun stopTyping()=engine.stop()
}
