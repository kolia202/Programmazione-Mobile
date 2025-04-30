package com.example.byteme.ui.features.authentication.signup

import android.widget.Space
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.byteme.ui.ByteMeTextField
import com.example.byteme.ui.GroupSocialButtons
import com.example.byteme.ui.theme.Green

@Composable
fun SignUpScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        var name by remember {
            mutableStateOf("")
        }
        var email by remember {
            mutableStateOf("")
        }
        var password by remember {
            mutableStateOf("")
            }
        Image(
            painter = painterResource(id = R.drawable.login_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
        )
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
            Box(modifier = Modifier.weight(1f))
            Text(text = stringResource(id = R.string.sign_up),
                color = Green,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                fontSize = 50.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Cursive)
            Spacer(modifier = Modifier.size(32.dp))
            ByteMeTextField(
                value = name,
                onValueChange = {name = it},
                label = {
                    Text(text = stringResource(id = R.string.nickname),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
            ByteMeTextField(
                value = email,
                onValueChange = {email = it},
                label = {
                    Text(text = stringResource(id = R.string.email),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
            ByteMeTextField(
                value = password,
                onValueChange = {password = it},
                label = {
                    Text(text = stringResource(id = R.string.password),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                        )
                },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                trailingIcon = {
                    Image(painter = painterResource(id = R.drawable.ic_eye),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp))
                }
            )
            Spacer(modifier = Modifier.size(32.dp))
            Button(onClick = { /*TODO*/ }, modifier = Modifier
                .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.75f))) {
                Text(text = stringResource(id = R.string.sign_up),
                    modifier = Modifier
                        .padding(horizontal = 32.dp),
                    color = Color.Black,
                    fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.size(16.dp))
            Text(text = stringResource(id = R.string.login), modifier = Modifier
                .padding(bottom = 10.dp)
                .fillMaxWidth(),
                color = Color.White,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            GroupSocialButtons(color = Color.White, onFacebookClick = { /* TODO */ }) { }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SignUpScreen()
}