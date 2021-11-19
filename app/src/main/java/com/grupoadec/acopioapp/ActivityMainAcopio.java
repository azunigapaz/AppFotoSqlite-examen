package com.grupoadec.acopioapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;
import com.grupoadec.acopioapp.Adaptadores.ListaAcopioPartidaTemporalAdapter;
import com.grupoadec.acopioapp.Configuracion.SQLiteConexion;
import com.grupoadec.acopioapp.Configuracion.Transacciones;
import com.grupoadec.acopioapp.Models.TablaAcopioPartidaTemporal;
import java.util.ArrayList;
import java.util.Calendar;
import ImpresionESCPOS.ImpresionESCPOS;

public class ActivityMainAcopio extends AppCompatActivity {
    // declaracion de variables
    ImageView btnvolveractivityproductoreslistview,btnactivityproductosparaacopio,btnguardaracopio;
    TextView textViewNoReciboAcopio, textViewProveedorAcopio;
    ListView acopio_listview;
    EditText subtotalacopio_input,impuestosacopio_input,totalacopio_input;
    SQLiteConexion objectSqLiteConexion;
    String dispositivoId,LS_OriginalCopia="",LS_TipoImpresora="";


    ArrayList<TablaAcopioPartidaTemporal> objectArrayListTablaAcopioPartidaTemporalLista = new ArrayList<>();
    TablaAcopioPartidaTemporal objectTablaAcopioPartidaTemporalListaAcopioPartidaTemporal = null;
    ListaAcopioPartidaTemporalAdapter objectAdapter;

    Double subTotal = 0.00, impuesto = 0.00, total = 0.00;
    String [] objectListItem;
    AlertDialog.Builder objectAlertDialogBuilderOpciones;
    AlertDialog.Builder objectAlertDialogBuilderOpcionesCantidadPrecio;
    AlertDialog.Builder objectAlertDialogBuilderOpcionesCopiasImprimir;
    AlertDialog.Builder objectAlertDialogBuilderConfirmarGuardarAcopio;
    AlertDialog.Builder objectAlertDialogBuilderConfirmarImprimirAcopio;

    String validarAccionTipo="";

    Integer tpPartidaNumero;
    String tpPartidaProductoClave;
    String tpPartidaProductoDescripcion;
    Double tpPartidaProductoCantidad;
    Double tpPartidaProductoPrecio;

    String parPeProveedorClave;
    String parPeProveedorNombre;
    String parPeProveedorRtn;

    String parPeAlmacenClave;
    String parPeAlmacenDescripcion;

    // variables para obtener fecha y hora actual
    Calendar objectCalendar;
    SimpleDateFormat objectSimpleDateFormatFechaDocumento;
    String fechaDocumento;
    SimpleDateFormat objectSimpleDateFormatFechaHoraDocumento;
    String fechaHoraDocumento;

