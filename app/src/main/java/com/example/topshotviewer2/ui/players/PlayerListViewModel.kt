package com.example.topshotviewer2.ui.players

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.topshotviewer2.data.players.PlayersRepository
import com.example.topshotviewer2.model.PlayerList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlayerListViewModel(
    private val playersRepository: PlayersRepository
) : ViewModel() {

    private val viewModelState = MutableStateFlow(PlayerListViewModelState(isLoading = false))
    val viewModelStatePublic: StateFlow<PlayerListViewModelState> = viewModelState.asStateFlow()

    fun refreshFavorites() {
        viewModelScope.launch {
            playersRepository.observeFavorites().collect { favorites ->
                viewModelState.update { it.copy(favorites = favorites) }
            }
        }
    }

    fun refreshPlayers() {
        viewModelState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val result = playersRepository.getPlayerList()
            viewModelState.update {
                it.copy(playerlist = result, isLoading = false)
            }
        }
    }

    companion object {
        fun provideFactory(
            playersRepository: PlayersRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PlayerListViewModel(playersRepository) as T
            }
        }
    }
}

data class PlayerListViewModelState(
    val isLoading: Boolean = false,
    val favorites: Set<String> = emptySet(),
    val playerlist: PlayerList = PlayerList(allPlayers = emptyList()),
)
