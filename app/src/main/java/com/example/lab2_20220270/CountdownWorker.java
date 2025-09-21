package com.example.lab2_20220270;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class CountdownWorker extends Worker {
    
    private static final String TAG = "CountdownWorker";
    public static final String PROGRESS_KEY = "tiempo_restante";
    public static final String IMAGE_KEY = "imagen_actual";
    public static final String FINISHED_KEY = "juego_terminado";
    
    public CountdownWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }
    
    @NonNull
    @Override
    public Result doWork() {
        try {
            int tiempoTotal = getInputData().getInt("tiempo_total", 12);
            int cantidad = getInputData().getInt("cantidad", 3);
            
            Log.d(TAG, "Iniciando countdown desde: " + tiempoTotal + " segundos para " + cantidad + " imágenes");
            
            for (int tiempoRestante = tiempoTotal; tiempoRestante >= 0; tiempoRestante--) {
                if (isStopped()) {
                    Log.d(TAG, "Countdown cancelado");
                    return Result.failure();
                }
                
                int tiempoTranscurrido = tiempoTotal - tiempoRestante;
                int imagenActual = Math.min((tiempoTranscurrido / 4) + 1, cantidad);
                
                Data progressData = new Data.Builder()
                        .putInt(PROGRESS_KEY, tiempoRestante)
                        .putInt(IMAGE_KEY, imagenActual)
                        .putBoolean(FINISHED_KEY, tiempoRestante == 0)
                        .build();
                
                setProgressAsync(progressData);
                Log.d(TAG, "Countdown: " + tiempoRestante + "s, Imagen: " + imagenActual);
                
                Thread.sleep(1000);
            }
            
            Data outputData = new Data.Builder()
                    .putBoolean(FINISHED_KEY, true)
                    .putInt(PROGRESS_KEY, 0)
                    .putInt(IMAGE_KEY, cantidad)
                    .build();
            
            Log.d(TAG, "Countdown completado");
            return Result.success(outputData);
            
        } catch (InterruptedException e) {
            Log.e(TAG, "Countdown interrumpido", e);
            return Result.failure();
        } catch (Exception e) {
            Log.e(TAG, "Error en countdown", e);
            return Result.failure();
        }
    }
}