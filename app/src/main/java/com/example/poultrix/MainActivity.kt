package com.example.poultrix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.poultrix.ui.theme.PoultrixTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PoultrixTheme {
                PoultrixApp()
            }
        }
    }
}

@Composable
fun PoultrixApp() {
    val navController = rememberNavController()
    val viewModel: PoultryViewModel = viewModel()
    
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") { SplashScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("dashboard") { DashboardScreen(navController, viewModel) }
        composable("hens_list") { HensListScreen(navController, viewModel) }
        composable("add_hen") { AddHenScreen(navController, viewModel) }
        composable("temp_detail") { TempDetailScreen(navController, viewModel) }
        composable("humidity_detail") { HumidityDetailScreen(navController, viewModel) }
    }
}

@Composable
fun SplashScreen(navController: NavController) {
    // Using a LaunchedEffect that only runs ONCE
    LaunchedEffect(key1 = true) {
        delay(2000)
        navController.navigate("login") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(modifier = Modifier.size(120.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.2f)) {
                Box(contentAlignment = Alignment.Center) { Text(text = "🐔", fontSize = 60.sp) }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "POULTRIX", fontSize = 42.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = 4.sp)
            Text(text = "Smart Poultry Monitoring", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Light)
            Spacer(modifier = Modifier.height(48.dp))
            CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Welcome Back", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(text = "Sign in to continue monitoring", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(48.dp))

            OutlinedTextField(
                value = username, onValueChange = { username = it; error = false },
                label = { Text("Username") }, leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), isError = error && username.isEmpty()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password, onValueChange = { password = it; error = false },
                label = { Text("Password") }, leadingIcon = { Icon(Icons.Default.Lock, null) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), isError = error && password.isEmpty()
            )

            AnimatedVisibility(visible = error) {
                Text(text = "Please fill in all fields", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { if (username.isNotEmpty() && password.isNotEmpty()) navController.navigate("dashboard") { popUpTo("login") { inclusive = true } } else error = true },
                modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)
            ) { Text("SIGN IN", fontWeight = FontWeight.Bold) }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { navController.navigate("register") }) { Text("New here? Create an account") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Registration", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(value = username, onValueChange = { username = it; errorMsg = null }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = email, onValueChange = { email = it; errorMsg = null }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = password, onValueChange = { password = it; errorMsg = null }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it; errorMsg = null }, label = { Text("Confirm Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))

            AnimatedVisibility(visible = errorMsg != null) {
                Text(text = errorMsg ?: "", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    if (username.isEmpty() || email.isEmpty() || password.isEmpty()) errorMsg = "Please fill in all fields"
                    else if (password != confirmPassword) errorMsg = "Passwords do not match"
                    else navController.navigate("dashboard") { popUpTo("login") { inclusive = true } }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)
            ) { Text("CREATE ACCOUNT", fontWeight = FontWeight.Bold) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController, viewModel: PoultryViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val connectedHens = uiState.hens.filter { it.isConnected }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Column { Text("Poultrix", fontWeight = FontWeight.ExtraBold); Text("Firebase Connected", style = MaterialTheme.typography.labelMedium) } },
                actions = { IconButton(onClick = { viewModel.refreshData() }) { Icon(Icons.Default.Refresh, contentDescription = "Refresh") } }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 20.dp).verticalScroll(rememberScrollState())) {
            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)))
                Spacer(modifier = Modifier.height(20.dp))
            }

            Text(text = "Environmental Metrics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                MetricCard(
                    title = "Active Sensors", value = "${connectedHens.size}", label = "Hardware", icon = Icons.Default.Info,
                    color = Color(0xFF4CAF50), modifier = Modifier.weight(1f), onClick = { navController.navigate("hens_list") }
                )
                Spacer(modifier = Modifier.width(12.dp))
                MetricCard(
                    title = "Ambient Temp", value = if(uiState.ambientTemp > 0) "${uiState.ambientTemp}°C" else "--", label = "IoT Data", icon = Icons.Default.Favorite,
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f), onClick = { navController.navigate("temp_detail") }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                MetricCard(
                    title = "Humidity", value = if(uiState.humidity > 0) "${uiState.humidity}%" else "--", label = "IoT Data", icon = Icons.Default.DateRange,
                    color = Color(0xFF2196F3), modifier = Modifier.weight(1f), onClick = { navController.navigate("humidity_detail") }
                )
                Spacer(modifier = Modifier.width(12.dp))
                MetricCard(
                    title = "Total Hens", value = "${uiState.hens.size}", label = "Manual", icon = Icons.Default.Person,
                    color = Color(0xFF9C27B0), modifier = Modifier.weight(1f), onClick = { navController.navigate("hens_list") }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
            
            if (connectedHens.isEmpty()) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "No Hardware Connected", fontWeight = FontWeight.Bold)
                        Text(text = "Connect an ESP32 to see live hen analytics", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { navController.navigate("add_hen") }) { Text("Connect New Hardware") }
                    }
                }
            } else {
                Text(text = "Ambient Temperature Trend", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                TemperatureGraph(history = uiState.temperatureHistory, modifier = Modifier.fillMaxWidth().height(180.dp).clickable { navController.navigate("temp_detail") })
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HensListScreen(navController: NavController, viewModel: PoultryViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hens Management", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = { IconButton(onClick = { navController.navigate("add_hen") }) { Icon(Icons.Default.Add, null) } }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            items(uiState.hens) { hen ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), shape = RoundedCornerShape(16.dp)) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Hen ID: ${hen.id}", fontWeight = FontWeight.Bold)
                            Text(text = "Age: ${hen.ageDays} days", style = MaterialTheme.typography.bodySmall)
                            Text(text = "HW ID: ${hen.hardwareId}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                        
                        if (hen.isConnected) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "${hen.currentTemp}°C", fontWeight = FontWeight.Bold, color = if (hen.currentTemp > 38.0) Color.Red else Color(0xFF4CAF50))
                                Text(text = hen.currentActivity, style = MaterialTheme.typography.bodySmall, color = Color(0xFF4CAF50))
                                Text(text = "Connected", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50))
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "--", color = Color.Gray)
                                Text(text = "Disconnected", style = MaterialTheme.typography.labelSmall, color = Color.Red)
                                Text(text = "Waiting for ESP32...", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHenScreen(navController: NavController, viewModel: PoultryViewModel) {
    var hardwareId by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Register Hardware") }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Settings, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Step 1: Add Hen Manually", fontWeight = FontWeight.Bold)
            Text(text = "Enter the hardware ID printed on your ESP32 module.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(value = hardwareId, onValueChange = { hardwareId = it }, label = { Text("ESP32 Hardware ID") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Hen Age (Days)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { if (hardwareId.isNotEmpty() && age.isNotEmpty()) { viewModel.addHen(hardwareId, age.toIntOrNull() ?: 10); navController.popBackStack() } },
                modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp)
            ) { Text("REGISTER & AWAIT CONNECTION") }
        }
    }
}

