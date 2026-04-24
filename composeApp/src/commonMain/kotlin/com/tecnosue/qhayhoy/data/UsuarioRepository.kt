package com.tecnosue.qhayhoy.data

import com.tecnosue.qhayhoy.domain.Usuario
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

/**
 * Repositorio encargado de gestionar todas las operaciones relacionadas
 * con usuarios y autenticación.
 *
 * Centraliza la comunicación con Firebase Authentication y con la
 * colección 'usuarios' de Cloud Firestore.
 */
class UsuarioRepository {

    private val auth = Firebase.auth
    private val firestore = Firebase.firestore

    /**
     * Registra un nuevo usuario con email y contraseña.
     * Además de crear la cuenta en Firebase Auth, crea el documento
     * correspondiente en la colección 'usuarios' de Firestore.
     */
    suspend fun registrar(nombre: String, email: String, password: String): Usuario {
        // 1. Crear cuenta en Firebase Authentication
        val resultado = auth.createUserWithEmailAndPassword(email, password)
        val uid = resultado.user?.uid
            ?: throw Exception("No se pudo crear el usuario")

        // 2. Crear documento en Firestore con los datos del usuario
        val usuario = Usuario(
            id = uid,
            nombre = nombre,
            email = email,
            fechaRegistro = Clock.System.now(),
            casasIds = emptyList()
        )

        firestore.collection("usuarios")
            .document(uid)
            .set(usuario)

        return usuario
    }

    /**
     * Inicia sesión con un usuario ya registrado.
     */
    suspend fun iniciarSesion(email: String, password: String): Usuario {
        val resultado = auth.signInWithEmailAndPassword(email, password)
        val uid = resultado.user?.uid
            ?: throw Exception("No se pudo iniciar sesión")

        // Obtenemos el documento del usuario desde Firestore
        val snapshot = firestore.collection("usuarios")
            .document(uid)
            .get()

        return snapshot.data(Usuario.serializer())
    }

    /**
     * Cierra la sesión del usuario actual.
     */
    suspend fun cerrarSesion() {
        auth.signOut()
    }

    /**
     * Observa el estado de autenticación del usuario actual.
     * Emite null cuando no hay sesión, o el Usuario autenticado cuando la hay.
     *
     * Se usa desde el ViewModel para decidir si mostrar Login o la pantalla principal.
     */
    fun observarUsuarioActual(): Flow<Usuario?> {
        return auth.authStateChanged.map { firebaseUser ->
            if (firebaseUser == null) {
                null
            } else {
                try {
                    val snapshot = firestore.collection("usuarios")
                        .document(firebaseUser.uid)
                        .get()
                    snapshot.data(Usuario.serializer())
                } catch (e: Exception) {
                    null
                }
            }
        }
    }
}