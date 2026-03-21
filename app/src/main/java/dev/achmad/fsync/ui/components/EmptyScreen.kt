package dev.achmad.fsync.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.achmad.fsync.ui.theme.AppTheme

data class EmptyScreenButtonConfig(
    val text: String,
    val onClick: () -> Unit,
    val icon: ImageVector? = null,
)

@Composable
fun EmptyScreen(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: (@Composable () -> Unit)? = null,
    primaryButton: EmptyScreenButtonConfig? = null,
    secondaryButton: EmptyScreenButtonConfig? = null,
) {
    Surface(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.height(16.dp))
            }
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge
            )
            if (description != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = description,
                    fontWeight = FontWeight.Normal,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (primaryButton != null || secondaryButton != null) {
                Spacer(modifier = Modifier.height(16.dp))
            }
            if (primaryButton != null) {
                Button(
                    onClick = primaryButton.onClick,
                    contentPadding = PaddingValues(
                        start = if (primaryButton.icon != null) 10.dp else 16.dp,
                        end = 16.dp
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        primaryButton.icon?.let {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                imageVector = it,
                                contentDescription = null,
                            )
                            Spacer(Modifier.width(4.dp))
                        }
                        Text(primaryButton.text)
                    }
                }
            }
            if (secondaryButton != null) {
                OutlinedButton(
                    onClick = secondaryButton.onClick,
                    contentPadding = PaddingValues(
                        start = if (secondaryButton.icon != null) 10.dp else 16.dp,
                        end = 16.dp
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        secondaryButton.icon?.let {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                imageVector = it,
                                contentDescription = null,
                            )
                            Spacer(Modifier.width(4.dp))
                        }
                        Text(secondaryButton.text)
                    }
                }
            }
        }
    }
}

@Composable
@Preview
private fun EmptyScreenPreview() {
    AppTheme {
        EmptyScreen(
            title = "Title",
            description = "This is a description",
            primaryButton = EmptyScreenButtonConfig(
                text = "Primary Button",
                icon = Icons.Default.Add,
                onClick = {}
            ),
            secondaryButton = EmptyScreenButtonConfig(
                text = "Secondary Button",
                icon = Icons.Default.Close,
                onClick = {}
            )
        )
    }
}

@Composable
@Preview
private fun EmptyScreenPreviewDark() {
    AppTheme(darkTheme = true) {
        EmptyScreen(
            title = "Title",
            description = "This is a description",
            primaryButton = EmptyScreenButtonConfig(
                text = "Primary Button",
                icon = Icons.Default.Add,
                onClick = {}
            ),
            secondaryButton = EmptyScreenButtonConfig(
                text = "Secondary Button",
                icon = Icons.Default.Close,
                onClick = {}
            )
        )
    }
}

@Composable
@Preview
private fun EmptyScreenPreviewDarkSecondary() {
    AppTheme(darkTheme = true) {
        EmptyScreen(
            title = "Title",
            description = "This is a description",
            secondaryButton = EmptyScreenButtonConfig(
                text = "Secondary Button",
                icon = Icons.Default.Close,
                onClick = {}
            )
        )
    }
}