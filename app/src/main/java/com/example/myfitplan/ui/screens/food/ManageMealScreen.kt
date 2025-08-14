package com.example.myfitplan.ui.screens.food

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.myfitplan.data.database.FoodInsideMeal
import com.example.myfitplan.data.database.MealType
import com.example.myfitplan.ui.screens.home.HomeViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun ManageMealScreen(
    mealType: MealType,
    date: String,
    userEmail: String,
    navController: NavController,
    viewModel: HomeViewModel = koinViewModel()
) {
    val foodsInsideMeal by viewModel.repositories.getFoodsInsideMeal(date, mealType, userEmail).collectAsState(initial = emptyList())
    val allFoods by viewModel.repositories.foods.collectAsState(initial = emptyList())
    val candidateFoods = allFoods.filter { it.email == userEmail && it.description == mealType.string }

    var addDialogFoodName by remember { mutableStateOf<String?>(null) }
    var addDialogQuantity by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    val colors = MaterialTheme.colorScheme

    Column(
        Modifier
            .fillMaxSize()
            .padding(18.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Gestione ${mealType.string}", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = colors.primary)
        Spacer(Modifier.height(10.dp))

        Text("Cibi già aggiunti", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = colors.primary)
        if (foodsInsideMeal.isEmpty()) {
            Text("Nessun cibo aggiunto per questo pasto.")
        } else {
            foodsInsideMeal.forEach { fimw ->
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        Modifier
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("${fimw.food.name} (${fimw.foodInsideMeal.quantity} ${fimw.food.unit.string})", fontWeight = FontWeight.Bold)
                            Text("${fimw.food.kcalPerc * fimw.foodInsideMeal.quantity} kcal")
                        }
                        IconButton(onClick = {

                            viewModel.viewModelScope.launch {
                                viewModel.repositories.deleteFoodInsideMeal(
                                    date,
                                    mealType,
                                    fimw.food.name,
                                    userEmail
                                )
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Rimuovi", tint = colors.error)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(22.dp))

        Text("Aggiungi alimento", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = colors.primary)
        if (candidateFoods.isEmpty()) {
            Text("Nessun alimento disponibile per ${mealType.string}. Puoi aggiungerli dalla sezione 'Foods'.")
        } else {
            candidateFoods.forEach { food ->
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        Modifier
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(food.name, fontWeight = FontWeight.SemiBold)
                            Text("${food.kcalPerc} kcal/100g", fontSize = 13.sp, color = colors.secondary)
                        }
                        Button(
                            onClick = {
                                addDialogFoodName = food.name
                                addDialogQuantity = ""
                                showAddDialog = true
                            },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Aggiungi", fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(30.dp))
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Torna alla Home")
        }
    }

    if (showAddDialog && addDialogFoodName != null) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Aggiungi ${addDialogFoodName} a ${mealType.string}") },
            text = {
                OutlinedTextField(
                    value = addDialogQuantity,
                    onValueChange = { addDialogQuantity = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Quantità (es: 100)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val quantity = addDialogQuantity.toFloatOrNull() ?: 0f
                        if (quantity > 0f) {
                            viewModel.viewModelScope.launch {
                                viewModel.repositories.upsertFoodInsideMeal(
                                    FoodInsideMeal(
                                        emailFIM = userEmail,
                                        foodName = addDialogFoodName!!,
                                        date = date,
                                        mealType = mealType,
                                        quantity = quantity
                                    )
                                )
                            }
                            showAddDialog = false
                        }
                    }
                ) { Text("Aggiungi") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Annulla") }
            }
        )
    }
}