package com.example.mainactivity

import com.example.mainactivity.ui.theme.MusicService
import com.example.mainactivity.ui.theme.HelpActivity


import android.app.Activity
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


// MUSICA DE FONDO
import android.content.Intent

// Notificaciones
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat

// CAPTURAS DE PANTALLA
import android.graphics.Bitmap
import android.graphics.Canvas
import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast

//CALENDARIO
import android.Manifest
import android.app.Activity.RESULT_OK
import android.provider.CalendarContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.ui.res.stringResource
import java.util.Calendar
import java.util.TimeZone

//UBICACION JUGADOR
import com.google.android.gms.location.LocationServices

// signin
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.runtime.mutableStateListOf
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

//interfaz de Retrofit
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.Callback
import retrofit2.Response

fun updateTopTen(playerName: String, coinsWon: Int) {
    val db = FirebaseFirestore.getInstance()

    // Referencia al Top Ten
    val topTenRef = db.collection("topTen").document("topTenList")

    // Obtener el Top Ten y agregar el nuevo puntaje
    topTenRef.get().addOnSuccessListener { document ->
        val currentTopTen = document.data?.get("players") as? List<Map<String, Any>> ?: emptyList()

        // Agregar el nuevo jugador a la lista
        val newPlayer = mapOf(
            "playerName" to playerName,
            "coinsWon" to coinsWon
        )
        val updatedTopTen = (currentTopTen + newPlayer).sortedByDescending { it["coinsWon"] as Int }
            .take(10) // Tomamos los 10 primeros

        // Actualizar el Top Ten
        topTenRef.update("players", updatedTopTen)
            .addOnSuccessListener {
                Log.d("Firestore", "Top Ten actualizado correctamente")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error al actualizar Top Ten", e)
            }
    }
}

fun startGame(playerName: String) {
    val db = FirebaseFirestore.getInstance()

    // Crear una nueva partida en Firestore
    val gameRef = db.collection("games").document(playerName)
    val gameData = mapOf(
        "playerName" to playerName,
        "status" to "inProgress",
        "startTime" to System.currentTimeMillis()
    )

    gameRef.set(gameData)
        .addOnSuccessListener {
            Log.d("Firestore", "Juego iniciado correctamente")
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error al iniciar juego", e)
        }
}

fun sendMessage(playerName: String, message: String) {
    val db = FirebaseFirestore.getInstance()

    // Referencia a la colección de mensajes
    val messagesRef = db.collection("gameMessages")

    val messageData = mapOf(
        "playerName" to playerName,
        "message" to message,
        "timestamp" to System.currentTimeMillis()
    )

    messagesRef.add(messageData)
        .addOnSuccessListener {
            Log.d("Firestore", "Mensaje enviado correctamente")
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error al enviar mensaje", e)
        }
}


interface ApiService {

    @GET("getTopTen")  // URL del endpoint
    fun getTopTen(@Query("gameId") gameId: String): Call<List<Partida>>

    // Otros endpoints según lo que necesites
}


val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

val retrofit = Retrofit.Builder()
    .baseUrl("https://api.example.com/")  // Cambia con la URL de tu backend
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()

val apiService = retrofit.create(ApiService::class.java)


fun increasePrize() {
    val db = FirebaseFirestore.getInstance()
    val prizeRef = db.collection("prize").document("currentPrize")

    // Aumentar el valor del premio común
    prizeRef.update("value", FieldValue.increment(10))  // Aumenta el premio en 10
        .addOnSuccessListener {
            Log.d("Firestore", "Premio común incrementado")
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error al incrementar el premio común", e)
        }
}

