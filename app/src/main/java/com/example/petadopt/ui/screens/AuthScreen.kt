package com.example.petadopt.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Storefront
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
    onAuthSuccess: (isNewAccount: Boolean) -> Unit,
    onBreederAuthSuccess: () -> Unit,
    initialIsRegister: Boolean = false,
    reason: String? = null,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var isRegister by remember(initialIsRegister) { mutableStateOf(initialIsRegister) }
    var isBreederMode by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    Screen {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {

        Text(
            text = when {
                isBreederMode && isRegister -> "Регистрация заводчика"
                isBreederMode -> "Вход для заводчиков"
                isRegister -> "Создать аккаунт в Хвостиках"
                else -> "Добро пожаловать в Хвостики"
            },
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = when {
                isBreederMode && isRegister -> "Создайте аккаунт и отправьте профиль питомника на проверку"
                isBreederMode -> "Войдите, чтобы открыть кабинет и профиль питомника"
                reason != null -> reason
                isRegister -> "Зарегистрируйтесь, чтобы найти питомца"
                else -> "Войдите в свой аккаунт"
            },
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(32.dp))

        AnimatedVisibility(visible = isRegister) {
            Column {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text(if (isBreederMode) "Имя представителя" else "Имя") },
                    placeholder = {
                        Text(if (isBreederMode) "Контактное лицо питомника" else "Как вас зовут?")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                Spacer(Modifier.height(12.dp))
            }
        }

        AnimatedVisibility(visible = isBreederMode && isRegister) {
            Column {
                OutlinedTextField(
                    value = state.kennelName,
                    onValueChange = viewModel::onKennelNameChange,
                    label = { Text("Название питомника") },
                    placeholder = { Text("Например: Добрый хвост") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.breederCity,
                    onValueChange = viewModel::onBreederCityChange,
                    label = { Text("Город") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.breederPhone,
                    onValueChange = viewModel::onBreederPhoneChange,
                    label = { Text("Телефон") },
                    placeholder = { Text("+7 900 000-00-00") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    )
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
                text = when {
                    isBreederMode && isRegister -> "Создать аккаунт заводчика"
                    isBreederMode -> "Войти как заводчик"
                    isRegister -> "Зарегистрироваться"
                    else -> "Войти"
                },
                onClick = {
                    when {
                        isBreederMode && isRegister -> {
                            viewModel.registerBreeder(onBreederAuthSuccess)
                        }
                        isBreederMode -> {
                            viewModel.loginBreeder(onBreederAuthSuccess)
                        }
                        isRegister -> {
                            viewModel.register { onAuthSuccess(true) }
                        }
                        else -> {
                            viewModel.login { onAuthSuccess(false) }
                        }
                    }
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

        Spacer(Modifier.height(8.dp))

        if (isBreederMode) {
            TextButton(
                onClick = {
                    isBreederMode = false
                    isRegister = false
                    viewModel.clearError()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Вернуться к обычному входу")
            }
        } else {
            OutlinedButton(
                onClick = {
                    isBreederMode = true
                    isRegister = true
                    viewModel.clearError()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Storefront,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Стать заводчиком")
            }
        }

        }
    }
}
