package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TaskEntity
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AccentPalette
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDialog(
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        description: String?,
        category: String,
        targetDateTime: Long,
        repeatType: String,
        colorAccentHex: String,
        customReminderTime: Long?,
        urgency: String
    ) -> Unit,
    taskToEdit: TaskEntity? = null
) {
    var title by remember { mutableStateOf(taskToEdit?.title ?: "") }
    var description by remember { mutableStateOf(taskToEdit?.description ?: "") }
    var showValidationAlert by remember { mutableStateOf(false) }
    
    val dynamicCategories = remember {
        val initialList = mutableListOf("Game", "Olahraga", "Belajar", "Kerja", "Lainnya")
        taskToEdit?.category?.let { cat ->
            if (!initialList.contains(cat)) {
                initialList.add(initialList.size - 1, cat)
            }
        }
        mutableStateListOf(*initialList.toTypedArray())
    }
    var selectedCategory by remember { mutableStateOf(taskToEdit?.category ?: dynamicCategories[0]) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    
    val urgencies = listOf("LOW", "MEDIUM", "HIGH")
    var selectedUrgency by remember { mutableStateOf(taskToEdit?.urgency ?: "MEDIUM") }
    
    val repeatOptions = listOf("NONE", "DAILY", "WEEKLY", "MONTHLY")
    var selectedRepeat by remember { mutableStateOf(taskToEdit?.repeatType ?: "NONE") }
    
    // Default accent color
    var selectedPalette by remember {
        val matchingPalette = AccentPalette.values().find { it.hexString == taskToEdit?.colorAccentHex }
        mutableStateOf(matchingPalette ?: AccentPalette.CYBER_PURPLE)
    }

    // Deadline presets
    val timePresets = listOf(
        PresetOption("Hari Ini (+4 Jam)", 4 * 3600 * 1000L),
        PresetOption("Hari Ini (17:00)", getTodayTimeMillis(17, 0)),
        PresetOption("Besok (09:00)", getTomorrowTimeMillis(9, 0)),
        PresetOption("Besok (17:00)", getTomorrowTimeMillis(17, 0)),
        PresetOption("1 Minggu Lagi", 7 * 24 * 3600 * 1000L),
        PresetOption("1 Bulan Lagi", 30L * 24 * 3600 * 1000L)
    )
    var selectedPresetIndex by remember { mutableIntStateOf(if (taskToEdit != null) -1 else 0) }
    
    // Custom deadline selected by calendar picker
    var customDeadlineMillis by remember { mutableStateOf<Long?>(taskToEdit?.targetDateTime) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Custom manual reminder setup (checked = custom alarm active)
    var enableCustomReminder by remember { mutableStateOf(taskToEdit?.customReminderTime != null) }
    val reminderPresets = listOf(
        PresetOption("15 Menit Lagi", 15 * 60 * 1000L),
        PresetOption("1 Jam Lagi", 1 * 3600 * 1000L),
        PresetOption("3 Jam Lagi", 3 * 3600 * 1000L),
        PresetOption("Besok Pagi", getTomorrowTimeMillis(8, 0) - System.currentTimeMillis())
    )
    var selectedReminderPresetIndex by remember { mutableIntStateOf(0) }

    val scrollState = rememberScrollState()

    // Date Picker Dialog trigger
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        customDeadlineMillis = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }
                ) {
                    Text("PILIH", fontWeight = FontWeight.Bold, color = selectedPalette.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("BATAL")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showValidationAlert) {
        AlertDialog(
            onDismissRequest = { showValidationAlert = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Peringatan",
                        tint = selectedPalette.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Judul Tugas Kosong", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text("Kolom judul tugas wajib diisi agar pengingat dan alarm dapat diatur dengan benar.")
            },
            confirmButton = {
                TextButton(
                    onClick = { showValidationAlert = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = selectedPalette.primary)
                ) {
                    Text("Mengerti", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { 
                showAddCategoryDialog = false
                newCategoryName = ""
            },
            title = { Text("Tambah Kategori Baru", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("Nama Kategori") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = selectedPalette.primary,
                        focusedLabelColor = selectedPalette.primary
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleaned = newCategoryName.trim()
                        if (cleaned.isNotEmpty()) {
                            if (!dynamicCategories.contains(cleaned)) {
                                val index = dynamicCategories.indexOf("Lainnya")
                                if (index != -1) {
                                    dynamicCategories.add(index, cleaned)
                                } else {
                                    dynamicCategories.add(cleaned)
                                }
                            }
                            selectedCategory = cleaned
                        }
                        showAddCategoryDialog = false
                        newCategoryName = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = selectedPalette.primary),
                    enabled = newCategoryName.trim().isNotEmpty()
                ) {
                    Text(
                        "Tambah", 
                        fontWeight = FontWeight.Bold,
                        color = if (selectedPalette == AccentPalette.BOLD_NEON) Color.Black else Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showAddCategoryDialog = false
                    newCategoryName = ""
                }) {
                    Text("Batal", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, selectedPalette.primary.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                .testTag("task_creator_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (taskToEdit != null) "Edit Tugas" else "Tambah Tugas Baru",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = selectedPalette.primary,
                            letterSpacing = 0.5.sp
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Scrollable content form
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Task Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Judul Tugas (wajib)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("task_title_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = selectedPalette.primary,
                            focusedLabelColor = selectedPalette.primary
                        ),
                        singleLine = true
                    )

                    // 2. Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Deskripsi / Catatan (opsional)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("task_description_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = selectedPalette.primary,
                            focusedLabelColor = selectedPalette.primary
                        ),
                        maxLines = 3
                    )

                    // 3. Category selector row
                    Text("Kategori", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        dynamicCategories.forEach { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat) },
                                leadingIcon = {
                                    val icon = when (cat) {
                                        "Game" -> Icons.Default.Gamepad
                                        "Olahraga" -> Icons.Default.FitnessCenter
                                        "Belajar" -> Icons.Default.School
                                        "Kerja" -> Icons.Default.Work
                                        else -> Icons.Default.Assignment
                                    }
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = selectedPalette.primary,
                                    selectedLabelColor = if (selectedPalette == AccentPalette.BOLD_NEON) Color.Black else Color.White,
                                    selectedLeadingIconColor = if (selectedPalette == AccentPalette.BOLD_NEON) Color.Black else Color.White,
                                    iconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            )
                        }
                        
                        FilterChip(
                            selected = false,
                            onClick = { showAddCategoryDialog = true },
                            label = { Text("Tambah Kategori", fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Tambah Kategori Baru",
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                labelColor = selectedPalette.primary,
                                iconColor = selectedPalette.primary
                            )
                        )
                    }

                    // 4. Color Theme selector row
                    Text("Pilih Warna Aksen", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AccentPalette.values().forEach { palette ->
                            val isSelected = selectedPalette == palette
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(palette.primary)
                                    .clickable { selectedPalette = palette }
                                    .border(
                                        3.dp,
                                        if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surface)
                                    )
                                }
                            }
                        }
                    }

                    // 5. Urgency Segmented Selector
                    Text("Urgency / Prioritas", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        urgencies.forEach { urg ->
                            val isSelected = selectedUrgency == urg
                            val color = when (urg) {
                                "HIGH" -> Color(0xFFFF4D4D)
                                "MEDIUM" -> Color(0xFFFF9F1C)
                                else -> Color(0xFF00C896)
                            }
                            OutlinedButton(
                                onClick = { selectedUrgency = urg },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) color.copy(alpha = 0.15f) else Color.Transparent
                                ),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) color else MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Text(urg, color = if (isSelected) color else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // 6. Target DateTime Presets with Custom Calendar Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tenggat Waktu / Deadline", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        TextButton(
                            onClick = { showDatePicker = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = selectedPalette.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Pilih Tanggal",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pilih Tanggal", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Render custom chosen date if exists
                            if (customDeadlineMillis != null) {
                                val sdf = java.text.SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                val formatted = sdf.format(Date(customDeadlineMillis!!))
                                InputChip(
                                    selected = selectedPresetIndex == -1,
                                    onClick = { selectedPresetIndex = -1 },
                                    label = { Text("Kustom: $formatted") },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Hapus",
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable {
                                                    customDeadlineMillis = null
                                                    if (selectedPresetIndex == -1) {
                                                        selectedPresetIndex = 0
                                                    }
                                                }
                                        )
                                    }
                                )
                            }

                            timePresets.forEachIndexed { idx, preset ->
                                val isSelected = selectedPresetIndex == idx && customDeadlineMillis == null
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { 
                                        selectedPresetIndex = idx
                                        customDeadlineMillis = null // Clear custom if preset is chosen
                                    },
                                    label = { Text(preset.label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = selectedPalette.primary,
                                        selectedLabelColor = if (selectedPalette == AccentPalette.BOLD_NEON) Color.Black else Color.White
                                    )
                                )
                            }
                        }
                    }

                    // 7. Recurrence rule selector
                    Text("Perulangan Rutin", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeatOptions.forEach { opt ->
                            val isSelected = selectedRepeat == opt
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedRepeat = opt },
                                label = { Text(if (opt == "NONE") "Tidak Berulang" else opt) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = selectedPalette.primary,
                                    selectedLabelColor = if (selectedPalette == AccentPalette.BOLD_NEON) Color.Black else Color.White
                                )
                            )
                        }
                    }

                    // 8. Custom Manual Alarm Switch & Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Alarm, contentDescription = "Manual Alarm", tint = selectedPalette.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Setel Pengingat Manual", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }
                        Switch(
                            checked = enableCustomReminder,
                            onCheckedChange = { enableCustomReminder = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = selectedPalette.primary
                            )
                        )
                    }

                    if (enableCustomReminder) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            reminderPresets.forEachIndexed { idx, preset ->
                                val isSelected = selectedReminderPresetIndex == idx
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedReminderPresetIndex = idx },
                                    label = { Text(preset.label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = selectedPalette.primary,
                                        selectedLabelColor = if (selectedPalette == AccentPalette.BOLD_NEON) Color.Black else Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Bottom actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Batal")
                    }
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                showValidationAlert = true
                            } else {
                                // Calculate deadline timestamp
                                val deadlineTime = if (customDeadlineMillis != null) {
                                    customDeadlineMillis!!
                                } else {
                                    val activeDeadlinePreset = timePresets[selectedPresetIndex]
                                    if (activeDeadlinePreset.offset > 0 && (activeDeadlinePreset.label.contains("+") || activeDeadlinePreset.label.contains("Lagi"))) {
                                        System.currentTimeMillis() + activeDeadlinePreset.offset
                                    } else {
                                        activeDeadlinePreset.offset // Static calculated timestamp
                                    }
                                }

                                // Calculate custom reminder timestamp
                                val customReminderTime = if (enableCustomReminder) {
                                    val activeReminderPreset = reminderPresets[selectedReminderPresetIndex]
                                    System.currentTimeMillis() + activeReminderPreset.offset
                                } else null

                                onSave(
                                    title.trim(),
                                    description.trim().ifEmpty { null },
                                    selectedCategory, // Clean category string, no emoji prefix!
                                    deadlineTime,
                                    selectedRepeat,
                                    selectedPalette.hexString,
                                    customReminderTime,
                                    selectedUrgency
                                )
                            }
                        },
                        enabled = true,
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("save_task_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = selectedPalette.primary)
                    ) {
                        Text(
                            text = "Simpan Tugas",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedPalette == AccentPalette.BOLD_NEON) Color.Black else Color.White
                        )
                    }
                }
            }
        }
    }
}

// Data class helper for presets
data class PresetOption(val label: String, val offset: Long)

private fun getTodayTimeMillis(hour: Int, minute: Int): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, hour)
    cal.set(Calendar.MINUTE, minute)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    
    // If targeted today time is in past, schedule for tomorrow
    if (cal.timeInMillis < System.currentTimeMillis()) {
        cal.add(Calendar.DAY_OF_YEAR, 1)
    }
    return cal.timeInMillis
}

private fun getTomorrowTimeMillis(hour: Int, minute: Int): Long {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, 1)
    cal.set(Calendar.HOUR_OF_DAY, hour)
    cal.set(Calendar.MINUTE, minute)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}