fun getPrizeAndReset() {
    val db = FirebaseFirestore.getInstance()
    val prizeRef = db.collection("prize").document("currentPrize")

    // Obtener el valor del premio común
    prizeRef.get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val prize = document.getLong("value") ?: 0
                Log.d("Firestore", "Premio común actual: $prize")

                // Aquí podrías entregar el premio al jugador y luego resetearlo
                awardPrizeToPlayer(prize)

                // Resetear el premio común
                prizeRef.update("value", 0)
                    .addOnSuccessListener {
                        Log.d("Firestore", "Premio común reseteado")
                    }
                    .addOnFailureListener { e ->
                        Log.w("Firestore", "Error al resetear el premio común", e)
                    }
            }
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error al obtener el premio común", e)
        }
}

fun awardPrizeToPlayer(prize: Long) {
    // Aquí puedes aumentar las monedas del jugador con el valor del premio común
    Log.d("Firestore", "Jugador ha ganado $prize monedas")

    // Actualizar las monedas del jugador, por ejemplo, aumentando su saldo en Firestore
    val playerRef = FirebaseFirestore.getInstance().collection("players").document("playerID")

    playerRef.update("coins", FieldValue.increment(prize))
        .addOnSuccessListener {
            Log.d("Firestore", "Jugador recibió el premio común")
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error al dar el premio al jugador", e)
        }
}




data class Victory(
    val playerName: String = "",
    val coinsWon: Int = 0,
    val timestamp: Long = System.currentTimeMillis()  // Timestamp para la fecha
)

fun saveVictory(playerName: String, coinsWon: Int) {
    val db = FirebaseFirestore.getInstance()
    val victoriesRef = db.collection("victories")

    // Crear un objeto de victoria
    val victory = hashMapOf(
        "playerName" to playerName,
        "coinsWon" to coinsWon,
        "timestamp" to System.currentTimeMillis()  // Timestamp para la fecha
    )

    // Guardar en la colección "victories"
    victoriesRef.add(victory)
        .addOnSuccessListener {
            Log.d("Firestore", "Victoria guardada correctamente")
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error al guardar victoria", e)
        }
}

fun getTopTen() {
    val db = FirebaseFirestore.getInstance()
    val victoriesRef = db.collection("victories")

    // Obtener las victorias ordenadas por "coinsWon" de forma descendente
    victoriesRef.orderBy("coinsWon", com.google.firebase.firestore.Query.Direction.DESCENDING)
        .limit(10)  // Limitar a las primeras 10 victorias
        .get()
        .addOnSuccessListener { result ->
            for (document in result) {
                val playerName = document.getString("playerName")
                val coinsWon = document.getLong("coinsWon")
                Log.d("Firestore", "Jugador: $playerName, Monedas Ganadas: $coinsWon")
                // Aquí puedes agregar la lógica para mostrar el Top Ten en la interfaz
            }
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error al obtener el Top Ten", e)
        }
}





@Composable
fun GoogleSignInButton() {
    val context = LocalContext.current  // Obtiene el contexto actual de Compose

    // Configuración de Google Sign-In
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("1:677106691035:android:5979a60ab707db3e73d5fe")  // Reemplaza con tu Web Client ID de Firebase
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)  // Crea el cliente de Google Sign-In

    // Configura el launcher para manejar el resultado de la actividad
    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)  // Autenticación en Firebase
            } catch (e: ApiException) {
                Log.w("Google SignIn", "signInResult:failed code=" + e.statusCode)
            }
        }
    }

    // Botón de Google Sign-In
    Button(onClick = {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)  // Lanza el intent de Google Sign-In
    }) {
        Text("Iniciar sesión con Google")
    }
}

private fun firebaseAuthWithGoogle(idToken: String) {
    val credential = GoogleAuthProvider.getCredential(idToken, null)
    FirebaseAuth.getInstance().signInWithCredential(credential)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // El inicio de sesión fue exitoso
                val user = FirebaseAuth.getInstance().currentUser
                Log.d("FirebaseAuth", "signInWithCredential:success")
                // Procede con lo que necesites, por ejemplo, redirigir a la pantalla principal
            } else {
                // Si el inicio de sesión falla
                Log.w("FirebaseAuth", "signInWithCredential:failure", task.exception)
            }
        }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MainActivityTheme {
        GoogleSignInButton()  // Vista previa del botón de Google Sign-In
    }
}









