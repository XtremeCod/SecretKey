package com.secretkey;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

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

public class SearchActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    String id_usuario, key, selected, last_login;

    ArrayList<CustomItem> customList;
    DbHelper dbhelper;
    Spinner spinner;
    TextView email,password,popup,fecha;
    ImageView btnclose;
    private CountDownTimer countDownTimer;
    private static final long COUNTDOWN_DURATION = 2 * 60 * 1000;

    /**
     * Método que inicia la vista
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        dbhelper = new DbHelper(SearchActivity.this);

        //Obtenemos los datos de la vista anterior
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            id_usuario = extras.getString("id");
            key = extras.getString("key");
            last_login = extras.getString("last_login");
        }

        spinner = findViewById(R.id.searchType);
        email = (TextView) findViewById(R.id.searchEmail);
        password = (TextView) findViewById(R.id.searchPassword);
        fecha = (TextView) findViewById(R.id.searchDate);


        //Iniciamos el spinner
        customList = getCustomList();
        CustomAdapter adapter = new CustomAdapter(this, customList);
        if (spinner != null) {
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(this);
        }

        //Creacion de popup indicar contraseña copiada en portapapeles
        LayoutInflater inflater = (LayoutInflater) SearchActivity.this
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_realizado, null);
        int width = ConstraintLayout.LayoutParams.MATCH_PARENT;
        int height = ConstraintLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popup = (TextView) popupView.findViewById(R.id.popup1Texto);


        btnclose = (ImageView) popupView.findViewById(R.id.popup1Cerrar);
        btnclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        password.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT=0;
                final int DRAWABLE_TOP=1;
                final int DRAWABLE_RIGHT=2;
                final int DRAWABLE_BOTTOM=3;

                if(event.getAction() == MotionEvent.ACTION_UP){
                    if(event.getRawX() >= (password.getRight() - password.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())){
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Password generado", password.getText().toString());
                        clipboard.setPrimaryClip(clip);

                        popup.setText(getText(R.string.SEARCHpopup));

                        popupWindow.showAtLocation(password, Gravity.CENTER, 0, 0);
                        popupWindow.setOutsideTouchable(true);
                        popupWindow.setFocusable(true);
                        popupWindow.setTouchable(true);
                        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dbhelper.insertLog(id_usuario,getText(R.string.SEARCHlog1).toString());
                        return true;
                    }
                }
                return false;
            }
        });
    }

    /**
     * Método que obtiene los tipos de servicio existentes
     * @return Retorna un ArrayList con los tipos de servicio
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
     * Método que gestiona la accion al presionar el boton de volver
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(SearchActivity.this, PanelActivity.class);
        intent.putExtra("id", id_usuario);
        intent.putExtra("last_login", last_login);
        intent.putExtra("key", key);
        SearchActivity.this.startActivity(intent);
    }

    /**
     * Método que gestiona las acciones cuando un tipo de servicio es seleccionado
     * @param adapterView The AdapterView where the selection happened
     * @param view The view within the AdapterView that was clicked
     * @param position The position of the view in the adapter
     * @param id The row id of the item that is selected
     */
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        CustomItem item = (CustomItem) adapterView.getSelectedItem();
        selected = item.getSpinnerItemName();

        //Obtenemos los datos
        try {
            String[] datos = dbhelper.selectedService(id_usuario, selected);

            email.setText(Cifrador.descifrar(key, datos[0]));
            password.setText(Cifrador.descifrar(key, datos[1]));
            fecha.setText(datos[2]);

            //Guardamos el log
            dbhelper.insertLog(id_usuario,getText(R.string.SEARCHlog) + " " + selected);

        } catch (NoSuchPaddingException e) {
            dbhelper.insertLog(id_usuario, getText(R.string.CIFRADOerror).toString());
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            dbhelper.insertLog(id_usuario, getText(R.string.CIFRADOerror).toString());
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            dbhelper.insertLog(id_usuario, getText(R.string.CIFRADOerror).toString());
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException e) {
            dbhelper.insertLog(id_usuario, getText(R.string.CIFRADOerror).toString());
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            dbhelper.insertLog(id_usuario, getText(R.string.CIFRADOerror).toString());
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            dbhelper.insertLog(id_usuario, getText(R.string.CIFRADOerror).toString());
            throw new RuntimeException(e);
        }
    }

    /**
     * Método que gestiona las acciones cuando un tipo de servicio no esta seleccionado
     * @param parent The AdapterView that now contains no selected item.
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    /**
     * Método que gestiona el estado de la vista cuando queda en segundo plano
     */
    @Override
    protected void onStop() {
        super.onStop();
        startCountdownTimer();

    }

    /**
     * Método contador. En este caso gestiona onStop segun el tiempo
     */
    private void startCountdownTimer() {
        countDownTimer = new CountDownTimer(COUNTDOWN_DURATION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsRemaining = millisUntilFinished / 1000;
            }

            @Override
            public void onFinish() {
                finish();
            }
        };
        countDownTimer.start();
    }

}