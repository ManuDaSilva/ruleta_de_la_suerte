package com.example.mainactivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.mainactivity.ui.theme.MainActivityTheme

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

//partida
import androidx.room.Entity
import androidx.room.PrimaryKey

//DAO
import androidx.room.*

//BASE DE DATOS
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

import java.text.SimpleDateFormat
import java.util.*
import android.util.Log

import androidx.compose.ui.platform.LocalContext
import com.example.mainactivity.database.AppDatabase
import com.example.mainactivity.database.Partida
import com.example.mainactivity.database.PartidaDao

import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon

//pantalla ranking
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Person

//pantalla bienvenida
import androidx.navigation.navArgument
import androidx.navigation.NavController

//ruleta girando
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource

// Imports necesarios
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember


//song spin
//import android.media.MediaPlayer

// Para Image y painterResource
import androidx.compose.foundation.Image
import androidx.compose.animation.core.LinearEasing





sealed class Pantalla(val ruta: String, val titulo: String) {
    object Bienvenida : Pantalla(ruta = "bienvenida", titulo = "Inicio")
    object Juego : Pantalla("juego", "Juego")
    object Historial : Pantalla("historial", "Historial")
    object Ranking : Pantalla("ranking", "Ranking")
}



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainActivityTheme {
                val navController = rememberNavController()
                val items = listOf(Pantalla.Juego, Pantalla.Historial, Pantalla.Ranking) // sin bienvenida
                val currentBackStack by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStack?.destination?.route

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            items.forEach { pantalla ->
                                NavigationBarItem(
                                    icon = {
                                        when (pantalla) {
                                            is Pantalla.Juego -> Icon(Icons.Filled.Home, contentDescription = "Juego")
                                            is Pantalla.Historial -> Icon(Icons.Filled.List, contentDescription = "Historial")
                                            is Pantalla.Ranking -> Icon(Icons.Filled.Star, contentDescription = "Ranking")
                                            is Pantalla.Bienvenida -> Icon(Icons.Filled.Person, contentDescription = "Inicio")
                                        }
                                    },
                                    label = { Text(pantalla.titulo) },
                                    selected = currentRoute == pantalla.ruta,
                                    onClick = {
                                        if (currentRoute != pantalla.ruta) {
                                            navController.navigate(pantalla.ruta) {
                                                popUpTo(navController.graph.startDestinationId) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Pantalla.Bienvenida.ruta,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Pantalla.Juego.ruta + "/{nombre}",
                            arguments = listOf(navArgument("nombre") { defaultValue = "Jugador" })
                        ) { backStackEntry ->
                            val nombreJugador = backStackEntry.arguments?.getString("nombre") ?: "Jugador"
                            PantallaJuego(nombreJugador)
                        }
                        composable(Pantalla.Bienvenida.ruta) {
                            PantallaBienvenida(navController = navController)
                        }
                        composable(Pantalla.Historial.ruta) {
                            PantallaHistorial()
                        }
                        composable(Pantalla.Ranking.ruta) {
                            PantallaRanking()
                        }

                    }
                }
            }
        }


    }
}



@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}


@Composable
fun PantallaBienvenida(navController: NavController) {
    var nombre by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "RULETA DE LA SUERTE",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 40.dp)
        )

        TextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre del jugador") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (nombre.isNotBlank()) {
                    navController.navigate("juego/${nombre}")
                }
            },
            enabled = nombre.isNotBlank()
        ) {
            Text("EMPEZAR A JUGAR")
        }
    }
}


