package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.theme.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

enum class Screen { Dashboard, History, Add, Stats, Profile }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                MainContainer()
            }
        }
    }
}

@Composable
fun MainContainer() {
    val viewModel: FinanceViewModel = viewModel()
    var currentScreen by remember { mutableStateOf(Screen.Dashboard) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                NavigationBarItem(
                    selected = currentScreen == Screen.Dashboard,
                    onClick = { currentScreen = Screen.Dashboard },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.History,
                    onClick = { currentScreen = Screen.History },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, null) },
                    label = { Text("Riwayat") }
                )
                FloatingActionButton(
                    onClick = { currentScreen = Screen.Add },
                    containerColor = PastelMint,
                    contentColor = TextDark,
                    shape = CircleShape,
                    modifier = Modifier.offset(y = (-10).dp)
                ) {
                    Icon(Icons.Default.Add, "Tambah")
                }
                NavigationBarItem(
                    selected = currentScreen == Screen.Stats,
                    onClick = { currentScreen = Screen.Stats },
                    icon = { Icon(Icons.Default.Info, null) },
                    label = { Text("Statistik") }
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.Profile,
                    onClick = { currentScreen = Screen.Profile },
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text("Profil") }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize().background(SoftGray)) {
            Crossfade(targetState = currentScreen, animationSpec = tween(500), label = "screen_fade") { screen ->
                when (screen) {
                    Screen.Dashboard -> DashboardScreen(viewModel)
                    Screen.History -> HistoryScreen(viewModel) { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
                    Screen.Add -> AddTransactionScreen(viewModel, onBack = { currentScreen = Screen.Dashboard }, onSuccess = { msg ->
                        currentScreen = Screen.Dashboard
                        scope.launch { snackbarHostState.showSnackbar(msg) }
                    })
                    Screen.Stats -> StatisticsScreen(viewModel)
                    Screen.Profile -> ProfileScreen()
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(viewModel: FinanceViewModel) {
    val totalSaldo by viewModel.totalSaldo.collectAsState()
    val totalTabungan by viewModel.totalTabungan.collectAsState()
    val stats by viewModel.dashboardStats.collectAsState()
    val goals by viewModel.savingGoals.collectAsState()

    var showGoalDialog by remember { mutableStateOf(false) }
    var showKantongDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Column {
            Text("Halo, Rahma! 👋", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
            Text("Ayo cek kesehatan keuanganmu hari ini.", fontSize = 14.sp, color = Color.Gray)
        }

        // Card Ringkasan Utama
        Card(modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(32.dp)), shape = RoundedCornerShape(32.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.background(PastelLavender.copy(alpha = 0.4f)).padding(28.dp)) {
                Text("Total Saldo Kamu", fontSize = 14.sp, color = TextDark.copy(0.6f))
                Text(text = viewModel.formatRupiah(totalSaldo), fontSize = 32.sp, fontWeight = FontWeight.Black, color = TextDark)
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Row Hari Ini
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FinanceSummaryBox("Masuk Hari Ini", viewModel.formatRupiah(stats.todayIn), PastelMint, Modifier.weight(1f))
                    FinanceSummaryBox("Keluar Hari Ini", viewModel.formatRupiah(stats.todayOut), PastelPink, Modifier.weight(1f))
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Row Bulan Ini
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FinanceSummaryBox("Masuk Bulan Ini", viewModel.formatRupiah(stats.monthIn), PastelYellow, Modifier.weight(1f))
                    FinanceSummaryBox("Keluar Bulan Ini", viewModel.formatRupiah(stats.monthOut), PastelOrange, Modifier.weight(1f))
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                FinanceSummaryBox("Total Tabungan", viewModel.formatRupiah(totalTabungan), PastelBlue, Modifier.fillMaxWidth())
            }
        }

        // BAGIAN KANTONG
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Kantong Saya 👛", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextDark)
                IconButton(onClick = { showKantongDialog = true }) {
                    Icon(Icons.Default.AddCircle, "Tambah Saldo", tint = PastelMint)
                }
            }
            
            Card(
                modifier = Modifier.fillMaxWidth().clickable { showKantongDialog = true },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).background(PastelMint.copy(0.2f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.ShoppingCart, null, tint = PastelMint)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Saldo Awal / Kantong", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Klik untuk input atau tambah saldo", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }

        // BAGIAN TARGET
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Target Menabung 🎯", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextDark)
            TextButton(onClick = { showGoalDialog = true }) { Text("+ Tambah Target") }
        }

        if (goals.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth().clickable { showGoalDialog = true }, shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Belum ada target. Yuk buat satu!", color = Color.Gray)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                goals.forEach { goal ->
                    GoalCard(viewModel, goal)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
    }

    if (showGoalDialog) {
        AddGoalDialog(viewModel) { showGoalDialog = false }
    }
    
    if (showKantongDialog) {
        KantongDialog(viewModel) { showKantongDialog = false }
    }
}

@Composable
fun KantongDialog(viewModel: FinanceViewModel, onDismiss: () -> Unit) {
    var amount by remember { mutableStateOf("") }
    val sdf = remember { java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")) }
    val currentDateTime = remember { sdf.format(java.util.Date()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Isi Saldo Kantong 👛", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Waktu Otomatis: $currentDateTime", fontSize = 12.sp, color = Color.Gray)
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.all { c -> c.isDigit() }) amount = it },
                    label = { Text("Nominal Saldo (Rp)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Batal") }
                    Button(onClick = {
                        if (amount.isNotEmpty()) {
                            viewModel.addTransaction(
                                title = "Input Saldo Kantong",
                                type = "Pemasukan",
                                amount = amount.toLongOrNull() ?: 0L,
                                emoji = "👛",
                                color = PastelMint
                            )
                            onDismiss()
                        }
                    }, colors = ButtonDefaults.buttonColors(containerColor = PastelMint)) {
                        Text("Simpan", color = TextDark)
                    }
                }
            }
        }
    }
}

