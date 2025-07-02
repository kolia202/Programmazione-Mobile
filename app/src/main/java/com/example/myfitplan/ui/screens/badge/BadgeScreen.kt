package com.example.myfitplan.ui.screens.badge

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myfitplan.R

@Composable
fun BadgeScreen(badgeViewModel: BadgeViewModel){

    val userBadges = badgeViewModel.userBadges.collectAsState().value

    LazyColumn (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ){
        items(userBadges) {badgeWithUserData->
            val badge= badgeWithUserData.badge
            val badgeUser = badgeWithUserData.badgeUser

            Card(
                modifier= Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ){
                Column (modifier = Modifier.padding(16.dp)){

                    Image(
                        painter = painterResource(
                            id = getImageResource(badge.icon)
                        ),
                        contentDescription = badge.title,
                        modifier = Modifier
                            .size(64.dp)
                            .padding(bottom=8.dp),
                        contentScale = ContentScale.Fit
                    )

                    Text(text = badge.title)
                    Text(text = badge.description)
                    Text(text = "Data ottenimento: ${badgeWithUserData.badgeUser.dataAchieved}")

                }
            }
        }
    }
}

fun getImageResource(iconName: String): Int{
    return when(iconName){
        "badge_star" -> R.drawable.badge_star
        "badge_cup" -> R.drawable.badge_cup
        else-> R.drawable.ic_default_badge
    }
}