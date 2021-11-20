package com.grupoadec.appsavephotosqlite;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    // declaracion de variables
    private EditText imageDetailsET;
    private ImageView objectImageView;

    private static final int PICK_IMAGE_REQUEST=100;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int PETICION_ACCESO_CAM = 101;

    private Uri imageFilePath;
    private Bitmap imageToStore;

    String httpUri = "https://pm2examengrupo1.luiszuniga.site/api/";
    String apiGetContactos, apiInsertarContacto, apiActualizarContacto, apiEliminarContacto;
    RequestQueue requestQueue;
    Button saveBtn;

    DatabaseHandler objectDatabaseHandler;
    String currentPhotoPath;
    EditText nombrecontacto_input, telefonocontacto_input;
    TextView longitudcontacto_input, latitudcontacto_input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            imageDetailsET=findViewById(R.id.nombrecontacto_input);
            objectImageView=findViewById(R.id.image);
            saveBtn = (Button)findViewById(R.id.saveBtn);

            apiInsertarContacto = httpUri + "crearcontacto.php";
            requestQueue = Volley.newRequestQueue(MainActivity.this);

            nombrecontacto_input = (EditText) findViewById(R.id.nombrecontacto_input);
            telefonocontacto_input = (EditText) findViewById(R.id.telefonocontacto_input);

            longitudcontacto_input = (TextView) findViewById(R.id.longitudcontacto_input);
            latitudcontacto_input = (TextView) findViewById(R.id.latitudcontacto_input);

            longitudcontacto_input.setText("0.00");
            latitudcontacto_input.setText("0.00");

            saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    insertarContactos();
                }
            });

        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void chooseImage(View objectView){
        try {
            Intent objectIntent=new Intent();
            objectIntent.setType("image/*");

            objectIntent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(objectIntent,PICK_IMAGE_REQUEST);
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int RequestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(RequestCode, permissions, grantResults);

        if (RequestCode == PETICION_ACCESO_CAM) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                tomarPhoto();
            } else {
                Toast.makeText(getApplicationContext(), "Se necesita el permiso de camara", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);

            if(requestCode==PICK_IMAGE_REQUEST && resultCode==RESULT_OK && data!=null && data.getData()!=null){
                imageFilePath=data.getData();
                imageToStore= MediaStore.Images.Media.getBitmap(getContentResolver(),imageFilePath);

                objectImageView.setImageBitmap(imageToStore);
            }

            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                // Bundle extras = data.getExtras();
                imageToStore = BitmapFactory.decodeFile(currentPhotoPath);
                objectImageView.setImageBitmap(imageToStore);
            }

        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void insertarContactos() {
        String nombrecontacto = nombrecontacto_input.getText().toString();
        String telefonocontacto = telefonocontacto_input.getText().toString();

        //ProgressDialog progressDialog = new ProgressDialog(this);
        if(nombrecontacto.isEmpty()) {
            nombrecontacto_input.setError("Ingrese el nombre del contacto por favor");
        } else if(telefonocontacto.isEmpty()) {
            telefonocontacto_input.setError("Ingrese el telefono del contacto por favor");
        } else {
            //progressDialog.show();
            StringRequest stringRequestInsertarContacto = new StringRequest(Request.Method.POST, apiInsertarContacto,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String serverResponse) {
                            // recibimos la respuesta del web services
                            try{

                                JSONObject jsonObject = new JSONObject(serverResponse);

                                // obtenemos las variables declaradas en el webservice
                                String mensajeApi = jsonObject.getString("mensajeinsertarcontacto");
                                Toast.makeText(getApplicationContext(),mensajeApi,Toast.LENGTH_SHORT).show();

                                //Toast.makeText(getApplicationContext(), "Usuarios actualizados", Toast.LENGTH_SHORT).show();


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
                protected Map<String,String> getParams()
                {
                    String image = GetStringImage(imageToStore);
                    Map<String,String> parametros = new HashMap<>();
                    // parametros que enviaremos al web service
                    parametros.put("contactonombre", nombrecontacto);
                    parametros.put("contactotelefono", telefonocontacto);
                    parametros.put("latitud", latitudcontacto_input.getText().toString());
                    parametros.put("longitud", longitudcontacto_input.getText().toString());
                    parametros.put("imagen", image);

                    return parametros;
                }
            };

            // ejecutamos la cadena(enviamos la peticion)
            requestQueue.add(stringRequestInsertarContacto);
        }
    }

//    public void storeImage(View view){
//        try {
//            if(!imageDetailsET.getText().toString().isEmpty() && objectImageView.getDrawable() !=null && imageToStore!=null){
//                objectDatabaseHandler.storeImage(new ModelClass(imageDetailsET.getText().toString(),imageToStore));
//                cleanObjects();
//            }else{
//                Toast.makeText(this, "Por favor escriba un texto y seleccione una imagen", Toast.LENGTH_SHORT).show();
//            }
//        }catch (Exception e){
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//    }

    public void moveToShowActivity(View view){
        startActivity(new Intent(this, ShowImagesActivity.class));
    }

    // Para tomar foto
    public void permisos(View view) {
        // Valido si el permiso esta otorgado
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Otorgo el permiso si no lo tengo
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.CAMERA }, PETICION_ACCESO_CAM);
        } else {
            tomarPhoto();
        }
    }

    private void tomarPhoto() {
        Intent TomarPhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (TomarPhoto.resolveActivity(getPackageManager()) != null) {
            File photo = null;
            try {
                photo = createImageFile();
            } catch(IOException ex) {
                Log.e("Error", ex.toString());
            }
            if(photo != null) {
                imageFilePath = FileProvider.getUriForFile(this, "com.grupoadec.appsavephotosqlite.fileprovider", photo);
                TomarPhoto.putExtra(MediaStore.EXTRA_OUTPUT, imageFilePath);
                startActivityForResult(TomarPhoto, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String imageFileName = "JPEG_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void cleanObjects(){
        imageDetailsET.setText("");

        String uri = "@drawable/mood";

        int imageResource = getResources().getIdentifier(uri, null, getPackageName());
        Drawable res = getResources().getDrawable(imageResource);
        objectImageView.setImageDrawable(res);
    }

    private String GetStringImage(Bitmap imageToStore) {
        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        imageToStore.compress(Bitmap.CompressFormat.JPEG, 100,ba);
        byte[] imagebyte = ba.toByteArray();
        String encode = Base64.encodeToString(imagebyte, Base64.DEFAULT);
        return encode;
    }

}