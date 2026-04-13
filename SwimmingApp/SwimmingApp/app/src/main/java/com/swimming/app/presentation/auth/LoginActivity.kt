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

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configurarBotones()
        observarResultado()
    }

    private fun configurarBotones() {
        binding.btnIniciarSesion.setOnClickListener { intentarLogin() }
        binding.btnCrearCuenta.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun intentarLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }
        binding.progressBar.visibility = View.VISIBLE
        viewModel.login(email, password)
    }

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
