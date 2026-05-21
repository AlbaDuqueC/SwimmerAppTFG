package com.swimming.app.presentation.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.swimming.app.databinding.ActivityLoginBinding
import com.swimming.app.presentation.main.MainActivity
import com.swimming.app.utils.NetworkResult
import dagger.hilt.android.AndroidEntryPoint

/**
 * Pantalla de inicio de sesión.
 * Permite al usuario introducir email y contraseña, o navegar al registro
 * para crear una cuenta nueva. Delega toda la lógica al LoginViewModel.
 *
 * @AndroidEntryPoint habilita la inyección de dependencias con Hilt.
 */
@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    // ViewBinding para acceder a las vistas del layout sin findViewById.
    private lateinit var binding: ActivityLoginBinding

    // ViewModel inyectado automáticamente por Hilt.
    private val viewModel: LoginViewModel by viewModels()

    /** Inicializa la pantalla al crearse. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configurarBotones()
        observarResultado()
    }

    /** Asocia los botones de la UI con sus acciones. */
    private fun configurarBotones() {
        binding.btnIniciarSesion.setOnClickListener { intentarLogin() }
        binding.btnCrearCuenta.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    /**
     * Valida los campos del formulario y, si son correctos,
     * delega la operación al ViewModel mostrando un indicador de carga.
     */
    private fun intentarLogin() {
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString()?.trim().orEmpty()
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }
        binding.progressBar.visibility = View.VISIBLE
        viewModel.login(email, password)
    }

    /**
     * Observa el LiveData del ViewModel y reacciona al resultado del login:
     * navega a MainActivity si tiene éxito o muestra un Toast si falla.
     */
    private fun observarResultado() {
        viewModel.loginResult.observe(this) { result ->
            binding.progressBar.visibility = View.GONE
            when (result) {
                is NetworkResult.Success -> { startActivity(Intent(this, MainActivity::class.java)); finish() }
                is NetworkResult.Error -> Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                is NetworkResult.Loading -> binding.progressBar.visibility = View.VISIBLE
            }
        }
    }
}