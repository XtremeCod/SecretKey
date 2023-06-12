package com.secretkey;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.secretkey.db.DbHelper;

public class PanelActivity extends AppCompatActivity {

    private Button changeMK,generate,search,modify,delete,backup;
    private String key,id_usuario, last_login;

    private Boolean first_login = false;


    /**
     * Método que se ejecuta al cargar la vista
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel);

        DbHelper dbhelper = new DbHelper(PanelActivity.this);

        //Obtenemos datos pasados por contexto
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            id_usuario = extras.getString("id");
            last_login = extras.getString("last_login");
            key = extras.getString("key");
            first_login = extras.getBoolean("first_login");
        }


        //Muestra y actualizacion de ultimo acceso
        TextView acceso = findViewById(R.id.infoUltimoAcceso);
        acceso.setText(getText(R.string.PANELlastlogin) + " " + last_login);

        //La primera vez que se accede al panel desde login o register se actualiza la ultima fecha de acceso
        if(first_login) {
            dbhelper.updateLastLogin(id_usuario);
        }

        //Accion del boton cambiar MasterKey
        changeMK = findViewById(R.id.panelMaster);
        changeMK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PanelActivity.this, MasterkeyActivity.class);
                intent.putExtra("id", id_usuario);
                intent.putExtra("key",key);
                intent.putExtra("last_login",last_login);
                PanelActivity.this.startActivity(intent);
                //Se finaliza la actividad porque depues debera volver a cargarse con la nueva key
                finish();
            }
        });

        //Accion del boton generar contraseña
        generate = findViewById(R.id.panelGenerar);
        generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PanelActivity.this, GenerateActivity.class);
                intent.putExtra("id", id_usuario);
                intent.putExtra("key",key);
                intent.putExtra("last_login",last_login);
                PanelActivity.this.startActivity(intent);
            }
        });

        //Accion del boton modificar servicio
        modify = findViewById(R.id.panelModificar);
        modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dbhelper.anyPassword(id_usuario)) {
                    Intent intent = new Intent(PanelActivity.this, ModifyActivity.class);
                    intent.putExtra("id", id_usuario);
                    intent.putExtra("key", key);
                    intent.putExtra("last_login", last_login);
                    PanelActivity.this.startActivity(intent);
                } else {
                    Toast.makeText(PanelActivity.this, getText(R.string.PANELnodata), Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Accion del boton consultar servicio
        search = findViewById(R.id.panelBuscar);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dbhelper.anyPassword(id_usuario)) {
                    Intent intent = new Intent(PanelActivity.this, SearchActivity.class);
                    intent.putExtra("id", id_usuario);
                    intent.putExtra("key", key);
                    intent.putExtra("last_login",last_login);
                    PanelActivity.this.startActivity(intent);
                } else {
                    Toast.makeText(PanelActivity.this, getText(R.string.PANELnodata), Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Accion del boton eliminar servicio
        delete = findViewById(R.id.panelEliminar);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dbhelper.anyPassword(id_usuario)) {
                    Intent intent = new Intent(PanelActivity.this, DeleteActivity.class);
                    intent.putExtra("id", id_usuario);
                    intent.putExtra("key", key);
                    intent.putExtra("last_login",last_login);
                    PanelActivity.this.startActivity(intent);
                } else {
                    Toast.makeText(PanelActivity.this, getText(R.string.PANELnodata), Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Accion del boton crear copia de seguridad
        backup = findViewById(R.id.panelBackup);
        backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PanelActivity.this, BackupActivity.class);
                intent.putExtra("id", id_usuario);
                intent.putExtra("key", key);
                intent.putExtra("last_login",last_login);
                PanelActivity.this.startActivity(intent);
            }
        });

    }

    /**
     * Método que gestiona la accion al presionar el boton retroceso
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


    /**
     * Método que gestiona la accion cuando la aplicacion deja de estar visible
     */
    @Override
    protected void onStop() {
        super.onStop();
        finish();

    }
}