package com.example.bucovinawanders.ui.screens.users

import android.widget.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import androidx.navigation.*

import retrofit2.*
import java.util.regex.*

import com.example.bucovinawanders.api.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteUserScreen(navController: NavController, onAccountDeleted: () -> Unit) {
    var context = LocalContext.current

    //variabile de stare pentru email si mesaj de eroare
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    //verificare daca emailul este valid
    val isEmailValid = Pattern.compile("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+").matcher(email).matches()

    //functie pentru afisarea unui mesaj toast
    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    //UI pentru ecranul de ștergere a contului
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sterge cont", color = Color.White) },
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
            //camp de introducere a emailului pentru a confirma stergerea contului
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Confirma e-mail-ul") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "E-mail") },
                isError = email.isNotEmpty() && !isEmailValid,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                CircularProgressIndicator()
            }

            //buton de stergere a contului
            Button(
                onClick = {
                    isLoading = true
                    ApiClient.usersApi.deleteUser(email).enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            isLoading = false
                            if (response.isSuccessful) {
                                showToast("Cont sters cu succes!")
                                onAccountDeleted()  //apeleaza metoda de callback pentru a elimina contul
                                navController.navigate("settingsScreen")  //navigare la ecranul de setari dupa ce s-a sters contul
                            } else {
                                showToast("Eroare la stergere a contului: ${response.message()}")
                            }
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            isLoading = false
                            showToast("Eroare de retea: ${t.message}")
                        }
                    })
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A3E0))
            ) {
                Text("Delete Account", color = Color.White)
            }

            if (message.isNotEmpty()) {
                Text(
                    text = message,
                    color = if (message.contains("Cont sters.")) Color(0xFF00A3E0) else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}