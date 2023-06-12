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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.secretkey.cifrador.Cifrador;
import com.secretkey.db.DbHelper;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.NoSuchPaddingException;

public class MainActivity extends AppCompatActivity {

    private Button loginbtn;
    private Animation animation;
    private TextView newaccount,popup;
    private EditText userlogin, passwordlogin;
    private ImageView logo,infobtn,btnclose;


    /**
     * Metodo que se ejecuta al iniciar la actividad
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DbHelper dbhelper = new DbHelper(MainActivity.this);

        //Identificacion de contenidos
        loginbtn = findViewById(R.id.mainLogin);
        logo = findViewById(R.id.mainLogo);
        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        newaccount = findViewById(R.id.mainNew);
        userlogin = findViewById(R.id.mainUser);
        passwordlogin = findViewById(R.id.mainPassword);
        infobtn = findViewById(R.id.mainInfo);

        //Iniciacion animación inicial logo
        logo.startAnimation(animation);

        //Funcion del boton login
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Iniciamos los servicios disponibles
                dbhelper.crearServicios();

                //Obtenemos los valores intruducidos por el usuario
                String user = userlogin.getText().toString();
                String passwd = passwordlogin.getText().toString();

                //Comprobamos si el usuario existe
                if (dbhelper.userExists(user)) {

                    //Obtenemos los datos del usuario
                    String[] userData = null;
                    try {
                        userData = dbhelper.allDataUser(user, passwd);
                    } catch (NoSuchPaddingException e) {
                        Toast.makeText(MainActivity.this,getText(R.string.CIFRADOerror), Toast.LENGTH_SHORT).show();
                        dbhelper.insertLog(userData[0],getText(R.string.CIFRADOerror).toString());
                        throw new RuntimeException(e);
                    } catch (NoSuchAlgorithmException e) {
                        Toast.makeText(MainActivity.this,getText(R.string.CIFRADOerror), Toast.LENGTH_SHORT).show();
                        dbhelper.insertLog(userData[0],getText(R.string.CIFRADOerror).toString());
                        throw new RuntimeException(e);
                    } catch (InvalidKeySpecException e) {
                        Toast.makeText(MainActivity.this,getText(R.string.CIFRADOerror), Toast.LENGTH_SHORT).show();
                        dbhelper.insertLog(userData[0],getText(R.string.CIFRADOerror).toString());
                        throw new RuntimeException(e);
                    }

                    //Si la password es correcta userData no sera nulo y contendra los datos del usuario
                    if (userData != null) {
                        try {

                            //Insertamos un log en la base de datos
                            dbhelper.insertLog(userData[0], getText(R.string.MAINlogacceso).toString());

                            //Abrimos una nueva actividad
                            Intent intent = new Intent(MainActivity.this, PanelActivity.class);
                            intent.putExtra("id", userData[0]);
                            intent.putExtra("last_login", userData[3]);
                            intent.putExtra("key", Cifrador.createKey(passwd,passwd));
                            intent.putExtra("first_login",true);
                            MainActivity.this.startActivity(intent);

                        } catch (NoSuchPaddingException e) {
                            Toast.makeText(MainActivity.this,getText(R.string.CIFRADOerror), Toast.LENGTH_SHORT).show();
                            dbhelper.insertLog(userData[0],getText(R.string.CIFRADOerror).toString());
                            throw new RuntimeException(e);
                        } catch (NoSuchAlgorithmException e) {
                            Toast.makeText(MainActivity.this,getText(R.string.CIFRADOerror), Toast.LENGTH_SHORT).show();
                            dbhelper.insertLog(userData[0],getText(R.string.CIFRADOerror).toString());
                            throw new RuntimeException(e);
                        } catch (InvalidKeySpecException e) {
                            Toast.makeText(MainActivity.this,getText(R.string.CIFRADOerror), Toast.LENGTH_SHORT).show();
                            dbhelper.insertLog(userData[0],getText(R.string.CIFRADOerror).toString());
                            throw new RuntimeException(e);
                        }

                    } else {
                        //Si userData es nulo significa que la contraseña no es correcta
                        Toast.makeText(MainActivity.this,getText(R.string.MAINlogcontraseniaincorrecta), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //Puede ocurrir que el usuario no exista, y asi se le notifica
                    Toast.makeText(MainActivity.this, getText(R.string.MAINlognoexisteusuario), Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Funcion del boton nueva cuenta - registro
        newaccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Abrimos una nueva actividad para el registro
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });

        infobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Creacion del popup con la informacion
                LayoutInflater inflater = (LayoutInflater) MainActivity.this
                        .getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.popup_about, null);
                int width = ConstraintLayout.LayoutParams.MATCH_PARENT;
                int height = ConstraintLayout.LayoutParams.MATCH_PARENT;
                boolean focusable = true;
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
                btnclose = popupView.findViewById(R.id.aboutClose);
                popup = popupView.findViewById(R.id.aboutText);

                //Establecemos el texto a mostrar en el popup
                popup.setText(getText(R.string.infoApp));

                //Se muestra el popup
                popupWindow.showAtLocation(newaccount, Gravity.CENTER, 0, 0);
                popupWindow.setOutsideTouchable(true);
                popupWindow.setFocusable(true);
                popupWindow.setTouchable(true);
                popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                //Gestion si se cierra el popoup desde el boton
                btnclose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });

            }
        });
    }

    /**
     * Metodo que gestiona la accion al presionar el boton volver
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    /**
     * Metodo que gestiona la accion si la aplicacion deja de estar en primer plano
     */
    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
}