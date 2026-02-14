package com.example.myapplication

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.ui.theme.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

data class DashboardStats(
    val todayIn: Long = 0,
    val todayOut: Long = 0,
    val monthIn: Long = 0,
    val monthOut: Long = 0
)

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
    
    private val db = AppDatabase.getDatabase(application)
    private val dao = db.transactionDao()
    private val goalDao = db.savingGoalDao()

    val transactions: StateFlow<List<TransactionEntity>> = dao.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboardStats: StateFlow<DashboardStats> = transactions.map { list ->
        val sdfFull = SimpleDateFormat("dd MMM", Locale("id", "ID"))
        val sdfMonth = SimpleDateFormat("MMM", Locale("id", "ID"))
        val now = Date()
        val todayStr = sdfFull.format(now)
        val monthStr = sdfMonth.format(now)

        DashboardStats(
            todayIn = list.filter { it.jenisTransaksi == "Pemasukan" && it.tanggal.contains(todayStr) }.sumOf { it.nominal },
            todayOut = list.filter { it.jenisTransaksi == "Pengeluaran" && it.tanggal.contains(todayStr) }.sumOf { it.nominal },
            monthIn = list.filter { it.jenisTransaksi == "Pemasukan" && it.tanggal.contains(monthStr) }.sumOf { it.nominal },
            monthOut = list.filter { it.jenisTransaksi == "Pengeluaran" && it.tanggal.contains(monthStr) }.sumOf { it.nominal }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    val totalPemasukan: StateFlow<Long> = dao.getTotalPemasukan()
        .map { it ?: 0L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalPengeluaran: StateFlow<Long> = dao.getTotalPengeluaran()
        .map { it ?: 0L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalTabungan: StateFlow<Long> = dao.getTotalTabungan()
        .map { it ?: 0L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalSaldo: StateFlow<Long> = dao.getAllTransactions()
        .map { list -> calculateSaldo(list) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val categoryStats: StateFlow<Map<String, Double>> = dao.getAllTransactions()
        .map { list ->
            val totalKeluar = list.filter { it.jenisTransaksi == "Pengeluaran" }.sumOf { it.nominal }.toDouble()
            if (totalKeluar == 0.0) emptyMap()
            else {
                list.filter { it.jenisTransaksi == "Pengeluaran" }
                    .groupBy { it.kategori }
                    .mapValues { (_, txs) -> 
                        (txs.sumOf { it.nominal }.toDouble() / totalKeluar) * 100
                    }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // --- LOGIKA TARGET TABUNGAN (BISA BANYAK) ---
    val savingGoals: StateFlow<List<SavingGoalEntity>> = goalDao.getAllGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSavingGoal(name: String, target: Long, current: Long = 0L) {
        viewModelScope.launch {
            goalDao.insertGoal(SavingGoalEntity(name = name, targetAmount = target, currentAmount = current))
        }
    }

    fun deleteSavingGoal(goal: SavingGoalEntity) {
        viewModelScope.launch {
            goalDao.deleteGoal(goal)
        }
    }

    fun calculateSaldo(list: List<TransactionEntity>): Long {
        val masuk = list.filter { it.jenisTransaksi == "Pemasukan" }.sumOf { it.nominal }
        val keluar = list.filter { it.jenisTransaksi == "Pengeluaran" }.sumOf { it.nominal }
        val tabungan = list.filter { it.jenisTransaksi == "Tabungan" }.sumOf { it.nominal }
        return masuk - keluar - tabungan
    }

    fun addTransaction(title: String, type: String, amount: Long, emoji: String, color: Color, savingGoalId: Int? = null, savingAmount: Long = 0L) {
        viewModelScope.launch {
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
            val currentTanggal = sdf.format(Date())
            
            val entity = TransactionEntity(
                nominal = amount,
                jenisTransaksi = type,
                kategori = title,
                tanggal = currentTanggal,
                catatan = "",
                emoji = emoji,
                colorHex = String.format("#%06X", 0xFFFFFF and color.toArgb())
            )
            dao.insertTransaction(entity)
            
            // Jika transaksi adalah "Tabungan", update juga progress goal nya
            if (type == "Tabungan" && savingGoalId != null) {
                val goals = savingGoals.value
                val targetGoal = goals.find { it.id == savingGoalId }
                if (targetGoal != null) {
                    goalDao.insertGoal(targetGoal.copy(currentAmount = targetGoal.currentAmount + amount))
                }
            }
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            dao.deleteTransaction(transaction)
        }
    }

    fun formatRupiah(amount: Long): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return format.format(amount).replace("Rp", "Rp ")
    }
}
