package com.example.bucovinawanders.ui.screens.users

import android.widget.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import androidx.navigation.*

import retrofit2.*
import java.util.regex.*

import com.example.bucovinawanders.api.*
import com.example.bucovinawanders.models.users.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, onLoginSuccess: (String, String) -> Unit) {
    val context = LocalContext.current

    //variabile pentru email, parola, mesaj si loading
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    //verificam daca emailul este valid
    val isEmailValid = Pattern.compile("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+").matcher(email).matches()

    //functie pentru a afisa un mesaj toast
    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    //UI pentru ecranul de login
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Autentificare", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF00A3E0))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    //campul de email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-mail") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "E-mail") },
                        isError = email.isNotEmpty() && !isEmailValid,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color(0xFF00A3E0),
                            unfocusedIndicatorColor = Color.Gray
                        )
                    )

                    if (email.isNotEmpty() && !isEmailValid) {
                        Text("E-mail invalid", color = MaterialTheme.colorScheme.error)
                    }

                    //campul de parola
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Parola") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color(0xFF00A3E0),
                            unfocusedIndicatorColor = Color.Gray
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }

                    //butonul de login
                    Button(
                        onClick = {
                            isLoading = true
                            ApiClient.usersApi.loginUser(email, password).enqueue(object : Callback<AuthResponseModel> {
                                override fun onResponse(call: Call<AuthResponseModel>, response: Response<AuthResponseModel>) {
                                    isLoading = false
                                    if (response.isSuccessful) {
                                        val authResponse = response.body()
                                        if (authResponse != null && authResponse.accessToken.isNotEmpty()) {
                                            onLoginSuccess(authResponse.accessToken, authResponse.userName) //trimitem tokenul si usernameul
                                            navController.navigate("mapsScreen") //navigam catre ecranul principal
                                        } else {
                                            showToast("Eroare la autentificare: E-mail sau parola incorecta.")
                                        }
                                    } else {
                                        showToast("Eroare la autentificare: E-mail sau parola incorecta.")
                                    }
                                }

                                override fun onFailure(call: Call<AuthResponseModel>, t: Throwable) {
                                    isLoading = false
                                    showToast("Eroare de retea: ${t.message}")
                                }
                            })
                        },
                        enabled = email.isNotEmpty() && password.isNotEmpty() && isEmailValid,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A3E0))
                    ) {
                        Text("Autentifica-te", color = Color.White)
                        Icon(
                            Icons.AutoMirrored.Filled.Login,
                            contentDescription = "Login",
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    if (message.isNotEmpty()) {
                        Text(
                            text = message,
                            color = if (message.contains("success")) Color(0xFF00A3E0) else MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    //butonul de navigare catre ecranul de inregistrare
                    TextButton(onClick = { navController.navigate("authScreen") }) {
                        Text("Nu ai cont? Inregistreaza-te.", color = Color(0xFF00A3E0))
                    }
                }
            }
        }
    }
}