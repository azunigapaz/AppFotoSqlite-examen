package com.grupoadec.appsavephotosqlite;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ListaContactosAdapter extends BaseAdapter {

    private static LayoutInflater objectLayoutInflater = null;
    Context objectContext;
    ArrayList<ModelContacto> objectArrayListModelContacto;
    ArrayList<ModelContacto> objectArrayListModelContactoOriginal;

    public ListaContactosAdapter(Context objectContext, ArrayList<ModelContacto> objectArrayListModelContacto) {
        try{
            this.objectContext = objectContext;
            this.objectArrayListModelContacto = objectArrayListModelContacto;

            objectArrayListModelContactoOriginal = new ArrayList<>();
            objectArrayListModelContactoOriginal.addAll(objectArrayListModelContacto);

            objectLayoutInflater = (LayoutInflater)objectContext.getSystemService(objectContext.LAYOUT_INFLATER_SERVICE);

        }catch (Exception e){
            Toast.makeText(objectContext, e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View objectView = objectLayoutInflater.inflate(R.layout.single_row,null);

        try{
            TextView sr_imageDetailsTV = (TextView) objectView.findViewById(R.id.sr_imageDetailsTV);
            ImageView sr_imageIV = (ImageView) objectView.findViewById(R.id.sr_imageIV);

            sr_imageDetailsTV.setText(objectArrayListModelContacto.get(position).getNombre());
            sr_imageIV.setImageBitmap(objectArrayListModelContacto.get(position).getImage());

        }catch(Exception e){
            Toast.makeText(objectContext, e.getMessage(),Toast.LENGTH_LONG).show();
        }

        return objectView;
    }

    @Override
    public int getCount() {
        return objectArrayListModelContacto.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void filtrarNombre(final CharSequence txtBuscar) {
        int longitud = txtBuscar.length();
        if (longitud > 0) {
            objectArrayListModelContacto.clear();
            for(int i = 0; i < objectArrayListModelContactoOriginal.size(); i++){
                if(objectArrayListModelContactoOriginal.get(i).getNombre().toLowerCase().contains(txtBuscar.toString().toLowerCase())){
                    objectArrayListModelContacto.add(objectArrayListModelContactoOriginal.get(i));
                }
            }
            notifyDataSetChanged();
        }else{
            objectArrayListModelContacto.clear();
            objectArrayListModelContacto.addAll(objectArrayListModelContactoOriginal);
            notifyDataSetChanged();
        }
    }

    public void buscarNombre(final CharSequence txtBuscar) {
        int longitud = txtBuscar.length();
        if (longitud == 0) {
            objectArrayListModelContacto.clear();
            objectArrayListModelContacto.addAll(objectArrayListModelContactoOriginal);
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                List<ModelContacto> collecion = objectArrayListModelContacto.stream()
                        .filter(i -> i.getNombre().toLowerCase().contains(txtBuscar.toString().toLowerCase()))
                        .collect(Collectors.toList());
                objectArrayListModelContacto.clear();
                objectArrayListModelContacto.addAll(collecion);
                notifyDataSetChanged();
            } else {
                for (int i = 0; i < objectArrayListModelContactoOriginal.size(); i++) {
                    if (objectArrayListModelContactoOriginal.get(i).getNombre().toLowerCase().contains(txtBuscar.toString().toLowerCase())) {
                        objectArrayListModelContacto.add(objectArrayListModelContactoOriginal.get(i));
                    }
                }
            }
        }
        notifyDataSetChanged();
    }


}