// Analytics and other components
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TempDetailScreen(navController: NavController, viewModel: PoultryViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = { TopAppBar(title = { Text("Ambient History") }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState())) {
            MetricDetailHeader(title = "Live Ambient Temperature", value = "${uiState.ambientTemp}°C", icon = Icons.Default.Favorite, color = Color(0xFFFF9800))
            Spacer(modifier = Modifier.height(24.dp))
            TemperatureGraph(history = uiState.temperatureHistory, modifier = Modifier.fillMaxWidth().height(250.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HumidityDetailScreen(navController: NavController, viewModel: PoultryViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = { TopAppBar(title = { Text("RH Analytics") }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            MetricDetailHeader(title = "Relative Humidity", value = "${uiState.humidity}%", icon = Icons.Default.DateRange, color = Color(0xFF2196F3))
            Spacer(modifier = Modifier.height(32.dp))
            LinearProgressIndicator(progress = { uiState.humidity / 100f }, modifier = Modifier.fillMaxWidth().height(24.dp).clip(RoundedCornerShape(12.dp)))
        }
    }
}

@Composable
fun MetricDetailHeader(title: String, value: String, icon: ImageVector, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = CircleShape, color = color.copy(alpha = 0.1f), modifier = Modifier.size(64.dp)) { Icon(imageVector = icon, null, tint = color, modifier = Modifier.padding(16.dp)) }
        Spacer(modifier = Modifier.width(16.dp))
        Column { Text(text = title, style = MaterialTheme.typography.titleMedium, color = Color.Gray); Text(text = value, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.ExtraBold) }
    }
}

@Composable
fun MetricCard(title: String, value: String, label: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    ElevatedCard(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = color.copy(alpha = 0.1f), modifier = Modifier.size(32.dp)) { Icon(imageVector = icon, null, tint = color, modifier = Modifier.padding(6.dp).size(16.dp)) }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Composable
fun TemperatureGraph(history: List<Double>, modifier: Modifier = Modifier) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    Canvas(modifier = modifier) {
        if (history.size < 2) return@Canvas
        val maxTemp = 41.0; val minTemp = 34.0; val range = maxTemp - minTemp
        val width = size.width; val height = size.height; val stepX = width / (history.size - 1)
        val points = history.mapIndexed { index, temp -> Offset(index * stepX, height - (((temp - minTemp) / range) * height).toFloat()) }
        val path = Path().apply { moveTo(points[0].x, points[0].y); points.forEach { lineTo(it.x, it.y) } }
        drawPath(path = path, brush = Brush.verticalGradient(listOf(primaryColor.copy(alpha = 0.2f), Color.Transparent)), alpha = 0.5f)
        drawPath(path = path, brush = Brush.horizontalGradient(listOf(primaryColor, secondaryColor)), style = Stroke(width = 3.dp.toPx()))
    }
}
