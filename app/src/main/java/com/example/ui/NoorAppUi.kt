package com.example.ui

import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoorAppUi(viewModel: ConversationViewModel) {
  val currentScreen by viewModel.currentScreen.collectAsState()
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()

  ModalNavigationDrawer(
    drawerState = drawerState,
    gesturesEnabled = currentScreen != AppScreen.ONBOARDING,
    drawerContent = {
      if (currentScreen != AppScreen.ONBOARDING) {
        NoorNavigationDrawer(
          currentScreen = currentScreen,
          onScreenSelected = { screen ->
            viewModel.navigateTo(screen)
            scope.launch { drawerState.close() }
          }
        )
      }
    }
  ) {
    Scaffold(
      modifier = Modifier.fillMaxSize(),
      containerColor = BgBase
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        AnimatedContent(
          targetState = currentScreen,
          transitionSpec = {
            slideInVertically { height -> height } + fadeIn() togetherWith
                slideOutVertically { height -> -height } + fadeOut()
          },
          label = "ScreenTransition"
        ) { screen ->
          when (screen) {
            AppScreen.ONBOARDING -> OnboardingScreen(viewModel)
            AppScreen.HOME -> HomeScreen(viewModel, onOpenDrawer = { scope.launch { drawerState.open() } })
            AppScreen.HISTORY -> ConversationHistoryScreen(viewModel, onBack = { viewModel.navigateTo(AppScreen.HOME) })
            AppScreen.BOOKMARKS -> BookmarksScreen(viewModel, onBack = { viewModel.navigateTo(AppScreen.HOME) })
            AppScreen.LISTENING_HISTORY -> ListeningHistoryScreen(viewModel, onBack = { viewModel.navigateTo(AppScreen.HOME) })
            AppScreen.SURAH_BROWSER -> SurahBrowserScreen(viewModel, onBack = { viewModel.navigateTo(AppScreen.HOME) })
            AppScreen.SETTINGS -> SettingsScreen(viewModel, onBack = { viewModel.navigateTo(AppScreen.HOME) })
          }
        }
      }
    }
  }
}

// 1. NAVIGATION DRAWER

@Composable
fun NoorNavigationDrawer(
  currentScreen: AppScreen,
  onScreenSelected: (AppScreen) -> Unit
) {
  ModalDrawerSheet(
    drawerContainerColor = BgSurface,
    drawerContentColor = TextPrimary,
    modifier = Modifier.width(300.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(24.dp)
    ) {
      // Header brand
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 24.dp)
      ) {
        Box(
          modifier = Modifier
            .size(48.dp)
            .background(AccentGlow, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Text("نور", color = Accent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
          Text(
            text = "NOOR",
            style = Typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "AI Quran Companion",
            style = Typography.labelSmall,
            color = TextSecondary
          )
        }
      }

      HorizontalDivider(color = TextTertiary.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

      val menuItems = listOf(
        DrawerItem(AppScreen.HOME, "Home", Icons.Filled.Home),
        DrawerItem(AppScreen.SURAH_BROWSER, "Quran Navigator", Icons.Filled.MenuBook),
        DrawerItem(AppScreen.HISTORY, "Conversations", Icons.Filled.History),
        DrawerItem(AppScreen.BOOKMARKS, "Bookmarks", Icons.Filled.Bookmark),
        DrawerItem(AppScreen.LISTENING_HISTORY, "Listening History", Icons.Filled.PlayArrow),
        DrawerItem(AppScreen.SETTINGS, "Settings", Icons.Filled.Settings)
      )

      menuItems.forEach { item ->
        val selected = currentScreen == item.screen
        NavigationDrawerItem(
          icon = { Icon(item.icon, contentDescription = null, tint = if (selected) BgBase else TextSecondary) },
          label = { Text(item.title, style = Typography.bodyLarge, color = if (selected) BgBase else TextPrimary) },
          selected = selected,
          onClick = { onScreenSelected(item.screen) },
          colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = Accent,
            unselectedContainerColor = Color.Transparent
          ),
          modifier = Modifier
            .padding(vertical = 4.dp)
            .testTag("drawer_item_${item.title.lowercase()}")
        )
      }

      Spacer(modifier = Modifier.weight(1f))
      Text("v1.0.0 (Beta)", color = TextTertiary, style = Typography.labelSmall)
    }
  }
}

data class DrawerItem(val screen: AppScreen, val title: String, val icon: ImageVector)

// 2. ONBOARDING SCREEN

