package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.db.AuraDatabase
import com.example.data.repository.FinanceRepositoryImpl
import com.example.ui.navigation.AuraMainApp
import com.example.viewmodel.AuraViewModel
import com.example.viewmodel.AuraViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: AuraViewModel by viewModels {
        val database = AuraDatabase.getDatabase(applicationContext)
        val repository = FinanceRepositoryImpl(database, applicationContext)
        AuraViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AuraMainApp(viewModel = viewModel)
        }
    }
}
