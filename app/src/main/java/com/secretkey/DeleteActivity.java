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
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.secretkey.db.DbHelper;
import com.secretkey.spinner.CustomAdapter;
import com.secretkey.spinner.CustomItem;

import java.util.ArrayList;
import java.util.Arrays;

public class DeleteActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private String id_usuario, key, selected, last_login;
    private ArrayList<CustomItem> customList;
    private DbHelper dbhelper;
    private Spinner spinner;
    private Button execute;
    private ImageView btnclose;
    private TextView popup;

    /**
     * Metodo que se ejecuta al crear la actividad
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete);

        dbhelper = new DbHelper(DeleteActivity.this);

        //Obtenemos los datos del contexto anterior
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            id_usuario = extras.getString("id");
            key = extras.getString("key");
            last_login = extras.getString("last_login");
        }

        //Inicializacion de componentes
        spinner = findViewById(R.id.deleteType);
        customList = getCustomList();
        CustomAdapter adapter = new CustomAdapter(this, customList);
        if (spinner != null) {
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(this);
        }

        //Accion del boton eliminar
        execute = findViewById(R.id.deleteExecute);
        execute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Elimina el servicio en la base de datos
                dbhelper.deleteRowPassword(id_usuario, selected);

                //Log
                dbhelper.insertLog(id_usuario, getText(R.string.DELETElog) + " " + selected);

                //Iniciamos el popup
                LayoutInflater inflater = (LayoutInflater) DeleteActivity.this
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
                        Intent intent = new Intent(DeleteActivity.this, PanelActivity.class);
                        intent.putExtra("id", id_usuario);
                        intent.putExtra("last_login", last_login);
                        intent.putExtra("key", key);
                        DeleteActivity.this.startActivity(intent);
                        finish();
                    }
                });

                //Mostramos popup
                popup.setText(getText(R.string.DELETEpopup));
                popupWindow.showAtLocation(execute, Gravity.CENTER, 0, 0);
                popupWindow.setOutsideTouchable(true);
                popupWindow.setFocusable(true);
                popupWindow.setTouchable(true);
                popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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
        Intent intent = new Intent(DeleteActivity.this, PanelActivity.class);
        intent.putExtra("id", id_usuario);
        intent.putExtra("last_login", last_login);
        intent.putExtra("key", key);
        DeleteActivity.this.startActivity(intent);
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