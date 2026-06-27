package com.example.base64;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class RegistroAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<RegistroModel> lista;

    public RegistroAdapter(Context context, ArrayList<RegistroModel> lista) {
        this.context = context;
        this.lista = lista;
    }

    @Override
    public int getCount() {
        return lista.size();
    }

    @Override
    public Object getItem(int position) {
        return lista.get(position);
    }

    @Override
    public long getItemId(int position) {
        return lista.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_registro, parent, false);
        }

        RegistroModel model = lista.get(position);

        ImageView imgDecoded = convertView.findViewById(R.id.img_registro);
        TextView txtDesc = convertView.findViewById(R.id.txt_descripcion);
        TextView txtFecha = convertView.findViewById(R.id.txt_fecha);
        TextView txtId = convertView.findViewById(R.id.txt_id_badge);

        txtDesc.setText(model.getDescripcionTexto());
        txtFecha.setText(model.getFechaRegistro());
        txtId.setText("ID: #" + model.getId());

        if (model.getImagenBase64() != null && !model.getImagenBase64().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(model.getImagenBase64(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                imgDecoded.setImageBitmap(decodedByte);
            } catch (Exception e) {
                imgDecoded.setImageResource(android.R.drawable.ic_menu_report_image);
            }
        } else {
            imgDecoded.setImageResource(android.R.drawable.ic_menu_camera);
        }

        return convertView;
    }
}