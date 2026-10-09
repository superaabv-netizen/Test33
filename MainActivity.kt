package bg.digitaltracker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val PREFS = "digital_tracker_data"
private const val KEY_CENTS = "profit_cents"
private const val KEY_LAST_DAY = "last_added_day"
private val Green = Color(0xFF326B58)
private val Background = Color(0xFFF6F7FB)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        setContent {
            MaterialTheme {
                var refresh by remember { mutableLongStateOf(0L) }
                var cents by remember { mutableLongStateOf(prefs.getLong(KEY_CENTS, 0L)) }
                var lastDay by remember { mutableStateOf(prefs.getString(KEY_LAST_DAY, "") ?: "") }
                val today = LocalDate.now().toString()
                val isLocked = lastDay == today

                // Refresh at local midnight even if the app remains open.
                LaunchedEffect(refresh) {
                    val now = LocalDateTime.now()
                    val midnight = now.toLocalDate().plusDays(1).atStartOfDay()
                    val wait = Duration.between(now, midnight).toMillis().coerceAtLeast(1000L)
                    delay(wait)
                    refresh++
                }

                Surface(modifier = Modifier.fillMaxSize(), color = Background) {
                    TrackerScreen(
                        cents = cents,
                        isLocked = isLocked,
                        onAdd = {
                            // Recheck the local date at click time to prevent duplicate taps.
                            val actualToday = LocalDate.now().toString()
                            if ((prefs.getString(KEY_LAST_DAY, "") ?: "") != actualToday) {
                                cents = prefs.getLong(KEY_CENTS, 0L) + 350L
                                lastDay = actualToday
                                prefs.edit()
                                    .putLong(KEY_CENTS, cents)
                                    .putString(KEY_LAST_DAY, actualToday)
                                    .apply()
                            }
                            refresh++
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackerScreen(cents: Long, isLocked: Boolean, onAdd: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Текуща печалба",
            color = Color(0xFF5E6672),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = formatEuro(cents),
            modifier = Modifier.padding(top = 10.dp, bottom = 36.dp),
            color = Color(0xFF17231F),
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-1).sp
        )
        Button(
            onClick = onAdd,
            enabled = !isLocked,
            modifier = Modifier.widthIn(min = 220.dp, max = 320.dp).heightIn(min = 58.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Green,
                contentColor = Color.White,
                disabledContainerColor = Color(0xFFB8C7C0),
                disabledContentColor = Color.White
            )
        ) {
            Text("Добави", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun formatEuro(cents: Long): String {
    val euros = cents / 100
    val remainder = cents % 100
    return "${euros},${remainder.toString().padStart(2, '0')} €"
}
