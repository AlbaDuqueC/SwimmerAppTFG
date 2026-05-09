package com.swimming.app.presentation.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.swimming.app.databinding.ActivityRegisterBinding
import com.swimming.app.presentation.main.MainActivity
import com.swimming.app.utils.NetworkResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configurarSpinner()
        configurarBotones()
        observarResultado()
    }

    private fun configurarSpinner() {
        val roles = listOf("Nadador", "Entrenador")
        binding.spinnerRol.adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles)
    }

    private fun configurarBotones() {
        binding.btnCrearCuenta.setOnClickListener { intentarRegistro() }
    }

    private fun intentarRegistro() {
        val nombre = binding.etNombre.text.toString().trim()
        val apellidos = binding.etApellidos.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val repetir = binding.etRepetirPassword.text.toString().trim()
        val rol = binding.spinnerRol.selectedItem.toString()

        if (nombre.isEmpty() || apellidos.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }
        if (password != repetir) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }
        binding.progressBar.visibility = View.VISIBLE
        viewModel.registrar(nombre, apellidos, email, password, rol)
    }

    private fun observarResultado() {
        viewModel.registerResult.observe(this) { result ->
            binding.progressBar.visibility = View.GONE
            when (result) {
                is NetworkResult.Success -> {
                    androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("¡Cuenta creada!")
                        .setMessage("Te hemos enviado un email de verificación. Pulsa el enlace del correo y luego inicia sesión.")
                        .setPositiveButton("Entendido") { _, _ ->
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                        .setCancelable(false)
                        .show()
                }
                is NetworkResult.Error -> Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                is NetworkResult.Loading -> binding.progressBar.visibility = View.VISIBLE
            }
        }
    }
}
