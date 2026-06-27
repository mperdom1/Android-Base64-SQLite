package com.example.base64;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_AUDIO_PERMISSION = 101;
    private static final int REQUEST_IMAGE_CAPTURE = 200;
    private static final int REQUEST_SPEECH_RECOGNIZER = 201;

    private ImageView ivPreview;
    private EditText etDescripcion;
    private Button btnTomarFoto, btnGrabarVoz, btnGuardar, btnConsultar;
    private ListView lvRecords;
    private TextView txtBadge, tvPlaceholder;

    private String imagenBase64Val = "";
    private DBHelper dbHelper;
    private RegistroAdapter adapter;
    private ArrayList<RegistroModel> listaRegistros;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivPreview = findViewById(R.id.iv_preview);
        etDescripcion = findViewById(R.id.et_descripcion);
        btnTomarFoto = findViewById(R.id.btn_tomar_foto);
        btnGrabarVoz = findViewById(R.id.btn_grabar_voz);
        btnGuardar = findViewById(R.id.btn_guardar);
        btnConsultar = findViewById(R.id.btn_consultar);
        lvRecords = findViewById(R.id.lv_records);
        txtBadge = findViewById(R.id.txt_badge);
        tvPlaceholder = findViewById(R.id.tv_placeholder_text);

        dbHelper = new DBHelper(this);
        listaRegistros = new ArrayList<>();

        btnTomarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verificarPermisosCamara();
            }
        });

        btnGrabarVoz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verificarPermisosAudio();
            }
        });

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarRegistroEnSQLite();
            }
        });

        btnConsultar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                consultarYMostrarRegistros();
            }
        });
    }

    private void verificarPermisosCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            abrirCamara();
        }
    }

    private void verificarPermisosAudio() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO_PERMISSION);
        } else {
            iniciarReconocimientoVoz();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirCamara();
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                iniciarReconocimientoVoz();
            } else {
                Toast.makeText(this, "Permiso de audio denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void abrirCamara() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "No se encontró aplicación de Cámara", Toast.LENGTH_SHORT).show();
        }
    }

    private void iniciarReconocimientoVoz() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hable para escribir la descripción...");
        try {
            startActivityForResult(intent, REQUEST_SPEECH_RECOGNIZER);
        } catch (Exception e) {
            Toast.makeText(this, "Su dispositivo no soporta reconocimiento de voz", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                ivPreview.setImageBitmap(photo);
                ivPreview.setAlpha(1.0f);
                if (tvPlaceholder != null) tvPlaceholder.setVisibility(View.GONE);
                imagenBase64Val = convertirBitmapABase64(photo);
                Toast.makeText(this, "Imagen convertida a Base64", Toast.LENGTH_SHORT).show();

            } else if (requestCode == REQUEST_SPEECH_RECOGNIZER) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (result != null && !result.isEmpty()) {
                    String speechText = result.get(0);
                    etDescripcion.setText(speechText);
                    Toast.makeText(this, "Transcripción completada", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String convertirBitmapABase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void guardarRegistroEnSQLite() {
        String descripcion = etDescripcion.getText().toString().trim();

        if (imagenBase64Val.isEmpty()) {
            Toast.makeText(this, "Primero capture una fotografía", Toast.LENGTH_SHORT).show();
            return;
        }
        if (descripcion.isEmpty()) {
            Toast.makeText(this, "Por favor, agregue una descripción por voz", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_IMAGEN_BASE64, imagenBase64Val);
        values.put(DBHelper.COLUMN_DESCRIPCION, descripcion);

        String fechaActual = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        values.put(DBHelper.COLUMN_FECHA_REGISTRO, fechaActual);

        long resultRowId = db.insert(DBHelper.TABLE_REGISTROS, null, values);

        if (resultRowId != -1) {
            Toast.makeText(this, "¡Registro guardado en SQLite con ID #" + resultRowId + "!", Toast.LENGTH_LONG).show();
            ivPreview.setImageResource(android.R.drawable.ic_menu_camera);
            ivPreview.setAlpha(0.3f);
            if (tvPlaceholder != null) tvPlaceholder.setVisibility(View.VISIBLE);
            etDescripcion.setText("");
            imagenBase64Val = "";
        } else {
            Toast.makeText(this, "Error al guardar en la Base de Datos", Toast.LENGTH_SHORT).show();
        }
    }

    private void consultarYMostrarRegistros() {
        listaRegistros.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] columns = {
                DBHelper.COLUMN_ID,
                DBHelper.COLUMN_IMAGEN_BASE64,
                DBHelper.COLUMN_DESCRIPCION,
                DBHelper.COLUMN_FECHA_REGISTRO
        };

        Cursor cursor = db.query(DBHelper.TABLE_REGISTROS, columns, null, null, null, null, DBHelper.COLUMN_ID + " DESC");

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_ID));
                String b64 = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_IMAGEN_BASE64));
                String desc = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_DESCRIPCION));
                String fecha = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_FECHA_REGISTRO));

                listaRegistros.add(new RegistroModel(id, b64, desc, fecha));
            } while (cursor.moveToNext());
        }
        cursor.close();

        if (adapter == null) {
            adapter = new RegistroAdapter(this, listaRegistros);
            lvRecords.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }

        if (listaRegistros.isEmpty()) {
            Toast.makeText(this, "No hay registros guardados aún en SQLite", Toast.LENGTH_SHORT).show();
            txtBadge.setText("0 notas");
        } else {
            Toast.makeText(this, "Mostrando " + listaRegistros.size() + " registros.", Toast.LENGTH_SHORT).show();
            txtBadge.setText(listaRegistros.size() + " notas");
            lvRecords.setVisibility(View.VISIBLE);
        }
    }
}