    String sufijoDocumento = "",nuevoDocumento = "", reciboNo = "";

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_acopio);

        try{
            btnvolveractivityproductoreslistview = (ImageView) findViewById(R.id.btnvolveractivityproductoreslistview);
            btnactivityproductosparaacopio = (ImageView) findViewById(R.id.btnactivityproductosparaacopio);
            btnguardaracopio = (ImageView) findViewById(R.id.btnguardaracopio);

            textViewNoReciboAcopio = (TextView) findViewById(R.id.textViewNoReciboAcopio);
            textViewProveedorAcopio = (TextView) findViewById(R.id.textViewProveedorAcopio);

            acopio_listview = (ListView) findViewById(R.id.acopio_listview);

            subtotalacopio_input = (EditText) findViewById(R.id.subtotalacopio_input);
            impuestosacopio_input = (EditText) findViewById(R.id.impuestosacopio_input);
            totalacopio_input = (EditText) findViewById(R.id.totalacopio_input);

            objectAlertDialogBuilderOpciones = new AlertDialog.Builder(this);
            objectAlertDialogBuilderOpcionesCantidadPrecio = new AlertDialog.Builder(this);
            objectAlertDialogBuilderConfirmarGuardarAcopio = new AlertDialog.Builder(this);
            objectAlertDialogBuilderConfirmarImprimirAcopio = new AlertDialog.Builder(this);
            objectAlertDialogBuilderOpcionesCopiasImprimir= new AlertDialog.Builder(this);

            objectSqLiteConexion = new SQLiteConexion(this, Transacciones.NameDatabase, null, 1);
            dispositivoId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

            objectCalendar = Calendar.getInstance();
            objectSimpleDateFormatFechaDocumento = new SimpleDateFormat("yyyy-MM-dd");
            fechaDocumento = objectSimpleDateFormatFechaDocumento.format(objectCalendar.getTime());

            objectSimpleDateFormatFechaHoraDocumento = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            fechaHoraDocumento = objectSimpleDateFormatFechaHoraDocumento.format(objectCalendar.getTime());

            ObtenerNumeroDeRecibo();

            // llenamos variables con los datos del putExtra
            String parPeNombres = getIntent().getStringExtra("peNombre");
            String parPeApellidos = getIntent().getStringExtra("peApellidos");
            String parPeCorreo = getIntent().getStringExtra("peCorreo");

            String parPeAccesoBajarDatos = getIntent().getStringExtra("iPeAccesoBajarDatos");
            String parPeAccesoSubirDatos = getIntent().getStringExtra("iPeAccesoSubirDatos");
            String parPeAccesoConfiguracion = getIntent().getStringExtra("iPeAccesoConfiguracion");
            String parPeAccesoRegistroAcopio = getIntent().getStringExtra("iPeAccesoRegistroAcopio");
            String parPeAccesoRegistroProductores = getIntent().getStringExtra("iPeAccesoRegistroProductores");

            parPeProveedorClave = getIntent().getStringExtra("iptProveedorClave");
            parPeProveedorNombre = getIntent().getStringExtra("iptProveedorNombre");
            parPeProveedorRtn = getIntent().getStringExtra("iptProveedorRtn");

            String parPeValidacionNuevaFactura = getIntent().getStringExtra("iPeNuevaFactura");

            parPeAlmacenClave = getIntent().getStringExtra("ipeAlmacenClave");
            parPeAlmacenDescripcion = getIntent().getStringExtra("ipeAlmacenDescripcion");

            textViewProveedorAcopio.setText(parPeProveedorClave.trim() + "-" + parPeProveedorNombre);

            if(parPeValidacionNuevaFactura.equals("1")){
                ElminarProductoAcopioPartidaTemporal();
            }

            ObtenerListaAcopioPartidasTemporal();
            objectAdapter = new ListaAcopioPartidaTemporalAdapter(this,objectArrayListTablaAcopioPartidaTemporalLista);
            acopio_listview.setAdapter(objectAdapter);

            acopio_listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                    try{
                        TablaAcopioPartidaTemporal tp = objectArrayListTablaAcopioPartidaTemporalLista.get(i);
                        tpPartidaNumero = tp.getAcopioPartidaNo();
                        tpPartidaProductoClave = tp.getAcopioPartidaProductoClave();
                        tpPartidaProductoDescripcion = tp.getAcopioPartidaProductoDescripcion();
                        tpPartidaProductoCantidad = tp.getAcopioPartidaProductoCantidad();
                        tpPartidaProductoPrecio = tp.getAcopioPartidaProductoPrecio();

                        objectListItem = new String[]{"Editar cantidad","Editar precio","Eliminar producto del recibo"};
                        objectAlertDialogBuilderOpciones.setTitle("Seleccione un opcion");
                        objectAlertDialogBuilderOpciones.setSingleChoiceItems(objectListItem, -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(objectListItem[i] == "Editar cantidad"){
                                    // 1 = Agregar, 2 = Modificar
                                    validarAccionTipo = "2";

                                    objectAlertDialogBuilderOpcionesCantidadPrecio.setTitle("Ingrese la cantidad");

                                    final EditText cantidadInput = new EditText(getApplicationContext());
                                    cantidadInput.setInputType(InputType.TYPE_CLASS_PHONE);

                                    objectAlertDialogBuilderOpcionesCantidadPrecio.setView(cantidadInput);

                                    objectAlertDialogBuilderOpcionesCantidadPrecio.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            tpPartidaProductoCantidad = Double.parseDouble(cantidadInput.getText().toString());

                                            ActualizarCantidadFilaListViewAcopio(tpPartidaNumero);
                                            objectArrayListTablaAcopioPartidaTemporalLista.clear();

                                            ObtenerListaAcopioPartidasTemporal();

                                            objectAdapter = new ListaAcopioPartidaTemporalAdapter(getApplicationContext(),objectArrayListTablaAcopioPartidaTemporalLista);
                                            acopio_listview.setAdapter(objectAdapter);

                                        }
                                    });
                                    objectAlertDialogBuilderOpcionesCantidadPrecio.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.cancel();
                                        }
                                    });
                                    objectAlertDialogBuilderOpcionesCantidadPrecio.show();

                                    dialogInterface.dismiss();

                                }else if(objectListItem[i] == "Eliminar producto del recibo"){
                                    //Si selecciona la opcion eliminar
                                    EliminarFilaListViewAcopio(tpPartidaNumero);
                                    objectArrayListTablaAcopioPartidaTemporalLista.clear();

                                    ObtenerListaAcopioPartidasTemporal();

                                    objectAdapter = new ListaAcopioPartidaTemporalAdapter(getApplicationContext(),objectArrayListTablaAcopioPartidaTemporalLista);
                                    acopio_listview.setAdapter(objectAdapter);

                                    dialogInterface.dismiss();
                                }else if(objectListItem[i] == "Editar precio"){
                                    objectAlertDialogBuilderOpcionesCantidadPrecio.setTitle("Ingrese el precio");

                                    final EditText cantidadInput = new EditText(getApplicationContext());
                                    cantidadInput.setInputType(InputType.TYPE_CLASS_PHONE);

                                    objectAlertDialogBuilderOpcionesCantidadPrecio.setView(cantidadInput);

                                    objectAlertDialogBuilderOpcionesCantidadPrecio.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            tpPartidaProductoPrecio = Double.parseDouble(cantidadInput.getText().toString());

                                            ActualizarPrecioFilaListViewAcopio(tpPartidaNumero);
                                            objectArrayListTablaAcopioPartidaTemporalLista.clear();

                                            ObtenerListaAcopioPartidasTemporal();

                                            objectAdapter = new ListaAcopioPartidaTemporalAdapter(getApplicationContext(),objectArrayListTablaAcopioPartidaTemporalLista);
                                            acopio_listview.setAdapter(objectAdapter);

                                        }
                                    });
                                    objectAlertDialogBuilderOpcionesCantidadPrecio.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.cancel();
                                        }
                                    });
                                    objectAlertDialogBuilderOpcionesCantidadPrecio.show();

                                    dialogInterface.dismiss();
                                }
                            }
                        });
                        objectAlertDialogBuilderOpciones.setNeutralButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });

                        objectAlertDialogBuilderOpciones.create();
                        objectAlertDialogBuilderOpciones.show();
                    }catch (Exception e){
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    return false;
                }
            });

            btnvolveractivityproductoreslistview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent objectIntent = new Intent(getApplicationContext(),ActivityListViewProveedoresSelect.class);

                    objectIntent.putExtra("iPeNombres", parPeNombres);
                    objectIntent.putExtra("iPeApellidos", parPeApellidos);
                    objectIntent.putExtra("iPeCorreo", parPeCorreo);
                    objectIntent.putExtra("iPeAccesoConfiguracion", parPeAccesoConfiguracion);
                    objectIntent.putExtra("iPeAccesoBajarDatos", parPeAccesoBajarDatos);
                    objectIntent.putExtra("iPeAccesoSubirDatos", parPeAccesoSubirDatos);
                    objectIntent.putExtra("iPeAccesoRegistroProductores", parPeAccesoRegistroProductores);
                    objectIntent.putExtra("iPeAccesoRegistroAcopio", parPeAccesoRegistroAcopio);

                    objectIntent.putExtra("ipeAlmacenClave", parPeAlmacenClave);
                    objectIntent.putExtra("ipeAlmacenDescripcion", parPeAlmacenDescripcion);

                    startActivity(objectIntent);
                    finish();
                }
            });

            btnactivityproductosparaacopio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 1 = Agregar, 2 = Modificar
                    validarAccionTipo = "1";

                    Intent objectIntent = new Intent(getApplicationContext(),ActivityListViewProductosSelectParaAcopio.class);

                    objectIntent.putExtra("iPeNombres", parPeNombres);
                    objectIntent.putExtra("iPeApellidos", parPeApellidos);
                    objectIntent.putExtra("iPeCorreo", parPeCorreo);
                    objectIntent.putExtra("iPeAccesoConfiguracion", parPeAccesoConfiguracion);
                    objectIntent.putExtra("iPeAccesoBajarDatos", parPeAccesoBajarDatos);
                    objectIntent.putExtra("iPeAccesoSubirDatos", parPeAccesoSubirDatos);
                    objectIntent.putExtra("iPeAccesoRegistroProductores", parPeAccesoRegistroProductores);
                    objectIntent.putExtra("iPeAccesoRegistroAcopio", parPeAccesoRegistroAcopio);

                    objectIntent.putExtra("iptProveedorClave", parPeProveedorClave);
                    objectIntent.putExtra("iptProveedorNombre", parPeProveedorNombre);
                    objectIntent.putExtra("iptProveedorRtn", parPeProveedorRtn);

                    // 1 = Agregar, 2 = Modificar
                    objectIntent.putExtra("iptAccionTipo", validarAccionTipo);

                    objectIntent.putExtra("ipeAlmacenClave", parPeAlmacenClave);
                    objectIntent.putExtra("ipeAlmacenDescripcion", parPeAlmacenDescripcion);

                    startActivity(objectIntent);
                    finish();

                }
            });

            btnguardaracopio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try{
                        if(objectArrayListTablaAcopioPartidaTemporalLista.size()>0){
                            objectAlertDialogBuilderConfirmarGuardarAcopio.setMessage("Esta seguro que desea guardar el Recibo No. " + textViewNoReciboAcopio.getText() + " del productor, " + textViewProveedorAcopio.getText())
                                    .setCancelable(false)
                                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            GuardarAcopio();

                                            final EditText cantidadInput = new EditText(getApplicationContext());
                                            cantidadInput.setInputType(InputType.TYPE_CLASS_PHONE);
                                            cantidadInput.setText("2");

                                            objectAlertDialogBuilderConfirmarImprimirAcopio.setMessage("Desea imprimir el Recibo No. " + textViewNoReciboAcopio.getText() + " del productor, " + textViewProveedorAcopio.getText()+"\n\n"+"Ingrese las copias a imprimir")
                                                    .setView(cantidadInput)
                                                    .setCancelable(false)
                                                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {

                                                            Intent objectIntent = new Intent(getApplicationContext(),ActivityListViewProveedoresSelect.class);

                                                            objectIntent.putExtra("iPeNombres", parPeNombres);
                                                            objectIntent.putExtra("iPeApellidos", parPeApellidos);
                                                            objectIntent.putExtra("iPeCorreo", parPeCorreo);
                                                            objectIntent.putExtra("iPeAccesoConfiguracion", parPeAccesoConfiguracion);
                                                            objectIntent.putExtra("iPeAccesoBajarDatos", parPeAccesoBajarDatos);
                                                            objectIntent.putExtra("iPeAccesoSubirDatos", parPeAccesoSubirDatos);
                                                            objectIntent.putExtra("iPeAccesoRegistroProductores", parPeAccesoRegistroProductores);
                                                            objectIntent.putExtra("iPeAccesoRegistroAcopio", parPeAccesoRegistroAcopio);

                                                            objectIntent.putExtra("ipeAlmacenClave", parPeAlmacenClave);
                                                            objectIntent.putExtra("ipeAlmacenDescripcion", parPeAlmacenDescripcion);

                                                            ElminarProductoAcopioPartidaTemporal();

                                                            try {
                                                                ImprimirReciboAcopio(textViewNoReciboAcopio.getText().toString(),cantidadInput.getText().toString());
                                                            } catch (EscPosConnectionException e) {
                                                                e.printStackTrace();
                                                            } catch (EscPosEncodingException e) {
                                                                e.printStackTrace();
                                                            } catch (EscPosBarcodeException e) {
                                                                e.printStackTrace();
                                                            } catch (EscPosParserException e) {
                                                                e.printStackTrace();
                                                            } catch (InterruptedException e) {
                                                                e.printStackTrace();
                                                            }
                                                            startActivity(objectIntent);
                                                            finish();
                                                        }
                                                    })
                                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            //  Action for 'NO' Button
                                                            //dialog.cancel();
                                                            Toast.makeText(getApplicationContext(),"No se imprimio el Recibo No. " + textViewNoReciboAcopio.getText(),
                                                                    Toast.LENGTH_SHORT).show();

                                                            Intent objectIntent = new Intent(getApplicationContext(),ActivityListViewProveedoresSelect.class);

                                                            objectIntent.putExtra("iPeNombres", parPeNombres);
                                                            objectIntent.putExtra("iPeApellidos", parPeApellidos);
                                                            objectIntent.putExtra("iPeCorreo", parPeCorreo);
                                                            objectIntent.putExtra("iPeAccesoConfiguracion", parPeAccesoConfiguracion);
                                                            objectIntent.putExtra("iPeAccesoBajarDatos", parPeAccesoBajarDatos);
                                                            objectIntent.putExtra("iPeAccesoSubirDatos", parPeAccesoSubirDatos);
                                                            objectIntent.putExtra("iPeAccesoRegistroProductores", parPeAccesoRegistroProductores);
                                                            objectIntent.putExtra("iPeAccesoRegistroAcopio", parPeAccesoRegistroAcopio);

                                                            objectIntent.putExtra("ipeAlmacenClave", parPeAlmacenClave);
                                                            objectIntent.putExtra("ipeAlmacenDescripcion", parPeAlmacenDescripcion);

                                                            ElminarProductoAcopioPartidaTemporal();

                                                            startActivity(objectIntent);
                                                            finish();
                                                        }
                                                    });
                                            //Creating dialog box
                                            AlertDialog alertConfirmarImprimirRecibo = objectAlertDialogBuilderConfirmarImprimirAcopio.create();
                                            //Setting the title manually
                                            alertConfirmarImprimirRecibo.setTitle("Alerta");
                                            alertConfirmarImprimirRecibo.show();
                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            //  Action for 'NO' Button
                                            dialog.cancel();
                                            Toast.makeText(getApplicationContext(),"No se guardo el Recibo No. " + textViewNoReciboAcopio.getText(),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            //Creating dialog box
                            AlertDialog alertConfirmarGuardarRecibo = objectAlertDialogBuilderConfirmarGuardarAcopio.create();
                            //Setting the title manually
                            alertConfirmarGuardarRecibo.setTitle("Alerta");
                            alertConfirmarGuardarRecibo.show();

                        }else{
                            Toast.makeText(getApplicationContext(),"Debe agregar al menos 1 producto",Toast.LENGTH_SHORT).show();
                        }

                    }catch (Exception e){
                        Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void ObtenerNumeroDeRecibo() {
        try{
            SQLiteDatabase objectSqLiteDatabase = objectSqLiteConexion.getWritableDatabase();
            String ConsultaSql = "SELECT * FROM " + Transacciones.tablaconfiguraciones + " WHERE ConfiguracionId = '" + dispositivoId + "'";

            Cursor objectCursor = objectSqLiteDatabase.rawQuery(ConsultaSql,null);

            if (objectCursor.getCount()!=0){
                while (objectCursor.moveToNext()){
                    sufijoDocumento = objectCursor.getString(1).toString();
                    // definimos el nuevo numero de recibo (ultimoDucumento + 1)
                    nuevoDocumento = String.valueOf(objectCursor.getInt(2) + 1);
                }
                reciboNo = sufijoDocumento + "-" + Replicar("0", 8 - nuevoDocumento.length()) + nuevoDocumento;
                textViewNoReciboAcopio.setText(reciboNo);
            }else{
                Toast.makeText(getApplicationContext(),"No existe configuración",Toast.LENGTH_SHORT).show();
            }
            objectCursor.close();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    //Replicar caracter
    private String Replicar(String ls_ParametroValor, int li_ParametroCantidadRepetir) {
        String ls_Resultado = "";
        if (li_ParametroCantidadRepetir > 0) {
            for (int li_Contadorfila = 1; li_Contadorfila <= li_ParametroCantidadRepetir; li_Contadorfila++) {
                ls_Resultado = ls_Resultado + ls_ParametroValor;
            }
        }
        return ls_Resultado;
    }

    private void ElminarProductoAcopioPartidaTemporal() {
        SQLiteDatabase db = objectSqLiteConexion.getWritableDatabase();

        Cursor objectCursor = db.rawQuery("SELECT * FROM " + Transacciones.tablaacopiopartidatmp, null);

        if(objectCursor.moveToNext()){
            db.delete(Transacciones.tablaacopiopartidatmp, null, null);
            db.close();
        }
        objectCursor.close();
    }

    private void ObtenerListaAcopioPartidasTemporal() {
        SQLiteDatabase objectSqLiteDatabase = objectSqLiteConexion.getReadableDatabase();

        Cursor objectCursor = objectSqLiteDatabase.rawQuery("SELECT * FROM " + Transacciones.tablaacopiopartidatmp, null);

        while (objectCursor.moveToNext()){
            objectTablaAcopioPartidaTemporalListaAcopioPartidaTemporal = new TablaAcopioPartidaTemporal();
            objectTablaAcopioPartidaTemporalListaAcopioPartidaTemporal.setAcopioPartidaNo(objectCursor.getInt(0));
            objectTablaAcopioPartidaTemporalListaAcopioPartidaTemporal.setAcopioPartidaProductoClave(objectCursor.getString(1));
            objectTablaAcopioPartidaTemporalListaAcopioPartidaTemporal.setAcopioPartidaProductoDescripcion(objectCursor.getString(2));
            objectTablaAcopioPartidaTemporalListaAcopioPartidaTemporal.setAcopioPartidaProductoCantidad(objectCursor.getDouble(3));
            objectTablaAcopioPartidaTemporalListaAcopioPartidaTemporal.setAcopioPartidaProductoPrecio(objectCursor.getDouble(4));
            objectTablaAcopioPartidaTemporalListaAcopioPartidaTemporal.setAcopioPartidaProductoSubTotal(objectCursor.getDouble(5));

            objectArrayListTablaAcopioPartidaTemporalLista.add(objectTablaAcopioPartidaTemporalListaAcopioPartidaTemporal);

            subTotal = subTotal + objectCursor.getDouble(5);
        }

        objectCursor.close();
        objectSqLiteConexion.close();

        total = subTotal + impuesto;

        subtotalacopio_input.setText(String.format(subTotal.toString(), "%.2f"));
        impuestosacopio_input.setText(String.format(impuesto.toString(),"%.2f"));
        totalacopio_input.setText(String.format(total.toString(), "%.2f"));
    }

    private void EliminarFilaListViewAcopio(Integer filaNumero) {
        SQLiteDatabase db = objectSqLiteConexion.getWritableDatabase();
        String [] params = { filaNumero.toString() };
        String wherecond = Transacciones.AcopioPartidaNo + "=?";
        db.delete(Transacciones.tablaacopiopartidatmp, wherecond, params);

        db.close();

        subTotal = 0.00;
        impuesto = 0.00;
        total = 0.00;
    }

    private void ActualizarCantidadFilaListViewAcopio(Integer filaNumero){
        SQLiteDatabase db = objectSqLiteConexion.getWritableDatabase();

        String [] params = { filaNumero.toString() };

        ContentValues valores = new ContentValues();
        valores.put(Transacciones.AcopioPartidaProductoCantidad, tpPartidaProductoCantidad);
        valores.put(Transacciones.AcopioPartidaProductoSubTotal, tpPartidaProductoCantidad * tpPartidaProductoPrecio);
        db.update(Transacciones.tablaacopiopartidatmp, valores, Transacciones.AcopioPartidaNo + "=?", params);

        db.close();

        subTotal = 0.00;
        impuesto = 0.00;
        total = 0.00;
    }

    private void ActualizarPrecioFilaListViewAcopio(Integer filaNumero){
        SQLiteDatabase db = objectSqLiteConexion.getWritableDatabase();

        String [] params = { filaNumero.toString() };

        ContentValues valores = new ContentValues();
        valores.put(Transacciones.AcopioPartidaProductoPrecio, tpPartidaProductoPrecio);
        valores.put(Transacciones.AcopioPartidaProductoSubTotal, tpPartidaProductoCantidad * tpPartidaProductoPrecio);
        db.update(Transacciones.tablaacopiopartidatmp, valores, Transacciones.AcopioPartidaNo + "=?", params);

        db.close();

        subTotal = 0.00;
        impuesto = 0.00;
        total = 0.00;
    }

    private void GuardarAcopio() {

        try{
            SQLiteDatabase objectSqLiteDatabase = objectSqLiteConexion.getWritableDatabase();

            objectSqLiteDatabase.beginTransaction();

            try{
                // encabezado de compras
                ContentValues objectContentValuesCompraEncabezado = new ContentValues();
                objectContentValuesCompraEncabezado.put(Transacciones.CompraEncabezadoDocumento, textViewNoReciboAcopio.getText().toString());
                objectContentValuesCompraEncabezado.put(Transacciones.CompraEncabezadoTipoDocumento, "c");
                objectContentValuesCompraEncabezado.put(Transacciones.CompraEncabezadoProveedorClave, parPeProveedorClave);
                objectContentValuesCompraEncabezado.put(Transacciones.CompraEncabezadoEstado, "O");
                objectContentValuesCompraEncabezado.put(Transacciones.CompraEncabezadoFecha, fechaDocumento);
                objectContentValuesCompraEncabezado.put(Transacciones.CompraEncabezadoSubTotal, subTotal);
                objectContentValuesCompraEncabezado.put(Transacciones.CompraEncabezadoImpuesto, impuesto);
                objectContentValuesCompraEncabezado.put(Transacciones.CompraEncabezadoTotal, subTotal);
                objectContentValuesCompraEncabezado.put(Transacciones.CompraEncabezadoSubTotal, total);
                objectContentValuesCompraEncabezado.put(Transacciones.CompraEncabezadoAlmacen, Integer.parseInt(parPeAlmacenClave));
                objectContentValuesCompraEncabezado.put(Transacciones.CompraEncabezadoFechaHora, fechaHoraDocumento);
                objectContentValuesCompraEncabezado.put(Transacciones.CompraEncabezadoSincronizado, 0);

                Long resultadoInsertComprasEncabezado = objectSqLiteDatabase.insert(Transacciones.tablacomprasencabezado, null, objectContentValuesCompraEncabezado);

                // detalle de compras
                Long resultadoInsertComprasDetalle = 12345678910L;
                Integer partidaContador = 0;
                for(int i = 0; i < objectArrayListTablaAcopioPartidaTemporalLista.size(); i++){
                    partidaContador = partidaContador + 1;
                    ContentValues objectContentValuesCompraDetalle = new ContentValues();
                    objectContentValuesCompraDetalle.put(Transacciones.CompraPartidaTipoDocumento, "c");
                    objectContentValuesCompraDetalle.put(Transacciones.CompraPartidaDocumento, textViewNoReciboAcopio.getText().toString());
                    objectContentValuesCompraDetalle.put(Transacciones.CompraPartidaNumeroFila, partidaContador);
                    objectContentValuesCompraDetalle.put(Transacciones.CompraPartidaProductoClave, objectArrayListTablaAcopioPartidaTemporalLista.get(i).getAcopioPartidaProductoClave());
                    objectContentValuesCompraDetalle.put(Transacciones.CompraPartidaCantidad, Double.parseDouble(objectArrayListTablaAcopioPartidaTemporalLista.get(i).getAcopioPartidaProductoCantidad().toString()));
                    objectContentValuesCompraDetalle.put(Transacciones.CompraPartidaCosto, Double.parseDouble(objectArrayListTablaAcopioPartidaTemporalLista.get(i).getAcopioPartidaProductoPrecio().toString()));
                    objectContentValuesCompraDetalle.put(Transacciones.CompraPartidaImpuesto, 0.00);
                    objectContentValuesCompraDetalle.put(Transacciones.CompraPartidaTotal, Double.parseDouble(objectArrayListTablaAcopioPartidaTemporalLista.get(i).getAcopioPartidaProductoSubTotal().toString()));
                    objectContentValuesCompraDetalle.put(Transacciones.CompraPartidaAlmacen, Integer.parseInt(parPeAlmacenClave));
                    objectContentValuesCompraDetalle.put(Transacciones.CompraPartidaSincronizado, 0);

                    resultadoInsertComprasDetalle = objectSqLiteDatabase.insert(Transacciones.tablacompraspartida, null, objectContentValuesCompraDetalle);
                }
                // cuentas por pagar encabezado

                ContentValues objectContentValuesCxpEncabezado = new ContentValues();
                objectContentValuesCxpEncabezado.put(Transacciones.CuentasPorPagarEncabezadoProveedorClave, parPeProveedorClave);
                objectContentValuesCxpEncabezado.put(Transacciones.CuentasPorPagarEncabezadoConcepto, 1);
                objectContentValuesCxpEncabezado.put(Transacciones.CuentasPorPagarEncabezadoReferencia, textViewNoReciboAcopio.getText().toString());
                objectContentValuesCxpEncabezado.put(Transacciones.CuentasPorPagarEncabezadoFactura, textViewNoReciboAcopio.getText().toString());
                objectContentValuesCxpEncabezado.put(Transacciones.CuentasPorPagarEncabezadoDocumento, textViewNoReciboAcopio.getText().toString());
                objectContentValuesCxpEncabezado.put(Transacciones.CuentasPorPagarEncabezadoImporte, total);
                objectContentValuesCxpEncabezado.put(Transacciones.CuentasPorPagarEncabezadoFechaAplicacion, fechaDocumento);
                objectContentValuesCxpEncabezado.put(Transacciones.CuentasPorPagarEncabezadoFechaVencimiento, fechaDocumento);
                objectContentValuesCxpEncabezado.put(Transacciones.CuentasPorPagarEncabezadoTipoMovimiento, "C");
                objectContentValuesCxpEncabezado.put(Transacciones.CuentasPorPagarEncabezadoSigno, 1);
                objectContentValuesCxpEncabezado.put(Transacciones.CuentasPorPagarEncabezadoFechaHora, fechaHoraDocumento);
                objectContentValuesCxpEncabezado.put(Transacciones.CuentasPorPagarEncabezadoSincronizado, 0);

                Long resultadoInsertCxpEncabezado = objectSqLiteDatabase.insert(Transacciones.tablacuentasporpagarencabezado, null, objectContentValuesCxpEncabezado);

                // Actualizamos el ultimo numero de documento
                String [] params = { dispositivoId };

                ContentValues objectContentValuesUpdateConfiguracion = new ContentValues();
                objectContentValuesUpdateConfiguracion.put(Transacciones.ConfiguracionUltimoDocumento, Integer.parseInt(nuevoDocumento));

                objectSqLiteDatabase.update(Transacciones.tablaconfiguraciones, objectContentValuesUpdateConfiguracion, Transacciones.ConfiguracionId + "=?", params);

                // hacemos commit
                objectSqLiteDatabase.setTransactionSuccessful();

            } finally {
                objectSqLiteDatabase.endTransaction();
                Toast.makeText(getApplicationContext(),"Documento: " + reciboNo + ", guardado",Toast.LENGTH_SHORT).show();
            }

        }catch (Exception e){
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    private void ImprimirReciboAcopio(String Documento,String CopiasImprimir) throws EscPosConnectionException, EscPosEncodingException, EscPosBarcodeException, EscPosParserException, InterruptedException {
        try{
            ImpresionESCPOS LI_Imprimir= new ImpresionESCPOS();
            SQLiteDatabase objectSqLiteDatabase = objectSqLiteConexion.getWritableDatabase();
            String ConsultaSql;
            Cursor objectCursor;
            Integer Copias=Integer.valueOf(CopiasImprimir);

            ConsultaSql = "SELECT A.CompraEncabezadoDocumento,A.CompraEncabezadoFecha,A.CompraEncabezadoProveedorClave,A.CompraEncabezadoEstado,A.CompraEncabezadoSubTotal,A.CompraEncabezadoImpuesto,A.CompraEncabezadoTotal," +
                    "B.CompraPartidaProductoClave,D.ProductoDescripcion,B.CompraPartidaCantidad,B.CompraPartidaCosto,B.CompraPartidaImpuesto,B.CompraPartidaTotal,"+
                    "C.ProveedorNombre,C.ProveedorRtn,C.ProveedorCalle, CASE WHEN trim(C.ProveedorCertificacion) = 'ECOLOGICO' THEN 'ECOLOGICO/FAIRTRADE' WHEN trim(C.ProveedorCertificacion) = 'CONVENCIONAL' THEN 'CONVENCIONAL/FAIRTRADE' WHEN trim(C.ProveedorCertificacion) = 'CONVERSIÓN' THEN 'CONVERSIÓN/FAIRTRADE' ELSE '' END AS  ProveedorCertificacion "+
                    " FROM " + Transacciones.tablacomprasencabezado + " A " +
                    " INNER JOIN "+Transacciones.tablacompraspartida+" B ON B."+Transacciones.CompraPartidaDocumento+"=A."+Transacciones.CompraEncabezadoDocumento+
                    " INNER JOIN "+Transacciones.tablaproveedores+" C ON C."+Transacciones.ProveedorClave+"=A."+Transacciones.CompraEncabezadoProveedorClave+
                    " INNER JOIN "+Transacciones.tablaproductos+" D ON D."+Transacciones.ProductoClave+"=B."+Transacciones.CompraPartidaProductoClave+
                    " WHERE A.CompraEncabezadoDocumento = '" + Documento + "'";

            objectCursor = objectSqLiteDatabase.rawQuery(ConsultaSql,null);
            if (objectCursor.getCount()!=0){
                LI_Imprimir.ImprimirDocumento(objectCursor,"ORIGINAL",Copias);

            }
            else{
                Toast.makeText(getApplicationContext(),"No se obtuvo informacion",Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e){
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }
}
