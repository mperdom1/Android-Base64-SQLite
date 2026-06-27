package com.example.base64;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "RegistrosDB.db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_REGISTROS = "registros";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_IMAGEN_BASE64 = "imagenBase64";
    public static final String COLUMN_DESCRIPCION = "descripcionTexto";
    public static final String COLUMN_FECHA_REGISTRO = "fechaRegistro";

    private static final String CREATE_TABLE_REGISTROS = "CREATE TABLE " + TABLE_REGISTROS + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_IMAGEN_BASE64 + " TEXT NOT NULL, "
            + COLUMN_DESCRIPCION + " TEXT NOT NULL, "
            + COLUMN_FECHA_REGISTRO + " TEXT NOT NULL"
            + ");";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_REGISTROS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REGISTROS);
        onCreate(db);
    }
}