@Composable
fun OnboardingScreen(viewModel: ConversationViewModel) {
  val step by viewModel.onboardingStep.collectAsState()
  val userLang by viewModel.userLanguage.collectAsState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(BgBase)
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    // Top Progress Line
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      for (i in 1..3) {
        Box(
          modifier = Modifier
            .weight(1f)
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(if (i <= step) Accent else TextTertiary.copy(alpha = 0.3f))
        )
      }
    }

    // Changing content per step
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.Center
    ) {
      when (step) {
        1 -> {
          // Decorative Orb
          Box(
            modifier = Modifier
              .size(160.dp)
              .background(AccentGlow, CircleShape)
              .blur(20.dp),
            contentAlignment = Alignment.Center
          ) {
            Box(
              modifier = Modifier
                .size(100.dp)
                .background(Accent, CircleShape)
            )
          }

          Spacer(modifier = Modifier.height(32.dp))
          Text(
            text = "Welcome to Noor",
            style = Typography.displayLarge,
            color = TextPrimary,
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = "A voice-first Quran companion designed for sensory accessibility.\nYou speak, Noor recites.",
            style = Typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
          )
        }
        2 -> {
          Box(
            modifier = Modifier
              .size(96.dp)
              .background(AccentGlow, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Filled.Mic, contentDescription = null, tint = Accent, modifier = Modifier.size(48.dp))
          }
          Spacer(modifier = Modifier.height(32.dp))
          Text(
            text = "Microphone Access",
            style = Typography.headlineMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = "Noor requires microphone access to hear and understand your voice commands offline.",
            style = Typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(24.dp))
          Text(
            text = "Rest assured: No audio is recorded or sent to any cloud server. Everything remains on your device.",
            style = Typography.bodyMedium,
            color = TextTertiary,
            textAlign = TextAlign.Center
          )
        }
        3 -> {
          Text(
            text = "A Beautiful Connection",
            style = Typography.headlineMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Choose your interaction language",
            style = Typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(32.dp))

          val languages = listOf("English", "Urdu (اردو)", "Arabic (عربي)")
          languages.forEach { lang ->
            val isSelected = userLang.startsWith(lang.split(" ")[0])
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .background(if (isSelected) AccentGlow else BgSurface, RoundedCornerShape(16.dp))
                .clickable { viewModel.setLanguage(lang.split(" ")[0]) }
                .padding(20.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(lang, style = Typography.titleLarge, color = if (isSelected) Accent else TextPrimary)
              if (isSelected) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Accent)
              }
            }
          }
        }
      }
    }

    // Bottom Navigation Elements
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (step > 1) {
        TextButton(onClick = { viewModel.nextOnboardingStep() }) {
          Text("Skip", color = TextSecondary, style = Typography.bodyLarge)
        }
      } else {
        Spacer(modifier = Modifier.width(48.dp))
      }

      Button(
        onClick = { viewModel.nextOnboardingStep() },
        colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = BgBase),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
          .height(56.dp)
          .width(160.dp)
          .testTag("onboarding_next")
      ) {
        Text(
          text = if (step == 3) "Start Companion" else "Get Started",
          style = Typography.bodyLarge,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}

// 3. HOME / CONVERSATION SCREEN

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(viewModel: ConversationViewModel, onOpenDrawer: () -> Unit) {
  val orbState by viewModel.orbState.collectAsState()
  val rawText by viewModel.speechPipeline.recognizedText.collectAsState()
  val transText by viewModel.speechPipeline.translatedText.collectAsState()
  val userLang by viewModel.userLanguage.collectAsState()

  val voiceLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
  ) { result ->
    viewModel.setListening(false)
    val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
    if (!matches.isNullOrEmpty()) {
      viewModel.speechPipeline.processSpokenText(matches[0])
    }
  }

  fun launchVoiceRecognition() {
    viewModel.setListening(true)
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
      putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
      putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
      putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
    }
    try {
      voiceLauncher.launch(intent)
    } catch (e: Exception) {
      viewModel.setListening(false)
    }
  }
  
  // Audio Player states
  val currentSurah by viewModel.audioPlayer.currentSurah.collectAsState()
  val currentVerse by viewModel.audioPlayer.currentVerse.collectAsState()
  val isBuffering by viewModel.audioPlayer.isBuffering.collectAsState()
  val isPlaying by viewModel.audioPlayer.isPlaying.collectAsState()
  val bookmarked by viewModel.isBookmarked.collectAsState()

  // Elegant drift particles animation
  val infiniteTransition = rememberInfiniteTransition(label = "BackgroundParticles")
  val driftY by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 200f,
    animationSpec = infiniteRepeatable(
      animation = tween(12000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "DriftY"
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .drawBehind {
        // Subtle ambient drifting particle stars background
        val particleColor = Accent.copy(alpha = 0.05f)
        for (i in 1..8) {
          val x = (i * 140) % size.width
          val y = ((i * 220) + driftY) % size.height
          drawCircle(
            color = particleColor,
            radius = 3.dp.toPx(),
            center = Offset(x, y)
          )
        }
      }
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    // TOP NAVIGATION HEADER (No drawer on standard visually impaired simple mode, but available for caregivers/sighted use)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onOpenDrawer,
        modifier = Modifier
          .background(BgSurface, CircleShape)
          .size(48.dp)
          .testTag("drawer_button")
      ) {
        Icon(Icons.Filled.Menu, contentDescription = "Menu Drawer", tint = TextPrimary)
      }

      Text(
        text = "نور",
        fontFamily = FontFamily.Serif,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )

      IconButton(
        onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
        modifier = Modifier
          .background(BgSurface, CircleShape)
          .size(48.dp)
          .testTag("settings_button")
      ) {
        Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = TextPrimary)
      }
    }

    // THE ORB DISPLAY CENTERPIECE
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.Center
    ) {
      NoorOrb(orbState = orbState, onClick = {
        if (orbState == OrbState.LISTENING) {
          viewModel.setListening(false)
        } else {
          launchVoiceRecognition()
        }
      })

      Spacer(modifier = Modifier.height(24.dp))

      Text(
        text = when (orbState) {
          OrbState.IDLE -> "Say 'Noor' or tap to speak"
          OrbState.LISTENING -> "LISTENING..."
          OrbState.PROCESSING -> "PROCESSING..."
          OrbState.SPEAKING -> "NOOR SPEAKING..."
          OrbState.PLAYING -> "PLAYING AUDIO..."
        },
        style = Typography.titleLarge,
        color = if (orbState == OrbState.LISTENING) Accent else TextSecondary,
        fontWeight = FontWeight.Bold
      )

      Spacer(modifier = Modifier.height(16.dp))

      var showTextInput by remember { mutableStateOf(false) }
      var typedCommand by remember { mutableStateOf("") }

      if (!showTextInput) {
        Row(
          modifier = Modifier
            .background(BgSurface, RoundedCornerShape(16.dp))
            .border(1.dp, Accent.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .clickable { showTextInput = true }
            .padding(horizontal = 16.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Filled.Keyboard, contentDescription = null, tint = Accent, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Type a command", style = Typography.labelMedium, color = Accent, fontWeight = FontWeight.Bold)
        }
      } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 8.dp)
              .background(BgSurface, RoundedCornerShape(24.dp))
              .border(1.dp, AccentGlow, RoundedCornerShape(24.dp))
              .padding(horizontal = 12.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            TextField(
              value = typedCommand,
              onValueChange = { typedCommand = it },
              placeholder = { Text("Type: 'play surah fatihah' or 'pause'", style = Typography.bodyMedium, color = TextSecondary) },
              modifier = Modifier.weight(1f),
              singleLine = true,
              colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Accent
              ),
              textStyle = Typography.bodyMedium
            )

            IconButton(
              onClick = {
                if (typedCommand.isNotBlank()) {
                  viewModel.speechPipeline.processSpokenText(typedCommand)
                  typedCommand = ""
                }
              },
              enabled = typedCommand.isNotBlank()
            ) {
              Icon(
                imageVector = Icons.Filled.Send,
                contentDescription = "Send Command",
                tint = if (typedCommand.isNotBlank()) Accent else TextTertiary
              )
            }

            IconButton(onClick = { showTextInput = false }) {
              Icon(Icons.Filled.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(18.dp))
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Quick action chips
          val suggestions = listOf(
            "Play Surah Al-Fatihah" to "Al-Fatihah 1",
            "Play Surah Al-Mulk" to "Al-Mulk 67",
            "Play Surah Ya-Sin" to "Ya-Sin 36",
            "pause" to "Pause",
            "resume" to "Resume",
            "bookmark" to "Bookmark"
          )

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Try:", style = Typography.labelMedium, color = TextSecondary)
            androidx.compose.foundation.lazy.LazyRow(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              modifier = Modifier.weight(1f)
            ) {
              items(suggestions.size) { index ->
                val (cmd, label) = suggestions[index]
                Box(
                  modifier = Modifier
                    .background(BgSurface, RoundedCornerShape(12.dp))
                    .border(1.dp, Accent.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .clickable { viewModel.speechPipeline.processSpokenText(cmd) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                  Text(label, style = Typography.labelMedium, color = Accent, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }

      // Live transcript box
      if (rawText.isNotEmpty()) {
        Spacer(modifier = Modifier.height(16.dp))
        Card(
          colors = CardDefaults.cardColors(containerColor = BgSurface),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .border(1.dp, AccentGlow, RoundedCornerShape(16.dp))
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "\"$rawText\"",
              style = Typography.bodyLarge,
              color = TextPrimary,
              textAlign = TextAlign.Center,
              fontWeight = FontWeight.Medium
            )
            if (transText.isNotEmpty() && transText != rawText) {
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Translation: $transText",
                style = Typography.bodyMedium,
                color = Accent,
                textAlign = TextAlign.Center
              )
            }
          }
        }
      }
    }

    // BOTTOM RECITATION CONTROLS (Only visible of play initiated)
    Column(
      modifier = Modifier.fillMaxWidth()
    ) {
      if (currentSurah != null) {
        val s = QuranData.surahs.firstOrNull { it.number == currentSurah }
        if (s != null) {
          Card(
            colors = CardDefaults.cardColors(containerColor = BgSurface),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("now_playing_panel")
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              // Surah details
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                IconButton(
                  onClick = { viewModel.toggleBookmark() },
                  modifier = Modifier.testTag("bookmark_toggle")
                ) {
                  Icon(
                    imageVector = if (bookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    tint = if (bookmarked) Accent else TextSecondary,
                    contentDescription = "Bookmark verse"
                  )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(
                    text = s.englishName,
                    style = Typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "Verse $currentVerse of ${s.verseCount} · Juz ${s.juzStart}",
                    style = Typography.bodyMedium,
                    color = TextSecondary
                  )
                }

                Text(
                  text = s.arabicName,
                  style = ArabicTextStyle,
                  color = TextArabic,
                  fontSize = 24.sp
                )
              }

              Spacer(modifier = Modifier.height(16.dp))

              // Player transport buttons
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
              ) {
                IconButton(
                  onClick = { viewModel.previousVerse() },
                  modifier = Modifier
                    .size(48.dp)
                    .testTag("prev_button")
                ) {
                  Icon(Icons.Filled.SkipPrevious, "Previous Verse", tint = TextPrimary, modifier = Modifier.size(32.dp))
                }

                IconButton(
                  onClick = { viewModel.audioPlayer.seekBackward10s() },
                  modifier = Modifier.size(48.dp)
                ) {
                  Icon(Icons.Filled.Replay10, "Rewind 10s", tint = TextPrimary)
                }

                Box(
                  contentAlignment = Alignment.Center,
                  modifier = Modifier.size(64.dp)
                ) {
                  if (isBuffering) {
                    CircularProgressIndicator(color = Accent, strokeWidth = 3.dp, modifier = Modifier.size(48.dp))
                  } else {
                    IconButton(
                      onClick = { viewModel.togglePlayPause() },
                      modifier = Modifier
                        .background(Accent, CircleShape)
                        .size(56.dp)
                        .testTag("play_pause_button")
                    ) {
                      Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = BgBase,
                        modifier = Modifier.size(32.dp)
                      )
                    }
                  }
                }

                IconButton(
                  onClick = { viewModel.audioPlayer.seekForward10s() },
                  modifier = Modifier.size(48.dp)
                ) {
                  Icon(Icons.Filled.Forward10, "Forward 10s", tint = TextPrimary)
                }

                IconButton(
                  onClick = { viewModel.nextVerse() },
                  modifier = Modifier
                    .size(48.dp)
                    .testTag("next_button")
                ) {
                  Icon(Icons.Filled.SkipNext, "Next Verse", tint = TextPrimary, modifier = Modifier.size(32.dp))
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // MASSIVE PUSH-TO-TALK BUTTON FOR VISUALLY IMPAIRED
      val isListening = orbState == OrbState.LISTENING
      Button(
        onClick = {
          if (isListening) {
            viewModel.setListening(false)
          } else {
            launchVoiceRecognition()
          }
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isListening) Accent else BgSurface,
          contentColor = if (isListening) BgBase else TextPrimary
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(72.dp)
          .testTag("push_to_hold_button")
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = if (isListening) Icons.Filled.Stop else Icons.Filled.Mic,
            contentDescription = null,
            modifier = Modifier.size(28.dp)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Text(
            text = if (isListening) "TAP TO STOP LISTENING" else "TAP TO TALK",
            style = Typography.titleLarge,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
          )
        }
      }
    }
  }
}

// 4. NOOR ORB CUSTOM ANIMATION

@Composable
fun NoorOrb(orbState: OrbState, onClick: () -> Unit) {
  val infiniteTransition = rememberInfiniteTransition(label = "OrbBreathing")
  val view = androidx.compose.ui.platform.LocalView.current

  // Dual phase breathing pulse scale
  val scale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = if (orbState == OrbState.LISTENING) 1.14f else 1.05f,
    animationSpec = infiniteRepeatable(
      animation = tween(if (orbState == OrbState.LISTENING) 1200 else 2400, easing = EaseInOutSine),
      repeatMode = RepeatMode.Reverse
    ),
    label = "OrbScale"
  )

  val scaleCore by infiniteTransition.animateFloat(
    initialValue = 0.96f,
    targetValue = if (orbState == OrbState.LISTENING) 1.06f else 1.02f,
    animationSpec = infiniteRepeatable(
      animation = tween(if (orbState == OrbState.LISTENING) 1500 else 3200, easing = EaseInOutSine),
      repeatMode = RepeatMode.Reverse
    ),
    label = "OrbScaleCore"
  )

  // Wave rotation animation
  val rotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(if (orbState == OrbState.PROCESSING) 2000 else 6000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "OrbRotation"
  )

  val color = when (orbState) {
    OrbState.IDLE -> OrbIdle
    OrbState.LISTENING -> OrbListening
    OrbState.PROCESSING -> OrbProcessing
    OrbState.SPEAKING -> OrbSpeaking
    OrbState.PLAYING -> OrbPlaying
  }

  val pulseGlowRadius = if (orbState == OrbState.LISTENING) 48.dp else 24.dp

  Box(
    contentAlignment = Alignment.Center,
    modifier = Modifier
      .size(200.dp)
      .clickable {
        try {
          view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
        } catch (_: Exception) {}
        onClick()
      }
  ) {
    // Phase 1: Outer wide ambient glow ring
    Box(
      modifier = Modifier
        .size(150.dp * scale)
        .blur(pulseGlowRadius)
        .background(color.copy(alpha = 0.15f), CircleShape)
    )

    // Phase 2: Core ambient glow ring
    Box(
      modifier = Modifier
        .size(130.dp * scaleCore)
        .blur(16.dp)
        .background(color.copy(alpha = 0.25f), CircleShape)
    )

    // Main central physical Orb
    Box(
      modifier = Modifier
        .size(124.dp)
        .clip(CircleShape)
        .background(
          brush = Brush.radialGradient(
            colors = listOf(color, color.copy(alpha = 0.8f)),
            radius = 180f
          )
        ),
      contentAlignment = Alignment.Center
    ) {
      if (orbState == OrbState.IDLE) {
        // Invite custom gold dotted ring & soft shimmering mic
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Filled.Mic,
            contentDescription = "Tap to speak",
            tint = Accent.copy(alpha = 0.7f),
            modifier = Modifier.size(34.dp)
          )
          Canvas(modifier = Modifier.size(96.dp)) {
            drawCircle(
              color = Accent.copy(alpha = 0.18f),
              style = Stroke(
                width = 1.5f.dp.toPx(),
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
              )
            )
          }
        }
      } else if (orbState == OrbState.LISTENING) {
        // Ripple lines & central active mic
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Filled.Mic,
            contentDescription = "Listening",
            tint = BgBase,
            modifier = Modifier.size(38.dp)
          )
          Canvas(modifier = Modifier.size(108.dp)) {
            drawCircle(
              color = BgBase.copy(alpha = 0.4f),
              style = Stroke(width = 2.dp.toPx())
            )
          }
        }
      } else if (orbState == OrbState.PROCESSING) {
        // High polish rotating processing halos
        Canvas(modifier = Modifier.size(100.dp)) {
          drawCircle(
            color = BgBase.copy(alpha = 0.2f),
            style = Stroke(width = 3.dp.toPx())
          )
          rotate(rotation) {
            drawArc(
              color = Accent,
              startAngle = 0f,
              sweepAngle = 120f,
              useCenter = false,
              style = Stroke(width = 4.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
          }
        }
      } else if (orbState == OrbState.SPEAKING) {
        // Ultra-fluid vocal frequency bars
        Row(
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          for (i in 0..5) {
            val waveHeight = 10.dp + 32.dp * sin((rotation * 1.5f + i * 40) * Math.PI / 180).toFloat().coerceAtLeast(0.1f)
            Box(
              modifier = Modifier
                .width(4.5.dp)
                .height(waveHeight)
                .clip(CircleShape)
                .background(BgBase)
            )
          }
        }
      } else if (orbState == OrbState.PLAYING) {
        // Visual representation of recitation: Spinning Rub El Hizb Star & centered audio frequency wave
        Box(contentAlignment = Alignment.Center) {
          // Inner bouncing frequency wave
          Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            for (i in 0..3) {
              val waveHeight = 8.dp + 22.dp * sin((rotation * 1.2f + i * 45) * Math.PI / 180).toFloat().coerceAtLeast(0.1f)
              Box(
                modifier = Modifier
                  .width(3.dp)
                  .height(waveHeight)
                  .clip(CircleShape)
                  .background(Accent)
              )
            }
          }
          // Custom detailed Rub El Hizb Islamic Star drawn symmetrically (Two overlapping squares tilted 45 degrees)
          Canvas(modifier = Modifier.size(102.dp)) {
            rotate(rotation * 0.4f) {
              // Draw square 1
              drawRoundRect(
                color = Accent.copy(alpha = 0.25f),
                topLeft = Offset(11.dp.toPx(), 11.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(80.dp.toPx(), 80.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
                style = Stroke(width = 1.5f.dp.toPx())
              )
              // Draw square 2 (tilted 45 degrees)
              rotate(45f) {
                drawRoundRect(
                  color = Accent.copy(alpha = 0.25f),
                  topLeft = Offset(11.dp.toPx(), 11.dp.toPx()),
                  size = androidx.compose.ui.geometry.Size(80.dp.toPx(), 80.dp.toPx()),
                  cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
                  style = Stroke(width = 1.5f.dp.toPx())
                )
              }
            }
          }
        }
      }
    }
  }
}

// 5. SURAH BROWSER / QURAN NAVIGATOR

@Composable
fun SurahBrowserScreen(viewModel: ConversationViewModel, onBack: () -> Unit) {
  var searchQuery by remember { mutableStateOf("") }
  var downloadOnlyMode by remember { mutableStateOf(false) }
  var isJuzSelection by remember { mutableStateOf(false) }

  val downloadedList by viewModel.downloadedSurahs.collectAsState()
  val activeDownloads by viewModel.downloadManager.activeDownloads.collectAsState()
  val downloadProgresses by viewModel.downloadManager.downloadProgresses.collectAsState()

  // Filter lists
  val filteredSurahs = remember(searchQuery, downloadOnlyMode, downloadedList) {
    QuranData.surahs.filter { s ->
      val matchesSearch = s.englishName.lowercase().contains(searchQuery.lowercase()) ||
          s.phoneticName.lowercase().contains(searchQuery.lowercase()) ||
          s.number.toString() == searchQuery
      
      val matchesDownloadMode = !downloadOnlyMode || downloadedList.any { it.surahNumber == s.number }
      matchesSearch && matchesDownloadMode
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(BgBase)
      .padding(24.dp)
  ) {
    // Title header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack, modifier = Modifier.background(BgSurface, CircleShape)) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text("Quran Navigator", style = Typography.headlineMedium, color = TextPrimary)
      }

      val displayBtnText = if (isJuzSelection) "Surah View" else "Juz View"
      Button(
        onClick = { isJuzSelection = !isJuzSelection },
        colors = ButtonDefaults.buttonColors(containerColor = BgSurface, contentColor = Accent),
        shape = RoundedCornerShape(12.dp)
      ) {
        Text(displayBtnText)
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Modern Tab filters
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Button(
        onClick = { downloadOnlyMode = false },
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (!downloadOnlyMode) Accent else BgSurface,
          contentColor = if (!downloadOnlyMode) BgBase else TextSecondary
        ),
        shape = RoundedCornerShape(12.dp)
      ) {
        Text("All Surahs", fontWeight = FontWeight.Bold)
      }

      Button(
        onClick = { downloadOnlyMode = true },
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (downloadOnlyMode) Accent else BgSurface,
          contentColor = if (downloadOnlyMode) BgBase else TextSecondary
        ),
        shape = RoundedCornerShape(12.dp)
      ) {
        Text("Downloaded only", fontWeight = FontWeight.Bold)
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Search bar
    TextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("surah_search_field"),
      placeholder = { Text("Search Surah name or chapter number...", color = TextSecondary) },
      leadingIcon = { Icon(Icons.Filled.Search, null, tint = TextSecondary) },
      colors = TextFieldDefaults.colors(
        focusedContainerColor = BgSurface,
        unfocusedContainerColor = BgSurface,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        cursorColor = Accent,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent
      ),
      shape = RoundedCornerShape(16.dp),
      singleLine = true
    )

    Spacer(modifier = Modifier.height(16.dp))

    if (isJuzSelection) {
      // Direct JUZ links list
      LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(30) { index ->
          val juzNumber = index + 1
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(BgSurface, RoundedCornerShape(16.dp))
              .clickable {
                // Find first surah of that juz
                val targetFirst = QuranData.surahs.firstOrNull { it.juzStart == juzNumber }
                if (targetFirst != null) {
                  viewModel.audioPlayer.playVerse(targetFirst.number, 1, viewModel.reciter.value)
                  viewModel.navigateTo(AppScreen.HOME)
                }
              }
              .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("Juz $juzNumber", style = Typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
              val startingSurah = QuranData.surahs.firstOrNull { it.juzStart == juzNumber }?.englishName ?: "Unknown"
              Text("Starts at Surah $startingSurah", style = Typography.bodyMedium, color = TextSecondary)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Accent)
          }
        }
      }
    } else {
      // Standard Surahs scroll list
      if (filteredSurahs.isEmpty()) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
          Text("No Surahs found match settings", color = TextSecondary, style = Typography.bodyLarge)
        }
      } else {
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.weight(1f)
        ) {
          items(filteredSurahs) { s ->
            val isDownloading = activeDownloads.contains(s.number)
            val isDownloaded = downloadedList.any { it.surahNumber == s.number }
            val downloadProgress = downloadProgresses[s.number] ?: 0f

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(BgSurface, RoundedCornerShape(16.dp))
                .clickable {
                  viewModel.audioPlayer.playVerse(s.number, 1, viewModel.reciter.value)
                  viewModel.navigateTo(AppScreen.HOME)
                }
                .padding(16.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                // Number Index Badge
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .background(BgBase, CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Text(s.number.toString(), style = Typography.bodyLarge, color = Accent, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                  Text(s.englishName, style = Typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                  Text("${s.verseCount} verses · Juz ${s.juzStart}", style = Typography.bodyMedium, color = TextSecondary)
                }
              }

              // Download Status controls
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(s.arabicName, style = ArabicTextStyle, color = TextArabic, fontSize = 20.sp, modifier = Modifier.padding(end = 12.dp))
                
                when {
                  isDownloaded -> {
                    IconButton(onClick = { viewModel.downloadManager.deleteDownload(s.number) }) {
                      Icon(Icons.Filled.OfflinePin, "Downloaded offline", tint = Success)
                    }
                  }
                  isDownloading -> {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp)) {
                      CircularProgressIndicator(
                        progress = { downloadProgress },
                        color = Accent,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(32.dp)
                      )
                    }
                  }
                  else -> {
                    IconButton(onClick = { viewModel.downloadManager.startDownload(s.number, viewModel.reciter.value) }) {
                      Icon(Icons.Filled.FileDownload, "Download offline", tint = TextSecondary)
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

// 6. CONVERSATION HISTORY SCREEN

@Composable
fun ConversationHistoryScreen(viewModel: ConversationViewModel, onBack: () -> Unit) {
  val exchanges by viewModel.allExchanges.collectAsState()
  val sessions by viewModel.allSessions.collectAsState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(BgBase)
      .padding(24.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      IconButton(onClick = onBack, modifier = Modifier.background(BgSurface, CircleShape)) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
      }
      Spacer(modifier = Modifier.width(16.dp))
      Text("Conversations History", style = Typography.headlineMedium, color = TextPrimary)
    }

    Spacer(modifier = Modifier.height(16.dp))

    if (exchanges.isEmpty()) {
      Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Outlined.Chat, null, tint = TextTertiary, modifier = Modifier.size(64.dp))
          Spacer(modifier = Modifier.height(16.dp))
          Text("No conversation history logs found", color = TextSecondary, style = Typography.bodyLarge)
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        items(exchanges) { exchange ->
          Column(modifier = Modifier.fillMaxWidth()) {
            val sdf = SimpleDateFormat("h:mm a · d MMM", Locale.getDefault())
            val dateStr = sdf.format(Date(exchange.timestamp))
            Text(
              text = dateStr,
              color = TextTertiary,
              style = Typography.labelSmall,
              modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
            )

            // User speech bubble (Right aligned)
            if (exchange.userText.isNotEmpty()) {
              Box(
                modifier = Modifier
                  .align(Alignment.End)
                  .padding(start = 48.dp)
                  .background(Accent, RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp))
                  .padding(16.dp)
              ) {
                Text(exchange.userText, style = Typography.bodyLarge, color = BgBase, fontWeight = FontWeight.Medium)
              }
              Spacer(modifier = Modifier.height(8.dp))
            }

            // Noor response speech bubble (Left aligned)
            Box(
              modifier = Modifier
                .align(Alignment.Start)
                .padding(end = 48.dp)
                .background(BgSurface, RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp))
                .padding(16.dp)
            ) {
              Column {
                Text("Noor", style = Typography.bodyMedium, color = Accent, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(exchange.noorText, style = Typography.bodyLarge, color = TextPrimary)
              }
            }
          }
        }
      }
    }
  }
}

