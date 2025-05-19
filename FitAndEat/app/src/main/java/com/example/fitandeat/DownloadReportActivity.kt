package com.example.fitandeat

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream

class DownloadReportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download_report)

        val downloadButton = findViewById<Button>(R.id.downloadButton)

        downloadButton.setOnClickListener {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            } else {
                generatePdfReport()
            }
        }
    }

    private fun generatePdfReport() {
        try {
            val document = Document()
            val path = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val file = File(path, "Reporte_FitAndEat.pdf")
            PdfWriter.getInstance(document, FileOutputStream(file))
            document.open()
            document.add(Paragraph("Historial de progreso Fit&Eat"))
            document.add(Paragraph("Fecha: ${java.util.Date()}"))
            document.close()

            Toast.makeText(this, "Reporte descargado en: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al generar PDF", Toast.LENGTH_SHORT).show()
        }
    }
}