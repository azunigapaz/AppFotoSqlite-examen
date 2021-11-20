package com.grupoadec.appsavephotosqlite;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityActualizarContacto extends AppCompatActivity {

    Integer idContacto;
    String nombreContacto;
    String telefonoContacto;
    String latitudContacto;
    String longitudContacto;
    RequestQueue requestQueue;
    String apiActualizarContacto;
    private static final int PICK_IMAGE_REQUEST=100;
    Button updateBtn;

    EditText actualizarnombrecontacto_input, actualizartelefonocontacto_input;
    TextView actualizarlongitudcontacto_input, actualizarlatitudcontacto_input;
    ImageView actualizarimage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actualizar_contacto);

        idContacto = Integer.parseInt(getIntent().getStringExtra("pieContactoID"));
        nombreContacto = getIntent().getStringExtra("nombreContacto");
        telefonoContacto = getIntent().getStringExtra("telefonoContacto");
        latitudContacto = getIntent().getStringExtra("latitudContacto");
        longitudContacto = getIntent().getStringExtra("longitudContacto");


        updateBtn = (Button)findViewById(R.id.updateBtn);
        actualizarnombrecontacto_input = (EditText)findViewById(R.id.actualizarnombrecontacto_input);
        actualizartelefonocontacto_input = (EditText)findViewById(R.id.actualizartelefonocontacto_input);
        actualizarlongitudcontacto_input = (TextView) findViewById(R.id.actualizarlongitudcontacto_input);
        actualizarlatitudcontacto_input = (TextView) findViewById(R.id.actualizarlatitudcontacto_input);
        actualizarimage = (ImageView) findViewById(R.id.actualizarimage);


        actualizarnombrecontacto_input.setText(nombreContacto);
        actualizartelefonocontacto_input.setText(telefonoContacto);
        actualizarlongitudcontacto_input.setText(latitudContacto);
        actualizarlatitudcontacto_input.setText(longitudContacto);
        //actualizarimage

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    public void chooseImage(View objectView){
        try {
            Intent objectIntent=new Intent();
            objectIntent.setType("actualizarimage/*");

            objectIntent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(objectIntent,PICK_IMAGE_REQUEST);
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void actulizaContacto(){
        try {

            StringRequest stringRequestActualizaContacto = new StringRequest(Request.Method.POST, apiActualizarContacto,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String serverResponse) {
                            // recibimos la respuesta del web services
                            try{

                                JSONObject jsonObject = new JSONObject(serverResponse);

                                // obtenemos las variables declaradas en el webservice
                                String mensajeApi = jsonObject.getString("mensajeactualizacontacto");
                                Toast.makeText(getApplicationContext(),mensajeApi,Toast.LENGTH_SHORT).show();

                            }catch (JSONException ex){
                                ex.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // si hay algun error por parte de la libreria Voley

                    // mostramos el error de la libreria
                    Toast.makeText(getApplicationContext(),error.toString(),Toast.LENGTH_LONG).show();
                }
            }){
                // el primer paso es enviar los datos al web services, con sus respectivos parametros
                // se hace un mapeo de un arreglo de 2 dimesiones
                protected Map<String,String> getParams(){
                    Map<String,String> parametros = new HashMap<>();
                    // parametros que enviaremos al web service
                    parametros.put("id", idContacto.toString());
                    parametros.put("contactonombre", nombreContacto);
                    parametros.put("contactotelefono", telefonoContacto);
                    parametros.put("latitud", latitudContacto);
                    parametros.put("longitud", longitudContacto);
                    return parametros;
                }
            };

            // ejecutamos la cadena(enviamos la peticion)
            requestQueue.add(stringRequestActualizaContacto);

        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}