// 7. BOOKMARKS SCREEN

@Composable
fun BookmarksScreen(viewModel: ConversationViewModel, onBack: () -> Unit) {
  val list by viewModel.bookmarks.collectAsState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(BgBase)
      .padding(24.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      IconButton(onClick = onBack, modifier = Modifier.background(BgSurface, CircleShape)) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
      }
      Spacer(modifier = Modifier.width(16.dp))
      Text("Bookmarks", style = Typography.headlineMedium, color = TextPrimary)
    }

    Spacer(modifier = Modifier.height(16.dp))

    if (list.isEmpty()) {
      Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Outlined.BookmarkBorder, null, tint = TextTertiary, modifier = Modifier.size(64.dp))
          Spacer(modifier = Modifier.height(16.dp))
          Text("No bookmarks added yet.", color = TextSecondary, style = Typography.bodyLarge)
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Say 'Noor, bookmark this' to save your place during playback.",
            color = TextTertiary,
            style = Typography.bodyMedium,
            textAlign = TextAlign.Center
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(list) { bookmark ->
          val s = QuranData.surahs.firstOrNull { it.number == bookmark.surahNumber }
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(BgSurface, RoundedCornerShape(16.dp))
              .clickable { viewModel.playBookmark(bookmark) }
              .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .background(AccentGlow, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Filled.Bookmark, null, tint = Accent)
              }
              Spacer(modifier = Modifier.width(16.dp))
              Column {
                Text(
                  text = "Surah ${bookmark.surahName}",
                  style = Typography.titleLarge,
                  color = TextPrimary,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Verse ${bookmark.verseNumber} · Juz ${bookmark.juzNumber ?: s?.juzStart}",
                  style = Typography.bodyMedium,
                  color = TextSecondary
                )
              }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TextSecondary)
          }
        }
      }
    }
  }
}

