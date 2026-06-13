package com.example.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import com.example.R
import androidx.compose.animation.*
import androidx.compose.ui.res.stringResource
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

  BackHandler(enabled = currentScreen != AppScreen.HOME && currentScreen != AppScreen.ONBOARDING) {
    viewModel.handleBack()
  }

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
    modifier = Modifier.width(320.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(24.dp)
    ) {
      // Header brand
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 32.dp)
      ) {
        Box(
          modifier = Modifier
            .size(56.dp)
            .background(AccentGlow, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Text("نور", color = Accent, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(20.dp))
        Column {
          Text(
            text = stringResource(R.string.app_name).uppercase(),
            style = Typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
          )
          Text(
            text = stringResource(R.string.app_tagline),
            style = Typography.labelSmall,
            color = TextTertiary
          )
        }
      }

      HorizontalDivider(color = TextTertiary.copy(alpha = 0.1f), modifier = Modifier.padding(bottom = 16.dp))

      val menuItems = listOf(
        DrawerItem(AppScreen.HOME, stringResource(R.string.nav_home), Icons.Filled.Home),
        DrawerItem(AppScreen.SURAH_BROWSER, stringResource(R.string.nav_navigator), Icons.Filled.MenuBook),
        DrawerItem(AppScreen.HISTORY, stringResource(R.string.nav_history), Icons.Filled.History),
        DrawerItem(AppScreen.BOOKMARKS, stringResource(R.string.nav_bookmarks), Icons.Filled.Bookmark),
        DrawerItem(AppScreen.LISTENING_HISTORY, stringResource(R.string.nav_listening), Icons.Filled.Headphones),
        DrawerItem(AppScreen.SETTINGS, stringResource(R.string.nav_settings), Icons.Filled.Settings)
      )

      menuItems.forEach { item ->
        val selected = currentScreen == item.screen
        NavigationDrawerItem(
          icon = { Icon(item.icon, contentDescription = null, tint = if (selected) BgBase else TextSecondary) },
          label = { Text(item.title, style = Typography.bodyLarge, color = if (selected) BgBase else TextPrimary, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium) },
          selected = selected,
          onClick = { onScreenSelected(item.screen) },
          colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = Accent,
            unselectedContainerColor = Color.Transparent
          ),
          modifier = Modifier
            .padding(vertical = 4.dp)
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .testTag("drawer_item_${item.title.lowercase()}")
        )
      }

      Spacer(modifier = Modifier.weight(1f))
      Text("Version 1.1.0-Stable", color = TextTertiary, style = Typography.labelSmall, modifier = Modifier.padding(start = 8.dp))
    }
  }
}

data class DrawerItem(val screen: AppScreen, val title: String, val icon: ImageVector)

// 2. ONBOARDING SCREEN

