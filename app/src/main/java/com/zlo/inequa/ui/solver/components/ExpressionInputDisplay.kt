package com.zlo.inequa.ui.solver.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zlo.inequa.R

@Composable
fun ExpressionInputDisplay(
    expression: String,
    answer: String,
    errorMessage: String?,
    canShowSteps: Boolean,
    onShowSteps: () -> Unit
) {
    val showResultPlaceholder = errorMessage == null && answer.isBlank()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = expression.ifBlank { stringResource(R.string.expression_placeholder) },
                style = MaterialTheme.typography.displaySmall,
                color = if (expression.isBlank()) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 72.dp)
                    .horizontalScroll(rememberScrollState()),
                textAlign = TextAlign.End
            )

            Text(
                text = errorMessage ?: answer.ifBlank { stringResource(R.string.result_placeholder) },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = when {
                    errorMessage != null -> MaterialTheme.colorScheme.error
                    showResultPlaceholder -> MaterialTheme.colorScheme.onSurfaceVariant
                    else -> MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )

            TextButton(
                onClick = onShowSteps,
                enabled = canShowSteps && errorMessage == null,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(R.string.steps_button))
            }
        }
    }
}
