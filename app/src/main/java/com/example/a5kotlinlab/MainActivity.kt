package com.example.a5kotlinlab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlin.math.pow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainCalculatorFunction()
        }
    }
}

@Composable
fun MainCalculatorFunction() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "main_screen") {
        composable("main_screen") {
            MainScreen(navController)
        }
        composable("first_task") {
            SystemReliabilityComparison() {
                navController.popBackStack()
            }
        }
        composable("second_task") {
            LossesFromPowerOutages() {
                navController.popBackStack()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar (
                title = {
                    Text("Калькулятор порівняння надійності систем")
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { navController.navigate("first_task") },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Порівняти надійність одноколової та двоколової систем електропередачі")
            }
            Button(
                onClick = { navController.navigate("second_task") },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Розрахувати збитки від перерв електропостачання у разі застосування однотрансформаторної ГПП")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemReliabilityComparison(navigateBack: () -> Unit) {
    val wList = remember { mutableStateListOf<String>() }
    val tList = remember { mutableStateListOf<String>() }
    var kpMax by remember { mutableStateOf("") }
    var result by remember { mutableStateOf(listOf<Pair<String, Double>>()) }

    Scaffold (
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "Завдання 1.")
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Localized description"
                        )
                    }
                },
            )
        }
    ) { paddingValues ->
        Box (
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = "Калькулятор порівняння надійності систем")

                TextField(
                    value = kpMax,
                    onValueChange = { kpMax = it },
                    label = { Text(text = "Mакс. коефіцієнт планового простою (год.)") }
                )
                Text("Введіть дані для розрахунку")

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(wList.size) { index ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextField(
                                value = wList[index],
                                onValueChange = { wList[index] = it },
                                label = { Text("ω, рік⁻¹ (${index + 1})") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TextField(
                                value = tList[index],
                                onValueChange = { tList[index] = it },
                                label = { Text("tB, год. (${index + 1})") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            IconButton(onClick = {
                                wList.removeAt(index)
                                tList.removeAt(index)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Видалити")
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        wList.add("")
                        tList.add("")
                    }) {
                        Text("Додати елемент")
                    }
                    Button(onClick = {
                        result = calculateSystemReliabilityComparison(wList, tList, kpMax.toDouble())
                    }) {
                        Text("Розрахувати")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                result.let {
                    if (it.isNotEmpty()) {
                        val wOS = it.first { pair -> pair.first == "wOS" }.second
                        val wDS = it.first { pair -> pair.first == "wDS" }.second

                        Text(text = "Частота відмов одноколової системи: $wOS рік^-1")
                        Text(text = "Частота відмов двоколової системи з урахуванням сейкційного вимикача: $wDS рік^-1")

                        if (wDS < wOS) {
                            Text(text = "Надійність двоколової системи електропередачі є вищою ніж одноколової")
                        } else {
                            Text(text = "Надійність одноколової системи електропередачі є вищою ніж двоколової")
                        }
                    } else {
                        Text(text = "Перевірте корректність введених даних")
                    }
                }

            }
        }
    }
}


fun Double.roundTo(n: Int): Double {
    val factor = 10.0.pow(n)
    return Math.round(this * factor) / factor
}

fun calculateSystemReliabilityComparison(wList: List<String>, tList: List<String>, kpMax: Double): List<Pair<String, Double>> {
    val wListDouble = wList.mapNotNull { it.toDoubleOrNull() }
    val tListDouble = tList.mapNotNull { it.toDoubleOrNull() }

    if (wListDouble.isEmpty() || tListDouble.isEmpty() || wListDouble.size != tListDouble.size) {
        return listOf()
    }
    // частота відмов одноколової системи
    val wOS = wListDouble.sum()

    // середня тривалість відновлення одноколової сиситеми
    val tvOS = wListDouble.zip(tListDouble).sumOf { it.first * it.second } / wOS

    // Коефіцієнт аварійного простою одноколової сиситеми
    val kaOS = (wOS * tvOS) / 8760

    // Коефіцієнт планового простою одноколової сиситеми
    val kpOS = 1.2 * (kpMax / 8760)

    // Частота відмов одночасно двох кіл двоколової системи
    val wdk = 2 * wOS * (kaOS + kpOS)
    // частота відмов двоколової системи з урахуванням секційного вимикача
    val wDS = wdk + 0.02
    return listOf(
        "wOS" to wOS,
        "wDS" to wDS.roundTo(4),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LossesFromPowerOutages(navigateBack: () -> Unit) {
    var Zpera by remember { mutableStateOf("") }
    var Zperp by remember { mutableStateOf("") }
    var omega by remember { mutableStateOf("") }
    var tv by remember { mutableStateOf("") }
    var kp by remember { mutableStateOf("") }
    var Pm by remember { mutableStateOf("") }
    var Tm by remember { mutableStateOf("") }
    var result by remember { mutableStateOf(listOf<Pair<String, Double>>()) }

    Scaffold (
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "Завдання 1.")
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Localized description"
                        )
                    }
                },
            )
        }
    ) { paddingValues ->
        Box (
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = "Калькулятор розрахунку збитків від перерв")

                TextField(
                    value = Zpera,
                    onValueChange = { Zpera = it },
                    label = { Text(text = "Збитки у разі аварійних вимкнень (грн./кВт*год)") }
                )
                TextField(
                    value = Zperp,
                    onValueChange = { Zperp = it },
                    label = { Text(text = "Збитки у разі планових вимкнень (грн./кВт*год)") }
                )
                TextField(
                    value = omega,
                    onValueChange = { omega = it },
                    label = { Text(text = "Частота відмов (рік^-1)") }
                )
                TextField(
                    value = tv,
                    onValueChange = { tv = it },
                    label = { Text(text = "Середній час відновлення") }
                )
                TextField(
                    value = kp,
                    onValueChange = { kp = it },
                    label = { Text(text = "Коефіцієнт планового простою") }
                )
                TextField(
                    value = Pm,
                    onValueChange = { Pm = it },
                    label = { Text(text = "Середньомаксимальна потужність системи (кВт)") }
                )
                TextField(
                    value = Tm,
                    onValueChange = { Tm = it },
                    label = { Text(text = "Тривалість розрахункового періоду (год.)") }
                )

                Button(onClick = {
                    result = calculateLossesFromPowerOutages(
                        Zpera.toDouble(),
                        Zperp.toDouble(),
                        omega.toDouble(),
                        tv.toDouble(),
                        kp.toDouble(),
                        Pm.toDouble(),
                        Tm.toDouble()
                    )
                }) {
                    Text("Розрахувати")
                }

                Spacer(modifier = Modifier.height(16.dp))

                result.let {
                    if (it.isNotEmpty()) {
                        val MZper = it.first { pair -> pair.first == "MZper" }.second

                        Text(text = "Математичне сподівання збитків від переривання електропостачання: $MZper грн")
                    }
                }

            }
        }
    }
}

fun calculateLossesFromPowerOutages(
    Zpera: Double, Zperp: Double, omega: Double,
    tv: Double, kp: Double, Pm: Double, Tm: Double
): List<Pair<String, Double>> {

    val MWneda = omega * tv * Pm * Tm
    val MWnedp = kp * Pm * Tm
    // Математичне сподівання збитків від переривання електропостачання
    val MZper = Zpera * MWneda + Zperp * MWnedp
    return listOf(
        "MZper" to MZper.roundTo(2)
    )
}
