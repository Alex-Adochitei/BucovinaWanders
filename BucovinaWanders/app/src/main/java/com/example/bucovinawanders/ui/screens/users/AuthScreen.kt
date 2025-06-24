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
import kotlinx.coroutines.*

import com.example.bucovinawanders.api.*
import com.example.bucovinawanders.models.users.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(navController: NavController) {
    var context = LocalContext.current

    //variabile de stare pentru campurile de introducere de date
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    //verificam daca email-ul introdus este valid folosind regex
    val isEmailValid = Pattern.compile("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+").matcher(email).matches()

    var shouldNavigateToLogin by remember { mutableStateOf(false) }

    //functie pentru a afisa un mesaj pop out
    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(shouldNavigateToLogin) {
        if (shouldNavigateToLogin) {
            delay(1000)
            navController.navigate("loginScreen") {
                popUpTo("authScreen") { inclusive = true }
            }
        }
    }

    //UI principal al ecranului de autentificare cu un top bar
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inregistrare", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF00A3E0))
            )
        }
    ) { paddingValues ->
        //continutul ecranului de autentificare
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //card care contine campurile de introducere de date
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
                    TextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Nume utilizator") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-mail") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "E-mail") },
                        isError = email.isNotEmpty() && !isEmailValid,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (email.isNotEmpty() && !isEmailValid) {
                        Text("Invalid e-mail.", color = MaterialTheme.colorScheme.error)
                    }

                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Parola") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    //indicator de incarcare in timpul apelului API
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }

                    //buton de autentificare
                    Button(
                        onClick = {
                            isLoading = true
                            val request = UserRegisterRequestModel(username, email, password)
                            ApiClient.usersApi.registerUser(request).enqueue(object : Callback<UserResponseModel> {
                                override fun onResponse(call: Call<UserResponseModel>, response: Response<UserResponseModel>) {
                                    isLoading = false
                                    if (response.isSuccessful) {
                                        showToast("Inregistrare cu succes!")
                                        username = ""
                                        email = ""
                                        password = ""
                                        shouldNavigateToLogin = true
                                    } else {
                                        showToast("Eroare la inregistrare: Exista deja un utilizator cu acest e-mail.")
                                    }
                                }

                                override fun onFailure(call: Call<UserResponseModel>, t: Throwable) {
                                    isLoading = false
                                    showToast("Eroare de retea: ${t.message}")
                                }
                            })
                        },
                        enabled = username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && isEmailValid,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A3E0))
                    ) {
                        Text("Inregistreaza-te", color = Color.White)
                        Icon(
                            Icons.Default.HowToReg,
                            contentDescription = "Register",
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    //mesaj de status pentru inregistrarea utilizatorului
                    if (message.isNotEmpty()) {
                        Text(
                            text = message,
                            color = if (message.contains("successful")) Color(0xFF00A3E0) else MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }
}