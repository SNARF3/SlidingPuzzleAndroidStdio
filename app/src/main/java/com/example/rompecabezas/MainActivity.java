package com.example.rompecabezas;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1; // Código para la galería
    private static final int REQUEST_IMAGE_CAPTURE = 2; // Código para la cámara
    private static final int REQUEST_CAMERA_PERMISSION = 100; // Código para solicitar permisos


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Referencia al ImageView que actúa como botón
        ImageView bt3x3 = findViewById(R.id.bt3x3);
        bt3x3.setOnClickListener(v -> mostrarDialogoSeleccionImagen());
    }

    // Mostrar diálogo para elegir entre galería y cámara
    private void mostrarDialogoSeleccionImagen() {
        CharSequence[] opciones = {"Galería", "Cámara"};
        new AlertDialog.Builder(this)
                .setTitle("Seleccionar imagen")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        abrirGaleria();
                    } else {
                        abrirCamara();
                    }
                })
                .show();
    }

    // Abrir la galería para seleccionar imagen
    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*"); // Filtra solo imágenes
        startActivityForResult(intent, PICK_IMAGE);
    }

    // Abrir la cámara para capturar imagen
    private void abrirCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CAMERA_PERMISSION);
        } else {
            iniciarIntentCamara();
        }
    }

    // Manejar respuesta de solicitud de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                iniciarIntentCamara();
            } else {
                Toast.makeText(this, "Se necesitan permisos para usar la cámara", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Iniciar la cámara creando un archivo para guardar la foto
    private void iniciarIntentCamara() {
        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE); // Usar la constante correcta
        } catch (Error e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al crear el archivo de imagen", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) { // Para la cámara
            Bundle extras = data.getExtras();
            Bitmap image = (Bitmap) extras.get("data");
            if (image != null) {
                abrirFragment3x3(image);
            }
        } else if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) { // Para la galería
            if (data != null && data.getData() != null) {
                try {
                    Uri selectedImageUri = data.getData();
                    Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    abrirFragment3x3(image);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private void abrirFragment3x3(Bitmap image) {
        if (findViewById(R.id.main) != null) {
            // Dividir la imagen en 9 partes iguales
            ArrayList<byte[]> imageParts = dividirImagenEnPartes(image, 9);

            // Crear el fragmento y pasar el ArrayList<byte[]> mediante un Bundle
            fragment3x3 fragment = new fragment3x3();
            Bundle bundle = new Bundle();
            bundle.putSerializable("imageParts", imageParts); // Usar "imageParts" como clave
            fragment.setArguments(bundle);

            // Realizar la transacción del fragmento
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.main, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        } else {
            Toast.makeText(this, "Contenedor no encontrado", Toast.LENGTH_SHORT).show();
        }
    }

    // Función para dividir la imagen en 9 partes iguales (3x3)
    private ArrayList<byte[]> dividirImagenEnPartes(Bitmap image, int numParts) {
        ArrayList<byte[]> imageParts = new ArrayList<>();

        int width = image.getWidth();
        int height = image.getHeight();

        // Calcular el tamaño de cada parte
        int partWidth = width / 3; // Dividir en 3 columnas
        int partHeight = height / 3; // Dividir en 3 filas (3x3 = 9 partes)

        // Recorrer la imagen y cortar las partes
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                // Calcular las coordenadas de la parte
                int x = col * partWidth;
                int y = row * partHeight;

                // Cortar la parte de la imagen
                Bitmap part = Bitmap.createBitmap(image, x, y, partWidth, partHeight);

                // Convertir la parte a byte[] y agregarla al ArrayList
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                part.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                imageParts.add(byteArray);
            }
        }

        return imageParts;
    }
}