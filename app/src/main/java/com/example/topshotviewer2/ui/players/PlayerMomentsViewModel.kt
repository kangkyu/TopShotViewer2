package com.example.topshotviewer2.ui.players

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.topshotviewer2.data.players.MintedMomentRepository
import com.example.topshotviewer2.data.players.PlayersRepository
import com.example.topshotviewer2.model.MintedMomentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlayerMomentsViewModel(private val mintedMomentRepository: MintedMomentRepository) : ViewModel() {

    private val viewModelState = MutableStateFlow(MomentModelViewModelState())
    val viewModelStatePublic: StateFlow<MomentModelViewModelState> = viewModelState.asStateFlow()

    fun loadMoments() {
        viewModelScope.launch {
            val result = mintedMomentRepository.getMoments()
            viewModelState.update {
                it.copy(momentList = result)
            }
        }
    }

    companion object {
        fun provideFactory(
            mintedMomentRepository: MintedMomentRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PlayerMomentsViewModel(mintedMomentRepository) as T
            }
        }
    }
}

data class MomentModelViewModelState(
    val momentList: MintedMomentList = MintedMomentList(moments = emptyList()),
)
