package com.example.topshotviewer2.data.players

import android.util.Log
import apolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.example.topshotviewer2.PlayerDetailsQuery
import com.example.topshotviewer2.PlayerListQuery
import com.example.topshotviewer2.model.Player
import com.example.topshotviewer2.model.PlayerList
import com.example.topshotviewer2.model.PlayerListPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class PlayersRepository() {
    // for now, store these in memory
    private val favorites: MutableStateFlow<Set<String>> = MutableStateFlow<Set<String>>(setOf())

    fun observeFavorites(): Flow<Set<String>> = favorites

    suspend fun toggleFavorite(favoritePlayerId: String) {
        favorites.update {
            it.addOrRemove(favoritePlayerId)
        }
    }
    suspend fun getPlayerList(): PlayerList {
        return withContext(Dispatchers.IO) {
            val response: ApolloResponse<PlayerListQuery.Data> =
                apolloClient.query(PlayerListQuery()).execute()
            Log.d("PlayerList", "Success ${response.data}")
            val playerlist: List<PlayerListQuery.Data1> =
                response.data?.allPlayers?.data?.filterNotNull().orEmpty()

            val players = playerlist.map { toPlayerListPlayer(it) }
            PlayerList(allPlayers = players)
        }
    }

    suspend fun getPlayer(playerId: String): Player? {
        return withContext(Dispatchers.IO) {
            val response: ApolloResponse<PlayerDetailsQuery.Data> =
                apolloClient.query(PlayerDetailsQuery(playerId = playerId)).execute()
            Log.d("PlayerDetails", "Success ${response.data}")
            val playerDetails: PlayerDetailsQuery.PlayerData? =
                response.data?.getPlayerDataWithCurrentStats?.playerData

            toPlayer(playerDetails, playerId)
        }
    }

    private fun toPlayer(playerDetails: PlayerDetailsQuery.PlayerData?, playerId: String): Player? {
        if (playerDetails == null) {
            return null
        }
        return Player(
            firstName = playerDetails.firstName, lastName = playerDetails.lastName,
            jerseyNumber = playerDetails.jerseyNumber,
            currentTeamName = playerDetails.currentTeamName, position = playerDetails.position,
            id = playerId
        )
    }

    private fun toPlayerListPlayer(player: PlayerListQuery.Data1): PlayerListPlayer {
        var name: String = ""
        player.name?.let { name = it }
        return PlayerListPlayer(id = player.id, name = name)
    }
}

internal fun <String> Set<String>.addOrRemove(element: String): Set<String> {
    return this.toMutableSet().apply {
        if (!add(element)) {
            remove(element)
        }
    }.toSet()
}
