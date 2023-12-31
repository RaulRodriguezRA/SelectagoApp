package com.miraimx.selectagoapp;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnPaidEventListener;
import com.google.android.gms.ads.ResponseInfo;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ResultadosDeteccion extends AppCompatActivity {

    TextView txtArboles, txtMuestra, txtDetectados, txtPromedio, txtEstimacion;
    private String fruto;
    private int arboles, muestra, detecciones, promedio, estimacion;
    //private String fechaConf;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultados_deteccion);
        ActionBar actionBar = getSupportActionBar();

        MobileAds.initialize(this);
      
        AdRequest adRequest = new AdRequest.Builder().build();

        anuncio(adRequest);

        if (actionBar != null) {
            //Poner el ícono al ActionBar
            actionBar.setIcon(R.drawable.tfl_logo);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#62aa00")));
        }

        txtArboles = findViewById(R.id.txtNumeroArboles);
        txtMuestra = findViewById(R.id.txtArbolesMuestreados);
        txtDetectados = findViewById(R.id.txtFrutosDetectados);
        txtPromedio = findViewById(R.id.txtFrutosArbol);
        txtEstimacion = findViewById(R.id.txtResultado);

        Intent intent = getIntent();
        fruto = intent.getStringExtra("fruto");
        arboles = intent.getIntExtra("arboles", 0);
        muestra = intent.getIntExtra("muestra", 0);
        detecciones = intent.getIntExtra("detecciones", 0);
        promedio = intent.getIntExtra("promedio", 0);
        estimacion = intent.getIntExtra("estimacion", 0);

        mostrarDeteccion();
    }

    private void anuncio(AdRequest adRequest) {
        InterstitialAd.load(this, "ca-app-pub-1183027072386754/9938389254", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdClicked() {
                                super.onAdClicked();
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent();
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                super.onAdFailedToShowFullScreenContent(adError);
                            }

                            @Override
                            public void onAdImpression() {
                                super.onAdImpression();
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent();
                                mInterstitialAd = null;
                            }
                        });
                        Log.i(TAG, "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.d(TAG, loadAdError.toString());
                        //mInterstitialAd = null;
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void mostrarDeteccion() {
        txtArboles.setText("Número de árboles: " + arboles);
        txtMuestra.setText("Árboles muestreados: " + muestra);
        txtDetectados.setText("Frutos Detectados: " + detecciones);
        txtPromedio.setText("Promedio por árbol: " + promedio);
        txtEstimacion.setText(estimacion + " frutos");
    }

    public void insertarDatosDeteccion(View view) {
        try (SQLiteHelperKotlin miBaseDeDatos = new SQLiteHelperKotlin(this)) {
            SQLiteDatabase db = miBaseDeDatos.getWritableDatabase();
            SQLiteDatabase consulta = miBaseDeDatos.getReadableDatabase();
            String fecha = fechasFormatos();
            String[] selectionArgs = {fruto, fecha};
            String query = "select fecha from detecciones WHERE fruto = ? AND fecha = ? ";
            @SuppressLint("Recycle") Cursor cursor = consulta.rawQuery(query, selectionArgs);
            int num_registros = cursor.getCount();
            ContentValues valores = new ContentValues();
            if (num_registros == 0) {
                // Insertar datos en la tabla
                valores.put("fruto", fruto);
                valores.put("fecha", fecha);
                valores.put("cantidad_arbol", arboles);
                valores.put("cantidad_parcela", estimacion);
                db.insert("detecciones", null, valores);
            } else {
                valores.put("cantidad_arbol", arboles);
                valores.put("cantidad_parcela", estimacion);
                String[] args = new String[]{fruto, fecha};
                db.update("detecciones", valores, "fruto=? AND fecha=?", args);
            }
            consulta.close();
            db.close();
            Toast.makeText(this, "Resultados guardados", Toast.LENGTH_SHORT).show();
        } catch (Exception ignored) {
            Toast.makeText(this, "No se pudieron guardar los resultados", Toast.LENGTH_SHORT).show();
        } finally {
            salirDatos(view);

        }
    }

    private String fechasFormatos() {

        // Obtener la fecha actual
        Date currentDate = new Date();

        // Definir el patrón de formato deseado
        //String pattern = "dd/MM/yyyy HH:mm:ss";
        String pattern = "yyyy-MM-dd";
        //String patternConfirmation = "dd/MM/yyyy";

        // Crear un objeto SimpleDateFormat con el patrón
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
        //fechaConf = (new SimpleDateFormat(patternConfirmation, Locale.getDefault())).format(currentDate);
        // Formatear la fecha
        return dateFormat.format(currentDate);
    }

    public void salirDatos(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mInterstitialAd != null) {
            mInterstitialAd.show(ResultadosDeteccion.this);
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.");
        }
    }
}