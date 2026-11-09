package com.example.pulseup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulseup.data.CompletadoRequest
import com.example.pulseup.data.Habito
import com.example.pulseup.data.HabitoRequest
import com.example.pulseup.data.ResumenDia
import com.example.pulseup.data.RetrofitClient
import com.example.pulseup.data.Session
import com.example.pulseup.ui.theme.PuBgSurface
import com.example.pulseup.ui.theme.PuBorder
import com.example.pulseup.ui.theme.PuTextSecondary
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.util.Calendar

// Los colores (PuBgBase, PuBgCard, PuAccent, etc.) salen de los que ya tienes
// declarados en el paquete com.example.pulseup. Aquí no se redefinen.

// ---------- Datos fijos de la pantalla ----------

// Categorías que se pueden elegir al crear un hábito (se guardan tal cual en el backend)
private val CATEGORIAS = listOf("Ejercicio", "Nutrición", "Sueño")

// Las tres tarjetas de módulos. "clave" es la categoría guardada; "etiqueta" es lo que se ve
private data class ModuloUi(val clave: String, val etiqueta: String, val emoji: String)

private val MODULOS = listOf(
    ModuloUi("Ejercicio", "Ejercicio", "🏃"),
    ModuloUi("Nutrición", "Alimentación", "🥗"),
    ModuloUi("Sueño", "Sueño", "🌙")
)

private val NOMBRES_DIA =
    listOf("lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo")
private val LETRAS_DIA = listOf("L", "M", "M", "J", "V", "S", "D")
private val NOMBRES_MES = listOf(
    "enero", "febrero", "marzo", "abril", "mayo", "junio",
    "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
)

// ---------- Funciones auxiliares ----------

private fun emojiDe(categoria: String): String {
    val c = categoria.lowercase()
    return when {
        c.contains("ejerc") -> "🏃"
        c.contains("nutri") || c.contains("alim") -> "🥗"
        c.contains("sue") -> "🌙"
        else -> "✨"
    }
}

// Saludo según la hora del dispositivo
private fun saludoPorHora(): String {
    val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hora) {
        in 5..11 -> "Buenos días"
        in 12..17 -> "Buenas tardes"
        else -> "Buenas noches"
    }
}

// "martes 3 de noviembre"
private fun fechaLarga(f: LocalDate): String =
    "${NOMBRES_DIA[f.dayOfWeek.value - 1]} ${f.dayOfMonth} de ${NOMBRES_MES[f.monthValue - 1]}"

// Texto bajo la semana: cambia si el día es hoy, pasado o futuro
private fun etiquetaDia(f: LocalDate, hoy: LocalDate): String = when {
    f == hoy -> "Hoy, ${fechaLarga(f)}"
    f.isBefore(hoy) -> "Lo que hiciste el ${fechaLarga(f)}"
    else -> "Programa hábitos para el ${fechaLarga(f)}"
}

// "Noviembre 2026"
private fun tituloMes(m: YearMonth): String =
    NOMBRES_MES[m.monthValue - 1].replaceFirstChar { it.uppercase() } + " " + m.year

// Lunes de la semana de una fecha (las semanas empiezan en lunes)
private fun lunesDe(f: LocalDate): LocalDate = f.minusDays((f.dayOfWeek.value - 1).toLong())

// Primer día de la cuadrícula del calendario: el lunes de la semana del día 1 del mes
private fun inicioCuadricula(mes: YearMonth): LocalDate = lunesDe(mes.atDay(1))

// ---------- Pantalla ----------

