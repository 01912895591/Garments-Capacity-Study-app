package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.data.database.AppDatabase
import com.example.data.repository.CapacityStudyRepository
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CapacityStudyViewModel
import com.example.ui.viewmodel.CapacityStudyViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Room local database and repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = CapacityStudyRepository(database.capacityStudyDao())
        
        // Instantiate the Shared Capacity ViewModel
        val viewModel: CapacityStudyViewModel by viewModels {
            CapacityStudyViewModelFactory(repository)
        }

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