// 8. LISTENING HISTORY SCREEN

@Composable
fun ListeningHistoryScreen(viewModel: ConversationViewModel, onBack: () -> Unit) {
  val history by viewModel.listeningHistory.collectAsState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(BgBase)
      .padding(24.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      IconButton(onClick = onBack, modifier = Modifier.background(BgSurface, CircleShape)) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
      }
      Spacer(modifier = Modifier.width(16.dp))
      Text("Listening History", style = Typography.headlineMedium, color = TextPrimary)
    }

    Spacer(modifier = Modifier.height(16.dp))

    if (history.isEmpty()) {
      Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Outlined.Headphones, null, tint = TextTertiary, modifier = Modifier.size(64.dp))
          Spacer(modifier = Modifier.height(16.dp))
          Text("No listening logs found", color = TextSecondary, style = Typography.bodyLarge)
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(history) { log ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(BgSurface, RoundedCornerShape(16.dp))
              .clickable {
                viewModel.audioPlayer.playVerse(log.surahNumber, log.startVerse ?: 1, viewModel.reciter.value)
                viewModel.navigateTo(AppScreen.HOME)
              }
              .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Surah ${log.surahName}",
                style = Typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
              )
              val startMsg = if (log.startVerse != null) "Resumes at verse ${log.startVerse}" else "Played recently"
              Text(text = startMsg, style = Typography.bodyMedium, color = TextSecondary)
            }
            Icon(Icons.Filled.PlayArrow, null, tint = Accent)
          }
        }
      }
    }
  }
}

