package com.zekibiyikli.nativemindscase.ui.home

import com.zekibiyikli.nativemindscase.data.content.model.Subject

data class HomeUiState(
    val subjects: List<Subject> = emptyList(),
    val selectedSubjectId: String = "",
    val isPremium: Boolean = false,
    val remainingFreeReads: Int = 0
)
