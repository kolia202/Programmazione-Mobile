package com.example.byteme.ui.features.authentication

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.byteme.R
import com.example.byteme.ui.theme.Green
import com.example.byteme.ui.theme.Orange


@Composable
fun AuthenticationScreen() {
    val imageSize = remember { mutableStateOf(IntSize.Zero) }
    val brush = Brush.verticalGradient(
        colors = listOf(
            Color.Transparent,
            Color.Black
        ),
        startY = imageSize.value.height.toFloat() / 2,
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    imageSize.value = it.size
                }
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(brush = brush)
        )

        Button(
            onClick = { /*TODO*/ },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.75f)),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .padding(top = 8.dp)
        ) {
            Text(text = stringResource(id = R.string.skip), color = Green)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 150.dp)
        ) {
            Text(
                text = stringResource(id = R.string.welcome),
                color = Orange,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                fontSize = 50.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(id = R.string.nameApp),
                color = Orange,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                fontSize = 50.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = stringResource(id = R.string.sign), color = Orange)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { /*TODO*/ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.75f)),
                    shape = RoundedCornerShape(32.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_facebook),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(id = R.string.facebook), color = Green)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = { /*TODO*/ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.75f)),
                    shape = RoundedCornerShape(32.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(id = R.string.google), color = Green)
                    }
                }
            }
            Button(
                onClick = { /*TODO*/ },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.50f)),
                shape = RoundedCornerShape(32.dp),
                modifier = Modifier.width(350.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.signEmail),
                    color = Green
                )
            }
            TextButton(
                onClick = { /*TODO*/ },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = stringResource(id = R.string.login),
                    color = Orange
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun AuthenticationScreenPreview() {
    AuthenticationScreen()
}