// 9. SETTINGS SCREEN

@Composable
fun SettingsScreen(viewModel: ConversationViewModel, onBack: () -> Unit) {
  val currentReciter by viewModel.reciter.collectAsState()
  val userLang by viewModel.userLanguage.collectAsState()
  val autoplay by viewModel.autoContinue.collectAsState()
  val sensitivity by viewModel.wakeSensitivity.collectAsState()
  val pushToHold by viewModel.pushToHold.collectAsState()

  val downloadedList by viewModel.downloadedSurahs.collectAsState()

  // Calculate total download storage
  val totalBytes = remember(downloadedList) {
    downloadedList.sumOf { it.totalBytes }
  }
  val megabytesStr = String.format(Locale.getDefault(), "%.1f", totalBytes / (1024f * 1024f))

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(BgBase)
      .padding(24.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      IconButton(onClick = onBack, modifier = Modifier.background(BgSurface, CircleShape)) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
      }
      Spacer(modifier = Modifier.width(16.dp))
      Text("Settings", style = Typography.headlineMedium, color = TextPrimary)
    }

    Spacer(modifier = Modifier.height(16.dp))

    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(20.dp),
      modifier = Modifier.weight(1f)
    ) {
      item {
        Text("Voice Configuration", style = Typography.titleLarge, color = Accent, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        // Reciters Row
        Card(colors = CardDefaults.cardColors(containerColor = BgSurface), shape = RoundedCornerShape(16.dp)) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text("Qari (Reciter)", style = Typography.bodyLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            val reciters = listOf(
              "ar.alafasy" to "Mishary Al-Afasy",
              "ar.abdulbasit" to "Abdul Basit",
              "ar.ghamadi" to "Saad Al-Ghamdi"
            )
            reciters.forEach { (id, name) ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { viewModel.setReciter(id) }
                  .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(name, color = if (currentReciter == id) Accent else TextPrimary, style = Typography.bodyLarge)
                if (currentReciter == id) {
                  Icon(Icons.Filled.RadioButtonChecked, null, tint = Accent)
                } else {
                  Icon(Icons.Filled.RadioButtonUnchecked, null, tint = TextSecondary)
                }
              }
            }
          }
        }
      }

      item {
        Text("Listening", style = Typography.titleLarge, color = Accent, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Card(colors = CardDefaults.cardColors(containerColor = BgSurface), shape = RoundedCornerShape(16.dp)) {
          Column(modifier = Modifier.padding(16.dp)) {
            // Autoplay switch
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text("Auto-Play next verse", style = Typography.bodyLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text("Automatically play continuing verses sequentially", style = Typography.bodyMedium, color = TextSecondary)
              }
              Switch(
                checked = autoplay,
                onCheckedChange = { viewModel.setAutoContinue(it) },
                colors = SwitchDefaults.colors(checkedThumbColor = BgBase, checkedTrackColor = Accent)
              )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Push to Hold switch
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text("Touch-to-Speak Assist", style = Typography.bodyLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text("Large lower dashboard button for visually impaired", style = Typography.bodyMedium, color = TextSecondary)
              }
              Switch(
                checked = pushToHold,
                onCheckedChange = { viewModel.setPushToHold(it) },
                colors = SwitchDefaults.colors(checkedThumbColor = BgBase, checkedTrackColor = Accent)
              )
            }
          }
        }
      }

      item {
        Text("Offline Audio Downloads", style = Typography.titleLarge, color = Accent, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Card(colors = CardDefaults.cardColors(containerColor = BgSurface), shape = RoundedCornerShape(16.dp)) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Total Download Storage", style = Typography.bodyLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
              Text("$megabytesStr MB (${downloadedList.size} Surahs)", style = Typography.bodyLarge, color = Accent)
            }

            if (downloadedList.isNotEmpty()) {
              Spacer(modifier = Modifier.height(16.dp))
              HorizontalDivider(color = TextTertiary.copy(alpha = 0.2f))
              Spacer(modifier = Modifier.height(8.dp))

              downloadedList.forEach { s ->
                val sizeStr = String.format(Locale.getDefault(), "%.1f", s.totalBytes / (1024f * 1024f))
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Text(s.surahName, style = Typography.bodyLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Text("$sizeStr MB · ${s.verseCount} verses", style = Typography.bodyMedium, color = TextSecondary)
                  }
                  IconButton(onClick = { viewModel.downloadManager.deleteDownload(s.surahNumber) }) {
                    Icon(Icons.Filled.Delete, "Delete download", tint = Error)
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
