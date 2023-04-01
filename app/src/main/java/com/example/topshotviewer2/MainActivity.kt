package com.example.topshotviewer2

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.example.topshotviewer2.ui.theme.TopShotViewer2Theme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TopShotViewer2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = colorResource(R.color.white)
                ) {
                    PlayerList()
                }
            }
        }
    }
}

@Composable
fun PlayerList() {
    var responseDetails: ApolloResponse<PlayerDetailsQuery.Data>? by remember { mutableStateOf(null) }
    var response: ApolloResponse<PlayerListQuery.Data>? by remember { mutableStateOf(null) }
    var playerDetails by remember {
        mutableStateOf(PlayerDetailsQuery.PlayerData(jerseyNumber = null, position = null))
    }
    val scope = rememberCoroutineScope()
    var playerList by rememberSaveable { mutableStateOf(emptyList<PlayerListQuery.Data1>()) }
    LaunchedEffect(Unit) {
        response = apolloClient.query(PlayerListQuery()).execute()
        Log.d("PlayerList", "Success ${response?.data}")
        playerList = response?.data?.allPlayers?.data?.filterNotNull().orEmpty()
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    ModalDrawer(
        drawerState = drawerState,
        drawerContent = {
            Text("Hello, I am a drawer")
            if (drawerState.isOpen && responseDetails != null && playerDetails != null) {
                Text(playerDetails.jerseyNumber?.let { "Jersey Number: $it" } ?: "details not available", fontFamily = FontFamily.Monospace)
                Text(playerDetails.position?.let { "Position: $it" } ?: "", fontFamily = FontFamily.Monospace)
            }
            OutlinedButton(
                onClick = {
                    scope.launch {
                        drawerState.close()
                    }
                },
                border = BorderStroke(width = 1.dp, color = colorResource(id = R.color.purple_700))
            ) {
                Text(stringResource(R.string.button_back))
            }
        }
    ) {
        LazyColumn {
            items(playerList) { player ->
                PlayerView(player, onDetailsClick = {
                    scope.launch {
                        drawerState.open()
                        responseDetails = apolloClient.query(PlayerDetailsQuery(playerId = player.id)).execute()
                        responseDetails?.data?.getPlayerDataWithCurrentStats?.playerData?.apply {
                            playerDetails = this
                        }
                    }
                })
            }
        }
    }
}

@Composable
fun PlayerView(player: PlayerListQuery.Data1, onDetailsClick: () -> Unit) {

    Surface(
        color = colorResource(R.color.white),
        modifier = Modifier.fillMaxWidth().padding(all = 4.dp)
    ) {
        Column {
            Row(modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)) {
                Column(modifier = Modifier
                    .weight(1f)
                    .padding(all = 4.dp)) {
                    Text(text = player.name.toString(), fontWeight = FontWeight.Bold, fontSize = 21.sp)
                }
                OutlinedButton(
                    onClick = {
                        onDetailsClick()
                    },
                    border = BorderStroke(width = 1.dp, color = colorResource(id = R.color.purple_700))
                ) {
                    Text(stringResource(R.string.button_details))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TopShotViewer2Theme {
        PlayerView(PlayerListQuery.Data1(id="1630462", name="Aari McDonald"), onDetailsClick = {})
    }
}
