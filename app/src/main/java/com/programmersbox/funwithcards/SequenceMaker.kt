package com.programmersbox.funwithcards

import kotlinx.coroutines.*

open class SequenceMaker<T>(private val sequence: List<T>, private val sequenceAchieved: () -> Unit) {
    constructor(vararg sequence: T, sequenceAchieved: () -> Unit) : this(sequence.toList(), sequenceAchieved)

    protected var sequenceFailed: () -> Unit = {}
    private val currentSequence = mutableListOf<T>()
    fun sequenceReset(block: () -> Unit): SequenceMaker<T> = apply { sequenceFailed = block }
    fun resetSequence() = currentSequence.clear()
    private fun validateSequence() = currentSequence[currentSequence.lastIndex] == sequence[currentSequence.lastIndex]
    private fun isAchieved() = currentSequence == sequence
    operator fun plusAssign(order: T) = addItem(order)
    open fun add(order: T) = addItem(order)
    private fun addItem(order: T) {
        currentSequence += order
        if (validateSequence()) {
            if (isAchieved()) {
                sequenceAchieved()
                resetSequence()
            }
        } else resetSequence().also { sequenceFailed() }
    }
}

class TimeSequenceMaker<T>(sequence: List<T>, private val timeout: Long = 5000, sequenceAchieved: () -> Unit) :
    SequenceMaker<T>(sequence, sequenceAchieved) {
    constructor(vararg sequence: T, timeout: Long = 5000, sequenceAchieved: () -> Unit) : this(sequence.toList(), timeout, sequenceAchieved)

    private val job = GlobalScope.launch {
        while (isActive) {
            delay(timeout)
            resetSequence().also { sequenceFailed() }
            yield()
        }
    }

    override fun add(order: T) = runBlocking {
        job.cancelChildren()
        super.add(order)
        job.start()
    }.let { Unit }

}