package com.example.myapplication

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transaksi ORDER BY id DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("SELECT SUM(nominal) FROM transaksi WHERE jenisTransaksi = 'Pemasukan'")
    fun getTotalPemasukan(): Flow<Long?>

    @Query("SELECT SUM(nominal) FROM transaksi WHERE jenisTransaksi = 'Pengeluaran'")
    fun getTotalPengeluaran(): Flow<Long?>

    @Query("SELECT SUM(nominal) FROM transaksi WHERE jenisTransaksi = 'Tabungan'")
    fun getTotalTabungan(): Flow<Long?>
}
