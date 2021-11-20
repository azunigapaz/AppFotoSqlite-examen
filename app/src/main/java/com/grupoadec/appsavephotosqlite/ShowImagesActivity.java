package com.grupoadec.appsavephotosqlite;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ShowImagesActivity extends AppCompatActivity {
    ListView objectListView;
    ArrayList<ModelContacto> objectArrayListModelContacto = new ArrayList<>();
    ModelContacto objectModelContactoLista = null;
    ListaContactosAdapter objectAdapter;
    RequestQueue requestQueue;
    String httpUri = "http://pm2examengrupo1.luiszuniga.site/api/";
    String apiGetContactos, apiInsertarContacto, apiActualizarContacto, apiEliminarContacto;

    EditText buscarcontactos_input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_images);
        try {
            objectListView=findViewById(R.id.contactoslistview);
            buscarcontactos_input = (EditText) findViewById(R.id.buscarcontactos_input);

            // inicializamos requestQueue
            requestQueue = Volley.newRequestQueue(this);

            apiGetContactos = httpUri + "consultacontacts.php";

            obtenerListaContactos();

            objectAdapter = new ListaContactosAdapter(this, objectArrayListModelContacto);
            objectListView.setAdapter(objectAdapter);

            buscarcontactos_input.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    objectAdapter.buscarNombre(charSequence);
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void obtenerListaContactos() {
        try{

            StringRequest stringRequestBajarUsuarios = new StringRequest(Request.Method.POST, apiGetContactos,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String serverResponse) {
                            // recibimos la respuesta del web services
                            try{

                                JSONObject jsonObject = new JSONObject(serverResponse);
                                JSONArray objectJsonArrayTablaContactos = jsonObject.getJSONArray("tablausuarios");

                                // obtenemos las variables declaradas en el webservice
                                String mensajeApi = jsonObject.getString("mensajeobtenerusuario");
                                Toast.makeText(getApplicationContext(),mensajeApi,Toast.LENGTH_SHORT).show();

                                //Toast.makeText(getApplicationContext(), "Usuarios actualizados", Toast.LENGTH_SHORT).show();

                                for(int i = 0; i < objectJsonArrayTablaContactos.length(); i++){
                                    JSONObject jsonObjectContactos = objectJsonArrayTablaContactos.getJSONObject(i);
                                    objectModelContactoLista = new ModelContacto();
                                    objectModelContactoLista.setId(jsonObjectContactos.getInt("id"));
                                    objectModelContactoLista.setNombre(jsonObjectContactos.getString("nombre"));
                                    objectModelContactoLista.setTelefono(jsonObjectContactos.getString("telefomo"));
                                    objectModelContactoLista.setLatitud(jsonObjectContactos.getString("latitud"));
                                    objectModelContactoLista.setLongitud(jsonObjectContactos.getString("longitud"));
                                    //objectModelContactoLista.setImage(jsonObjectContactos.getString("imagen"));
                                    objectArrayListModelContacto.add(objectModelContactoLista);
                                }

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
                    String nombre = buscarcontactos_input.getText().toString();
                    // parametros que enviaremos al web service
                    parametros.put("opcion", "obtenerusuarios");
                    parametros.put("nombre", nombre);

                    return parametros;
                }
            };

            // ejecutamos la cadena(enviamos la peticion)
            requestQueue.add(stringRequestBajarUsuarios);

        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}