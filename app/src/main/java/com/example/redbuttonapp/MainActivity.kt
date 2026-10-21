package com.example.redbuttonapp

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.redbuttonapp.ui.theme.RedButtonAppTheme
import kotlinx.coroutines.delay
import java.util.Locale

private val Ink = Color(0xFF24262B)
private val Muted = Color(0xFF777A82)
private val Red = Color(0xFFE54747)
private val Green = Color(0xFF269D73)
private val Paper = Color(0xFFF8F7F4)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { RedButtonAppTheme { ButtonRoute() } }
    }
}

@Composable
private fun ButtonRoute() {
    var minutes by rememberSaveable { mutableIntStateOf(30) }
    var deadline by rememberSaveable { mutableLongStateOf(0L) }
    var remaining by remember { mutableLongStateOf(0L) }
    LaunchedEffect(deadline) {
        if (deadline == 0L) remaining = 0L
        else {
            do {
                remaining = (deadline - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
                if (remaining > 0L) delay(200L)
            } while (remaining > 0L)
            deadline = 0L
        }
    }
    ButtonScreen(minutes, deadline != 0L, remaining,
        onMinutesChange = { minutes = it },
        onActivate = {
            remaining = minutes * 60_000L
            deadline = SystemClock.elapsedRealtime() + remaining
        },
        onReset = { deadline = 0L })
}

@Composable
private fun ButtonScreen(
    minutes: Int, active: Boolean, remainingMillis: Long,
    onMinutesChange: (Int) -> Unit, onActivate: () -> Unit, onReset: () -> Unit
) {
    val accent by animateColorAsState(if (active) Green else Red, label = "buttonColor")
    val seconds = if (active) (remainingMillis + 999L) / 1000L else minutes * 60L
    val time = String.format(Locale.ROOT, "%02d:%02d", seconds / 60, seconds % 60)
    Surface(Modifier.fillMaxSize(), color = Paper, contentColor = Ink) {
        Box(Modifier.safeDrawingPadding(), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier.widthIn(max = 480.dp).fillMaxWidth()
                    .verticalScroll(rememberScrollState()).padding(horizontal = 28.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp).background(Red, CircleShape))
                    Spacer(Modifier.width(10.dp))
                    Text("RED BUTTON", fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                }
                Spacer(Modifier.height(42.dp))
                Text(if (active) "Todo en marcha." else "Un toque para empezar.",
                    fontSize = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.8).sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(Modifier.height(10.dp))
                Text(if (active) "Tu tiempo está corriendo. Tú sigue con lo tuyo."
                    else "Activa tu intervalo y sigue con tu día.", fontSize = 14.sp, color = Muted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(Modifier.height(36.dp))
                Surface(color = accent.copy(alpha = 0.09f), shape = CircleShape) {
                    Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(6.dp).background(accent, CircleShape))
                        Spacer(Modifier.width(8.dp))
                        Text(if (active) "Intervalo activo" else "Listo para activar", color = accent,
                            fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(Modifier.height(24.dp))
                Box(Modifier.widthIn(max = 300.dp).fillMaxWidth().aspectRatio(1f),
                    contentAlignment = Alignment.Center) {
                    Box(Modifier.fillMaxSize().background(accent.copy(alpha = 0.045f), CircleShape))
                    Box(Modifier.fillMaxSize(0.9f).background(accent.copy(alpha = 0.075f), CircleShape))
                    Surface(onClick = { if (!active) onActivate() }, enabled = !active,
                        modifier = Modifier.fillMaxSize(0.78f)
                            .shadow(18.dp, CircleShape, ambientColor = accent, spotColor = accent)
                            .semantics { contentDescription = if (active) "Temporizador activo" else "Activar temporizador" },
                        shape = CircleShape, color = accent, contentColor = Color.White) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center) {
                            PowerSymbol()
                            Spacer(Modifier.height(16.dp))
                            Text(if (active) "ACTIVO" else "ACTIVAR", fontWeight = FontWeight.Bold,
                                fontSize = 14.sp, letterSpacing = 2.sp)
                        }
                    }
                }
                Spacer(Modifier.height(25.dp))
                Text(time, fontSize = 52.sp, fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Light, letterSpacing = (-2).sp)
                Text(if (active) "TIEMPO RESTANTE" else "DURACIÓN DEL INTERVALO", color = Muted,
                    fontSize = 10.sp, letterSpacing = 1.8.sp)
                Spacer(Modifier.height(28.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(15, 30, 60).forEach { duration ->
                        val selected = minutes == duration
                        Surface(onClick = { onMinutesChange(duration) }, enabled = !active,
                            shape = RoundedCornerShape(14.dp),
                            color = if (selected) Ink else Color.White,
                            contentColor = if (selected) Color.White else Muted,
                            modifier = Modifier.weight(1f)) {
                            Text("$duration min", Modifier.padding(vertical = 13.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                if (active) {
                    TextButton(onClick = onReset, colors = ButtonDefaults.textButtonColors(contentColor = Muted)) {
                        Text("Restablecer intervalo")
                    }
                } else {
                    Text("El botón volverá a rojo al terminar.", color = Muted, fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 14.dp))
                }
            }
        }
    }
}

@Composable
private fun PowerSymbol() {
    Canvas(Modifier.size(42.dp)) {
        val line = 3.dp.toPx()
        drawArc(Color.White, -50f, 280f, false,
            topLeft = Offset(size.width * 0.14f, size.height * 0.14f),
            size = Size(size.width * 0.72f, size.height * 0.72f),
            style = Stroke(line, cap = StrokeCap.Round))
        drawLine(Color.White, Offset(size.width / 2, size.height * 0.05f),
            Offset(size.width / 2, size.height * 0.48f), line, StrokeCap.Round)
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun ReadyPreview() {
    RedButtonAppTheme { ButtonScreen(30, false, 0L, {}, {}, {}) }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun ActivePreview() {
    RedButtonAppTheme { ButtonScreen(30, true, 1_245_000L, {}, {}, {}) }
}
