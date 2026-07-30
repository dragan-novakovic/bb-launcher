package com.dragannovakovic.bblauncher.ui.status

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dragannovakovic.bblauncher.data.system.SystemStatus
import com.dragannovakovic.bblauncher.data.system.SystemStatusRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class StatusBarViewModel(application: Application) : AndroidViewModel(application) {
    val status = SystemStatusRepository(application)
        .status
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = SystemStatus(),
        )
}
