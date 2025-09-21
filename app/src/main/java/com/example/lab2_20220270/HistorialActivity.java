package com.example.lab2_20220270;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lab2_20220270.databinding.ActivityHistorialBinding;

public class HistorialActivity extends AppCompatActivity {

    private ActivityHistorialBinding binding;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistorialBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeComponents();
        loadHistorial();
        setupListeners();
    }

    private void initializeComponents() {
        prefs = getSharedPreferences("TeleCatHistory", MODE_PRIVATE);
    }

    private void loadHistorial() {
        LinearLayout historyContainer = binding.layoutHistoryContent;
        
        historyContainer.removeAllViews();
        
        int sessionCount = prefs.getInt("sessionCount", 0);
        
        if (sessionCount == 0) {
            binding.tvNoHistory.setVisibility(View.VISIBLE);
        } else {
            binding.tvNoHistory.setVisibility(View.GONE);
            
            for (int i = 1; i <= sessionCount; i++) {
                int cantidad = prefs.getInt("session_" + i + "_cantidad", 0);
                
                if (cantidad > 0) {
                    TextView historyItem = createHistoryItem(i, cantidad);
                    historyContainer.addView(historyItem);
                }
            }
        }
    }

    private TextView createHistoryItem(int sessionNumber, int cantidad) {
        TextView textView = new TextView(this);
        textView.setText(getString(R.string.interaccion_format, sessionNumber, cantidad));
        textView.setTextSize(18);
        textView.setTextColor(getResources().getColor(android.R.color.black, null));
        textView.setPadding(0, 16, 0, 16);
        
        textView.setCompoundDrawablesWithIntrinsicBounds(
            0, 0, 0, android.R.drawable.divider_horizontal_bright);
        
        return textView;
    }

    private void setupListeners() {
        binding.btnVolverAJugar.setOnClickListener(v -> showConfirmationDialog());
    }

    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirmacion);
        builder.setMessage(R.string.confirmar_volver_jugar);
        
        builder.setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(HistorialActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
        
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
            getResources().getColor(R.color.telecat_primary, null));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
            getResources().getColor(R.color.disabled_gray, null));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}