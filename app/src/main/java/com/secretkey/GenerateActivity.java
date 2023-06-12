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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.secretkey.db.DbHelper;
import com.secretkey.functions.SecurePassword;

public class GenerateActivity extends AppCompatActivity {

    private EditText password;
    private TextView popup;
    private Button usar,own;
    private ImageView btnclose;
    private String id_usuario,key,last_login;
    private CountDownTimer countDownTimer;
    private static final long COUNTDOWN_DURATION = 2 * 60 * 1000;

    /**
     * Metodo que se ejecuta al iniciar la vista
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate);

        DbHelper dbhelper = new DbHelper(GenerateActivity.this);

        //Obtenemos datos de actividad anterior
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            id_usuario = extras.getString("id");
            key = extras.getString("key");
            last_login = extras.getString("last_login");
        }

        //Hacemos casting de los elementos
        password = findViewById(R.id.generatePassword);
        password.setText(SecurePassword.generarPassword());
        usar = findViewById(R.id.generateUse);


        //Accion del boton usar contraseña generada
        usar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GenerateActivity.this, SaveActivity.class);
                intent.putExtra("id", id_usuario);
                intent.putExtra("password", password.getText().toString());
                intent.putExtra("key",key);
                intent.putExtra("last_login",last_login);
                GenerateActivity.this.startActivity(intent);
                finish();
            }
        });

        //Accion del boton usar contraseña propia
        own = findViewById(R.id.generateExists);
        own.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GenerateActivity.this, SaveActivity.class);
                intent.putExtra("id", id_usuario);
                intent.putExtra("password", "");
                intent.putExtra("key",key);
                intent.putExtra("last_login",last_login);
                GenerateActivity.this.startActivity(intent);
                finish();
            }
        });

        //Accion al presionar en copiar
        password.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT=0;
                final int DRAWABLE_TOP=1;
                final int DRAWABLE_RIGHT=2;
                final int DRAWABLE_BOTTOM=3;

                //Creacion de popup indicar contraseña copiada en portapapeles
                LayoutInflater inflater = (LayoutInflater) GenerateActivity.this
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
                        popupWindow.dismiss();
                    }
                });

                if(event.getAction() == MotionEvent.ACTION_UP){
                    if(event.getRawX() >= (password.getRight() - password.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())){
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Password generado", password.getText().toString());
                        clipboard.setPrimaryClip(clip);

                        popup.setText(getText(R.string.GENERATEpopupexito));

                        popupWindow.showAtLocation(password, Gravity.CENTER, 0, 0);
                        popupWindow.setOutsideTouchable(true);
                        popupWindow.setFocusable(true);
                        popupWindow.setTouchable(true);
                        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dbhelper.insertLog(id_usuario,getText(R.string.GENERATElog).toString());
                        return true;
                    }
                }
                return false;
            }
        });
    }

    /**
     * Método que se ejecuta al presionar el boton de volver
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(GenerateActivity.this, PanelActivity.class);
        intent.putExtra("id", id_usuario);
        intent.putExtra("last_login", last_login);
        intent.putExtra("key", key);
        GenerateActivity.this.startActivity(intent);
    }

    /**
     * Método que se ejecuta cuando la aplicacion queda en segundo plano
     */
    @Override
    protected void onStop() {
        super.onStop();
        //El contador esta fijado en 1 minutos, si transcurren ejecutan finish()
        startCountdownTimer();

    }

    /**
     * Metodo que ejerce como contador
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