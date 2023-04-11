package com.example.topshotviewer2.ui.players

import com.example.topshotviewer2.model.Player
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.topshotviewer2.data.players.PlayersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val playersRepository: PlayersRepository
) : ViewModel() {

    private val viewModelState = MutableStateFlow(PlayerViewModelState(isLoading = false))
    val viewModelStatePublic: StateFlow<PlayerViewModelState> = viewModelState.asStateFlow()

    fun loadPlayer(playerId: String) {
        viewModelState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val result = playersRepository.getPlayer(playerId)
            viewModelState.update {
                it.copy(player = result, isLoading = false)
            }
        }
    }

    companion object {
        fun provideFactory(
            playersRepository: PlayersRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PlayerViewModel(playersRepository) as T
            }
        }
    }
}

data class PlayerViewModelState(
    val isLoading: Boolean = false,
    val player: Player? = null
)