@Composable
fun GoalCard(viewModel: FinanceViewModel, goal: SavingGoalEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(50.dp).background(PastelCream, CircleShape), contentAlignment = Alignment.Center) {
                Text("💰", fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(goal.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                val progress = if (goal.targetAmount > 0) (goal.currentAmount.toFloat() / goal.targetAmount.toFloat()) else 0f
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).height(8.dp).clip(CircleShape),
                    color = PastelOrange,
                    trackColor = SoftGray
                )
                Text("${viewModel.formatRupiah(goal.currentAmount)} / ${viewModel.formatRupiah(goal.targetAmount)}", fontSize = 12.sp, color = Color.Gray)
            }
            IconButton(onClick = { viewModel.deleteSavingGoal(goal) }) {
                Icon(Icons.Default.Delete, null, tint = Color.LightGray)
            }
        }
    }
}

@Composable
fun AddGoalDialog(viewModel: FinanceViewModel, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Tambah Target Baru 🎯", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Target") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = targetAmount, onValueChange = { if (it.all { c -> c.isDigit() }) targetAmount = it }, label = { Text("Nominal Target (Rp)") }, modifier = Modifier.fillMaxWidth())

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Batal") }
                    Button(onClick = {
                        if (name.isNotEmpty() && targetAmount.isNotEmpty()) {
                            viewModel.addSavingGoal(name, targetAmount.toLongOrNull() ?: 0L, 0L)
                            onDismiss()
                        }
                    }, colors = ButtonDefaults.buttonColors(containerColor = PastelMint)) {
                        Text("Simpan", color = TextDark)
                    }
                }
            }
        }
    }
}

