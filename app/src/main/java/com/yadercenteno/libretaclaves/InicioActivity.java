package com.yadercenteno.libretaclaves;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ImageSpan;
import android.view.ContextMenu;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import static com.yadercenteno.libretaclaves.Constantes.COLUMNA_ID;
import static com.yadercenteno.libretaclaves.Constantes.COLUMNA_NOMBRE;

public class InicioActivity extends AppCompatActivity {

    public String Llave = "";
    private Context context = this;
    File databaseFile = null;
    public Boolean nueva_BD;
    public Boolean autenticado;
    private ListView lview;
    private EditText EditTextBuscar;
    TabLayout tabLayout;
    Integer tab_actual = 0;
    public Boolean tabs_ya_creados;
    public Boolean clave_visible;

    private android.app.AlertDialog.Builder alertDialog1;

    // Listado donde guardaré los datos a mostrar en en listado
    private ArrayList<HashMap<String,String>> list;

    // Dialogo Acerca de...
    android.app.AlertDialog dialogoPersonalizado;
    android.app.AlertDialog.Builder constructor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_inicio);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Nada de momento
                Intent i = new Intent(InicioActivity.this, Agregar.class);
                i.putExtra("Llave", Llave);

                startActivity(i);
            }
        });

        EditTextBuscar = (EditText) findViewById(R.id.EditTextBuscar);
        tabLayout = (TabLayout) findViewById(R.id.tabs);

        // Inicializo algunas variables de validación
        autenticado = false;
        tabs_ya_creados = false;
        clave_visible = false;

        // Caragamos las librerías del SQLcipher
        SQLiteDatabase.loadLibs(this);

        // Aquí creo el elemento del listado
        lview = (ListView) findViewById(R.id.listview);

        // Aqui detectamos cualquier cambio en la búsqueda
        EditTextBuscar.addTextChangedListener(BuscarTextWatcher);

        if (savedInstanceState != null) {
            Llave = savedInstanceState.getString("Llave");
        }

        // Método del click para el listado
        lview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // Saco el ID de la operación
                String id_registro0 = list.get(arg2).get(COLUMNA_ID);

                if (id_registro0 != "" || id_registro0 != "0") {
                    Intent i = new Intent(InicioActivity.this, Editar.class);
                    i.putExtra("id", id_registro0);
                    i.putExtra("Llave", Llave);
                    i.putExtra("accion", "Ver");

                    startActivity(i);
                }
            }
        });

        obtenerClave();
    }

    private void obtenerClave() {
        // Para crear un Layout y meter los 2 EditText
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        final AlertDialog.Builder inputAlert = new AlertDialog.Builder(context);

        // Creo los 2 EditText, 1 para la Clave y la otra para confirmar (solo la primera vez)
        final EditText userInput = new EditText(context);
        final EditText userInput1 = new EditText(context);

        String texto_boton_OK = "";

        // Por seguridad, limpio la clave
        autenticado = false;
        Llave = "";

        databaseFile = getDatabasePath("libreta.db");

        // Verificar si la BD ya existe o NO
        if (databaseFile.exists()) {
            // La BD YA Existe
            databaseFile.mkdirs();

            nueva_BD = false;

            inputAlert.setTitle("Validacion de permiso");
            inputAlert.setMessage("Ingresá tu clave de acceso, por favor:");
            texto_boton_OK = "Verificar";
        } else {
            // La BD NO Existe y hay que crearla nuevamente
            databaseFile.mkdirs();
            databaseFile.delete();

            nueva_BD = true;

            inputAlert.setTitle("Creación de Nueva Base de Datos");
            inputAlert.setMessage("Por favor, Ingresá una clave de acceso y recordala muy bien, porque NO habrá forma de recuperar esta clave si se te olvida:");
            texto_boton_OK = "Crear";
        }

        // Configuraciones del 1er. EditText (ingresar la clave)
        userInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        userInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
        userInput.setMinEms(6);

        if (nueva_BD) {
            // Si es la primera vez, entonces muestra la segunda EditText para confirmar
            userInput.setHint("Ingresá tu clave");
            userInput1.setHint("Ingresá tu clave de nuevo");

            userInput1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            userInput1.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }

        layout.addView(userInput);

        userInput.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (clave_visible) {
                    userInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                    if (nueva_BD) {
                        userInput1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    }

                    clave_visible = false;
                } else {
                    userInput.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);

                    if (nueva_BD) {
                        userInput1.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    }

                    clave_visible = true;
                }

                return false;
            }
        });

        if (nueva_BD) {
            layout.addView(userInput1);
        }

        inputAlert.setCancelable(false);
        inputAlert.setView(layout);
        inputAlert.setPositiveButton(texto_boton_OK, null);
        inputAlert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                autenticado = false;
                dialog.dismiss();

                salir_sistema();
            }
        });

        final AlertDialog alertDialog = inputAlert.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button ok = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

                if (ok != null) {
                    ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (nueva_BD) {
                                if (!userInput.getText().toString().equals(userInput1.getText().toString())) {
                                    userInput.setError("Las 2 claves introducidas NO coinciden");
                                } else if (userInput.getText().toString().length() <= 6) {
                                    userInput.setError("Las clave NO puede tener menos de 6 caracteres");
                                } else if (userInput.getText().toString().isEmpty()) {
                                    userInput.setError("Las clave NO puede quedar vacia");
                                }
                                else {
                                    IniciarSQLCipher(userInput.getText().toString());
                                    alertDialog.cancel();
                                }
                            } else {
                                if (userInput.getText().toString().length() <= 6) {
                                    userInput.setError("Las clave NO puede tener menos de 6 caracteres");
                                } else if (userInput.getText().toString().isEmpty()) {
                                    userInput.setError("Las clave NO puede quedar vacia");
                                }
                                else {
                                    IniciarSQLCipher(userInput.getText().toString());
                                    alertDialog.cancel();
                                }
                            }
                        }
                    });
                }
            }
        });

        alertDialog.show();
    }

    private void IniciarSQLCipher(String clave) {
        if (clave.equals("")) {
            //La BD NO puede tener la clave en Blanco
            // Vuelve a pedir la clave
            autenticado = false;

            obtenerClave();
        }
        else {
            try {
                Llave = clave;

                SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(databaseFile, Llave, null);

                // Creamos las tablas
                database.execSQL("CREATE TABLE IF NOT EXISTS categorias(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "nombre TEXT)");
                database.execSQL("CREATE TABLE IF NOT EXISTS libretas(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "id_categoria INTEGER not null, " +
                        "nombre TEXT not null, " +
                        "usuario TEXT null, " +
                        "clave TEXT null, " +
                        "url TEXT null, " +
                        "pregunta_seguridad TEXT null, " +
                        "respuesta_seguridad TEXT null, " +
                        "notas TEXT null" +
                        ")");

                if (nueva_BD) {
                    // Se hace esto por PRIMERA VEZ, solamente la una vez
                    database.execSQL("insert into categorias(id, nombre) values(?, ?)", new Object[]{"1",
                            "Personal"});
                    database.execSQL("insert into categorias(id, nombre) values(?, ?)", new Object[]{"2",
                            "Laboral"});
                    database.execSQL("insert into categorias(id, nombre) values(?, ?)", new Object[]{"3",
                            "Otros"});

                    CrearTabs();
                }
                else {
                    CrearTabs();
                }

                autenticado = true;

                database.close();

                showAlertDialog(InicioActivity.this, "Autenticación exitosa",
                        "¡Ahora si podés usar la Libreta!", true);
            } catch (Exception e) {
                autenticado = false;

                showAlertDialog(InicioActivity.this, "Problemas de autenticación",
                        "La Clave de la Libreta es INCORRECTA.  Por favor, volvé a ingresar tu clave otra vez!", false);
            }
        }
    }

    // Solo sirve para mostrar el Dialogo de que dice que NO hay internet
    public void showAlertDialog(Context context, String title, String message, Boolean status) {
        android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting alert dialog icon
        alertDialog.setIcon((status) ? R.drawable.coninternet : R.drawable.sininternet);

        // Setting OK Button
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Aceptar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //Nos salimos de la aplicación
                if (autenticado) {
                    dialog.cancel();
                    mostrar_listado(1);
                }
                else {
                    dialog.cancel();
                    obtenerClave();
                }
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    private void mostrar_listado(Integer categoria_id) {
        if (categoria_id > 0) {
            list = new ArrayList<HashMap<String, String>>();

            File databaseFile1 = getDatabasePath("libreta.db");
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(databaseFile1, Llave, null);

            android.database.Cursor cursor_listado = db.rawQuery("SELECT id,nombre FROM libretas WHERE id_categoria=" + categoria_id + " ORDER BY id", null);

            if (cursor_listado != null) {
                if (cursor_listado.moveToFirst()) {
                    list.clear();

                    do {
                        int listado_id0 = cursor_listado.getInt(0);
                        String listado_id = Integer.toString(listado_id0);
                        String listado_nombre = cursor_listado.getString(1);

                        HashMap<String, String> temp = new HashMap<String, String>();
                        temp.put(COLUMNA_ID, listado_id);
                        temp.put(COLUMNA_NOMBRE, listado_nombre);

                        list.add(temp);

                    } while (cursor_listado.moveToNext());
                }

                if (cursor_listado != null && !cursor_listado.isClosed()) {
                    cursor_listado.close();
                }
            }

            // Cargo el listado en el Spinner
            ListadoAdaptador adapter = new ListadoAdaptador(InicioActivity.this, list);
            lview.setAdapter(adapter);

            db.close();

            adapter.notifyDataSetChanged();

            //Registro el menú contextual
            registerForContextMenu(lview);
        }
    }

    private void buscar_listado(Integer categoria_id, String query) {
        if (categoria_id > 0 && query != "") {
            list = new ArrayList<HashMap<String, String>>();

            File databaseFile1 = getDatabasePath("libreta.db");
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(databaseFile1, Llave, null);

            android.database.Cursor cursor_listado = db.rawQuery("SELECT id,nombre,usuario FROM libretas WHERE (nombre LIKE '%"+query+"%' OR usuario LIKE '%"+query+"%') AND id_categoria=" + categoria_id + " ORDER BY id", null);

            if (cursor_listado != null) {
                if (cursor_listado.moveToFirst()) {
                    list.clear();

                    do {
                        int listado_id0 = cursor_listado.getInt(0);
                        String listado_id = Integer.toString(listado_id0);
                        String listado_nombre = cursor_listado.getString(1);

                        HashMap<String, String> temp = new HashMap<String, String>();
                        temp.put(COLUMNA_ID, listado_id);
                        temp.put(COLUMNA_NOMBRE, listado_nombre);

                        list.add(temp);

                    } while (cursor_listado.moveToNext());
                }

                if (cursor_listado != null && !cursor_listado.isClosed()) {
                    cursor_listado.close();
                }
            }

            // Cargo el listado en el Spinner
            ListadoAdaptador adapter = new ListadoAdaptador(InicioActivity.this, list);
            lview.setAdapter(adapter);

            db.close();

            adapter.notifyDataSetChanged();

            //Registro el menú contextual
            registerForContextMenu(lview);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Escogé una acción");
        menu.add(0, v.getId(), 1, "Ver detalles");
        menu.add(0, v.getId(), 2, "Editar registro");
        menu.add(0, v.getId(), 3, "Eliminar registro");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // Obtengo la posición del elemento en el listado
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

        // Saco el ID de la operación
        String id_registro = list.get(info.position).get(COLUMNA_ID);

        if (id_registro != "" ||  id_registro != "0") {
            try {
                if (item.getTitle()=="Ver detalles") {
                    final String id_con = id_registro;

                    Intent i=new Intent(InicioActivity.this, Editar.class);
                    i.putExtra("id",id_con);
                    i.putExtra("Llave",Llave);
                    i.putExtra("accion","Ver");

                    startActivity(i);
                }

                if (item.getTitle()=="Editar registro") {
                    final String id_con1 = id_registro;

                    Intent i1 = new Intent(InicioActivity.this, Editar.class);
                    i1.putExtra("id",id_con1);
                    i1.putExtra("Llave",Llave);
                    i1.putExtra("accion","Editar");

                    startActivity(i1);
                }

                if (item.getTitle()=="Eliminar registro") {
                    final String id_con2 = id_registro;

                    // Hago el llamado asincrónico para subir el gasto
                    android.app.AlertDialog.Builder adb=new android.app.AlertDialog.Builder(InicioActivity.this);
                    adb.setTitle("Eliminar Registro?");
                    adb.setMessage("¿Estas seguro que querés eliminar el registro con ID # "+id_con2+"?");

                    adb.setNegativeButton("No", null);
                    adb.setPositiveButton("Si", new android.app.AlertDialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            alertDialog1 = new android.app.AlertDialog.Builder(InicioActivity.this);

                            File databaseFile1 = getDatabasePath("libreta.db");
                            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(databaseFile1, Llave, null);

                            db.delete("libretas", "id=" + id_con2, null);

                            String que_buscar = "";

                            if (EditTextBuscar.getText().toString().equals("")) {
                                que_buscar = "";
                                mostrar_listado(tab_actual);
                            }
                            else {
                                que_buscar = EditTextBuscar.getText().toString().replaceAll("[+\"<$/>|\\¦'+^:]", "");

                                buscar_listado(tab_actual, que_buscar);
                            }

                            // Separo los mensajes de error recibidos
                            alertDialog1.setCancelable(false);
                            alertDialog1.setTitle("Registro eliminado");
                            alertDialog1.setIcon(R.drawable.coninternet);
                            alertDialog1.setMessage("¡El registro fue eliminado correctamente!");
                            alertDialog1.setNeutralButton("Aceptar", null);
                        }
                    });
                    adb.show();
                }

                return true;
            }
            catch(Exception e) {
                return true;
            }
        }
        else {
            return false;
        }
    }

    protected TextWatcher BuscarTextWatcher = new TextWatcher() {

        @Override
        public void afterTextChanged(Editable s) {

            // Valores que obtendre de las cajas de texto
            String que_buscar = "";

            // Obtengo los valores actuales de las cajas de texto
            if (EditTextBuscar.getText().toString().equals("")) {
                que_buscar = "";
                mostrar_listado(tab_actual);
            }
            else {
                que_buscar = EditTextBuscar.getText().toString().replaceAll("[+\"<$/>|\\¦'+^:]", "");

                buscar_listado(tab_actual, que_buscar);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Nada
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Nada
        }
    };

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("Llave", Llave);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Si hay que buscar, busco, si no solo muestro la búsqueda por ciudad
        String que_buscar = "";

        // Obtengo los valores actuales de las cajas de texto
        if (EditTextBuscar.getText().toString().equals("")) {
            if (tab_actual==null || tab_actual < 0) {
                mostrar_listado(0);
            }
            else {
                mostrar_listado(tab_actual);
            }

        }
        else {
            que_buscar = EditTextBuscar.getText().toString().replaceAll("[+\"<$/>|\\¦'+^:]", "");

            buscar_listado(tab_actual, que_buscar);
        }
    }

    private void CrearTabs() {
        if (!tabs_ya_creados) {
            try {
                File databaseFile1 = getDatabasePath("libreta.db");
                SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(databaseFile1, Llave, null);

                Cursor c = db.rawQuery("SELECT id,nombre FROM categorias ORDER BY id", null);
                Integer primero = 0;

                if (c.moveToFirst()) {
                    do {
                        if (primero == 0) {
                            tabLayout.addTab(tabLayout.newTab().setText(c.getString(c.getColumnIndex("nombre"))), true);
                        } else {
                            tabLayout.addTab(tabLayout.newTab().setText(c.getString(c.getColumnIndex("nombre"))));
                        }

                        primero = primero + 1;
                    } while (c.moveToNext());
                }

                tabs_ya_creados = true;

                tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        tab_actual = tab.getPosition() + 1;
                        mostrar_listado(tab_actual);
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {

                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {

                    }
                });

                tab_actual = tabLayout.getSelectedTabPosition() + 1;

                c.close();
                db.close();
            }
            catch (Exception e) {
            }
        }
    }

    private void LlamadaCambiarClave() {
        if (autenticado) {
            // Para crear un Layout y meter los 2 EditText
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);

            final AlertDialog.Builder inputAlert = new AlertDialog.Builder(context);

            // Creo los 2 EditText, 1 para la Clave y la otra para confirmar
            final EditText userInput = new EditText(context);
            final EditText userInput1 = new EditText(context);

            String texto_boton_OK = "Cambiar";
            inputAlert.setTitle("Cambiar la Clave");
            inputAlert.setMessage("Por favor, ingresa una nueva clave y recordala muy bien, porque NO habrá forma de recuperar esta clave si se te olvida:");

            // Configuraciones del 1er. EditText (ingresar la clave)
            userInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            userInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
            userInput.setMinEms(6);
            layout.addView(userInput);

            //Muestra la segunda EditText para confirmar
            userInput.setHint("Ingresá tu nueva clave");
            userInput1.setHint("Ingresá tu nueva clave otra vez");

            userInput1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            userInput1.setTransformationMethod(PasswordTransformationMethod.getInstance());
            layout.addView(userInput1);

            inputAlert.setCancelable(false);
            inputAlert.setView(layout);
            inputAlert.setPositiveButton(texto_boton_OK, null);
            inputAlert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            final AlertDialog alertDialog = inputAlert.create();

            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Button ok = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

                    if (ok != null) {
                        ok.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (!userInput.getText().toString().equals(userInput1.getText().toString())) {
                                    userInput.setError("Las 2 claves introducidas NO coinciden");
                                } else if (userInput.getText().toString().length() <= 6) {
                                    userInput.setError("Las clave NO puede tener menos de 6 caracteres");
                                } else if (userInput.getText().toString().isEmpty()) {
                                    userInput.setError("Las clave NO puede quedar vacia");
                                }
                                else {
                                    CambiarClave(userInput.getText().toString());
                                    alertDialog.cancel();
                                }
                            }
                        });
                    }
                }
            });

            alertDialog.show();
        }
        else {
            autenticado = false;

            showAlertDialog(InicioActivity.this, "Problemas de autenticación",
                    "La Clave de la Libreta es INCORRECTA.  Por favor, volvé a ingresar tu clave otra vez!", false);
        }
    }

    private void CambiarClave(String nueva_clave) {
        try {
            // Obtengo la BD
            File mi_db = getDatabasePath("libreta.db");
            SQLiteDatabase myDb = SQLiteDatabase.openOrCreateDatabase(mi_db, Llave, null);

            myDb.rawExecSQL("PRAGMA key = '" + Llave + "';");
            myDb.rawExecSQL("PRAGMA rekey = '" + nueva_clave + "';");

            myDb.close();

            Llave = nueva_clave;

            showAlertDialog(InicioActivity.this, "Operación exitosa",
                    "¡La clave de la BD se ha cambiado exitosamente!", true);
        }
        catch (Exception e) {
            autenticado = false;

            showAlertDialog(InicioActivity.this, "Problemas de autenticación",
                    "La Clave de la Libreta es INCORRECTA.  Por favor, volvé a ingresar tu clave otra vez!", false);
        }

    }


    private void ExportarBD(Boolean encriptada) {
        try {
            // Obtengo la BD
            File mi_db = getDatabasePath("libreta.db");
            SQLiteDatabase myDb = SQLiteDatabase.openOrCreateDatabase(mi_db, Llave, null);

            // Exporto la BD SIN encriptar
            if (!encriptada) {
                // Reviso si existe o no la nueva DB, si existe la borra, primero
                File mi_nueva_db = new File(Environment.getExternalStorageDirectory()
                        + File.separator + "libretasinencriptar.db");

                if (mi_nueva_db.isFile()) {
                    mi_nueva_db.delete();
                }

                myDb.rawExecSQL("PRAGMA key = '" + Llave + "';");
                myDb.rawExecSQL("ATTACH DATABASE '" + Environment.getExternalStorageDirectory()
                        + File.separator + "libretasinencriptar.db" + "' AS plaintext KEY '';");
                myDb.rawExecSQL("SELECT sqlcipher_export('plaintext');");
                myDb.rawExecSQL("DETACH DATABASE plaintext;");
            } else {
                // Reviso si existe o no la nueva DB, si existe la borra, primero
                File mi_nueva_db1 = new File(Environment.getExternalStorageDirectory()
                        + File.separator + "libreta.db");

                if (mi_nueva_db1.isFile()) {
                    mi_nueva_db1.delete();
                }

                // Exporta la BD encriptada
                String sql = String.format("ATTACH DATABASE '%s' AS encrypted KEY '%s'", Environment.getExternalStorageDirectory()
                        + File.separator + "libreta.db", Llave);
                myDb.rawExecSQL(sql);
                myDb.rawExecSQL("SELECT sqlcipher_export('encrypted')");
                myDb.rawExecSQL("DETACH DATABASE encrypted");
            }

            myDb.close();

            showAlertDialog(InicioActivity.this, "Operación exitosa",
                    "¡La BD se ha Exportado exitosamente!", true);
        }
        catch (Exception e) {
            autenticado = false;

            showAlertDialog(InicioActivity.this, "Problemas de autenticación",
                    "La Clave de la Libreta es INCORRECTA.  Por favor, volvé a ingresar tu clave otra vez!", false);
        }

    }

    private void ImportarBD() {
        try {
            File currentDB = getDatabasePath("libreta.db");
            File backupDB = new File(Environment.getExternalStorageDirectory()
                    +File.separator+"libreta.db");

            if (currentDB.exists()) {
                FileChannel src = new FileInputStream(backupDB).getChannel();
                FileChannel dst = new FileOutputStream(currentDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();

                showAlertDialog(InicioActivity.this, "Operación exitosa",
                        "¡La BD ha sido Importada correctamente!", true);

                obtenerClave();
            }
        }
        catch (Exception e) {
            autenticado = false;

            showAlertDialog(InicioActivity.this, "Problemas en la operación",
                    "Hubo problemas al importar la BD a la Aplicación.  Por favor, salí de la aplicación e intentalo de nuevo!", false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_inicio, menu);
        super.onCreateOptionsMenu(menu);

        final MenuItem salir = menu.findItem(R.id.action_salir);
        final ImageSpan imageSpan2=new ImageSpan(this,R.drawable.boton_salir);
        final CharSequence title2="  "+salir.getTitle();
        final SpannableString spannableString2 =new SpannableString(title2);
        spannableString2.setSpan(imageSpan2, 0, 1, 0);
        salir.setTitle(spannableString2);

        final MenuItem acerca_de = menu.findItem(R.id.action_acerca_de);
        final ImageSpan imageSpan22=new ImageSpan(this,R.drawable.boton_acerca_de);
        final CharSequence title22="  "+acerca_de.getTitle();
        final SpannableString spannableString22 =new SpannableString(title22);
        spannableString22.setSpan(imageSpan22, 0, 1, 0);
        acerca_de.setTitle(spannableString22);
        
        final MenuItem action_cambiarclave = menu.findItem(R.id.action_cambiarclave);
        final ImageSpan imageSpan=new ImageSpan(this,R.drawable.cambiar_clave1);
        final CharSequence title="  "+action_cambiarclave.getTitle();
        final SpannableString spannableString=new SpannableString(title);
        spannableString.setSpan(imageSpan, 0, 1, 0);
        action_cambiarclave.setTitle(spannableString);

        final MenuItem exportar = menu.findItem(R.id.action_exportar);
        final ImageSpan imageSpan1=new ImageSpan(this,R.drawable.exportar_bd_encriptada);
        final CharSequence title1="  "+exportar.getTitle();
        final SpannableString spannableString1 =new SpannableString(title1);
        spannableString1.setSpan(imageSpan1,0,1,0);
        exportar.setTitle(spannableString1);

        final MenuItem exportar1 = menu.findItem(R.id.action_exportar1);
        final ImageSpan imageSpan3=new ImageSpan(this,R.drawable.exportar_bd_sinencriptar);
        final CharSequence title3="  "+exportar1.getTitle();
        final SpannableString spannableString3 =new SpannableString(title3);
        spannableString3.setSpan(imageSpan3,0,1,0);
        exportar1.setTitle(spannableString3);

        final MenuItem importar = menu.findItem(R.id.action_importar);
        final ImageSpan imageSpan4=new ImageSpan(this,R.drawable.importar1);
        final CharSequence title4="  "+importar.getTitle();
        final SpannableString spannableString4 =new SpannableString(title4);
        spannableString4.setSpan(imageSpan4,0,1,0);
        importar.setTitle(spannableString4);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Cambiar la clave a la BD
        if (id == R.id.action_cambiarclave) {
            LlamadaCambiarClave();

            return true;
        }

        // Exportar la BD (encriptada)
        if (id == R.id.action_exportar) {
            ExportarBD(true);

            return true;
        }

        // Exportar la BD (SIN encriptar)
        if (id == R.id.action_exportar1) {
            ExportarBD(false);

            return true;
        }

        // Importar la BD (encriptada)
        if (id == R.id.action_importar) {
            ImportarBD();

            return true;
        }

        // Acerca de...
        if (id == R.id.action_acerca_de) {
            ventanita_acerca_de();

            return true;
        }

        // Salir del sistema
        if (id == R.id.action_salir) {
            salir_sistema();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void salir_sistema() {
        //Estos nos SACA del sistema, cierra la App
        Intent intent = new Intent(Intent.ACTION_MAIN);
        finish();
        System.exit(0);
    }

    private void ventanita_acerca_de() {
        // Se "infla" la vista desde el elemento raíz del layout
        View layoutDialogo =
                getLayoutInflater().inflate(R.layout.acerca_de,
                        (ViewGroup) findViewById(R.id.dialogo_acerca_de));

        // Se instancia el constructor, pasándole el contexto
        constructor = new android.app.AlertDialog.Builder(InicioActivity.this);
        // Se asocia la vista "inflada" desde el XML
        constructor.setView(layoutDialogo);

        TextView textProgramador1 = (TextView) layoutDialogo.findViewById(R.id.textProgramador1);
        textProgramador1.setMovementMethod(LinkMovementMethod.getInstance());

        // Se añade el botón "Aceptar" solo para cerrar el dialogo
        constructor.setNeutralButton("ACEPTAR", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // El listener cerrará el diálogo
                dialogoPersonalizado.dismiss();
            }
        });

        // Se crea el diálogo a través del constructor
        dialogoPersonalizado = constructor.create();
        dialogoPersonalizado.show();
    }
}
