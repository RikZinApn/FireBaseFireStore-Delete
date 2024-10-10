package com.example.firebasefirestore

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.firebasefirestore.ui.theme.FirebasefirestoreTheme
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        db = Firebase.firestore // Inicializa o Firestore
        setContent {
            FirebasefirestoreTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    App(db)
                }
            }
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun App(db: FirebaseFirestore) {
    var nome by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }

    // Inicializa a lista de clientes
    val clientes = remember { mutableStateListOf<Pair<String, HashMap<String, String>>>() }

    // Função para carregar clientes
    fun loadClientes() {
        db.collection("Clientes")
            .get()
            .addOnSuccessListener { documents ->
                // Limpa a lista antes de adicionar novos dados
                clientes.clear()
                for (document in documents) {
                    val cliente = hashMapOf(
                        "nome" to "${document.data["nome"]}",
                        "telefone" to "${document.data["telefone"]}"
                    )
                    clientes.add(Pair(document.id, cliente)) // Guardar o ID do documento junto com os dados
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }

    // Carrega os clientes ao iniciar o Composable
    loadClientes()

    Column(
        Modifier.fillMaxWidth()
    ) {
        // Seção de campos de input e botão Cadastrar
        Row(
            Modifier.fillMaxWidth().padding(20.dp)
        ) {}

        Row(
            Modifier.fillMaxWidth(),
            Arrangement.Center
        ) {
            Text(text = "App Firebase Firestore")
        }

        Row(
            Modifier.fillMaxWidth().padding(20.dp)
        ) {}

        Row(
            Modifier.fillMaxWidth()
        ) {
            Column(Modifier.fillMaxWidth(0.3f)) {
                Text(text = "Nome:")
            }
            Column {
                TextField(
                    value = nome,
                    onValueChange = { nome = it }
                )
            }
        }

        Row(
            Modifier.fillMaxWidth()
        ) {
            Column(Modifier.fillMaxWidth(0.3f)) {
                Text(text = "Telefone:")
            }
            Column {
                TextField(
                    value = telefone,
                    onValueChange = { telefone = it }
                )
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(20.dp)
        ) {}

        // Botão "Cadastrar"
        Row(
            Modifier.fillMaxWidth(),
            Arrangement.Center
        ) {
            Button(onClick = {
                val pessoas = hashMapOf(
                    "nome" to nome,
                    "telefone" to telefone
                )

                db.collection("Clientes").add(pessoas)
                    .addOnSuccessListener { documentReference ->
                        Log.d(TAG, "DocumentSnapshot written with ID: ${documentReference.id}")
                        // Atualiza a lista após adicionar
                        loadClientes()
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error adding document", e)
                    }
            }) {
                Text(text = "Cadastrar")
            }
        }

        // Botão para deletar todos os documentos
        Row(
            Modifier.fillMaxWidth(),
            Arrangement.Center
        ) {
            Button(onClick = {
                db.collection("Clientes").get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            db.collection("Clientes").document(document.id).delete()
                                .addOnSuccessListener {
                                    Log.d(TAG, "DocumentSnapshot successfully deleted!")
                                    // Atualiza a lista ao deletar todos
                                    loadClientes()
                                }
                                .addOnFailureListener { e ->
                                    Log.w(TAG, "Error deleting document", e)
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error getting documents to delete", e)
                    }
            }) {
                Text(text = "Deletar Todos")
            }
        }

        // Exibir lista de clientes com botões para deletar individualmente
        Row(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth(0.5f)) {
                Text(text = "Nome:")
            }
            Column(Modifier.fillMaxWidth(0.5f)) {
                Text(text = "Telefone:")
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(clientes) { clientePair ->
                val (docId, cliente) = clientePair // Recuperar o ID e os dados do cliente
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.weight(0.4f)) {
                        Text(text = cliente["nome"] ?: "---")
                    }
                    Column(modifier = Modifier.weight(0.4f)) {
                        Text(text = cliente["telefone"] ?: "---")
                    }
                    Column(modifier = Modifier.weight(0.5f)) {
                        Button(onClick = {
                            // Deletar o documento específico pelo ID
                            db.collection("Clientes").document(docId).delete()
                                .addOnSuccessListener {
                                    Log.d(TAG, "DocumentSnapshot successfully deleted!")
                                    // Atualizar a lista ao remover o cliente
                                    loadClientes()
                                }
                                .addOnFailureListener { e ->
                                    Log.w(TAG, "Error deleting document", e)
                                }
                        }) {
                            Text(text = "Deletar")
                        }
                    }
                }
            }
        }
    }
}