...
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
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      for (i in 1..3) {
        Box(
          modifier = Modifier
            .weight(1f)
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(if (i <= step) Accent else BgElevated)
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
              .size(200.dp)
              .background(AccentGlow, CircleShape)
              .blur(30.dp),
            contentAlignment = Alignment.Center
          ) {
            Box(
              modifier = Modifier
                .size(120.dp)
                .background(Accent, CircleShape)
            )
          }

          Spacer(modifier = Modifier.height(48.dp))
          Text(
            text = stringResource(R.string.welcome_title),
            style = Typography.displayLarge,
            color = TextPrimary,
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = stringResource(R.string.welcome_desc),
            style = Typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
          )
        }
        2 -> {
          Box(
            modifier = Modifier
              .size(110.dp)
              .background(AccentGlow, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Filled.Mic, contentDescription = null, tint = Accent, modifier = Modifier.size(56.dp))
          }
          Spacer(modifier = Modifier.height(40.dp))
          Text(
            text = stringResource(R.string.mic_access_title),
            style = Typography.headlineMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = stringResource(R.string.mic_access_desc),
            style = Typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(32.dp))
          Text(
            text = stringResource(R.string.privacy_note),
            style = Typography.bodyMedium,
            color = TextTertiary,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
          )
        }
        3 -> {
          Text(
            text = stringResource(R.string.language_title),
            style = Typography.headlineMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = stringResource(R.string.language_desc),
            style = Typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(40.dp))

          val languages = listOf("English", "Urdu (اردو)", "Arabic (عربي)")
          languages.forEach { lang ->
            val isSelected = userLang.startsWith(lang.split(" ")[0])
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(if (isSelected) AccentGlow else BgSurface)
                .border(1.dp, if (isSelected) Accent else Color.Transparent, RoundedCornerShape(20.dp))
                .clickable { viewModel.setLanguage(lang.split(" ")[0]) }
                .padding(24.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(lang, style = Typography.titleLarge, color = if (isSelected) Accent else TextPrimary)
              if (isSelected) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Accent)
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
        .padding(bottom = 24.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (step > 1) {
        TextButton(onClick = { viewModel.nextOnboardingStep() }) {
          Text(stringResource(R.string.btn_skip), color = TextSecondary, style = Typography.bodyLarge)
        }
      } else {
        Spacer(modifier = Modifier.width(48.dp))
      }

      Button(
        onClick = { viewModel.nextOnboardingStep() },
        colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = BgBase),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
          .height(64.dp)
          .width(180.dp)
          .testTag("onboarding_next")
      ) {
        Text(
          text = stringResource(if (step == 3) R.string.btn_start_companion else R.string.btn_get_started),
          style = Typography.bodyLarge,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.5.sp
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

  fun launchVoiceRecognition() {
    viewModel.setListening(true)
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

      Spacer(modifier = Modifier.height(32.dp))

      Text(
        text = stringResource(when (orbState) {
          OrbState.IDLE -> R.string.status_idle
          OrbState.LISTENING -> R.string.status_listening
          OrbState.PROCESSING -> R.string.status_processing
          OrbState.SPEAKING -> R.string.status_speaking
          OrbState.PLAYING -> R.string.status_playing
        }),
        style = Typography.titleLarge,
        color = if (orbState == OrbState.LISTENING) Accent else TextSecondary,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp
      )

      val handsFreeModeActive by viewModel.handsFreeMode.collectAsState()
      if (handsFreeModeActive) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Filled.AutoMode, null, tint = Accent, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Hands-free Active", style = Typography.labelSmall, color = Accent)
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      var showTextInput by remember { mutableStateOf(false) }
      var typedCommand by remember { mutableStateOf("") }

      if (!showTextInput) {
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(BgSurface)
            .border(1.dp, Accent.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .clickable { showTextInput = true }
            .padding(horizontal = 20.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Filled.Keyboard, contentDescription = null, tint = Accent, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(10.dp))
          Text(stringResource(R.string.hint_type_command), style = Typography.labelLarge, color = Accent, fontWeight = FontWeight.Bold)
        }
      } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 8.dp)
              .background(BgSurface, RoundedCornerShape(28.dp))
              .border(1.dp, AccentGlow, RoundedCornerShape(28.dp))
              .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            TextField(
              value = typedCommand,
              onValueChange = { typedCommand = it },
              placeholder = { Text(stringResource(R.string.placeholder_command), style = Typography.bodyMedium, color = TextTertiary) },
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
                contentDescription = "Send",
                tint = if (typedCommand.isNotBlank()) Accent else TextTertiary
              )
            }

            IconButton(onClick = { showTextInput = false }) {
              Icon(Icons.Filled.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(20.dp))
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Quick action chips
          val suggestions = listOf(
            "Play Surah Al-Fatihah" to "Al-Fatihah",
            "Play Surah Al-Mulk" to "Al-Mulk",
            "Play Surah Ya-Sin" to "Ya-Sin",
            "pause" to "Pause",
            "resume" to "Resume"
          )

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(stringResource(R.string.label_try), style = Typography.labelMedium, color = TextTertiary)
            androidx.compose.foundation.lazy.LazyRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              items(suggestions.size) { index ->
                val (cmd, label) = suggestions[index]
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(BgElevated)
                    .clickable { viewModel.speechPipeline.processSpokenText(cmd) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                  Text(label, style = Typography.labelSmall, color = TextSecondary, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }

      // Live transcript box
      if (rawText.isNotEmpty()) {
        Spacer(modifier = Modifier.height(24.dp))
        Card(
          colors = CardDefaults.cardColors(containerColor = BgSurface),
          shape = RoundedCornerShape(20.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .border(1.dp, AccentGlow, RoundedCornerShape(20.dp))
        ) {
          Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "\"$rawText\"",
              style = Typography.bodyLarge,
              color = TextPrimary,
              textAlign = TextAlign.Center,
              fontWeight = FontWeight.Medium,
              fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
            if (transText.isNotEmpty() && transText != rawText) {
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = transText,
                style = Typography.bodyMedium,
                color = Accent,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }
      }
    }

    // BOTTOM RECITATION CONTROLS
    Column(
      modifier = Modifier.fillMaxWidth()
    ) {
      if (currentSurah != null) {
        val s = QuranData.surahs.firstOrNull { it.number == currentSurah }
        if (s != null) {
          Card(
            colors = CardDefaults.cardColors(containerColor = BgSurface),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("now_playing_panel")
          ) {
            Column(
              modifier = Modifier.padding(24.dp),
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
                    tint = if (bookmarked) Accent else TextTertiary,
                    contentDescription = "Bookmark",
                    modifier = Modifier.size(28.dp)
                  )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(
                    text = s.englishName,
                    style = Typography.headlineSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = stringResource(R.string.label_verse, currentVerse ?: 1, s.verseCount) + " · " + stringResource(R.string.label_juz, s.juzStart),
                    style = Typography.bodyMedium,
                    color = TextSecondary
                  )
                }

                Text(
                  text = s.arabicName,
                  style = ArabicTextStyle,
                  color = TextArabic,
                  fontSize = 28.sp
                )
              }

              Spacer(modifier = Modifier.height(24.dp))

              // Player transport buttons
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
              ) {
                IconButton(
                  onClick = { viewModel.previousSurah() },
                  modifier = Modifier.size(56.dp).testTag("prev_surah_button")
                ) {
                  Icon(Icons.Filled.SkipPrevious, "Previous Surah", tint = TextPrimary, modifier = Modifier.size(36.dp))
                }

                IconButton(
                  onClick = { viewModel.previousVerse() },
                  modifier = Modifier.size(48.dp)
                ) {
                  Icon(Icons.Filled.FastRewind, "Previous Verse", tint = TextSecondary, modifier = Modifier.size(28.dp))
                }

                Box(
                  contentAlignment = Alignment.Center,
                  modifier = Modifier.size(72.dp)
                ) {
                  if (isBuffering) {
                    CircularProgressIndicator(color = Accent, strokeWidth = 4.dp, modifier = Modifier.size(56.dp))
                  } else {
                    IconButton(
                      onClick = { viewModel.togglePlayPause() },
                      modifier = Modifier
                        .background(Accent, CircleShape)
                        .size(64.dp)
                        .testTag("play_pause_button")
                    ) {
                      Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = BgBase,
                        modifier = Modifier.size(36.dp)
                      )
                    }
                  }
                }

                IconButton(
                  onClick = { viewModel.nextVerse() },
                  modifier = Modifier.size(48.dp)
                ) {
                  Icon(Icons.Filled.FastForward, "Next Verse", tint = TextSecondary, modifier = Modifier.size(28.dp))
                }

                IconButton(
                  onClick = { viewModel.nextSurah() },
                  modifier = Modifier.size(56.dp).testTag("next_surah_button")
                ) {
                  Icon(Icons.Filled.SkipNext, "Next Surah", tint = TextPrimary, modifier = Modifier.size(36.dp))
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // MASSIVE PUSH-TO-TALK BUTTON
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
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(80.dp)
          .testTag("push_to_hold_button")
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = if (isListening) Icons.Filled.Stop else Icons.Filled.Mic,
            contentDescription = null,
            modifier = Modifier.size(32.dp)
          )
          Spacer(modifier = Modifier.width(16.dp))
          Text(
            text = stringResource(if (isListening) R.string.btn_tap_to_stop else R.string.btn_tap_to_talk),
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
fun WavyVoiceAnimation(
  modifier: Modifier = Modifier,
  color: Color = Accent,
  isListening: Boolean = false
) {
  val infiniteTransition = rememberInfiniteTransition(label = "WaveAnimation")
  
  val phase by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 2f * Math.PI.toFloat(),
    animationSpec = infiniteRepeatable(
      animation = tween(if (isListening) 1200 else 2500, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "Phase"
  )

  Canvas(modifier = modifier) {
    val width = size.width
    val height = size.height
    val centerY = height / 2

    for (i in 0 until 4) {
      val progress = i.toFloat() / 4
      val alpha = 0.8f - (progress * 0.6f)
      val amplitude = (if (isListening) 35.dp.toPx() else 20.dp.toPx()) * (1f - progress * 0.5f)
      val frequency = 0.015f * (1f + progress)
      
      val path = androidx.compose.ui.graphics.Path()
      path.moveTo(0f, centerY)

      for (x in 0..width.toInt() step 4) {
        // Dynamic sine wave logic
        val verticalOffset = amplitude * sin(frequency * x + phase + i * 0.8f)
        path.lineTo(x.toFloat(), centerY + verticalOffset)
      }

      drawPath(
        path = path,
        color = color.copy(alpha = alpha),
        style = Stroke(width = (4.dp.toPx() * (1f - progress * 0.5f)), cap = androidx.compose.ui.graphics.StrokeCap.Round)
      )
    }
  }
}

@Composable
fun NoorOrb(orbState: OrbState, onClick: () -> Unit) {
  val infiniteTransition = rememberInfiniteTransition(label = "OrbBreathing")
  val view = androidx.compose.ui.platform.LocalView.current

  // Enhanced breathing animations
  val scale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = when(orbState) {
      OrbState.LISTENING -> 1.18f
      OrbState.PROCESSING -> 1.05f
      OrbState.SPEAKING -> 1.12f
      OrbState.PLAYING -> 1.08f
      else -> 1.03f
    },
    animationSpec = infiniteRepeatable(
      animation = tween(if (orbState == OrbState.LISTENING) 1000 else 3000, easing = EaseInOutSine),
      repeatMode = RepeatMode.Reverse
    ),
    label = "OrbScale"
  )

  val rotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(if (orbState == OrbState.PROCESSING) 1500 else 8000, easing = LinearEasing),
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

  Box(
    contentAlignment = Alignment.Center,
    modifier = Modifier
      .size(240.dp)
      .clickable {
        try {
          view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
        } catch (_: Exception) {}
        onClick()
      }
  ) {
    // Background Glow
    Box(
      modifier = Modifier
        .size(180.dp * scale)
        .blur(40.dp)
        .background(color.copy(alpha = 0.2f), CircleShape)
    )

    // Main Orb Surface
    Box(
      modifier = Modifier
        .size(140.dp)
        .clip(CircleShape)
        .background(
          brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.9f), color),
            radius = 200f
          )
        )
        .border(2.dp, color.copy(alpha = 0.3f), CircleShape),
      contentAlignment = Alignment.Center
    ) {
      when (orbState) {
        OrbState.IDLE -> {
          Icon(
            imageVector = Icons.Filled.Mic,
            contentDescription = "Tap to speak",
            tint = TextPrimary.copy(alpha = 0.6f),
            modifier = Modifier.size(40.dp)
          )
        }
        OrbState.LISTENING -> {
          WavyVoiceAnimation(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            color = BgBase,
            isListening = true
          )
        }
        OrbState.SPEAKING -> {
          WavyVoiceAnimation(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            color = BgBase,
            isListening = false
          )
        }
        OrbState.PROCESSING -> {
          Canvas(modifier = Modifier.size(80.dp)) {
            rotate(rotation) {
              drawArc(
                color = BgBase,
                startAngle = 0f,
                sweepAngle = 100f,
                useCenter = false,
                style = Stroke(width = 6.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
              )
            }
          }
        }
        OrbState.PLAYING -> {
          Box(contentAlignment = Alignment.Center) {
            // Recitation Star
            Canvas(modifier = Modifier.size(90.dp)) {
              rotate(rotation * 0.5f) {
                for (angle in 0 until 360 step 45) {
                  rotate(angle.toFloat()) {
                    drawRoundRect(
                      color = BgBase.copy(alpha = 0.7f),
                      topLeft = Offset(15.dp.toPx(), 15.dp.toPx()),
                      size = androidx.compose.ui.geometry.Size(60.dp.toPx(), 60.dp.toPx()),
                      cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                      style = Stroke(width = 2.dp.toPx())
                    )
                  }
                }
              }
            }
            Icon(Icons.Filled.PlayArrow, null, tint = BgBase, modifier = Modifier.size(32.dp))
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
        Spacer(modifier = Modifier.width(20.dp))
        Text(stringResource(R.string.nav_navigator), style = Typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
      }

      TextButton(
        onClick = { isJuzSelection = !isJuzSelection },
        colors = ButtonDefaults.textButtonColors(contentColor = Accent),
        shape = RoundedCornerShape(12.dp)
      ) {
        Text(if (isJuzSelection) "Switch to Surahs" else "Switch to Juz", fontWeight = FontWeight.Bold)
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Modern Tab filters
    Row(
      modifier = Modifier.fillMaxWidth()
        .background(BgSurface, RoundedCornerShape(16.dp))
        .padding(4.dp),
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Button(
        onClick = { downloadOnlyMode = false },
        modifier = Modifier.weight(1f),
        elevation = null,
        colors = ButtonDefaults.buttonColors(
          containerColor = if (!downloadOnlyMode) Accent else Color.Transparent,
          contentColor = if (!downloadOnlyMode) BgBase else TextSecondary
        ),
        shape = RoundedCornerShape(12.dp)
      ) {
        Text("All Chapters", fontWeight = FontWeight.Bold)
      }

      Button(
        onClick = { downloadOnlyMode = true },
        modifier = Modifier.weight(1f),
        elevation = null,
        colors = ButtonDefaults.buttonColors(
          containerColor = if (downloadOnlyMode) Accent else Color.Transparent,
          contentColor = if (downloadOnlyMode) BgBase else TextSecondary
        ),
        shape = RoundedCornerShape(12.dp)
      ) {
        Text("Offline", fontWeight = FontWeight.Bold)
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Search bar
    TextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("surah_search_field"),
      placeholder = { Text("Search by name or number...", color = TextTertiary) },
      leadingIcon = { Icon(Icons.Filled.Search, null, tint = TextTertiary) },
      colors = TextFieldDefaults.colors(
        focusedContainerColor = BgSurface,
        unfocusedContainerColor = BgSurface,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        cursorColor = Accent,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent
      ),
      shape = RoundedCornerShape(20.dp),
      singleLine = true
    )

    Spacer(modifier = Modifier.height(24.dp))

    if (isJuzSelection) {
      // Direct JUZ links list
      LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(30) { index ->
          val juzNumber = index + 1
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(20.dp))
              .background(BgSurface)
              .clickable {
                val targetFirst = QuranData.surahs.firstOrNull { it.juzStart == juzNumber }
                if (targetFirst != null) {
                  viewModel.audioPlayer.playVerse(targetFirst.number, 1, viewModel.reciter.value)
                  viewModel.navigateTo(AppScreen.HOME)
                }
              }
              .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("Juz $juzNumber", style = Typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
              val startingSurah = QuranData.surahs.firstOrNull { it.juzStart == juzNumber }?.englishName ?: "Unknown"
              Text("Starts with Surah $startingSurah", style = Typography.bodyMedium, color = TextSecondary)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Accent)
          }
        }
      }
    } else {
      // Standard Surahs scroll list
      if (filteredSurahs.isEmpty()) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
          Text("No results match your search", color = TextTertiary, style = Typography.bodyLarge)
        }
      } else {
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.weight(1f)
        ) {
          items(filteredSurahs) { s ->
            val isDownloading = activeDownloads.contains(s.number)
            val isDownloaded = downloadedList.any { it.surahNumber == s.number }
            val downloadProgress = downloadProgresses[s.number] ?: 0f

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(BgSurface)
                .clickable {
                  viewModel.audioPlayer.playVerse(s.number, 1, viewModel.reciter.value)
                  viewModel.navigateTo(AppScreen.HOME)
                }
                .padding(20.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                // Number Index Badge
                Box(
                  modifier = Modifier
                    .size(44.dp)
                    .background(BgBase, CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Text(s.number.toString(), style = Typography.bodyLarge, color = Accent, fontWeight = FontWeight.Black)
                }
                Spacer(modifier = Modifier.width(20.dp))
                Column {
                  Text(s.englishName, style = Typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                  Text("${s.verseCount} Verses · Juz ${s.juzStart}", style = Typography.bodySmall, color = TextSecondary)
                }
              }

              // Download Status controls
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(s.arabicName, style = ArabicTextStyle, color = TextArabic, fontSize = 22.sp, modifier = Modifier.padding(end = 16.dp))
                
                when {
                  isDownloaded -> {
                    IconButton(onClick = { viewModel.downloadManager.deleteDownload(s.number) }) {
                      Icon(Icons.Filled.OfflinePin, null, tint = Success, modifier = Modifier.size(28.dp))
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
                      Icon(Icons.Filled.FileDownload, null, tint = TextTertiary)
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
      Spacer(modifier = Modifier.width(20.dp))
      Text(stringResource(R.string.nav_history), style = Typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
    }

    Spacer(modifier = Modifier.height(24.dp))

    if (exchanges.isEmpty()) {
      Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Outlined.Chat, null, tint = BgElevated, modifier = Modifier.size(80.dp))
          Spacer(modifier = Modifier.height(24.dp))
          Text("No conversation logs found", color = TextTertiary, style = Typography.bodyLarge)
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(20.dp)
      ) {
        items(exchanges) { exchange ->
          Column(modifier = Modifier.fillMaxWidth()) {
            val sdf = SimpleDateFormat("h:mm a · d MMM", Locale.getDefault())
            val dateStr = sdf.format(Date(exchange.timestamp))
            Text(
              text = dateStr,
              color = TextTertiary,
              style = Typography.labelSmall,
              modifier = Modifier.padding(start = 12.dp, bottom = 6.dp)
            )

            // User speech bubble
            if (exchange.userText.isNotEmpty()) {
              Box(
                modifier = Modifier
                  .align(Alignment.End)
                  .padding(start = 60.dp)
                  .clip(RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp))
                  .background(BgSurface)
                  .border(1.dp, AccentGlow, RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp))
                  .padding(16.dp)
              ) {
                Text(exchange.userText, style = Typography.bodyLarge, color = TextPrimary, fontWeight = FontWeight.Medium)
              }
              Spacer(modifier = Modifier.height(10.dp))
            }

            // Noor response speech bubble
            Box(
              modifier = Modifier
                .align(Alignment.Start)
                .padding(end = 60.dp)
                .clip(RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp))
                .background(AccentGlow)
                .padding(16.dp)
            ) {
              Column {
                Text("Noor", style = Typography.labelSmall, color = Accent, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
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
      Spacer(modifier = Modifier.width(20.dp))
      Text(stringResource(R.string.nav_bookmarks), style = Typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
    }

    Spacer(modifier = Modifier.height(24.dp))

    if (list.isEmpty()) {
      Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Outlined.BookmarkBorder, null, tint = BgElevated, modifier = Modifier.size(80.dp))
          Spacer(modifier = Modifier.height(24.dp))
          Text("Your bookmarks will appear here", color = TextTertiary, style = Typography.bodyLarge)
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "Say \"Noor, bookmark this\" while listening",
            color = TextTertiary.copy(alpha = 0.6f),
            style = Typography.bodySmall,
            textAlign = TextAlign.Center
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(list) { bookmark ->
          val s = QuranData.surahs.firstOrNull { it.number == bookmark.surahNumber }
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(20.dp))
              .background(BgSurface)
              .clickable { viewModel.playBookmark(bookmark) }
              .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(48.dp)
                  .background(AccentGlow, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Filled.Bookmark, null, tint = Accent, modifier = Modifier.size(24.dp))
              }
              Spacer(modifier = Modifier.width(20.dp))
              Column {
                Text(
                  text = "Surah ${bookmark.surahName}",
                  style = Typography.titleLarge,
                  color = TextPrimary,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = stringResource(R.string.label_verse, bookmark.verseNumber, s?.verseCount ?: 0) + " · " + stringResource(R.string.label_juz, bookmark.juzNumber ?: s?.juzStart ?: 0),
                  style = Typography.bodyMedium,
                  color = TextSecondary
                )
              }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TextTertiary)
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
      Spacer(modifier = Modifier.width(20.dp))
      Text(stringResource(R.string.nav_listening), style = Typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
    }

    Spacer(modifier = Modifier.height(24.dp))

    if (history.isEmpty()) {
      Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Outlined.Headphones, null, tint = BgElevated, modifier = Modifier.size(80.dp))
          Spacer(modifier = Modifier.height(24.dp))
          Text("No listening logs found", color = TextTertiary, style = Typography.bodyLarge)
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(history) { log ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(20.dp))
              .background(BgSurface)
              .clickable {
                viewModel.audioPlayer.playVerse(log.surahNumber, log.startVerse ?: 1, viewModel.reciter.value)
                viewModel.navigateTo(AppScreen.HOME)
              }
              .padding(20.dp),
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
              val startMsg = if (log.startVerse != null) "Resumed at verse ${log.startVerse}" else "Recently played"
              Text(text = startMsg, style = Typography.bodyMedium, color = TextSecondary)
            }
            Icon(Icons.Filled.PlayArrow, null, tint = Accent, modifier = Modifier.size(28.dp))
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
  val handsFreeModeActive by viewModel.handsFreeMode.collectAsState()
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
      Spacer(modifier = Modifier.width(20.dp))
      Text(stringResource(R.string.nav_settings), style = Typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
    }

    Spacer(modifier = Modifier.height(24.dp))

    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(24.dp),
      modifier = Modifier.weight(1f)
    ) {
      item {
        Text("Voice Configuration", style = Typography.titleLarge, color = Accent, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Card(colors = CardDefaults.cardColors(containerColor = BgSurface), shape = RoundedCornerShape(24.dp)) {
          Column(modifier = Modifier.padding(24.dp)) {
            Text("Qari (Reciter)", style = Typography.bodyLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
            Text("Choose the voice for Quran recitation", style = Typography.labelSmall, color = TextTertiary)
            Spacer(modifier = Modifier.height(16.dp))
            
            val reciters = listOf(
              "ar.alafasy" to "Mishary Al-Afasy",
              "ar.abdulbasit" to "Abdul Basit",
              "ar.ghamadi" to "Saad Al-Ghamdi"
            )
            reciters.forEach { (id, name) ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .clickable { viewModel.setReciter(id) }
                  .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(name, color = if (currentReciter == id) Accent else TextPrimary, style = Typography.bodyLarge, fontWeight = if (currentReciter == id) FontWeight.Bold else FontWeight.Normal)
                RadioButton(
                  selected = currentReciter == id,
                  onClick = { viewModel.setReciter(id) },
                  colors = RadioButtonDefaults.colors(selectedColor = Accent, unselectedColor = TextTertiary)
                )
              }
            }
          }
        }
      }

      item {
        Text("Listening Experience", style = Typography.titleLarge, color = Accent, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(16.dp))

        Card(colors = CardDefaults.cardColors(containerColor = BgSurface), shape = RoundedCornerShape(24.dp)) {
          Column(modifier = Modifier.padding(24.dp)) {
            // Autoplay switch
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text("Continuous Recitation", style = Typography.bodyLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text("Automatically play the next verse", style = Typography.bodySmall, color = TextSecondary)
              }
              Switch(
                checked = autoplay,
                onCheckedChange = { viewModel.setAutoContinue(it) },
                colors = SwitchDefaults.colors(checkedThumbColor = BgBase, checkedTrackColor = Accent, uncheckedTrackColor = BgElevated)
              )
            }

            HorizontalDivider(color = TextTertiary.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 16.dp))

            // Hands-free switch
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text("Hands-free Interruption", style = Typography.bodyLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text("App listens continuously; interrupt by speaking", style = Typography.bodySmall, color = TextSecondary)
              }
              Switch(
                checked = handsFreeModeActive,
                onCheckedChange = { viewModel.setHandsFreeMode(it) },
                colors = SwitchDefaults.colors(checkedThumbColor = BgBase, checkedTrackColor = Accent, uncheckedTrackColor = BgElevated)
              )
            }

            HorizontalDivider(color = TextTertiary.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 16.dp))

            // Push to Hold switch
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text("Assistant Accessibility", style = Typography.bodyLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text("Show large dashboard button for visually impaired", style = Typography.bodySmall, color = TextSecondary)
              }
              Switch(
                checked = pushToHold,
                onCheckedChange = { viewModel.setPushToHold(it) },
                colors = SwitchDefaults.colors(checkedThumbColor = BgBase, checkedTrackColor = Accent, uncheckedTrackColor = BgElevated)
              )
            }
          }
        }
      }

      item {
        Text("Offline Content", style = Typography.titleLarge, color = Accent, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(16.dp))

        Card(colors = CardDefaults.cardColors(containerColor = BgSurface), shape = RoundedCornerShape(24.dp)) {
          Column(modifier = Modifier.padding(24.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text("Storage Usage", style = Typography.bodyLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text("${downloadedList.size} chapters saved offline", style = Typography.bodySmall, color = TextSecondary)
              }
              Text("$megabytesStr MB", style = Typography.titleLarge, color = Accent, fontWeight = FontWeight.Black)
            }

            if (downloadedList.isNotEmpty()) {
              Spacer(modifier = Modifier.height(20.dp))
              HorizontalDivider(color = TextTertiary.copy(alpha = 0.1f))
              Spacer(modifier = Modifier.height(12.dp))

              downloadedList.forEach { s ->
                val sizeStr = String.format(Locale.getDefault(), "%.1f", s.totalBytes / (1024f * 1024f))
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Text(s.surahName, style = Typography.bodyLarge, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text("$sizeStr MB · ${s.verseCount} verses", style = Typography.bodySmall, color = TextTertiary)
                  }
                  IconButton(onClick = { viewModel.downloadManager.deleteDownload(s.surahNumber) }) {
                    Icon(Icons.Filled.DeleteOutline, "Delete", tint = Error)
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