fun guardarEventoEnCalendario(context: Context, premio: Int) {
    val calendar = Calendar.getInstance()
    val horaInicio = calendar.timeInMillis
    val horaFin = horaInicio + 60 * 60 * 1000 // 1 hora de duración

    val values = ContentValues().apply {
        put(CalendarContract.Events.DTSTART, horaInicio)
        put(CalendarContract.Events.DTEND, horaFin)
        put(CalendarContract.Events.TITLE, "Victoria en la ruleta")
        put(CalendarContract.Events.DESCRIPTION, "Ganaste $premio monedas")
        put(CalendarContract.Events.CALENDAR_ID, 1)
        put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
    }

    val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
    if (uri != null) {
        Log.d("Calendario", "✅ Evento guardado: $uri")
    } else {
        Log.e("Calendario", "❌ Error al guardar evento")
    }
}


fun guardarCaptura(nombreArchivo: String, view: View, context: Context) {
    val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    view.draw(canvas)

    val resolver = context.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, "$nombreArchivo.jpg")
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Ruleta")
    }

    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    uri?.let {
        val stream = resolver.openOutputStream(it)
        stream?.let { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
            output.close()
            Toast.makeText(context, "📸 Captura guardada en Galería", Toast.LENGTH_SHORT).show()
        }
    }
}


fun mostrarNotificacion(context: android.content.Context, premio: Int) {
    val channelId = "victoria_channel"
    val channelName = "Notificaciones de Victoria"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, channelName, importance)
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    val intent = Intent(context, MainActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle("🎉 ¡Has ganado!")
        .setContentText("Obtuviste $premio monedas en la ruleta")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)

    if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
        == PackageManager.PERMISSION_GRANTED
    ) {
        val notificationId = 1
        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

}


