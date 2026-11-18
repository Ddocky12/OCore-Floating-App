package com.ocore.nextgen

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.DataOutputStream

class ControlPanelActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 50, 50, 50)
            setBackgroundColor(Color.parseColor("#121212"))
        }

        val title = TextView(this).apply {
            text = "OCore Control Panel"
            textSize = 24f
            setTextColor(Color.WHITE)
            setPadding(0, 0, 0, 40)
        }
        layout.addView(title)

        // Tombol Mode Performa
        layout.addView(createButton("Mode Performance") {
            runRootCommand("/data/adb/modules/ocore-nextgen/backends/cpu_backend.sh --apply performance")
        })

        // Tombol Mode Balance
        layout.addView(createButton("Mode Balance") {
            runRootCommand("/data/adb/modules/ocore-nextgen/backends/cpu_backend.sh --apply balance")
        })

        // Tombol Mode Power Save
        layout.addView(createButton("Mode Power Save (Eco)") {
            runRootCommand("/data/adb/modules/ocore-nextgen/backends/cpu_backend.sh --apply powersave")
        })
        
        // Tombol Tutup Panel
        layout.addView(createButton("Tutup Panel") {
            finish()
        })

        setContentView(layout)
    }

    private fun createButton(text: String, onClick: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            setOnClickListener { onClick() }
        }
    }

    private fun runRootCommand(command: String) {
        try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            os.writeBytes("$command\n")
            os.writeBytes("exit\n")
            os.flush()
            process.waitFor()
            Toast.makeText(this, "Perintah dikirim!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal mendapatkan akses Root", Toast.LENGTH_SHORT).show()
        }
    }
}
