package com.example.base64;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.io.ByteArrayOutputStream;

public class DetailActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK = 301;

    private ImageView ivDetailImage;
    private TextView tvDetailDate;
    private EditText etDetailDescription;
    private CardView cvBack, cvDelete;
    private Button btnUpdate, btnChangeImage;

    private RegistroModel model;
    private DBHelper dbHelper;
    private String newImagenBase64 = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ivDetailImage = findViewById(R.id.iv_detail_image);
        tvDetailDate = findViewById(R.id.tv_detail_date);
        etDetailDescription = findViewById(R.id.et_detail_description);
        cvBack = findViewById(R.id.cv_back);
        cvDelete = findViewById(R.id.cv_delete);
        btnUpdate = findViewById(R.id.btn_update);
        btnChangeImage = findViewById(R.id.btn_change_image);

        dbHelper = new DBHelper(this);

        model = (RegistroModel) getIntent().getSerializableExtra("registro");

        if (model != null) {
            displayData();
            newImagenBase64 = model.getImagenBase64();
        }

        cvBack.setOnClickListener(v -> finish());

        cvDelete.setOnClickListener(v -> showDeleteConfirmation());

        btnUpdate.setOnClickListener(v -> updateRegistro());

        btnChangeImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE_PICK);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_IMAGE_PICK && data != null) {
            try {
                Bitmap photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                Bitmap scaled = escalarBitmap(photo, 800);
                ivDetailImage.setImageBitmap(scaled);
                newImagenBase64 = convertirBitmapABase64(scaled);
            } catch (Exception e) {
                Toast.makeText(this, "Error al cambiar imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String convertirBitmapABase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private Bitmap escalarBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    private void displayData() {
        tvDetailDate.setText("Fecha: " + model.getFechaRegistro());
        etDetailDescription.setText(model.getDescripcionTexto());

        if (model.getImagenBase64() != null && !model.getImagenBase64().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(model.getImagenBase64(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                ivDetailImage.setImageBitmap(decodedByte);
            } catch (Exception e) {
                ivDetailImage.setImageResource(android.R.drawable.ic_menu_report_image);
            }
        }
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Nota")
                .setMessage("¿Estás seguro de que deseas eliminar esta nota?")
                .setPositiveButton("Eliminar", (dialog, which) -> deleteRegistro())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deleteRegistro() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int deletedRows = db.delete(DBHelper.TABLE_REGISTROS, DBHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(model.getId())});

        if (deletedRows > 0) {
            Toast.makeText(this, "Nota eliminada", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Error al eliminar nota", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateRegistro() {
        String newDescription = etDetailDescription.getText().toString().trim();
        if (newDescription.isEmpty()) {
            Toast.makeText(this, "La descripción no puede estar vacía", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_DESCRIPCION, newDescription);
        values.put(DBHelper.COLUMN_IMAGEN_BASE64, newImagenBase64);

        int updatedRows = db.update(DBHelper.TABLE_REGISTROS, values, DBHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(model.getId())});

        if (updatedRows > 0) {
            Toast.makeText(this, "Nota actualizada", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Error al actualizar nota", Toast.LENGTH_SHORT).show();
        }
    }
}