@Composable
fun HomeScreen(
    nombre: String = "Usuario",
    onHabitoClick: (Habito) -> Unit = {},
    onAdminClick: () -> Unit = {},
    onCerrarSesion: () -> Unit = {},
    onEditarPerfilClick: () -> Unit = {},
    onCambiarPasswordClick: () -> Unit = {},
    onNotificacionesClick: () -> Unit = {},
    onConfiguracionClick: () -> Unit = {},
    onProgresoClick: () -> Unit = {}
) {
    val hoy = remember { LocalDate.now() }

    var fechaSel by remember { mutableStateOf(hoy) }
    var mesVisible by remember { mutableStateOf(YearMonth.from(hoy)) }
    var resumen by remember { mutableStateOf<Map<String, ResumenDia>>(emptyMap()) }
    var filtro by remember { mutableStateOf<String?>(null) }

    var listaHabitos by remember { mutableStateOf<List<Habito>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var mensaje by remember { mutableStateOf("") }
    var guardando by remember { mutableStateOf(false) }

    var mostrarNuevo by remember { mutableStateOf(false) }
    var mostrarPerfil by remember { mutableStateOf(false) }
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    var textoNuevo by remember { mutableStateOf("") }
    var categoriaNueva by remember { mutableStateOf(CATEGORIAS.first()) }

    val scope = rememberCoroutineScope()

    // Hábitos del día seleccionado
    suspend fun cargarHabitos() {
        val resp = RetrofitClient.api.getHabitos(Session.bearer(), fechaSel.toString())
        if (resp.isSuccessful) {
            listaHabitos = resp.body()?.habitos ?: emptyList()
        }
    }

    // Resumen de las 6 semanas visibles del calendario (para las llamas)
    suspend fun cargarResumen() {
        val desde = inicioCuadricula(mesVisible)
        val hasta = desde.plusDays(41)
        val resp = RetrofitClient.api.getResumen(Session.bearer(), desde.toString(), hasta.toString())
        if (resp.isSuccessful) {
            resumen = (resp.body()?.dias ?: emptyList()).associateBy { it.fecha }
        }
    }

    // Se vuelve a ejecutar cada vez que cambia el día seleccionado
    LaunchedEffect(fechaSel) {
        cargando = true
        mensaje = ""
        try {
            cargarHabitos()
            cargando = false
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            mensaje = "No se pudieron cargar los hábitos"
            cargando = false
        }
    }

    // Se vuelve a ejecutar cada vez que cambia el mes visible
    LaunchedEffect(mesVisible) {
        try {
            cargarResumen()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Si falla, simplemente no se muestran las llamas
        }
    }

    fun seleccionarDia(f: LocalDate) {
        fechaSel = f
        mesVisible = YearMonth.from(f)
    }

    fun guardarHabito() {
        if (textoNuevo.isBlank()) return
        mensaje = ""
        guardando = true
        scope.launch {
            try {
                val resp = RetrofitClient.api.crearHabito(
                    Session.bearer(),
                    HabitoRequest(
                        descripcion = textoNuevo.trim(),
                        categoria = categoriaNueva,
                        fecha = fechaSel.toString()
                    )
                )
                if (resp.isSuccessful) {
                    textoNuevo = ""
                    mostrarNuevo = false
                    cargarHabitos()
                    cargarResumen()
                } else {
                    mensaje = "Error al guardar (código ${resp.code()})"
                    mostrarNuevo = false
                }
            } catch (e: Exception) {
                mensaje = "No se pudo conectar: ${e.message}"
                mostrarNuevo = false
            } finally {
                guardando = false
            }
        }
    }

    fun alternarHabito(h: Habito) {
        scope.launch {
            try {
                val resp = RetrofitClient.api.marcarCompletado(
                    Session.bearer(),
                    h.id,
                    CompletadoRequest(!h.completado)
                )
                if (resp.isSuccessful) {
                    cargarHabitos()
                    cargarResumen()
                } else {
                    mensaje = "No se pudo actualizar (código ${resp.code()})"
                }
            } catch (e: Exception) {
                mensaje = "No se pudo conectar: ${e.message}"
            }
        }
    }

    // ---------- Diálogo: nuevo hábito ----------
    if (mostrarNuevo) {
        AlertDialog(
            onDismissRequest = { if (!guardando) mostrarNuevo = false },
            containerColor = PuBgCard,
            title = { Text("Nuevo hábito", color = PuTextPrimary) },
            text = {
                Column {
                    Text(
                        "Para el ${fechaLarga(fechaSel)}",
                        color = PuTextMuted,
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = textoNuevo,
                        onValueChange = { textoNuevo = it },
                        label = { Text("Ej: 30 min de caminata") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PuTextPrimary,
                            unfocusedTextColor = PuTextPrimary,
                            focusedBorderColor = PuAccent,
                            unfocusedBorderColor = PuBorder,
                            focusedLabelColor = PuAccent,
                            unfocusedLabelColor = PuTextMuted,
                            cursorColor = PuAccent
                        )
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CATEGORIAS.forEach { cat ->
                            val activa = cat == categoriaNueva
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (activa) PuAccentDark else PuBgBase)
                                    .border(
                                        0.5.dp,
                                        if (activa) PuAccent else PuBorder,
                                        RoundedCornerShape(20.dp)
                                    )
                                    .clickable { categoriaNueva = cat }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    cat,
                                    fontSize = 12.sp,
                                    color = if (activa) PuAccentMid else PuTextSecondary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { guardarHabito() },
                    enabled = !guardando && textoNuevo.isNotBlank()
                ) {
                    Text(if (guardando) "Guardando..." else "Agregar", color = PuAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarNuevo = false }, enabled = !guardando) {
                    Text("Cancelar", color = PuTextMuted)
                }
            }
        )
    }

    // ---------- Diálogo: menú de perfil ----------
    if (mostrarPerfil) {
        AlertDialog(
            onDismissRequest = { mostrarPerfil = false },
            containerColor = PuBgCard,
            title = { Text("Tu cuenta", color = PuTextPrimary) },
            text = {
                Column {
                    TextButton(onClick = { mostrarPerfil = false; onEditarPerfilClick() }) {
                        Text("Editar datos", color = PuAccent)
                    }
                    TextButton(onClick = { mostrarPerfil = false; onCambiarPasswordClick() }) {
                        Text("Cambiar contraseña", color = PuAccent)
                    }
                    if (Session.isAdmin) {
                        TextButton(onClick = { mostrarPerfil = false; onAdminClick() }) {
                            Text("Administrar usuarios", color = PuAccent)
                        }
                    }
                    TextButton(onClick = { mostrarPerfil = false; mostrarConfirmacion = true }) {
                        Text("Cerrar sesión", color = PuAccentMid)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarPerfil = false }) {
                    Text("Cerrar", color = PuTextMuted)
                }
            }
        )
    }

    // ---------- Diálogo: confirmar cierre de sesión ----------
    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            containerColor = PuBgCard,
            title = { Text("¿Seguro que quieres cerrar sesión?", color = PuTextPrimary) },
            confirmButton = {
                TextButton(onClick = {
                    mostrarConfirmacion = false
                    Session.cerrarSesion()
                    onCerrarSesion()
                }) {
                    Text("Cerrar sesión", color = PuAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacion = false }) {
                    Text("Cancelar", color = PuTextMuted)
                }
            }
        )
    }

    // Lo que se ve en la lista: todos los del día, o solo los de la categoría elegida
    val visibles = if (filtro == null) {
        listaHabitos
    } else {
        listaHabitos.filter { it.categoria.equals(filtro, ignoreCase = true) }
    }
    val hechos = listaHabitos.count { it.completado }

    Scaffold(
        containerColor = PuBgBase,
        bottomBar = {
            PuBottomBar(
                onProgresoClick = onProgresoClick,
                onAgregarClick = { mostrarNuevo = true },
                onNotificacionesClick = onNotificacionesClick,
                onPerfilClick = { mostrarPerfil = true }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Encabezado(nombre, onConfiguracionClick) }

            item {
                TiraSemana(
                    seleccionada = fechaSel,
                    resumen = resumen,
                    onDiaClick = { seleccionarDia(it) }
                )
            }

            item {
                Text(
                    etiquetaDia(fechaSel, hoy),
                    color = PuTextMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item { FraseMotivadora(Session.frase) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MODULOS.forEach { modulo ->
                        val delModulo = listaHabitos.filter {
                            it.categoria.equals(modulo.clave, ignoreCase = true)
                        }
                        TarjetaModulo(
                            modulo = modulo,
                            hechos = delModulo.count { it.completado },
                            total = delModulo.size,
                            activa = filtro == modulo.clave,
                            onClick = { filtro = if (filtro == modulo.clave) null else modulo.clave },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                TarjetaProgreso(
                    etiqueta = if (fechaSel == hoy) "Progreso de hoy"
                    else if (fechaSel.isBefore(hoy)) "Progreso del día"
                    else "Planeado",
                    hechos = hechos,
                    total = listaHabitos.size
                )
            }

            item {
                CalendarioMes(
                    mes = mesVisible,
                    seleccionada = fechaSel,
                    hoy = hoy,
                    resumen = resumen,
                    onMesAnterior = { mesVisible = mesVisible.minusMonths(1) },
                    onMesSiguiente = { mesVisible = mesVisible.plusMonths(1) },
                    onDiaClick = { seleccionarDia(it) }
                )
            }

            item {
                Text(
                    when {
                        filtro != null -> "Hábitos de $filtro"
                        fechaSel == hoy -> "Mis hábitos de hoy"
                        else -> "Hábitos del día"
                    },
                    color = PuTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            if (mensaje.isNotEmpty()) {
                item { Text(mensaje, color = PuAccentMid, fontSize = 13.sp) }
            }
            if (cargando) {
                item { Text("Cargando...", color = PuTextMuted, fontSize = 13.sp) }
            } else if (visibles.isEmpty() && mensaje.isEmpty()) {
                item {
                    Text(
                        "No hay hábitos para este día. Toca + para agregar uno.",
                        color = PuTextMuted,
                        fontSize = 13.sp
                    )
                }
            }
            items(visibles, key = { it.id }) { habito ->
                TarjetaHabito(
                    habito = habito,
                    onClick = { onHabitoClick(habito) },
                    onToggle = { alternarHabito(habito) }
                )
            }
        }
    }
}

// ---------- Componentes ----------

@Composable
private fun Encabezado(nombre: String, onConfiguracionClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("${remember { saludoPorHora() }},", color = PuTextMuted, fontSize = 13.sp)
            Text(
                nombre,
                color = PuTextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PuBgCard)
                .border(0.5.dp, PuBorder, RoundedCornerShape(10.dp))
                .clickable { onConfiguracionClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Settings,
                contentDescription = "Configuración",
                tint = PuAccentMid,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Los 7 días de la semana del día seleccionado
@Composable
private fun TiraSemana(
    seleccionada: LocalDate,
    resumen: Map<String, ResumenDia>,
    onDiaClick: (LocalDate) -> Unit
) {
    val lunes = lunesDe(seleccionada)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (i in 0 until 7) {
            val dia = lunes.plusDays(i.toLong())
            val r = resumen[dia.toString()]
            val sel = dia == seleccionada
            val cumplido = r != null && r.total > 0 && r.completados == r.total
            val punto = when {
                cumplido -> PuAccent
                r != null && r.total > 0 -> PuTextMuted
                else -> Color.Transparent
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (sel) PuAccentDark else Color.Transparent)
                    .clickable { onDiaClick(dia) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(LETRAS_DIA[i], fontSize = 11.sp, color = if (sel) PuAccentMid else PuTextMuted)
                Text(
                    dia.dayOfMonth.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (sel) PuTextPrimary else PuTextSecondary
                )
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(punto)
                )
            }
        }
    }
}

@Composable
private fun FraseMotivadora(frase: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = frase,
            color = PuTextSecondary,
            fontSize = 14.sp,
            lineHeight = 21.sp,
            fontStyle = FontStyle.Italic,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TarjetaModulo(
    modulo: ModuloUi,
    hechos: Int,
    total: Int,
    activa: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PuBgCard),
        border = BorderStroke(0.5.dp, if (activa) PuAccent else PuBorder),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PuAccentDark),
                contentAlignment = Alignment.Center
            ) {
                Text(modulo.emoji, fontSize = 18.sp)
            }
            Text(modulo.etiqueta, color = PuTextPrimary, fontSize = 11.sp, maxLines = 1)
            Text(
                if (total == 0) "Sin hábitos" else "$hechos de $total",
                color = PuTextMuted,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun TarjetaProgreso(etiqueta: String, hechos: Int, total: Int) {
    val fraccion = if (total == 0) 0f else hechos.toFloat() / total
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PuAccentDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(etiqueta, color = PuAccentMid, fontSize = 13.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "$hechos de $total",
                        color = PuTextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = PuAccent,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(PuBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraccion)
                        .fillMaxHeight()
                        .background(PuAccent)
                )
            }
        }
    }
}

// Calendario del mes: 6 filas de 7 días, con llama morada en los días cumplidos
@Composable
private fun CalendarioMes(
    mes: YearMonth,
    seleccionada: LocalDate,
    hoy: LocalDate,
    resumen: Map<String, ResumenDia>,
    onMesAnterior: () -> Unit,
    onMesSiguiente: () -> Unit,
    onDiaClick: (LocalDate) -> Unit
) {
    val inicio = inicioCuadricula(mes)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PuBgCard),
        border = BorderStroke(0.5.dp, PuBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onMesAnterior) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Mes anterior",
                        tint = PuAccentMid
                    )
                }
                Text(
                    tituloMes(mes),
                    color = PuTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                IconButton(onClick = onMesSiguiente) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Mes siguiente",
                        tint = PuAccentMid
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                LETRAS_DIA.forEach { letra ->
                    Text(
                        letra,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = PuTextMuted,
                        fontSize = 11.sp
                    )
                }
            }
            Spacer(Modifier.height(4.dp))

            for (fila in 0 until 6) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0 until 7) {
                        val dia = inicio.plusDays((fila * 7 + col).toLong())
                        val r = resumen[dia.toString()]
                        CeldaDia(
                            fecha = dia,
                            delMes = YearMonth.from(dia) == mes,
                            seleccionada = dia == seleccionada,
                            esHoy = dia == hoy,
                            fuego = r != null && r.total > 0 && r.completados == r.total,
                            onClick = { onDiaClick(dia) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = PuAccent,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("Cumpliste los objetivos del día", color = PuTextMuted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun CeldaDia(
    fecha: LocalDate,
    delMes: Boolean,
    seleccionada: Boolean,
    esHoy: Boolean,
    fuego: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val forma = RoundedCornerShape(10.dp)
    Column(
        modifier = modifier
            .height(40.dp)
            .padding(1.dp)
            .clip(forma)
            .background(if (seleccionada) PuAccentDark else Color.Transparent)
            .then(
                if (esHoy && !seleccionada) Modifier.border(0.5.dp, PuAccent, forma)
                else Modifier
            )
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            fecha.dayOfMonth.toString(),
            fontSize = 12.sp,
            color = when {
                seleccionada -> PuTextPrimary
                delMes -> PuTextSecondary
                else -> PuTextMuted
            }
        )
        if (fuego) {
            Icon(
                Icons.Default.LocalFireDepartment,
                contentDescription = "Cumplió los objetivos",
                tint = if (delMes) PuAccent else PuAccentDark,
                modifier = Modifier.size(12.dp)
            )
        } else {
            Spacer(Modifier.size(12.dp))
        }
    }
}

@Composable
private fun TarjetaHabito(
    habito: Habito,
    onClick: () -> Unit,
    onToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PuBgCard),
        border = BorderStroke(0.5.dp, PuBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PuAccentDark),
                contentAlignment = Alignment.Center
            ) {
                Text(emojiDe(habito.categoria), fontSize = 18.sp)
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    habito.descripcion,
                    color = PuTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(habito.categoria, color = PuTextMuted, fontSize = 12.sp)
            }
            if (habito.completado) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(PuAccent)
                        .clickable { onToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Hecho",
                        tint = PuAccentDark,
                        modifier = Modifier.size(14.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, PuBorder, CircleShape)
                        .clickable { onToggle() }
                )
            }
        }
    }
}

