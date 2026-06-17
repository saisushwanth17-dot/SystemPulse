package com.example.data

import kotlinx.coroutines.flow.Flow

interface SystemInfoRepository {
    fun observeSystemState(pollIntervalMs: Long): Flow<SystemOverviewState>
    fun getRunningProcesses(): List<ProcessState>
}
