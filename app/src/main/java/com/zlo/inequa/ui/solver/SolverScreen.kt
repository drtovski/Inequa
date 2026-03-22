package com.zlo.inequa.ui.solver

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zlo.inequa.R
import com.zlo.inequa.ui.solver.components.ExpressionInputDisplay
import com.zlo.inequa.ui.solver.components.MathKeyboard
import com.zlo.inequa.ui.solver.components.StepsBottomSheet

@Composable
fun SolverScreen(
    viewModel: SolverViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Header()

            ExpressionInputDisplay(
                expression = uiState.expression,
                answer = uiState.answer,
                errorMessage = uiState.errorMessage,
                canShowSteps = uiState.canShowSteps,
                onShowSteps = viewModel::onOpenSteps
            )

            MathKeyboard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                onInsert = viewModel::onInsertToken,
                onBackspace = viewModel::onBackspace,
                onClear = viewModel::onClear,
                onEquals = viewModel::onSolve
            )
        }
    }

    StepsBottomSheet(
        isVisible = uiState.isStepsSheetVisible,
        steps = uiState.steps,
        onDismiss = viewModel::onCloseSteps
    )
}

@Composable
private fun Header() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(id = R.string.app_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
