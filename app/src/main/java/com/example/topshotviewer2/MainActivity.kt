package com.example.topshotviewer2

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import apolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.example.topshotviewer2.ui.theme.TopShotViewer2Theme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TopShotApp() }
    }
}

@Composable
fun TopShotApp() {
    TopShotViewer2Theme {
        Scaffold() { padding ->
            PlayerList(Modifier.padding(padding))
        }
    }
}

@Composable
fun PlayerList(modifier: Modifier = Modifier) {
    var responseDetails: ApolloResponse<PlayerDetailsQuery.Data>? by remember { mutableStateOf(null) }
    var response: ApolloResponse<PlayerListQuery.Data>? by remember { mutableStateOf(null) }
    var playerDetails by remember {
        mutableStateOf(PlayerDetailsQuery.PlayerData(null, null, null, null, null, null, null, null))
    }
    var playerList by rememberSaveable { mutableStateOf(emptyList<PlayerListQuery.Data1>()) }
    LaunchedEffect(Unit) {
        response = apolloClient.query(PlayerListQuery()).execute()
        Log.d("PlayerList", "Success ${response?.data}")
        playerList = response?.data?.allPlayers?.data?.filterNotNull().orEmpty()
    }
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    ModalDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(modifier = modifier) {
                if (drawerState.isOpen && responseDetails != null) {
                    Text(playerDetails.firstName ?: "", style = MaterialTheme.typography.h1)
                    Text(playerDetails.lastName ?: "", style = MaterialTheme.typography.h1)
                    Text(playerDetails.jerseyNumber?.let { "Jersey Number: $it" } ?: "details not available")
                    Text(playerDetails.currentTeamName?.let { "Team Name: $it" } ?: "")
                    Text(playerDetails.position?.let { "Position: $it" } ?: "")
                }
                OutlinedButton(
                    onClick = {
                        scope.launch { drawerState.close() }
                    }
                ) {
                    Text(stringResource(R.string.button_back), style = MaterialTheme.typography.body2)
                }
            }
        }
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier
        ) {
            items(
                items = playerList,
                key = { player -> player.id }
            ) {player ->
                PlayerView(player, onDetailsClick = {
                    scope.launch {
                        drawerState.open()
                        responseDetails = apolloClient.query(PlayerDetailsQuery(playerId = player.id)).execute()
                        responseDetails?.data?.getPlayerDataWithCurrentStats?.playerData?.apply {
                            this.also { playerDetails = it }
                        }
                    }
                })
            }
        }
    }

}

@Composable
fun PlayerView(player: PlayerListQuery.Data1, onDetailsClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        shape = MaterialTheme.shapes.small,
        modifier = modifier.clickable(onClick = { onDetailsClick() })
    ) {
        Row(modifier = modifier.padding(vertical = 4.dp, horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(player.name.toString(),
                modifier = Modifier.padding(all = 4.dp),
                style = MaterialTheme.typography.h3,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TopShotViewer2Theme {
        PlayerView(
            PlayerListQuery.Data1(id="1630462", name="Aari McDonald"),
            modifier = Modifier.padding(8.dp),
            onDetailsClick = {}
        )
    }
}
