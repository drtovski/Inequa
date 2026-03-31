package com.zlo.inequa.ui.solver

import androidx.lifecycle.ViewModel
import com.zlo.inequa.domain.formatter.MathExpressionFormatter
import com.zlo.inequa.domain.input.ExpressionInputEditor
import com.zlo.inequa.domain.model.InputExpression
import com.zlo.inequa.domain.solver.SolveLinearInequalityUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SolverViewModel(
    private val solveLinearInequalityUseCase: SolveLinearInequalityUseCase = SolveLinearInequalityUseCase(),
    private val inputEditor: ExpressionInputEditor = ExpressionInputEditor(),
    private val formatter: MathExpressionFormatter = MathExpressionFormatter()
) : ViewModel() {

    private var rawExpression: String = ""

    private val _uiState = MutableStateFlow(SolverUiState())
    val uiState: StateFlow<SolverUiState> = _uiState.asStateFlow()

    fun onInsertToken(token: String) {
        val nextRaw = inputEditor.append(rawExpression, token)
        if (nextRaw == rawExpression) return

        rawExpression = nextRaw
        _uiState.update { current ->
            current.copy(
                expression = formatter.format(rawExpression),
                answer = "",
                steps = emptyList(),
                graphIntervals = emptyList(),
                variableName = "x",
                errorMessage = null,
                isStepsSheetVisible = false
            )
        }
    }

    fun onBackspace() {
        if (rawExpression.isEmpty()) return

        rawExpression = inputEditor.backspace(rawExpression)
        _uiState.update { current ->
            current.copy(
                expression = formatter.format(rawExpression),
                answer = "",
                steps = emptyList(),
                graphIntervals = emptyList(),
                variableName = "x",
                errorMessage = null,
                isStepsSheetVisible = false
            )
        }
    }

    fun onClear() {
        if (rawExpression.isEmpty() && _uiState.value.expression.isEmpty()) return

        rawExpression = ""
        _uiState.update { current ->
            current.copy(
                expression = "",
                answer = "",
                steps = emptyList(),
                graphIntervals = emptyList(),
                variableName = "x",
                errorMessage = null,
                isStepsSheetVisible = false
            )
        }
    }

    fun onSolve() {
        if (rawExpression.isBlank()) {
            _uiState.update { current ->
                current.copy(
                    answer = "",
                    steps = emptyList(),
                    graphIntervals = emptyList(),
                    errorMessage = "Введите неравенство.",
                    isStepsSheetVisible = false
                )
            }
            return
        }

        runCatching {
            solveLinearInequalityUseCase(InputExpression(rawExpression))
        }.onSuccess { solution ->
            _uiState.update { current ->
                current.copy(
                    expression = formatter.format(rawExpression),
                    answer = solution.shortAnswer.ifBlank { solution.intervalAnswer },
                    steps = solution.steps,
                    graphIntervals = solution.graphIntervals,
                    variableName = solution.variableName,
                    errorMessage = null,
                    isStepsSheetVisible = false
                )
            }
        }.onFailure { throwable ->
            _uiState.update { current ->
                current.copy(
                    expression = formatter.format(rawExpression),
                    answer = "",
                    steps = emptyList(),
                    graphIntervals = emptyList(),
                    variableName = "x",
                    errorMessage = throwable.message ?: "Не удалось решить неравенство.",
                    isStepsSheetVisible = false
                )
            }
        }
    }

    fun onOpenSteps() {
        if (!_uiState.value.canShowSteps || _uiState.value.errorMessage != null) return

        _uiState.update { current ->
            current.copy(isStepsSheetVisible = true)
        }
    }

    fun onCloseSteps() {
        _uiState.update { current ->
            current.copy(isStepsSheetVisible = false)
        }
    }
}
