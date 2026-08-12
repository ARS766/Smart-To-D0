package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TaskEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskCard(
    task: TaskEntity,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardColor = if (task.isCompleted) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val elevation by animateDpAsState(
        targetValue = if (task.isCompleted) 0.dp else 4.dp,
        label = "elevation"
    )

    val textColor = if (task.isCompleted) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    // Determine priority color tag
    val priorityColor = when (task.urgency.uppercase()) {
        "HIGH" -> Color(0xFFFF4D4D)
        "MEDIUM" -> Color(0xFFFF9F1C)
        else -> Color(0xFF00C896)
    }

    val parsedTaskAccent = try {
        Color(android.graphics.Color.parseColor(task.colorAccentHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable { onToggleComplete() }
            .testTag("task_item_card_${task.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Interactive Large Custom Circular Checkbox (Nested dot style from HTML theme)
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (task.isCompleted) parsedTaskAccent.copy(alpha = 0.15f) else Color.Transparent
                    )
                    .clickable { onToggleComplete() }
                    .border(
                        BorderStroke(2.dp, if (task.isCompleted) parsedTaskAccent.copy(alpha = 0.4f) else parsedTaskAccent),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(parsedTaskAccent)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 2. Task Text Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Category Badge with clean Material Icon (no emojis)
                    Box(
                        modifier = Modifier
                            .background(parsedTaskAccent.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val catIcon = when (task.category) {
                                "Game" -> Icons.Default.Gamepad
                                "Olahraga" -> Icons.Default.FitnessCenter
                                "Belajar" -> Icons.Default.School
                                "Kerja" -> Icons.Default.Work
                                else -> Icons.Default.Assignment
                            }
                            Icon(
                                imageVector = catIcon,
                                contentDescription = null,
                                tint = parsedTaskAccent,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = task.category,
                                color = parsedTaskAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Urgency Badge
                    Box(
                        modifier = Modifier
                            .background(priorityColor.copy(alpha = 0.15f), CircleShape)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = task.urgency,
                            color = priorityColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Title
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Description (optional)
                if (!task.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = textColor.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Time remaining and reminder tags
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Due Date Badge
                    Text(
                        text = "Due: " + formatEpoch(task.targetDateTime),
                        fontSize = 11.sp,
                        color = if (isOverdue(task)) Color(0xFFFF4D4D) else textColor.copy(alpha = 0.6f),
                        fontWeight = if (isOverdue(task)) FontWeight.Bold else FontWeight.Normal
                    )

                    // Smart automated reminder active indicator
                    if (!task.isCompleted) {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Smart Reminder Active",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "Smart " + getReminderBadgeText(task),
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    // Custom Alarm Active Badge
                    if (task.customReminderTime != null && !task.isCompleted) {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = "Custom Alarm",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "Alarm " + formatEpochShort(task.customReminderTime),
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // Recurrence badge
                    if (task.repeatType != "NONE") {
                        Row(
                            modifier = Modifier
                                .background(Color(0xFF9D4EDD).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Autorenew,
                                contentDescription = "Siklus Berulang",
                                tint = Color(0xFF9D4EDD),
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = task.repeatType,
                                fontSize = 9.sp,
                                color = Color(0xFF9D4EDD),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 2.5. Vertical accent strip on the right side of the card (From HTML design spec)
            if (!task.isCompleted) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(36.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(parsedTaskAccent)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            // 3. Action Buttons Column (Edit above Delete per user request)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .testTag("edit_task_button_${task.id}")
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Tugas",
                        tint = parsedTaskAccent
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .testTag("delete_task_button_${task.id}")
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus Tugas",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

private fun formatEpoch(epochMillis: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return sdf.format(Date(epochMillis))
}

private fun formatEpochShort(epochMillis: Long): String {
    val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
    return sdf.format(Date(epochMillis))
}

private fun isOverdue(task: TaskEntity): Boolean {
    return task.targetDateTime < System.currentTimeMillis() && !task.isCompleted
}

private fun getReminderBadgeText(task: TaskEntity): String {
    val diffMillis = task.targetDateTime - System.currentTimeMillis()
    val diffDays = diffMillis / (1000 * 60 * 60 * 24)

    return if (diffDays < 1) {
        "8 jam sebelum"
    } else if (diffDays in 1..7) {
        "3d & 2d sebelum"
    } else {
        "10d, 5d & 2d"
    }
}