sealed class Pantalla(val ruta: String, val titulo: String) {
    object Bienvenida : Pantalla(ruta = "bienvenida", titulo = "Inicio")
    object Juego : Pantalla("juego", "Juego")
    object Historial : Pantalla("historial", "Historial")
    object Ranking : Pantalla("ranking", "Ranking")
}
//solicitar ubicacion
fun solicitarPermisosUbicacion(activity: Activity) {
    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {

        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            1001
        )
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        solicitarPermisosUbicacion(this)
        //notificacion
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
        //calendario
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CALENDAR),
                1001
            )
        }
        //ubicacion jugador
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                1002
            )
        }

        setContent {
            MainActivityTheme {
                GoogleSignInButton()  // Llama al botón de Google Sign-In
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
            text = stringResource(R.string.app_name),
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
            Text(text = stringResource(R.string.start_game))
        }
        val context = LocalContext.current

        Button(onClick = {
            context.startActivity(Intent(context, HelpActivity::class.java))
        }) {
            Text(text = stringResource(R.string.help))
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

    var musicaActiva by remember { mutableStateOf(false) }

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

                    val rootView = (context as Activity).window.decorView.rootView
                    guardarCaptura(
                        "captura_ruleta_${System.currentTimeMillis()}",
                        rootView,
                        context
                    )
                    guardarEventoEnCalendario(context, premio)

                    giro += (720..1440).random().toFloat()

                    // Si el jugador pierde, aumenta el premio común
                    //      if (premio == 0) {
                    //          increasePrize()
                    //      } else {
                    // Si el jugador gana, recupera el premio común y resetealo
                    //         getPrizeAndReset()
                    //      }

                    // 🔔 Mostrar notificación al ganar
                    mostrarNotificacion(context, premio)

                    //ubicacion jugador
                    val fusedLocationClient =
                        LocationServices.getFusedLocationProviderClient(context as Activity)

                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            val latitud = location?.latitude
                            val longitud = location?.longitude

                            // Guardar partida
                            val fecha =
                                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                                    Date()
                                )
                            val partida = Partida(
                                fecha = fecha,
                                monedasGanadas = premio,
                                saldoFinal = saldo,
                                nombreJugador = nombreJugador,
                                latitud = latitud,
                                longitud = longitud
                            )

                            AppDatabase.getInstance(context).partidaDao()
                                .insertar(partida)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                    { Log.d("BD", "Partida guardada con ubicación") },
                                    { error -> Log.e("BD", "Error al guardar partida", error) }
                                )
                        }
                    } else {
                        Log.e("Ubicación", "Permiso de ubicación no concedido")
                    }
                }
            },
            enabled = saldo > 0
        ) {
            Text("Girar ruleta (-1 moneda)")
        }


        // BOTÓN DE MÚSICA DE FONDO
        Button(
            onClick = {
                val intent = Intent(context, MusicService::class.java)
                if (!musicaActiva) {
                    context.startService(intent)
                } else {
                    context.stopService(intent)
                }
                musicaActiva = !musicaActiva
            }
        ) {
            Text(if (musicaActiva) "🔇 Detener Música" else "🔊 Activar Música")
        }

        // MOSTRAR PREMIO
        ultimoPremio?.let {
            Text(text = stringResource(R.string.win_message))
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

    //@Query("SELECT * FROM partidas ORDER BY id DESC")
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
                        if (partida.latitud != null && partida.longitud != null) {
                            Text(text = stringResource(R.string.location_label))
                        } else {
                            Text(text = "Ubicación no disponible")
                        }
                        val lat = partida.latitud?.let { String.format("%.4f", it) } ?: "?"
                        val lon = partida.longitud?.let { String.format("%.4f", it) } ?: "?"
                        Text(text = "Ubicación: $lat, $lon")
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
    val rankingState = remember { mutableStateOf<List<Partida>>(emptyList()) }  // Datos de Room
    val topTen = remember { mutableStateListOf<Map<String, Any>>() }  // Datos de Firestore (Retrofit)

    // Llamada a la API de Retrofit para obtener el Top Ten
    LaunchedEffect(Unit) {
        val call = apiService.getTopTen("game123")  // Llamada a tu API
        call.enqueue(object : Callback<List<Partida>> {
            override fun onResponse(call: Call<List<Partida>>, response: Response<List<Partida>>) {
                if (response.isSuccessful) {
                    val topTen = response.body() ?: emptyList()
                    rankingState.value = topTen  // Actualiza el estado con el Top Ten
                } else {
                    Log.e("Retrofit", "Error en la respuesta")
                }
            }

            override fun onFailure(call: Call<List<Partida>>, t: Throwable) {
                Log.e("Retrofit", "Error en la llamada API", t)
            }
        })
    }

    // Obtener datos de Room (base de datos local)
    LaunchedEffect(Unit) {
        AppDatabase.getInstance(context).partidaDao()
            .obtenerTodas()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { lista ->
                    val ordenado = lista.sortedByDescending { it.monedasGanadas }  // Ordenar por monedas ganadas
                    rankingState.value = ordenado
                },
                { error -> Log.e("BD", "Error al cargar ranking desde Room", error) }
            )
    }

    // UI
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
        Text(
            text = "🏆 Ranking de Jugadores",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Mostrar el ranking de Firestore (Retrofit)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(topTen) { victory ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "Jugador: ${victory["playerName"]}")
                        Text(text = "Monedas Ganadas: ${victory["coinsWon"]}")
                    }
                }
            }
        }

        // Mostrar el ranking de Room (base de datos local)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(rankingState.value) { partida ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "Jugador: ${partida.nombreJugador}")
                        Text(text = "Monedas Ganadas: ${partida.monedasGanadas}")
                        Text(text = "Saldo Final: ${partida.saldoFinal}")
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
