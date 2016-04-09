package com.yadercenteno.libretaclaves;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Vibrator;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
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

public class Agregar extends AppCompatActivity {

    private TextView textViewCategoria_id;
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
    private Context context = this;
    public Boolean autenticado;
    File databaseFile = null;
    SQLiteDatabase db;

    Integer cant_categorias_lista = 0;

    private android.app.AlertDialog.Builder alertDialog1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar);

        textViewCategoria_id = (TextView) findViewById(R.id.textViewCategoria_id);

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

        // Esto es para saber la Clave de la BD
        Bundle extras = getIntent().getExtras();
        // Obtenemos datos enviados en el intent.
        if ((extras.getString("Llave") != null) || (extras.getString("Llave") != "")) {
            Llave = extras.getString("Llave");
        }

        // Si se ha guardado la clave ANTES, se restaura
        if (savedInstanceState != null) {
            Llave = savedInstanceState.getString("Llave");
        }

        SQLiteDatabase.loadLibs(this);

        try {
            File databaseFile1 = getDatabasePath("libreta.db");
            db = SQLiteDatabase.openOrCreateDatabase(databaseFile1, Llave, null);

            // Ambos SQL con un campo tipo "_id"
            Cursor cursor_categorias = db.rawQuery("SELECT id AS _id, nombre FROM categorias", null);

            // Hago un adaptador desde cada uno de los 2 cursores definidos
            String[] from = new String[] {"nombre"};
            int[] to = new int[] {android.R.id.text1};
            sca_categorias = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursor_categorias, from, to, 0);

            // Agregamos el adaptador al Spinner
            sca_categorias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            combo_Categoria.setAdapter(sca_categorias);

            cursor_categorias.moveToFirst();
            cant_categorias_lista = cursor_categorias.getCount();

            db.close();
        }
        finally {
            // No hacer nada
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
                    guardar_registro();
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

    public void guardar_registro() {
        // Valido todo los datos
        Boolean paso_prueba = true;

        // Obtengo algunos parametros a usar en el SQL
        if (combo_Categoria.getSelectedItem().toString().equals("") && paso_prueba) {
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

            Boolean agregado_exitosamente = false;

            try {
                // Intento guardar este registro
                db.execSQL("insert into libretas(id_categoria, nombre, usuario, clave, url, pregunta_seguridad, respuesta_seguridad, notas) values(?, ?, ?, ?, ?, ?, ?, ?)", new Object[]{categoria_id, nombre,
                        usuario, clave, url, pregunta_seguridad, respuesta_seguridad, notas});

                agregado_exitosamente = true;

                db.close();
            } catch (SQLException e) {
                agregado_exitosamente = false;
            }

            // Si todo salio bien
            alertDialog1 = new android.app.AlertDialog.Builder(Agregar.this);
            if (agregado_exitosamente) {
                alertDialog1.setTitle("Registro agregado");
                alertDialog1.setIcon(R.drawable.coninternet);
                alertDialog1.setNeutralButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        // TODO Auto-generated method stub
                        finish();
                    }
                });
                alertDialog1.setMessage("¡El registro fue agregado EXITOSAMENTE a la Libreta!");
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
                alertDialog1.setMessage("El registro NO pudo ser agregado a la Libreta.  Por favor, revisá los datos introducidos y volvé a intentarlo.");
                alertDialog1.show();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("Llave", Llave);

        super.onSaveInstanceState(savedInstanceState);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        Llave = savedInstanceState.getString("Llave");
    }
}