@Composable
fun PantallaJuego(nombreJugador: String) {
    var saldo by remember { mutableStateOf(100) }
    var ultimoPremio by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current

    var giro by remember { mutableStateOf(0f) }
    val rotacion by animateFloatAsState(
        targetValue = giro,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "rotarRuleta"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // SALDO
        Text("Saldo: $saldo monedas", style = MaterialTheme.typography.headlineSmall)

        // IMAGEN DE RULETA ANIMADA
        Image(
            painter = painterResource(id = R.drawable.ruleta),
            contentDescription = "Ruleta",
            modifier = Modifier
                .size(200.dp)
                .graphicsLayer {
                    rotationZ = rotacion
                }
        )

        // BOTÓN DE JUEGO
        Button(
            onClick = {
                if (saldo > 0) {
                    saldo -= 1
                    val premio = (0..20).random()
                    saldo += premio
                    ultimoPremio = premio
                    giro += (720..1440).random().toFloat() // rotación aleatoria

                    //song spin
//                    val mediaPlayer = MediaPlayer.create(context, R.raw.spin_sound_effect)
//                    mediaPlayer.start()


                    // GUARDAR PARTIDA
                    val fecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                    val partida = Partida(
                        fecha = fecha,
                        monedasGanadas = premio,
                        saldoFinal = saldo,
                        nombreJugador = nombreJugador
                    )

                    AppDatabase.getInstance(context).partidaDao()
                        .insertar(partida)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            { Log.d("BD", "Partida guardada") },
                            { error -> Log.e("BD", "Error al guardar", error) }
                        )
                }
            },
            enabled = saldo > 0
        ) {
            Text("Girar ruleta (-1 moneda)")
        }

        ultimoPremio?.let {
            Text("¡Ganaste $it monedas!")
        }
    }
}




//partida
@Entity(tableName = "partidas")
data class Partida(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombreJugador: String,
    val fecha: String,
    val monedasGanadas: Int,
    val saldoFinal: Int
)

//DAO
@Dao
interface PartidaDao {
    @Insert
    suspend fun insertar(partida: Partida)

    @Query("SELECT * FROM partidas ORDER BY id DESC")
    suspend fun obtenerTodas(): List<Partida>
}

//BASE DE DATOS
@Database(entities = [Partida::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun partidaDao(): PartidaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ruleta_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

//PANTALLA HISTORIAL
@Composable
fun PantallaHistorial() {
    val context = LocalContext.current
    val partidasState = remember { mutableStateOf<List<Partida>>(emptyList()) }

    // Obtener datos al cargar la pantalla
    LaunchedEffect(Unit) {
        AppDatabase.getInstance(context).partidaDao()
            .obtenerTodas()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { lista -> partidasState.value = lista },
                { error -> Log.e("BD", "Error al cargar historial", error) }
            )
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text(
            text = "Historial de Partidas",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(partidasState.value) { partida ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "Nombre: ${partida.nombreJugador}")
                        Text(text = "Fecha: ${partida.fecha}")
                        Text(text = "Ganadas: ${partida.monedasGanadas}")
                        Text(text = "Saldo final: ${partida.saldoFinal}")
                    }
                }
            }
        }
    }
}

//PANTALLA RANKING
@Composable
fun PantallaRanking() {
    val context = LocalContext.current
    val rankingState = remember { mutableStateOf<List<Partida>>(emptyList()) }

    LaunchedEffect(Unit) {
        AppDatabase.getInstance(context).partidaDao()
            .obtenerTodas()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { lista ->
                    val ordenado = lista.sortedByDescending { it.monedasGanadas }
                    rankingState.value = ordenado
                },
                { error -> Log.e("BD", "Error al cargar ranking", error) }
            )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "🏆 Ranking de Partidas",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(rankingState.value) { index, partida ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (index) {
                            0 -> MaterialTheme.colorScheme.primaryContainer // 🥇 primer lugar
                            1 -> MaterialTheme.colorScheme.secondaryContainer // 🥈 segundo
                            2 -> MaterialTheme.colorScheme.tertiaryContainer // 🥉 tercero
                            else -> MaterialTheme.colorScheme.surface
                        }
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "#${index + 1}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(text = "Nombre: ${partida.nombreJugador}")
                        Text(text = "Ganadas: ${partida.monedasGanadas}")
                        Text(text = "Saldo final: ${partida.saldoFinal}")
                        Text(text = "Fecha: ${partida.fecha}")
                    }
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MainActivityTheme {
        Greeting(name = "Android")
    }
}
