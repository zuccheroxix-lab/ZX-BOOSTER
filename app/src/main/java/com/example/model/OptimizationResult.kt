package com.example.model

data class OptimizationResult(
    val timestamp: Long = System.currentTimeMillis(),
    val launcherRamFreedMb: Float = 0f,
    val ramUsedBeforeMb: Float = 0f,
    val ramUsedAfterMb: Float = 0f,
    val backgroundProcessesTrimmed: Int = 0,
    val actionsExecuted: List<String> = emptyList(),
    val isSuccess: Boolean = true,
    val summaryMessage: String = "Optimization complete"
)