@Composable
private fun PuBottomBar(
    onProgresoClick: () -> Unit,
    onAgregarClick: () -> Unit,
    onNotificacionesClick: () -> Unit,
    onPerfilClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PuBgSurface)
            .navigationBarsPadding()
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(PuBorder)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ItemNav("Inicio", Icons.Default.Home, activo = true, onClick = {})
                ItemNav(
                    "Progreso",
                    Icons.Default.DateRange,
                    activo = false,
                    onClick = onProgresoClick
                )
            }

            Box(
                modifier = Modifier
                    .offset(y = (-14).dp)
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PuAccent)
                    .clickable { onAgregarClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Agregar hábito",
                    tint = PuAccentDark,
                    modifier = Modifier.size(24.dp)
                )
            }

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ItemNav("Avisos", Icons.Default.Notifications, activo = false, onClick = onNotificacionesClick)
                ItemNav("Perfil", Icons.Default.Person, activo = false, onClick = onPerfilClick)
            }
        }
    }
}

@Composable
private fun ItemNav(
    etiqueta: String,
    icono: ImageVector,
    activo: Boolean,
    onClick: () -> Unit
) {
    val color = if (activo) PuAccent else PuTextMuted
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Icon(icono, contentDescription = etiqueta, tint = color, modifier = Modifier.size(22.dp))
        Text(etiqueta, color = color, fontSize = 11.sp)
    }
}