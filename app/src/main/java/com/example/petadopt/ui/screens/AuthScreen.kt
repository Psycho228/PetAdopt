package com.example.petadopt.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petadopt.ui.components.Screen
import com.example.petadopt.ui.components.PrimaryButton
import com.example.petadopt.ui.theme.TextSecondary
import com.example.petadopt.viewmodel.AuthViewModel

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var isRegister by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    Screen {
        Spacer(Modifier.weight(1f))

        Text(
            text = if (isRegister) "Создать аккаунт в Хвостиках" else "Добро пожаловать в Хвостики",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = if (isRegister) "Зарегистрируйтесь чтобы найти питомца"
            else "Войдите в свой аккаунт",
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(32.dp))

        AnimatedVisibility(visible = isRegister) {
            Column {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("Имя") },
                    placeholder = { Text("Как вас зовут?") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                Spacer(Modifier.height(12.dp))
            }
        }

        OutlinedTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            label = { Text("Email") },
            placeholder = { Text("example@mail.com") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            label = { Text("Пароль") },
            placeholder = { Text("Минимум 6 символов") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (passwordVisible)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Text(text = if (passwordVisible) "Скрыть" else "Показать")
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        )

        Spacer(Modifier.height(16.dp))

        AnimatedVisibility(visible = state.error != null) {
            state.error?.let { errorMsg ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMsg,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        if (state.isLoading) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            PrimaryButton(
                text = if (isRegister) "Зарегистрироваться" else "Войти",
                onClick = {
                    if (isRegister) viewModel.register(onAuthSuccess)
                    else viewModel.login(onAuthSuccess)
                }
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isRegister) "Уже есть аккаунт? " else "Нет аккаунта? ",
                color = TextSecondary
            )
            TextButton(onClick = { isRegister = !isRegister }) {
                Text(if (isRegister) "Войти" else "Зарегистрироваться")
            }
        }

        Spacer(Modifier.weight(1f))
    }
}
