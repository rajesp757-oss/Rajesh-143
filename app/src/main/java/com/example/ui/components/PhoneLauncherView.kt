package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.PermContactCalendar
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.InstalledAppInfo
import com.example.data.models.LauncherActionType
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletGlow
import com.example.ui.theme.VioletNeon

data class QuickActionItem(
    val type: LauncherActionType,
    val icon: ImageVector,
    val gradientColors: List<Color>
)

@Composable
fun PhoneLauncherView(
    installedApps: List<InstalledAppInfo>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onExecuteAction: (LauncherActionType) -> Unit,
    onLaunchPackage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val quickActions = remember {
        listOf(
            QuickActionItem(
                type = LauncherActionType.PHONE,
                icon = Icons.Default.Phone,
                gradientColors = listOf(Color(0xFF00E676), Color(0xFF00B0FF))
            ),
            QuickActionItem(
                type = LauncherActionType.CAMERA,
                icon = Icons.Default.PhotoCamera,
                gradientColors = listOf(Color(0xFFFF5252), Color(0xFFFF7A00))
            ),
            QuickActionItem(
                type = LauncherActionType.MESSAGES,
                icon = Icons.Default.Message,
                gradientColors = listOf(Color(0xFF2979FF), Color(0xFF00E5FF))
            ),
            QuickActionItem(
                type = LauncherActionType.CONTACTS,
                icon = Icons.Default.PermContactCalendar,
                gradientColors = listOf(Color(0xFF9D4EDD), Color(0xFF7B2CBF))
            ),
            QuickActionItem(
                type = LauncherActionType.SETTINGS,
                icon = Icons.Default.Settings,
                gradientColors = listOf(Color(0xFF78909C), Color(0xFF37474F))
            ),
            QuickActionItem(
                type = LauncherActionType.CLOCK,
                icon = Icons.Default.Alarm,
                gradientColors = listOf(Color(0xFFFFB703), Color(0xFFFB8500))
            ),
            QuickActionItem(
                type = LauncherActionType.CALCULATOR,
                icon = Icons.Default.Calculate,
                gradientColors = listOf(Color(0xFF00B4D8), Color(0xFF0077B6))
            ),
            QuickActionItem(
                type = LauncherActionType.BROWSER,
                icon = Icons.Default.Language,
                gradientColors = listOf(Color(0xFFEC4899), Color(0xFF8B5CF6))
            )
        )
    }

    val filteredApps = remember(installedApps, searchQuery) {
        if (searchQuery.isBlank()) {
            installedApps
        } else {
            installedApps.filter {
                it.label.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Title Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Apps,
                contentDescription = null,
                tint = CyanBright,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Phone Launcher",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "फ़ोन लॉन्चर • ଫୋନ୍ ଲଞ୍ଚର୍",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search installed applications...", fontSize = 14.sp, color = TextMuted) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = CyanNeon)
            },
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_apps_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceCard,
                unfocusedContainerColor = SurfaceCard,
                focusedBorderColor = CyanNeon,
                unfocusedBorderColor = SurfaceBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Quick System Actions Section Header
            item(span = { GridItemSpan(4) }) {
                Text(
                    text = "QUICK TOOLS (त्वरित सेवाएं / ଶୀଘ୍ର ସେବା)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = CyanBright,
                    letterSpacing = 1.sp
                )
            }

            // Quick Actions 4x2 Grid
            items(quickActions) { action ->
                QuickActionTile(
                    action = action,
                    onClick = { onExecuteAction(action.type) }
                )
            }

            // Installed Apps Section Header
            item(span = { GridItemSpan(4) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "INSTALLED APPS (${filteredApps.size})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = VioletGlow,
                        letterSpacing = 1.sp
                    )

                    if (searchQuery.isNotBlank()) {
                        Text(
                            text = "Clear Filter",
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentAmber,
                            modifier = Modifier
                                .clickable { onSearchQueryChange("") }
                                .padding(4.dp)
                        )
                    }
                }
            }

            if (filteredApps.isEmpty()) {
                item(span = { GridItemSpan(4) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) {
                                "No apps found matching \"$searchQuery\""
                            } else {
                                "Scanning installed applications..."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }
                }
            } else {
                items(filteredApps, key = { it.packageName }) { appInfo ->
                    AppIconTile(
                        appInfo = appInfo,
                        onClick = { onLaunchPackage(appInfo.packageName) }
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionTile(
    action: QuickActionItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag("quick_action_${action.type.name.lowercase()}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.linearGradient(action.gradientColors))
                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.type.title,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = action.type.title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = "${action.type.hindiTitle} / ${action.type.odiaTitle}",
            fontSize = 9.sp,
            color = TextMuted,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AppIconTile(
    appInfo: InstalledAppInfo,
    onClick: () -> Unit
) {
    val bitmap = remember(appInfo.icon) {
        drawableToBitmap(appInfo.icon)
    }

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
            .testTag("installed_app_${appInfo.packageName}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceCard)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = appInfo.label,
                    modifier = Modifier.size(38.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CyanNeon.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = appInfo.label.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = CyanBright,
                        fontSize = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = appInfo.label,
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun drawableToBitmap(drawable: Drawable?): Bitmap? {
    if (drawable == null) return null
    if (drawable is BitmapDrawable && drawable.bitmap != null) {
        return drawable.bitmap
    }
    return try {
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bitmap
    } catch (e: Exception) {
        null
    }
}
