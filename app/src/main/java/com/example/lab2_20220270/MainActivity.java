package com.example.lab2_20220270;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lab2_20220270.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupSpinner();
        setupListeners();
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.texto_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerTexto.setAdapter(adapter);

        binding.spinnerTexto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                
                if ("Si".equals(selected)) {
                    binding.etEscribirTexto.setEnabled(true);
                    binding.tilEscribirTexto.setBoxBackgroundColor(getResources().getColor(android.R.color.white, null));
                } else {
                    binding.etEscribirTexto.setEnabled(false);
                    binding.etEscribirTexto.setText("");
                    binding.tilEscribirTexto.setBoxBackgroundColor(getResources().getColor(android.R.color.darker_gray, null));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupListeners() {
        binding.btnComprobarConexion.setOnClickListener(v -> checkInternetConnection());
        binding.btnComenzar.setOnClickListener(v -> startGame());
    }

    private void checkInternetConnection() {
        ConnectivityManager connectivityManager = 
            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            
            if (isConnected) {
                Toast.makeText(this, R.string.conexion_exitosa, Toast.LENGTH_SHORT).show();
                binding.btnComenzar.setEnabled(true);
                binding.btnComenzar.setBackgroundTintList(
                    getResources().getColorStateList(R.color.telecat_primary, null));
                binding.btnComprobarConexion.setIcon(getDrawable(android.R.drawable.button_onoff_indicator_on));
            } else {
                Toast.makeText(this, R.string.sin_conexion, Toast.LENGTH_SHORT).show();
                binding.btnComenzar.setEnabled(false);
                binding.btnComenzar.setBackgroundTintList(
                    getResources().getColorStateList(R.color.disabled_gray, null));
            }
        }
    }

    private void startGame() {
        if (!isConnected) {
            Toast.makeText(this, R.string.debe_comprobar_conexion, Toast.LENGTH_SHORT).show();
            return;
        }

        String cantidadStr = binding.etCantidad.getText().toString().trim();
        if (cantidadStr.isEmpty()) {
            Toast.makeText(this, R.string.cantidad_requerida, Toast.LENGTH_SHORT).show();
            return;
        }

        String textoOption = binding.spinnerTexto.getSelectedItem().toString();
        String customText = binding.etEscribirTexto.getText().toString().trim();

        if ("Elegir".equals(textoOption)) {
            Toast.makeText(this, "Debe seleccionar 'Si' o 'No' en la opción Texto", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("Si".equals(textoOption) && customText.isEmpty()) {
            Toast.makeText(this, R.string.texto_requerido_si, Toast.LENGTH_SHORT).show();
            return;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(cantidadStr);
            if (cantidad <= 0) {
                Toast.makeText(this, "La cantidad debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ingrese un número válido", Toast.LENGTH_SHORT).show();
            return;
        }

        saveGameSession(cantidad);

        Intent intent = new Intent(this, JuegoActivity.class);
        intent.putExtra("cantidad", cantidad);
        intent.putExtra("textoOption", textoOption);
        intent.putExtra("customText", customText);
        startActivity(intent);
    }

    private void saveGameSession(int cantidad) {
        SharedPreferences prefs = getSharedPreferences("TeleCatHistory", MODE_PRIVATE);
        int sessionCount = prefs.getInt("sessionCount", 0) + 1;
        
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("sessionCount", sessionCount);
        editor.putInt("session_" + sessionCount + "_cantidad", cantidad);
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}