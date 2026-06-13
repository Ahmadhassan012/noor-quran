package com.example

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.ui.ConversationViewModel
import com.example.ui.NoorAppUi
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  
  private val viewModel: ConversationViewModel by viewModels()

  private val requestMicPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { isGranted: Boolean ->
    if (!isGranted) {
      Toast.makeText(
        this,
        "Noor relies on microphone permission to parse voice commands.",
        Toast.LENGTH_LONG
      ).show()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // Proactively launch permission check for convenience
    requestMicPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)

    setContent {
      MyApplicationTheme {
        NoorAppUi(viewModel = viewModel)
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    viewModel.audioPlayer.release()
  }
}

