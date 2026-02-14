package com.example.myapplication

import androidx.room.Entity
import androidx.room.PrimaryKey

// 1. Entity: Representasi tabel "transaksi" di database
@Entity(tableName = "transaksi")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nominal: Long,
    val jenisTransaksi: String, // "Pemasukan" atau "Pengeluaran"
    val kategori: String,
    val tanggal: String,
    val catatan: String,
    val emoji: String,
    val colorHex: String // Simpan warna dalam format Hex String
)
