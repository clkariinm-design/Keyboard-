package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.FinanceCategory
import com.example.data.model.SubscriptionEntity
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.VividViolet
import java.util.Locale

@Composable
fun SubscriptionRowCard(
    subscription: SubscriptionEntity,
    currencySymbol: String,
    onToggleActive: () -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val category = FinanceCategory.fromString(subscription.category)
    val now = System.currentTimeMillis()
    val daysUntil = ((subscription.nextBillingTimestamp - now) / (1000 * 60 * 60 * 24)).coerceAtLeast(0)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier
            .fillMaxWidth()
            .testTag("subscription_card_${subscription.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(VividViolet.copy(alpha = 0.18f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = null,
                    tint = VividViolet,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subscription.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = subscription.billingCycle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (daysUntil == 0L) "Renews today" else "Renews in ${daysUntil}d",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (daysUntil <= 3) EmeraldLight else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (daysUntil <= 3) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format(Locale.US, "%s%,.2f", currencySymbol, subscription.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Switch(
                    checked = subscription.isActive,
                    onCheckedChange = { onToggleActive() },
                    modifier = Modifier
                        .scaleSmall()
                        .testTag("toggle_sub_${subscription.id}")
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = { onDelete(subscription.id) },
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete Subscription",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private fun Modifier.scaleSmall(): Modifier = this.then(Modifier.size(width = 44.dp, height = 28.dp))
