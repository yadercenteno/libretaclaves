package com.yadercenteno.libretaclaves;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import net.sqlcipher.database.SQLiteDatabase;

import java.io.File;
import java.util.HashMap;
import java.util.NoSuchElementException;

import static com.yadercenteno.libretaclaves.Constantes.COLUMNA_ID;
import static com.yadercenteno.libretaclaves.Constantes.COLUMNA_NOMBRE;

public class Editar extends AppCompatActivity {

    private TextView textViewCategoria_id;
    private TextView textViewID;
    private EditText editTextNombre;
    private EditText editTextUsuario;
    private EditText editTextClave;
    private EditText editTextURL;
    private EditText editTextPreguntaSeguridad;
    private EditText editTextRespuestaSeguridad;
    private EditText editTextNotas;

    private ToggleButton toggleButtonUsuario;
    private ToggleButton toggleButtonClave;

    private Spinner combo_Categoria;
    SimpleCursorAdapter sca_categorias;

    private Button buttonCancelar;
    private Button buttonGuardar;

    public String Llave = "";
    public String Id_registro = "";
    public String accion = "";
    private Context context = this;
    public Boolean autenticado;
    File databaseFile = null;
    SQLiteDatabase db;

    Integer cant_categorias_lista = 0;

    private android.app.AlertDialog.Builder alertDialog1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar);

        textViewCategoria_id = (TextView) findViewById(R.id.textViewCategoria_id);
        textViewID = (TextView) findViewById(R.id.textViewID);

        buttonCancelar = (Button) findViewById(R.id.buttonCancelar);
        buttonGuardar = (Button) findViewById(R.id.buttonGuardar);

        combo_Categoria = (Spinner) findViewById(R.id.combo_Categoria);

        toggleButtonUsuario = (ToggleButton) findViewById(R.id.toggleButtonUsuario);
        toggleButtonClave = (ToggleButton) findViewById(R.id.toggleButtonClave);

        editTextNombre = (EditText) findViewById(R.id.editTextNombre);
        editTextUsuario = (EditText) findViewById(R.id.editTextUsuario);
        editTextClave = (EditText) findViewById(R.id.editTextClave);
        editTextURL = (EditText) findViewById(R.id.editTextURL);
        editTextPreguntaSeguridad = (EditText) findViewById(R.id.editTextPreguntaSeguridad);
        editTextRespuestaSeguridad = (EditText) findViewById(R.id.editTextRespuestaSeguridad);
        editTextNotas = (EditText) findViewById(R.id.editTextNotas);

        toggleButtonUsuario.setBackgroundResource(R.drawable.on);
        toggleButtonClave.setBackgroundResource(R.drawable.off);

        // Esto es para saber si la clave de la BD
        Bundle extras = getIntent().getExtras();
        // Obtenemos datos enviados en el intent.
        if ((extras.getString("Llave") != null) || (extras.getString("Llave") != "")) {
            Llave = extras.getString("Llave");
        }
        if ((extras.getString("id") != null) || (extras.getString("id") != "")) {
            Id_registro = extras.getString("id");
        }
        if ((extras.getString("accion") != null) || (extras.getString("accion") != "")) {
            accion = extras.getString("accion");
        }

        // Si se ha guardado la clave ANTES, se restaura
        if (savedInstanceState != null) {
            Llave = savedInstanceState.getString("Llave");
            Id_registro = savedInstanceState.getString("id");
            accion = savedInstanceState.getString("accion");
        }

        SQLiteDatabase.loadLibs(this);

        // Dependiendo de la acción habilito o no los campos
        if (accion.equals("Ver")) {
            buttonGuardar.setText("Editar");
            buttonGuardar.setCompoundDrawablesWithIntrinsicBounds(R.drawable.boton_editar, 0, 0, 0);

            combo_Categoria.setEnabled(false);
            editTextNombre.setEnabled(false);
            editTextUsuario.setEnabled(false);
            editTextClave.setEnabled(false);
            editTextURL.setEnabled(false);
            editTextPreguntaSeguridad.setEnabled(false);
            editTextRespuestaSeguridad.setEnabled(false);
            editTextNotas.setEnabled(false);
        }
        else {
            buttonGuardar.setText("Guardar");
            buttonGuardar.setCompoundDrawablesWithIntrinsicBounds(R.drawable.boton_guardar, 0, 0, 0);
        }

        try {
            File databaseFile1 = getDatabasePath("libreta.db");
            db = SQLiteDatabase.openOrCreateDatabase(databaseFile1, Llave, null);

            android.database.Cursor cursor_listado = db.rawQuery("SELECT * FROM libretas WHERE id=" + Id_registro, null);

            if (cursor_listado != null) {
                int listado_id0 = 0;
                int listado_categoria_id0 = 0;
                String listado_id = "";
                String listado_categoria_id = "";
                String listado_nombre = "";
                String listado_usuario = "";
                String listado_clave = "";
                String listado_url = "";
                String listado_pregunta_seguridad = "";
                String listado_respuesta_seguridad = "";
                String listado_notas = "";

                if (cursor_listado.moveToFirst()) {
                    do {
                        listado_id0 = cursor_listado.getInt(0);
                        listado_categoria_id0 = cursor_listado.getInt(1);
                        listado_id = Integer.toString(listado_id0);
                        listado_categoria_id = Integer.toString(listado_categoria_id0);
                        listado_nombre = cursor_listado.getString(2);
                        listado_usuario = cursor_listado.getString(3);
                        listado_clave = cursor_listado.getString(4);
                        listado_url = cursor_listado.getString(5);
                        listado_pregunta_seguridad = cursor_listado.getString(6);
                        listado_respuesta_seguridad = cursor_listado.getString(7);
                        listado_notas = cursor_listado.getString(8);

                    } while (cursor_listado.moveToNext());
                }

                db.close();

                textViewID.setText(listado_id);
                if (listado_nombre == null || listado_nombre.equals("")) {
                    //Nada
                } else {
                    editTextNombre.setText(listado_nombre);
                }
                if (listado_usuario == null || listado_usuario.equals("")) {
                    //Nada
                } else {
                    editTextUsuario.setText(listado_usuario);
                }
                if (listado_clave == null || listado_clave.equals("")) {
                    //Nada
                } else {
                    editTextClave.setText(listado_clave);
                }
                if (listado_url == null || listado_url.equals("")) {
                    //Nada
                } else {
                    editTextURL.setText(listado_url);
                }
                if (listado_pregunta_seguridad == null || listado_pregunta_seguridad.equals("")) {
                    //Nada
                } else {
                    editTextPreguntaSeguridad.setText(listado_pregunta_seguridad);
                }
                if (listado_respuesta_seguridad == null || listado_respuesta_seguridad.equals("")) {
                    //Nada
                } else {
                    editTextRespuestaSeguridad.setText(listado_respuesta_seguridad);
                }
                if (listado_notas == null || listado_notas.equals("")) {
                    //Nada
                } else {
                    editTextNotas.setText(listado_notas);
                }

                try {
                    File databaseFile2 = getDatabasePath("libreta.db");
                    db = SQLiteDatabase.openOrCreateDatabase(databaseFile2, Llave, null);

                    // Ambos SQL con un campo tipo "_id"
                    Cursor cursor_categorias = db.rawQuery("SELECT id AS _id, nombre FROM categorias", null);

                    // Hago un adaptador desde cada uno de los 2 cursores definidos
                    String[] from = new String[]{"nombre"};
                    int[] to = new int[]{android.R.id.text1};
                    sca_categorias = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursor_categorias, from, to, 0);

                    // Agregamos el adaptador al Spinner
                    sca_categorias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    combo_Categoria.setAdapter(sca_categorias);

                    if (listado_categoria_id == null || listado_categoria_id.equals("")) {
                        //Nada
                    } else {
                        combo_Categoria.setSelection(getAdapterPositionById(sca_categorias, listado_categoria_id0));
                    }

                    cursor_categorias.moveToFirst();
                    cant_categorias_lista = cursor_categorias.getCount();

                    db.close();
                } finally {

                }

                if (cursor_listado != null && !cursor_listado.isClosed()) {
                    cursor_listado.close();
                }
            }
        }
        finally {

        }

        // Cuando hace click en el combo de las Categorias
        combo_Categoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            /**
             * Evento llamado al momento de seleccionar un elemento
             */
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                textViewCategoria_id.setText(String.valueOf(id));
            }

            public void onNothingSelected(AdapterView<?> parent) {
                // No hacemos nada
            }
        });

        // Boton para Guardar este registro
        buttonGuardar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (cant_categorias_lista > 0) {
                    if (accion.equals("Ver")) {
                        habilitar_edicion();
                    }
                    else {
                        guardar_registro();
                    }
                } else {
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(200);
                    Toast toast11 = Toast.makeText(getApplicationContext(), "NO has seleccionado la Categoría, por favor, hacelo antes de guardar este registro...", Toast.LENGTH_LONG);
                    toast11.show();
                }
            }
        });

        // Botón toogle para ver o no ver el dato del nombre de usuario
        toggleButtonUsuario.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    toggleButtonUsuario.setBackgroundResource(R.drawable.off);

                    editTextUsuario.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                } else {
                    toggleButtonUsuario.setBackgroundResource(R.drawable.on);

                    editTextUsuario.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        });

        // Botón toogle para ver o no ver el dato de la clave
        toggleButtonClave.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    toggleButtonClave.setBackgroundResource(R.drawable.off);

                    editTextClave.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                } else {
                    toggleButtonClave.setBackgroundResource(R.drawable.on);

                    editTextClave.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        });

        // Boton para cancelar este activity
        buttonCancelar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Al dar click se muestra el escogedor de fechas
                finish();
            }
        });
    }

    public void habilitar_edicion() {
        combo_Categoria.setEnabled(true);
        editTextNombre.setEnabled(true);
        editTextUsuario.setEnabled(true);
        editTextClave.setEnabled(true);
        editTextURL.setEnabled(true);
        editTextPreguntaSeguridad.setEnabled(true);
        editTextRespuestaSeguridad.setEnabled(true);
        editTextNotas.setEnabled(true);

        accion = "Editar";

        buttonGuardar.setText("Guardar");
        buttonGuardar.setCompoundDrawablesWithIntrinsicBounds(R.drawable.boton_guardar, 0, 0, 0);
    }

    public void guardar_registro() {
        if (accion.equals("Ver")) {
            //NO hace nada si la acción es "Ver"
        }
        else {
            // Valido todo los datos
            Boolean paso_prueba = true;

            // Obtengo algunos parametros a usar en el SQL
            if (combo_Categoria.getSelectedItem().toString().equals("")) {
                Vibrator vibrator =(Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(200);
                Toast toast1 = Toast.makeText(getApplicationContext(),"Debes seleccionar una Categoría, antes de guardar este registro...", Toast.LENGTH_LONG);
                toast1.show();

                paso_prueba = false;
            }

            if ((textViewCategoria_id.getText().toString().equals("") || textViewCategoria_id.getText().toString().equals("0")) && paso_prueba) {
                Vibrator vibrator =(Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(200);
                Toast toast1 = Toast.makeText(getApplicationContext(),"Debes seleccionar una Categoría, antes de guardar este registro...", Toast.LENGTH_LONG);
                toast1.show();

                paso_prueba = false;
            }

            if ((editTextNombre.getText().toString().equals("") || editTextNombre.getText() == null) && paso_prueba) {
                Vibrator vibrator =(Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(200);
                Toast toast1 = Toast.makeText(getApplicationContext(),"Debes ingresar una Descripción, antes de guardar este registro...", Toast.LENGTH_LONG);
                toast1.show();

                paso_prueba = false;

                editTextNombre.requestFocus();
            }

            // Limpio TODOS las variables ante de guardarlas en la BD
            String nombre_limpio = editTextNombre.getText().toString().replaceAll("[+@\"<&/>|\\¦'-+^]", "");

            // Finalmente grabo los datos, SI pasaron las validaciones
            if (paso_prueba) {
                String categoria_id = textViewCategoria_id.getText().toString();
                String nombre = nombre_limpio;
                String usuario = editTextUsuario.getText().toString();
                String clave = editTextClave.getText().toString();
                String url = editTextURL.getText().toString();
                String pregunta_seguridad = editTextPreguntaSeguridad.getText().toString();
                String respuesta_seguridad = editTextRespuestaSeguridad.getText().toString();
                String notas = editTextNotas.getText().toString();

                // Procedo a abrir la BD
                File databaseFile1 = getDatabasePath("libreta.db");
                db = SQLiteDatabase.openOrCreateDatabase(databaseFile1, Llave, null);

                Boolean guardado_exitosamente = false;

                try {
                    // Intento guardar este registro
                    db.execSQL("update libretas set id_categoria=?, nombre=?, usuario=?, clave=?, url=?, pregunta_seguridad=?, respuesta_seguridad=?, notas=? where id=?", new Object[]{categoria_id, nombre,
                            usuario, clave, url, pregunta_seguridad, respuesta_seguridad, notas, Id_registro});

                    guardado_exitosamente = true;

                    db.close();
                }
                catch (Exception e) {
                    guardado_exitosamente = false;
                }

                // Si todo salio bien
                alertDialog1 = new android.app.AlertDialog.Builder(Editar.this);
                if (guardado_exitosamente) {
                    alertDialog1.setTitle("Registro actualizado");
                    alertDialog1.setIcon(R.drawable.coninternet);
                    alertDialog1.setNeutralButton("Aceptar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            // TODO Auto-generated method stub
                            finish();
                        }
                    });
                    alertDialog1.setMessage("¡El registro fue actualizado EXITOSAMENTE en la Libreta!");
                    alertDialog1.show();
                }
                else {
                    alertDialog1.setTitle("Operación fallida");
                    alertDialog1.setIcon(R.drawable.sininternet);
                    alertDialog1.setNeutralButton("Aceptar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            // TODO Auto-generated method stub

                        }
                    });
                    alertDialog1.setMessage("El registro NO pudo ser actualizado en la Libreta.  Por favor, revisá los datos introducidos y volvé a intentarlo.");
                    alertDialog1.show();
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("Llave", Llave);
        savedInstanceState.putString("id", Id_registro);
        savedInstanceState.putString("accion", accion);

        super.onSaveInstanceState(savedInstanceState);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        Llave = savedInstanceState.getString("Llave");
        Id_registro = savedInstanceState.getString("id");
        accion = savedInstanceState.getString("accion");
    }

    static final int getAdapterPositionById(final Adapter adapter, final long id) throws NoSuchElementException {
        final int count = adapter.getCount();

        for (int pos = 0; pos < count; pos++) {
            if (id == adapter.getItemId(pos)) {
                return pos;
            }
        }

        throw new NoSuchElementException();
    }
}
