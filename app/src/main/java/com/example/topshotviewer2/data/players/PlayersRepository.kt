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
import kotlinx.coroutines.withContext

class PlayersRepository() {

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

            toPlayer(playerDetails)
        }
    }

    private fun toPlayer(playerDetails: PlayerDetailsQuery.PlayerData?): Player? {
        if (playerDetails == null) {
            return null
        }
        return Player(
            firstName = playerDetails.firstName, lastName = playerDetails.lastName,
            jerseyNumber = playerDetails.jerseyNumber,
            currentTeamName = playerDetails.currentTeamName, position = playerDetails.position,
        )
    }

    private fun toPlayerListPlayer(player: PlayerListQuery.Data1): PlayerListPlayer {
        var name: String = ""
        player.name?.let { name = it }
        return PlayerListPlayer(id = player.id, name = name)
    }
}
