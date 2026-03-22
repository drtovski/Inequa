package com.zlo.inequa.ui.solver.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zlo.inequa.R

@Composable
fun MathKeyboard(
    modifier: Modifier = Modifier,
    onInsert: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onEquals: () -> Unit
) {
    val clearLabel = stringResource(R.string.clear_button_short)
    val backspaceLabel = stringResource(R.string.backspace_button_short)
    val equalsLabel = stringResource(R.string.equals_button)
    val key0 = stringResource(R.string.key_0)
    val key1 = stringResource(R.string.key_1)
    val key2 = stringResource(R.string.key_2)
    val key3 = stringResource(R.string.key_3)
    val key4 = stringResource(R.string.key_4)
    val key5 = stringResource(R.string.key_5)
    val key6 = stringResource(R.string.key_6)
    val key7 = stringResource(R.string.key_7)
    val key8 = stringResource(R.string.key_8)
    val key9 = stringResource(R.string.key_9)
    val keyX = stringResource(R.string.key_x)
    val keyXSquared = stringResource(R.string.key_x_squared)
    val keyOpenParenthesis = stringResource(R.string.key_open_parenthesis)
    val keyCloseParenthesis = stringResource(R.string.key_close_parenthesis)
    val keyPlus = stringResource(R.string.key_plus)
    val keyMinus = stringResource(R.string.key_minus)
    val keyDivide = stringResource(R.string.key_divide)
    val keyDecimalComma = stringResource(R.string.key_decimal_comma)
    val keyGreater = stringResource(R.string.key_greater)
    val keyLess = stringResource(R.string.key_less)
    val keyGreaterOrEqual = stringResource(R.string.key_greater_or_equal)
    val keyLessOrEqual = stringResource(R.string.key_less_or_equal)
    val keyAbsolute = stringResource(R.string.key_absolute)
    val keyRoot = stringResource(R.string.key_root)

    val rows = listOf(
        listOf(
            KeyboardKey(key7, KeyType.Number),
            KeyboardKey(key8, KeyType.Number),
            KeyboardKey(key9, KeyType.Number),
            KeyboardKey(keyOpenParenthesis, KeyType.Operator),
            KeyboardKey(keyCloseParenthesis, KeyType.Operator)
        ),
        listOf(
            KeyboardKey(key4, KeyType.Number),
            KeyboardKey(key5, KeyType.Number),
            KeyboardKey(key6, KeyType.Number),
            KeyboardKey(keyPlus, KeyType.Operator),
            KeyboardKey(keyMinus, KeyType.Operator)
        ),
        listOf(
            KeyboardKey(key1, KeyType.Number),
            KeyboardKey(key2, KeyType.Number),
            KeyboardKey(key3, KeyType.Number),
            KeyboardKey(keyX, KeyType.Operator),
            KeyboardKey(keyXSquared, KeyType.Operator)
        ),
        listOf(
            KeyboardKey(key0, KeyType.Number),
            KeyboardKey(keyDecimalComma, KeyType.Operator),
            KeyboardKey(keyDivide, KeyType.Operator),
            KeyboardKey(keyGreater, KeyType.Operator),
            KeyboardKey(keyLess, KeyType.Operator)
        ),
        listOf(
            KeyboardKey(keyGreaterOrEqual, KeyType.Operator),
            KeyboardKey(keyLessOrEqual, KeyType.Operator),
            KeyboardKey(keyAbsolute, KeyType.Operator),
            KeyboardKey(keyRoot, KeyType.Operator),
            KeyboardKey(equalsLabel, KeyType.Equals)
        ),
        listOf(
            KeyboardKey(clearLabel, KeyType.Action),
            KeyboardKey(backspaceLabel, KeyType.Action)
        )
    )
    val maxColumns = rows.maxOf { it.size }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            rows.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { key ->
                        KeyboardButton(
                            key = key,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            onClick = {
                                when (key.label) {
                                    clearLabel -> onClear()
                                    backspaceLabel -> onBackspace()
                                    equalsLabel -> onEquals()
                                    else -> onInsert(key.label)
                                }
                            }
                        )
                    }

                    repeat(maxColumns - row.size) {
                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyboardButton(
    key: KeyboardKey,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val colors = when (key.type) {
        KeyType.Number -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )

        KeyType.Operator -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )

        KeyType.Action -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )

        KeyType.Equals -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    }

    Button(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = colors,
        contentPadding = ButtonDefaults.ContentPadding
    ) {
        Text(
            text = key.label,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

private data class KeyboardKey(
    val label: String,
    val type: KeyType
)

private enum class KeyType {
    Number,
    Operator,
    Action,
    Equals
}