@Composable
fun FinanceSummaryBox(label: String, amount: String, color: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = Color.White, shape = RoundedCornerShape(20.dp), shadowElevation = 2.dp) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
                Spacer(modifier = Modifier.width(6.dp))
                Text(label, fontSize = 11.sp, color = Color.Gray)
            }
            Text(amount, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionScreen(viewModel: FinanceViewModel, onBack: () -> Unit, onSuccess: (String) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var selectedGoalId by remember { mutableStateOf<Int?>(null) }
    var type by remember { mutableStateOf("Pengeluaran") }
    var selectedCategory by remember { mutableStateOf("Makanan dan Minuman") }
    
    val sdf = remember { java.text.SimpleDateFormat("HH:mm", Locale("id", "ID")) }
    val currentTime = remember { sdf.format(java.util.Date()) }

    val goals by viewModel.savingGoals.collectAsState(initial = emptyList())

    val expenseCategories = listOf(
        "Acara Sosial", "Belanja", "Cicilan", "Hiburan", "Keluarga", 
        "Kesehatan", "Makanan dan Minuman", "Pendidikan", "Tabungan", 
        "Tagihan", "Top Up", "Transportasi", "Lainnya"
    )

    Column(modifier = Modifier.fillMaxSize().background(Color.White).padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.background(SoftGray, CircleShape)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Tambah Transaksi", fontSize = 22.sp, fontWeight = FontWeight.Black)
        }

        // Selector Jenis Transaksi
        Row(modifier = Modifier.fillMaxWidth().background(SoftGray, RoundedCornerShape(16.dp)).padding(4.dp)) {
            listOf("Pengeluaran", "Tabungan").forEach { item ->
                val isSelected = type == item
                val bgColor = if (item == "Pengeluaran") PastelPink else PastelBlue
                Box(modifier = Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(12.dp)).background(if (isSelected) bgColor else Color.Transparent).clickable { 
                    type = item 
                    if (item == "Tabungan") selectedCategory = "Tabungan"
                }, contentAlignment = Alignment.Center) {
                    Text(item, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp)
                }
            }
        }

        // Poin Waktu Otomatis
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SoftGray.copy(0.5f))) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, null, tint = Color.Gray)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Waktu Input: $currentTime WIB", fontWeight = FontWeight.Medium, color = Color.DarkGray)
            }
        }
        
        OutlinedTextField(
            value = amount, 
            onValueChange = { if (it.all { c -> c.isDigit() }) amount = it }, 
            label = { Text("Nominal (Rp)") }, 
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
        )

        if (type == "Tabungan") {
            if (goals.isNotEmpty()) {
                Text("Pilih Target Tabungan", fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    goals.forEach { goal ->
                        FilterChip(
                            selected = selectedGoalId == goal.id,
                            onClick = { selectedGoalId = goal.id },
                            label = { Text(goal.name) }
                        )
                    }
                }
            } else {
                Text("Belum ada target tabungan. Buat di Dashboard dulu ya!", color = Color.Red, fontSize = 12.sp)
            }
        } else {
            Text("Pilih Kategori", fontWeight = FontWeight.Bold)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                expenseCategories.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PastelPink.copy(0.3f))
                    )
                }
            }
        }

        Button(onClick = {
            if (amount.isNotEmpty()) {
                if (type == "Tabungan" && selectedGoalId == null) return@Button
                
                viewModel.addTransaction(
                    title = if (type == "Tabungan") "Tabungan" else selectedCategory,
                    type = type,
                    amount = amount.toLong(),
                    emoji = if (type == "Tabungan") "💰" else "💸",
                    color = if (type == "Tabungan") PastelBlue else PastelPink,
                    savingGoalId = if (type == "Tabungan") selectedGoalId else null,
                    savingAmount = if (type == "Tabungan") amount.toLong() else 0L
                )
                onSuccess("Berhasil dicatat!")
            }
        }, modifier = Modifier.fillMaxWidth().height(60.dp), shape = RoundedCornerShape(20.dp), colors = ButtonDefaults.buttonColors(containerColor = if (type == "Tabungan") PastelBlue else PastelPink)) {
            Text("Simpan ${type}", color = TextDark, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun HistoryScreen(viewModel: FinanceViewModel, onDeleted: (String) -> Unit) {
    val transactions by viewModel.transactions.collectAsState(initial = emptyList())
    var filterType by remember { mutableStateOf("Semua") }

    val filteredTransactions = when (filterType) {
        "Masuk" -> transactions.filter { it.jenisTransaksi == "Pemasukan" }
        "Keluar" -> transactions.filter { it.jenisTransaksi == "Pengeluaran" }
        "Tabungan" -> transactions.filter { it.jenisTransaksi == "Tabungan" }
        else -> transactions
    }

    Column(modifier = Modifier.fillMaxSize().background(SoftGray).padding(horizontal = 24.dp)) {
        Spacer(modifier = Modifier.height(32.dp))
        Text("Riwayat Transaksi 📜", fontSize = 28.sp, fontWeight = FontWeight.Black, color = TextDark)
        Text("Pantau aliran uangmu di sini", fontSize = 14.sp, color = Color.Gray)
        
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("Semua", "Masuk", "Keluar", "Tabungan").forEach { tab ->
                val isSelected = filterType == tab
                val bgColor by animateColorAsState(if (isSelected) PastelLavender else Color.White, label = "tab_color")
                Surface(
                    modifier = Modifier.clickable { filterType = tab },
                    shape = RoundedCornerShape(16.dp),
                    color = bgColor,
                    shadowElevation = if (isSelected) 4.dp else 1.dp
                ) {
                    Text(
                        text = tab,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp,
                        color = TextDark
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (filteredTransactions.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏜️", fontSize = 72.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Belum ada riwayat nih.", color = Color.Gray, fontWeight = FontWeight.Medium)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
            ) {
                items(filteredTransactions) { tx ->
                    TransactionItem(viewModel, tx) { 
                        viewModel.deleteTransaction(tx)
                        onDeleted("Berhasil dihapus!") 
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(viewModel: FinanceViewModel, tx: TransactionEntity, onDelete: () -> Unit) {
    val txColor = when (tx.jenisTransaksi) {
        "Pemasukan" -> PastelMint
        "Pengeluaran" -> PastelPink
        else -> PastelBlue
    }

    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(54.dp).background(txColor.copy(alpha = 0.25f), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(tx.emoji, fontSize = 26.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(tx.kategori, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextDark)
                Text("${tx.jenisTransaksi} • ${tx.tanggal}", fontSize = 12.sp, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                val symbol = if (tx.jenisTransaksi == "Pemasukan") "+" else "-"
                val amountColor = if (tx.jenisTransaksi == "Pemasukan") Color(0xFF43A047) else if (tx.jenisTransaksi == "Tabungan") Color(0xFF1E88E5) else Color(0xFFE53935)
                
                Text(
                    text = "$symbol${viewModel.formatRupiah(tx.nominal)}",
                    color = amountColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
                
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp).padding(top = 4.dp)) {
                    Icon(Icons.Default.Delete, null, tint = Color.LightGray.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun StatisticsScreen(viewModel: FinanceViewModel) {
    val totalMasuk by viewModel.totalPemasukan.collectAsState(initial = 0L)
    val totalKeluar by viewModel.totalPengeluaran.collectAsState(initial = 0L)
    val totalTabungan by viewModel.totalTabungan.collectAsState(initial = 0L)
    val totalSaldo by viewModel.totalSaldo.collectAsState(initial = 0L)
    val categoryStats by viewModel.categoryStats.collectAsState(initial = emptyMap())
    
    val totalAll = totalMasuk + totalKeluar + totalTabungan

    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Statistik Keuangan 📊", fontSize = 26.sp, fontWeight = FontWeight.Black, color = TextDark)
        Text("Ringkasan aktivitas keuanganmu", fontSize = 14.sp, color = Color.Gray)
        
        Spacer(modifier = Modifier.height(32.dp))

        if (totalAll > 0) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Distribusi Pengeluaran", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    DonutChart(categoryStats)
                }
            }
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(28.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                StatItem("Total Pemasukan", "📈", totalMasuk, totalAll, PastelMint)
                StatItem("Total Pengeluaran", "📉", totalKeluar, totalAll, PastelPink)
                StatItem("Total Tabungan", "💰", totalTabungan, totalAll, PastelBlue)
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = SoftGray)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Saldo Saat Ini", fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text(viewModel.formatRupiah(totalSaldo), fontWeight = FontWeight.Black, color = TextDark, fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun DonutChart(data: Map<String, Double>) {
    if (data.isEmpty()) {
        Text("Tidak ada data kategori", color = Color.Gray, fontSize = 12.sp)
        return
    }

    val colors = listOf(PastelPink, PastelBlue, PastelYellow, PastelMint, PastelLavender, PastelOrange, PastelCream)
    
    Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            var startAngle = -90f
            data.values.forEachIndexed { index, value ->
                val sweepAngle = (value.toFloat() / 100f) * 360f
                drawArc(
                    color = colors[index % colors.size],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = 40.dp.toPx(), cap = StrokeCap.Butt)
                )
                startAngle += sweepAngle
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Pengeluaran", fontSize = 12.sp, color = Color.Gray)
            Text("Kategori", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        data.keys.forEachIndexed { index, category ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                Box(modifier = Modifier.size(8.dp).background(colors[index % colors.size], CircleShape))
                Spacer(modifier = Modifier.width(4.dp))
                Text(category, fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun StatItem(label: String, icon: String, amount: Long, total: Long, color: Color) {
    val progressTarget = if (total > 0) amount.toFloat() / total.toFloat() else 0f
    val animatedProgress by animateFloatAsState(targetValue = progressTarget, animationSpec = tween(durationMillis = 1000), label = "progress")
    val percent = (progressTarget * 100).toInt()
    
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(36.dp).background(color.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                    Text(icon, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(label, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
            }
            Text("$percent%", fontWeight = FontWeight.Black, color = color, fontSize = 16.sp)
        }
        
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxWidth().height(14.dp).clip(CircleShape),
            color = color,
            trackColor = SoftGray
        )
        
        val format = NumberFormat.getInstance(Locale.forLanguageTag("id-ID"))
        Text(
            text = "Total: Rp ${format.format(amount)}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(110.dp).background(PastelBlue, CircleShape), contentAlignment = Alignment.Center) { Text("👩‍🎓", fontSize = 56.sp) }
        Spacer(modifier = Modifier.height(20.dp))
        Text("Rahma", fontSize = 24.sp, fontWeight = FontWeight.Black)
        Text("Mahasiswa Teknik Informatika", fontSize = 14.sp, color = Color.Gray)
        
        Spacer(modifier = Modifier.height(40.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Akun Saya", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                ProfileMenuRow(Icons.Default.Star, "Beri Penilaian dan Ulasan") {
                    Toast.makeText(context, "Terima kasih atas penilaiannya!", Toast.LENGTH_SHORT).show()
                }
                ProfileMenuRow(Icons.Default.Email, "Masukan dan Saran") {
                    Toast.makeText(context, "Masukan Anda sangat berharga bagi kami.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        Text("Versi Aplikasi 1.2.0 • Dibuat dengan ❤️", fontSize = 11.sp, color = Color.LightGray)
    }
}

@Composable
fun ProfileMenuRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color = TextDark, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, modifier = Modifier.weight(1f), color = color, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, modifier = Modifier.weight(1f), color = Color.Gray, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Bold, color = TextDark, fontSize = 14.sp)
    }
}
