// Create file: app/src/main/java/com/example/myapplication/ui/screens/EmailListScreen.kt
package com.example.myapplication.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.entity.EmailEntity
import com.example.myapplication.ui.viewmodel.EmailViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EmailListScreen(
    viewModel: EmailViewModel,
    onEmailSelected: (EmailEntity) -> Unit
) {
    val emails by viewModel.emails.collectAsState(initial = emptyList())

    Scaffold(

    ) { paddingValues ->
        if (emails.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(emails) { email ->
                    EmailListItem(email = email) {
                        onEmailSelected(email)
                    }
                    Divider()
                }
            }
        }
    }
}

@Composable
fun EmailListItem(email: EmailEntity, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = email.sender,
                fontWeight = if (!email.read) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = formatDate(email.timestamp),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = email.subject,
            fontWeight = if (!email.read) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        val displayText = email.summary ?: email.snippet
        Text(
            text = displayText,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val now = Calendar.getInstance()
    val date = Calendar.getInstance().apply { timeInMillis = timestamp }

    return when {
        // Today
        now.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR) &&
                now.get(Calendar.YEAR) == date.get(Calendar.YEAR) -> {
            SimpleDateFormat("h:mm a", Locale.getDefault()).format(date.time)
        }
        // This week
        now.get(Calendar.WEEK_OF_YEAR) == date.get(Calendar.WEEK_OF_YEAR) &&
                now.get(Calendar.YEAR) == date.get(Calendar.YEAR) -> {
            SimpleDateFormat("EEE", Locale.getDefault()).format(date.time)
        }
        // This year
        now.get(Calendar.YEAR) == date.get(Calendar.YEAR) -> {
            SimpleDateFormat("MMM d", Locale.getDefault()).format(date.time)
        }
        // Older
        else -> {
            SimpleDateFormat("MM/dd/yy", Locale.getDefault()).format(date.time)
        }
    }
}