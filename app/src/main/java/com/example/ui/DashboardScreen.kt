package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.TaskEntity
import androidx.compose.material.icons.filled.Delete
import kotlinx.coroutines.launch
import com.example.ui.components.TaskCard
import com.example.ui.components.TaskDialog
import com.example.ui.theme.AccentPalette
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.filteredTasks.collectAsStateWithLifecycle()
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedTimeFilter by viewModel.selectedTimeFilter.collectAsStateWithLifecycle()
    
    val currentPalette by viewModel.currentPalette.collectAsStateWithLifecycle()
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val isOled by viewModel.isOledMode.collectAsStateWithLifecycle()
    val username by viewModel.username.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var onboardingUsernameInput by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var taskToDelete by remember { mutableStateOf<TaskEntity?>(null) }
    var taskToEdit by remember { mutableStateOf<TaskEntity?>(null) }
    var showEditUsernameDialog by remember { mutableStateOf(false) }
    var editUsernameInput by remember { mutableStateOf("") }

    // Task statistics
    val totalTasks = allTasks.size
    val completedTasks = allTasks.count { it.isCompleted }
    val completionProgress = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0.0f
    val animatedProgress by animateFloatAsState(targetValue = completionProgress, label = "progress")

    val formattedDate = remember {
        val sdf = SimpleDateFormat("EEEE, d MMMM", Locale("id", "ID"))
        sdf.format(Date())
    }

    // Onboarding Username Dialog
    if (username.isEmpty()) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = {}, // Force filling username
            properties = androidx.compose.ui.window.DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, currentPalette.primary)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Halo! Selamat Datang",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = currentPalette.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Silakan masukkan nama Anda untuk sapaan harian dan pengingat.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    OutlinedTextField(
                        value = onboardingUsernameInput,
                        onValueChange = { onboardingUsernameInput = it },
                        label = { Text("Username / Nama Panggilan") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = currentPalette.primary,
                            focusedLabelColor = currentPalette.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            if (onboardingUsernameInput.isNotBlank()) {
                                viewModel.saveUsername(onboardingUsernameInput)
                            }
                        },
                        enabled = onboardingUsernameInput.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = currentPalette.primary)
                    ) {
                        Text(
                            text = "Mulai Sekarang",
                            fontWeight = FontWeight.Bold,
                            color = if (currentPalette == AccentPalette.BOLD_NEON) Color.Black else Color.White
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = currentPalette.primary,
                contentColor = if (currentPalette == AccentPalette.BOLD_NEON) Color.Black else Color.White,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .testTag("add_task_fab")
                    .padding(8.dp)
                    .size(60.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Tugas",
                    tint = if (currentPalette == AccentPalette.BOLD_NEON) Color.Black else Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            // A. Gorgeous Bold Typography Header Row (From Design spec)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = formattedDate.uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Hey, ${username.ifEmpty { "Alex" }} 👋",
                        style = MaterialTheme.typography.displayLarge.copy(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Black
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                editUsernameInput = username
                                showEditUsernameDialog = true
                            }
                    )
                }
                
                // Elegant brush theme icon button per user request
                IconButton(
                    onClick = { showSettingsSheet = true },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(currentPalette.primary.copy(alpha = 0.15f))
                        .border(1.dp, currentPalette.primary.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Brush,
                        contentDescription = "Ganti Tema",
                        tint = currentPalette.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // B. Progress Overview Card (Restyled per Design Spec)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(32.dp), // 2rem
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "DAILY PROGRESS",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${(animatedProgress * 100).toInt()}%",
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 32.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Complete",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        fontWeight = FontWeight.Medium
                                    ),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }
                        
                        // Right side badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(currentPalette.primary.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$completedTasks/$totalTasks Tasks",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = currentPalette.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Progress Bar
                    LinearProgressIndicator(
                        progress = animatedProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = currentPalette.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            // 2. Horizontal category filters
            val categories = listOf("Semua", "Game", "Olahraga", "Belajar", "Kerja", "Lainnya")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory.lowercase() == cat.lowercase()
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) currentPalette.primary else MaterialTheme.colorScheme.surface
                            )
                            .border(
                                1.dp,
                                if (isSelected) currentPalette.primary else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { viewModel.selectedCategory.value = cat }
                            .padding(horizontal = 18.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cat,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isSelected) {
                                if (currentPalette == AccentPalette.BOLD_NEON) Color.Black else Color.White
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            }
                        )
                    }
                }
            }

            // 3. Quick time deadline filters
            val timeFilters = listOf("Semua", "Hari Ini", "Mendatang", "Terlambat")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                timeFilters.forEach { filter ->
                    val isSelected = selectedTimeFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(
                                if (isSelected) currentPalette.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                            )
                            .border(
                                1.dp,
                                if (isSelected) currentPalette.primary else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(30.dp)
                            )
                            .clickable { viewModel.selectedTimeFilter.value = filter }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = filter,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) currentPalette.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Tasks List
            if (tasks.isEmpty()) {
                // Beautiful Empty State
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Kosong",
                            tint = currentPalette.primary.copy(alpha = 0.25f),
                            modifier = Modifier.size(92.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Semua Beres!",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tidak ada tugas aktif di filter ini. Tap tombol + di bawah untuk menambah tugas baru.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("task_list_lazy_column"),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            onToggleComplete = {
                                viewModel.toggleTaskCompletion(task)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = if (!task.isCompleted) "Tugas berhasil diselesaikan" else "Tugas dikembalikan ke aktif",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            },
                            onDelete = { taskToDelete = task },
                            onEdit = { taskToEdit = task }
                        )
                    }
                }
            }
        }

        // 5. Add Task Creator Dialog
        if (showAddDialog) {
            TaskDialog(
                onDismiss = { showAddDialog = false },
                onSave = { title, desc, cat, deadline, repeat, colorHex, customTime, urgency ->
                    viewModel.addTask(title, desc, cat, deadline, repeat, colorHex, customTime, urgency)
                    showAddDialog = false
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Tugas berhasil ditambahkan",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            )
        }

        // 5.5. Edit Task Dialog
        taskToEdit?.let { task ->
            TaskDialog(
                onDismiss = { taskToEdit = null },
                taskToEdit = task,
                onSave = { title, desc, cat, deadline, repeat, colorHex, customTime, urgency ->
                    val updatedTask = task.copy(
                        title = title,
                        description = desc,
                        category = cat,
                        targetDateTime = deadline,
                        repeatType = repeat,
                        colorAccentHex = colorHex,
                        customReminderTime = customTime,
                        urgency = urgency
                    )
                    viewModel.updateTask(updatedTask)
                    taskToEdit = null
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Tugas berhasil diperbarui",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            )
        }

        // 5.6. Change Username Dialog
        if (showEditUsernameDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showEditUsernameDialog = false 
                },
                title = { 
                    Text(
                        text = "Ganti Username", 
                        fontWeight = FontWeight.Bold,
                        color = currentPalette.primary
                    ) 
                },
                text = {
                    Column {
                        Text(
                            text = "Silakan ubah nama panggilan Anda di bawah ini.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = editUsernameInput,
                            onValueChange = { editUsernameInput = it },
                            label = { Text("Username Baru") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = currentPalette.primary,
                                focusedLabelColor = currentPalette.primary
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val cleaned = editUsernameInput.trim()
                            if (cleaned.isNotEmpty()) {
                                viewModel.saveUsername(cleaned)
                                showEditUsernameDialog = false
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Username berhasil diubah",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = currentPalette.primary),
                        enabled = editUsernameInput.trim().isNotEmpty(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Simpan",
                            fontWeight = FontWeight.Bold,
                            color = if (currentPalette == AccentPalette.BOLD_NEON) Color.Black else Color.White
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditUsernameDialog = false }) {
                        Text("Batal", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }

        // 6. Dynamic Theme Selector Settings Sheet
        if (showSettingsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSettingsSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 8.dp, bottom = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Brush, contentDescription = "Personalization", tint = currentPalette.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Kustomisasi Tema",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Palette selection
                    Text("Pilih Aksen Warna", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AccentPalette.values().forEach { palette ->
                            val isSelected = currentPalette == palette
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) palette.primary.copy(alpha = 0.15f) else Color.Transparent
                                    )
                                    .border(
                                        if (isSelected) 2.dp else 1.dp,
                                        if (isSelected) palette.primary else MaterialTheme.colorScheme.outlineVariant,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.currentPalette.value = palette }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(palette.primary)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = palette.displayName,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) palette.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Dark mode & OLED switches
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Mode Gelap (Dark Mode)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                Text("Mengurangi kelelahan mata di malam hari", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                            Switch(
                                checked = isDark,
                                onCheckedChange = { viewModel.isDarkMode.value = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = currentPalette.primary
                                )
                            )
                        }

                        AnimatedVisibility(visible = isDark) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Mode Hitam OLED", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                    Text("Gunakan latar hitam pekat untuk menghemat baterai", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                                Switch(
                                    checked = isOled,
                                    onCheckedChange = { viewModel.isOledMode.value = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = currentPalette.primary
                                    )
                                )
                            }
                        }
                    }
                    
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    
                    // Close button
                    Button(
                        onClick = { showSettingsSheet = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = currentPalette.primary)
                    ) {
                        Text(
                            text = "Terapkan",
                            color = if (currentPalette == AccentPalette.BOLD_NEON) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 7. Delete Task Confirmation Dialog
        taskToDelete?.let { task ->
            AlertDialog(
                onDismissRequest = { taskToDelete = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus Tugas",
                            tint = currentPalette.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Konfirmasi Penghapusan", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Text("Apakah Anda yakin ingin membatalkan atau menghapus tugas \"" + task.title + "\"? Tindakan ini akan membatalkan seluruh pengingat dan alarm aktif untuk tugas ini.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteTask(task)
                            taskToDelete = null
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Tugas berhasil dihapus",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = currentPalette.primary)
                    ) {
                        Text(
                            text = "Hapus",
                            fontWeight = FontWeight.Bold,
                            color = if (currentPalette == AccentPalette.BOLD_NEON) Color.Black else Color.White
                        )
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { taskToDelete = null }
                    ) {
                        Text("Kembali")
                    }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
