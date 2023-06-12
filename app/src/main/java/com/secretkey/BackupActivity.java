package com.secretkey;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.secretkey.db.DbHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class BackupActivity extends AppCompatActivity {

    private Button crear, restaurar;
    private File fileSource, fileDestination;
    private String id_usuario, last_login, key;
    private TextView popup;
    private ImageView btnclose;
    private DbHelper dbhelper;
    private Boolean restaurada = false;

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
        setContentView(R.layout.activity_backup);

        dbhelper = new DbHelper(BackupActivity.this);

        //Obtenemos los datos del contexto anterior
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            id_usuario = extras.getString("id");
            last_login = extras.getString("last_login");
            key = extras.getString("key");
        }

        //Inicializamos el popup
        LayoutInflater inflater = (LayoutInflater) BackupActivity.this
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_realizado, null);
        int width = ConstraintLayout.LayoutParams.MATCH_PARENT;
        int height = ConstraintLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popup = popupView.findViewById(R.id.popup1Texto);
        btnclose = popupView.findViewById(R.id.popup1Cerrar);
        btnclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (restaurada) {
                    finish();
                } else {
                    popupWindow.dismiss();
                }
            }
        });

        //Solicitud de permisos
        ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE},
                PackageManager.PERMISSION_GRANTED);

        //Funcion del boton crear backup
        crear = findViewById(R.id.backupCreate);
        crear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StorageManager storageManager = (StorageManager) getSystemService(STORAGE_SERVICE);
                List<StorageVolume> storageVolumeList = storageManager.getStorageVolumes();
                StorageVolume storageVolume = storageVolumeList.get(0);
                Context context = getApplicationContext();

                //Comprobacion de la API
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    fileSource = new File(context.getDatabasePath("secretKey.db").getPath());
                    fileDestination = new File(storageVolume.getDirectory().getPath() + "/Download/secretKey.db");

                    //Escribimos la base de datos en su destino
                    try {
                        InputStream inputStream = new FileInputStream(fileSource);
                        OutputStream outputStream = new FileOutputStream(fileDestination);

                        byte[] byteArrayBuffer = new byte[1024];
                        int intLenght;
                        while ((intLenght = inputStream.read(byteArrayBuffer)) > 0) {
                            outputStream.write(byteArrayBuffer, 0, intLenght);
                        }
                        inputStream.close();
                        outputStream.close();

                        //Mostramos el popup
                        popup.setText(getText(R.string.BACKUPpopup));
                        popupWindow.showAtLocation(crear, Gravity.CENTER, 0, 0);
                        popupWindow.setOutsideTouchable(true);
                        popupWindow.setFocusable(true);
                        popupWindow.setTouchable(true);
                        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                        //Log
                        dbhelper.insertLog(id_usuario, getText(R.string.BACKUPlog).toString());

                    } catch (FileNotFoundException e) {
                        Toast.makeText(BackupActivity.this, getText(R.string.FILEerror), Toast.LENGTH_SHORT).show();
                        dbhelper.insertLog(id_usuario, getText(R.string.FILEerror).toString());
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        Toast.makeText(BackupActivity.this, getText(R.string.FILEerror), Toast.LENGTH_SHORT).show();
                        dbhelper.insertLog(id_usuario, getText(R.string.FILEerror).toString());
                        throw new RuntimeException(e);
                    }
                } else {
                    Toast.makeText(BackupActivity.this, getText(R.string.BACKUPlogAPI), Toast.LENGTH_SHORT).show();
                    dbhelper.insertLog(id_usuario, getText(R.string.BACKUPlogAPI).toString());
                }


            }
        });

        //Funcion del boton restaurar copia
        restaurar = findViewById(R.id.backupRestore);
        restaurar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StorageManager storageManager = (StorageManager) getSystemService(STORAGE_SERVICE);
                List<StorageVolume> storageVolumeList = storageManager.getStorageVolumes();
                StorageVolume storageVolume = storageVolumeList.get(0);
                Context context = getApplicationContext();

                //Comprobacion de la API
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    fileDestination = new File(context.getDatabasePath("secretKey.db").getPath());
                    fileSource = new File(storageVolume.getDirectory().getPath() + "/Download/secretKey.db");

                    //Si el fichero de origen existe copiamos el fichero en nuestra ruta d ela aplicacion
                    if (fileSource.exists()) {
                        try {
                            InputStream inputStream = new FileInputStream(fileSource);
                            OutputStream outputStream = new FileOutputStream(fileDestination);

                            byte[] byteArrayBuffer = new byte[1024];
                            int intLenght;
                            while ((intLenght = inputStream.read(byteArrayBuffer)) > 0) {
                                outputStream.write(byteArrayBuffer, 0, intLenght);
                            }
                            inputStream.close();
                            outputStream.close();

                            //Mostramos el popup
                            popup.setText(getText(R.string.BACKUPpopup2));
                            popupWindow.showAtLocation(crear, Gravity.CENTER, 0, 0);
                            popupWindow.setOutsideTouchable(true);
                            popupWindow.setFocusable(true);
                            popupWindow.setTouchable(true);
                            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                            //Marcamos true para que finalice la aplicacion
                            restaurada = true;

                        } catch (FileNotFoundException e) {
                            Toast.makeText(BackupActivity.this, getText(R.string.FILEerror), Toast.LENGTH_SHORT).show();
                            dbhelper.insertLog(id_usuario, getText(R.string.FILEerror).toString());
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            Toast.makeText(BackupActivity.this, getText(R.string.FILEerror), Toast.LENGTH_SHORT).show();
                            dbhelper.insertLog(id_usuario, getText(R.string.FILEerror).toString());
                            throw new RuntimeException(e);
                        }
                    } else {
                        Toast.makeText(BackupActivity.this, getText(R.string.BACKUPlog2), Toast.LENGTH_SHORT).show();
                        dbhelper.insertLog(id_usuario, getText(R.string.BACKUPlog2).toString());
                    }
                } else {
                    Toast.makeText(BackupActivity.this, getText(R.string.BACKUPlogAPI), Toast.LENGTH_SHORT).show();
                    dbhelper.insertLog(id_usuario, getText(R.string.BACKUPlogAPI).toString());
                }


            }

        });
    }

    /**
     * Metodo que gestiona la accion si se presiona el boton de volver
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(BackupActivity.this, PanelActivity.class);
        intent.putExtra("id", id_usuario);
        intent.putExtra("key", key);
        intent.putExtra("last_login", last_login);
        BackupActivity.this.startActivity(intent);
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