package com.secretkey;

import static com.secretkey.cifrador.Cifrador.cifrar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.secretkey.db.DbHelper;
import com.secretkey.spinner.CustomAdapter;
import com.secretkey.spinner.CustomItem;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.crypto.NoSuchPaddingException;

public class SaveActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Spinner spinner;
    private String id_usuario, selected_password, key, selected, last_login;
    private EditText password, email, servicio;
    private Button guardar;
    private ArrayList<CustomItem> customList;
    private ImageView btnclose;
    private TextView popup;
    private String redes_sociales, compras, educacion, email1, bancos, otros;
    private DbHelper dbhelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save);

        dbhelper = new DbHelper(SaveActivity.this);

        //Asignamos valores
        redes_sociales = getString(R.string.TYPE1);
        compras = getString(R.string.TYPE2);
        educacion = getString(R.string.TYPE3);
        email1 = getString(R.string.TYPE4);
        bancos = getString(R.string.TYPE5);
        otros = getString(R.string.TYPE6);
        password = findViewById(R.id.savePassword);
        spinner = findViewById(R.id.saveType);
        servicio = findViewById(R.id.saveService);
        email = findViewById(R.id.saveEmail);
        guardar = findViewById(R.id.saveGuardar);
        customList = getCustomList();

        //Inicializamos el spinner
        CustomAdapter adapter = new CustomAdapter(this, customList);
        if (spinner != null) {
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(this);
        }

        //Obtenemos datos del contexto anterior
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            id_usuario = extras.getString("id");
            selected_password = extras.getString("password");
            key = extras.getString("key");
            last_login = extras.getString("last_login");
        }

        //Si el usuario eligio la contraseña generada se muestra ya introducida
        if (selected_password.length() > 0) {
            password.setText(selected_password);
        }

        //Funcion del boton guardar
        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Comprobaciones
                if (servicio.length() > 1 && servicio.length() < 50) {
                    if (email.length() > 2 && email.length() < 50) {
                        if (password.getText().length() > 4 && password.getText().length() < 50) {
                            if (!dbhelper.serviceExists(id_usuario, servicio.getText().toString())) {

                                //Insertamos la contraseña
                                try {
                                    dbhelper.createPassword(Integer.parseInt(id_usuario),
                                            servicio.getText().toString(),
                                            cifrar(key, password.getText().toString()),
                                            cifrar(key, email.getText().toString()),
                                            selected);

                                    //Log
                                    dbhelper.insertLog(id_usuario, getText(R.string.SAVElog) + " " + servicio.getText().toString());

                                    //Inicializamos los popup
                                    LayoutInflater inflater = (LayoutInflater) SaveActivity.this
                                            .getSystemService(LAYOUT_INFLATER_SERVICE);
                                    View popupView = inflater.inflate(R.layout.popup_realizado, null);
                                    int width = ConstraintLayout.LayoutParams.MATCH_PARENT;
                                    int height = ConstraintLayout.LayoutParams.MATCH_PARENT;
                                    boolean focusable = true;
                                    final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
                                    btnclose = popupView.findViewById(R.id.popup1Cerrar);
                                    popup = popupView.findViewById(R.id.popup1Texto);
                                    btnclose.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            popupWindow.dismiss();
                                            Intent intent = new Intent(SaveActivity.this, PanelActivity.class);
                                            intent.putExtra("id", id_usuario);
                                            intent.putExtra("key", key);
                                            intent.putExtra("last_login", last_login);
                                            SaveActivity.this.startActivity(intent);
                                        }
                                    });

                                    //Mostramos popup exito
                                    popup.setText(getText(R.string.SAVEpopup));

                                    popupWindow.showAtLocation(password, Gravity.CENTER, 0, 0);
                                    popupWindow.setOutsideTouchable(true);
                                    popupWindow.setFocusable(true);
                                    popupWindow.setTouchable(true);
                                    popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                                } catch (NoSuchAlgorithmException e) {
                                    Toast.makeText(SaveActivity.this, getText(R.string.CIFRADOerror), Toast.LENGTH_SHORT).show();
                                    dbhelper.insertLog(id_usuario, getText(R.string.CIFRADOerror).toString());
                                    throw new RuntimeException(e);
                                } catch (NoSuchPaddingException e) {
                                    Toast.makeText(SaveActivity.this, getText(R.string.CIFRADOerror), Toast.LENGTH_SHORT).show();
                                    dbhelper.insertLog(id_usuario, getText(R.string.CIFRADOerror).toString());
                                    throw new RuntimeException(e);
                                }

                            } else {
                                Toast.makeText(SaveActivity.this, getText(R.string.SAVElog1), Toast.LENGTH_SHORT).show();
                                dbhelper.insertLog(id_usuario, getText(R.string.SAVElog1).toString());
                            }
                        } else {
                            Toast.makeText(SaveActivity.this, getText(R.string.SAVElog4), Toast.LENGTH_SHORT).show();
                            dbhelper.insertLog(id_usuario, getText(R.string.SAVElog4).toString());
                        }
                    } else {
                        Toast.makeText(SaveActivity.this, getText(R.string.SAVElog2), Toast.LENGTH_SHORT).show();
                        dbhelper.insertLog(id_usuario, getText(R.string.SAVElog2).toString());
                    }
                } else {
                    Toast.makeText(SaveActivity.this, getText(R.string.SAVElog3), Toast.LENGTH_SHORT).show();
                    dbhelper.insertLog(id_usuario, getText(R.string.SAVElog3).toString());
                }

            }
        });

    }

    /**
     * Metodo para obtener los componentes del spinner
     *
     * @return Lista de elementos
     */
    private ArrayList<CustomItem> getCustomList() {
        int recorrer = 0;
        String[] servicios = dbhelper.allTypeServices();

        customList = new ArrayList<>();
        while (recorrer < servicios.length) {
            if (servicios[recorrer].equals(email1)) {
                customList.add(new CustomItem(email1, R.drawable.type_email));
                recorrer++;
            } else if (servicios[recorrer].equals(bancos)) {
                customList.add(new CustomItem(bancos, R.drawable.type_bank));
                recorrer++;
            } else if (servicios[recorrer].equals(educacion)) {
                customList.add(new CustomItem(educacion, R.drawable.type_educacion));
                recorrer++;
            } else if (servicios[recorrer].equals(compras)) {
                customList.add(new CustomItem(compras, R.drawable.type_shop));
                recorrer++;
            } else if (servicios[recorrer].equals(redes_sociales)) {
                customList.add(new CustomItem(redes_sociales, R.drawable.type_social));
                recorrer++;
            } else if (servicios[recorrer].equals(otros)) {
                customList.add(new CustomItem(otros, R.drawable.type_other));
                recorrer++;
            }
        }
        return customList;
    }

    /**
     * Metodo que se ejecuta al seleccionar un tipo en el spinner
     *
     * @param adapterView The AdapterView where the selection happened
     * @param view        The view within the AdapterView that was clicked
     * @param position    The position of the view in the adapter
     * @param id          The row id of the item that is selected
     */
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        CustomItem item = (CustomItem) adapterView.getSelectedItem();
        selected = item.getSpinnerItemName();
    }


    /**
     * Metodo de las acciones a realizar en los item no seleccionados
     *
     * @param parent The AdapterView that now contains no selected item.
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }


    /**
     * Metodo que gestiona la accion si se presiona el boton de volver
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(SaveActivity.this, PanelActivity.class);
        intent.putExtra("id", id_usuario);
        intent.putExtra("key", key);
        intent.putExtra("last_login", last_login);
        SaveActivity.this.startActivity(intent);
    }


    /**
     * Metodo que gestiona la accion si la aplicacion queda en segundo plano
     */
    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
}