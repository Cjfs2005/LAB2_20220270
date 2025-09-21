package com.example.lab2_20220270;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.example.lab2_20220270.databinding.ActivityJuegoBinding;

import java.io.InputStream;
import java.util.UUID;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class JuegoActivity extends AppCompatActivity {

    private ActivityJuegoBinding binding;
    private CatApiService catApiService;
    private WorkManager workManager;
    private UUID workerId;
    
    private int cantidad;
    private String textoOption;
    private String customText;
    
    private int imagenActualCargada = 0;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityJuegoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeComponents();
        getGameData();
        setupWorkManager();
        loadFirstImage();
    }

    private void initializeComponents() {
        mainHandler = new Handler(Looper.getMainLooper());
        workManager = WorkManager.getInstance(this);
        
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://cataas.com/")
                .build();
        catApiService = retrofit.create(CatApiService.class);
        
        binding.btnSiguiente.setEnabled(false);
        binding.btnSiguiente.setBackgroundTintList(
            getResources().getColorStateList(R.color.disabled_gray, null));
        
        binding.btnSiguiente.setOnClickListener(v -> navigateToHistorial());
    }

    private void getGameData() {
        Intent intent = getIntent();
        cantidad = intent.getIntExtra("cantidad", 3);
        textoOption = intent.getStringExtra("textoOption");
        customText = intent.getStringExtra("customText");
        
        binding.tvCantidad.setText("Cantidad = " + cantidad);
        
        Log.d("JuegoActivity", "Juego configurado: " + cantidad + " imágenes, opción: " + textoOption);
    }

    /*
    Modelo: Claude Sonnet 4
    Prompt:  Eres un desarrollador de aplicaciones para Android y necesitas integrar WorkManager con un activity denominado JuegoActivity para reemplazar el Timer que no persiste y tambien conectar con la API de gatos cataas.com cuyos endpoints se pueden llamar mediante las funciones getRandomCat() y getCatWithText(Text) que definiste ¿Qué código necesirias?
    Mejoras: Optimice la lógica de carga de imagenes para evitar llamadas duplicadas a la API
    */
    private void setupWorkManager() {
        int tiempoTotal = cantidad * 4;
        
        Data inputData = new Data.Builder()
                .putInt("tiempo_total", tiempoTotal)
                .putInt("cantidad", cantidad)
                .build();

        OneTimeWorkRequest countdownWork = new OneTimeWorkRequest.Builder(CountdownWorker.class)
                .setInputData(inputData)
                .build();

        workerId = countdownWork.getId();
        workManager.enqueue(countdownWork);

        workManager.getWorkInfoByIdLiveData(workerId).observe(this, workInfo -> {
            if (workInfo != null) {
                if (workInfo.getState() == WorkInfo.State.RUNNING) {
                    Data progress = workInfo.getProgress();
                    int tiempoRestante = progress.getInt(CountdownWorker.PROGRESS_KEY, tiempoTotal);
                    int imagenActual = progress.getInt(CountdownWorker.IMAGE_KEY, 1);
                    
                    updateTimer(tiempoRestante);
                    
                    if (imagenActual != imagenActualCargada && imagenActual <= cantidad) {
                        imagenActualCargada = imagenActual;
                        loadCatImage();
                    }
                    
                } else if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                    endGame();
                    Log.d("JuegoActivity", "Countdown Worker completado");
                }
            }
        });
    }

    private void loadFirstImage() {
        imagenActualCargada = 1;
        loadCatImage();
    }

    private void updateTimer(int tiempoRestante) {
        if (tiempoRestante < 0) {
            tiempoRestante = 0;
        }
        
        int minutos = tiempoRestante / 60;
        int segundos = tiempoRestante % 60;
        binding.tvTimer.setText(String.format("%02d:%02d", minutos, segundos));
    }

    private void loadCatImage() {
        Log.d("JuegoActivity", "Cargando imagen " + imagenActualCargada + " de " + cantidad);
        
        binding.progressBar.setVisibility(android.view.View.VISIBLE);
        
        Call<ResponseBody> call;
        if ("Si".equals(textoOption) && customText != null && !customText.trim().isEmpty()) {
            call = catApiService.getCatWithText(customText.trim());
        } else {
            call = catApiService.getRandomCat();
        }
            
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            InputStream inputStream = response.body().byteStream();
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            
                            mainHandler.post(() -> {
                                binding.ivCatImage.setImageBitmap(bitmap);
                                binding.progressBar.setVisibility(android.view.View.GONE);
                                Log.d("JuegoActivity", "Imagen " + imagenActualCargada + " cargada exitosamente");
                            });
                            
                        } catch (Exception e) {
                            Log.e("JuegoActivity", "Error al procesar imagen: " + e.getMessage());
                            mainHandler.post(() -> binding.progressBar.setVisibility(android.view.View.GONE));
                        }
                    } else {
                        Log.e("JuegoActivity", "Error en respuesta: " + response.code());
                        mainHandler.post(() -> binding.progressBar.setVisibility(android.view.View.GONE));
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("JuegoActivity", "Error al cargar imagen: " + t.getMessage());
                    mainHandler.post(() -> binding.progressBar.setVisibility(android.view.View.GONE));
                }
            });
    }

    private void endGame() {
        Log.d("JuegoActivity", "Juego terminado");
        
        binding.btnSiguiente.setEnabled(true);
        binding.btnSiguiente.setBackgroundTintList(
            getResources().getColorStateList(R.color.telecat_primary, null));
        
        saveGameToHistory();
    }

    private void saveGameToHistory() {
        SharedPreferences sharedPref = getSharedPreferences("TeleCatHistory", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        
        String gameData = "Cantidad: " + cantidad + 
                         ", Texto personalizado: " + textoOption +
                         (customText != null && !customText.isEmpty() ? " (" + customText + ")" : "");
        
        int historialCount = sharedPref.getInt("historial_count", 0);
        editor.putString("juego_" + historialCount, gameData);
        editor.putInt("historial_count", historialCount + 1);
        editor.apply();
        
        Log.d("JuegoActivity", "Juego guardado en historial: " + gameData);
    }

    private void navigateToHistorial() {
        Intent intent = new Intent(this, HistorialActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}