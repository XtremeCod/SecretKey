package com.secretkey;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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

import com.secretkey.cifrador.Cifrador;
import com.secretkey.db.DbHelper;
import com.secretkey.spinner.CustomAdapter;
import com.secretkey.spinner.CustomItem;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class ModifyActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private String id_usuario, key, selected, last_login;
    private ArrayList<CustomItem> customList;
    private DbHelper dbhelper;
    private Spinner spinner;
    private Button modificar;
    private EditText email, password;
    private ImageView btnclose;
    private TextView popup;

    /**
     * Metodo que se ejecuta al crear la vista
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify);

        dbhelper = new DbHelper(ModifyActivity.this);

        //Obtenemos los datos del contexto anterior
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            id_usuario = extras.getString("id");
            key = extras.getString("key");
            last_login = extras.getString("last_login");
        }

        //Identificacion de componentes
        spinner = findViewById(R.id.modifyType);
        customList = getCustomList();
        CustomAdapter adapter = new CustomAdapter(this, customList);
        if (spinner != null) {
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(this);
        }

        //Accion del boton modificar
        modificar = findViewById(R.id.modifyExecute);
        modificar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Inicializacion del popup
                LayoutInflater inflater = (LayoutInflater) ModifyActivity.this
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
                        Intent intent = new Intent(ModifyActivity.this, PanelActivity.class);
                        intent.putExtra("id", id_usuario);
                        intent.putExtra("last_login", last_login);
                        intent.putExtra("key", key);
                        ModifyActivity.this.startActivity(intent);
                        finish();
                    }
                });

                //Comprobaciones
                if (email.length() > 2 && email.length() < 50) {
                    if (password.length() > 4 && password.length() < 50) {

                        //Modificamos datos en la base de datos
                        try {
                            dbhelper.updateRowPassword(selected, id_usuario,
                                    Cifrador.cifrar(key, email.getText().toString()),
                                    Cifrador.cifrar(key, password.getText().toString()));

                            //Mostramos popup
                            popup.setText(getText(R.string.MODIFYpopup));
                            popupWindow.showAtLocation(password, Gravity.CENTER, 0, 0);
                            popupWindow.setOutsideTouchable(true);
                            popupWindow.setFocusable(true);
                            popupWindow.setTouchable(true);
                            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                            //Log
                            dbhelper.insertLog(id_usuario, getText(R.string.MODIFYlog) + " " + selected);

                        } catch (NoSuchAlgorithmException e) {
                            Toast.makeText(ModifyActivity.this, getText(R.string.CIFRADOerror), Toast.LENGTH_SHORT).show();
                            dbhelper.insertLog(id_usuario, getText(R.string.CIFRADOerror).toString());
                            throw new RuntimeException(e);
                        } catch (NoSuchPaddingException e) {
                            Toast.makeText(ModifyActivity.this, getText(R.string.CIFRADOerror), Toast.LENGTH_SHORT).show();
                            dbhelper.insertLog(id_usuario, getText(R.string.CIFRADOerror).toString());
                            throw new RuntimeException(e);
                        }
                    } else {
                        Toast.makeText(ModifyActivity.this, getText(R.string.MODIFYlog1), Toast.LENGTH_SHORT).show();
                        dbhelper.insertLog(id_usuario, getText(R.string.MODIFYlog1).toString());
                    }
                } else {
                    Toast.makeText(ModifyActivity.this, getText(R.string.MODIFYlog2), Toast.LENGTH_SHORT).show();
                    dbhelper.insertLog(id_usuario, getText(R.string.MODIFYlog2).toString());
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
        String[] servicios = dbhelper.allServices(id_usuario);
        Arrays.sort(servicios);

        customList = new ArrayList<>();
        while (recorrer < servicios.length) {
            customList.add(new CustomItem(servicios[recorrer], 0));
            recorrer++;
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

        //Obtenemos cada uno de los datos segun el servicio seleccionado
        CustomItem item = (CustomItem) adapterView.getSelectedItem();
        selected = item.getSpinnerItemName();

        //Obtenemos los datos seleccionados
        try {
            String[] datos = dbhelper.selectedService(id_usuario, selected);

            email = findViewById(R.id.modifyEmail);
            password = findViewById(R.id.modifyPassword);

            //Mostramos los datos
            email.setText(Cifrador.descifrar(key, datos[0]));
            password.setText(Cifrador.descifrar(key, datos[1]));

        } catch (NoSuchPaddingException e) {
            Toast.makeText(ModifyActivity.this, getText(R.string.CIFRADOerror), Toast.LENGTH_SHORT).show();
            dbhelper.insertLog(id_usuario, getText(R.string.CIFRADOerror).toString());
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            Toast.makeText(ModifyActivity.this, getText(R.string.CIFRADOerror), Toast.LENGTH_SHORT).show();
            dbhelper.insertLog(id_usuario, getText(R.string.CIFRADOerror).toString());
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            Toast.makeText(ModifyActivity.this, getText(R.string.CIFRADOerror), Toast.LENGTH_SHORT).show();
            dbhelper.insertLog(id_usuario, getText(R.string.CIFRADOerror).toString());
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException e) {
            Toast.makeText(ModifyActivity.this, getText(R.string.CIFRADOerror), Toast.LENGTH_SHORT).show();
            dbhelper.insertLog(id_usuario, getText(R.string.CIFRADOerror).toString());
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            Toast.makeText(ModifyActivity.this, getText(R.string.CIFRADOerror), Toast.LENGTH_SHORT).show();
            dbhelper.insertLog(id_usuario, getText(R.string.CIFRADOerror).toString());
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            Toast.makeText(ModifyActivity.this, getText(R.string.CIFRADOerror), Toast.LENGTH_SHORT).show();
            dbhelper.insertLog(id_usuario, getText(R.string.CIFRADOerror).toString());
            throw new RuntimeException(e);
        }
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
        Intent intent = new Intent(ModifyActivity.this, PanelActivity.class);
        intent.putExtra("id", id_usuario);
        intent.putExtra("last_login", last_login);
        intent.putExtra("key", key);
        ModifyActivity.this.startActivity(intent);
        finish();
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