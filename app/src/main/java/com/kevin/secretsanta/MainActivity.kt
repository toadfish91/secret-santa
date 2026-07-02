package com.kevin.secretsanta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kevin.secretsanta.data.SecretSantaDatabase
import com.kevin.secretsanta.data.SecretSantaRepository
import com.kevin.secretsanta.ui.SecretSantaApp
import com.kevin.secretsanta.ui.ViewModelFactory
import com.kevin.secretsanta.ui.theme.SecretSantaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = SecretSantaDatabase.getInstance(applicationContext)
        val repository = SecretSantaRepository(db)
        val factory = ViewModelFactory(repository)

        setContent {
            SecretSantaTheme {
                SecretSantaApp(factory = factory)
            }
        }
    }
}
