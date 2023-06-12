package com.secretkey;

import static com.secretkey.functions.SecurePassword.verificarPassword;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.secretkey.cifrador.Cifrador;
import com.secretkey.db.DbHelper;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.NoSuchPaddingException;

public class RegisterActivity extends AppCompatActivity {

    private Button button_register;
    private EditText usuario, password, confirm_password;

    private ImageView btnclose;

    /**
     * Método que se ejecuta al crear la vista
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        DbHelper dbhelper = new DbHelper(RegisterActivity.this);

        //Casting de componentes
        button_register = findViewById(R.id.registerCreate);
        usuario = findViewById(R.id.registerUser);
        password = findViewById(R.id.registerPassword);
        confirm_password = findViewById(R.id.registerPassword2);

        //Acciones al presionar boton registrar
        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Creacion de servicios
                dbhelper.crearServicios();

                //Valores introducidos por el usuario
                String usuarioVar = usuario.getText().toString();
                String passwordVar = password.getText().toString();
                String password_confirmVar = confirm_password.getText().toString();

                //Creacion del popup
                LayoutInflater inflater = (LayoutInflater) RegisterActivity.this
                        .getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.popup_password, null);
                int width = ConstraintLayout.LayoutParams.MATCH_PARENT;
                int height = ConstraintLayout.LayoutParams.MATCH_PARENT;
                boolean focusable = true;
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
                btnclose = popupView.findViewById(R.id.popupCerrar);
                btnclose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });

                //Comprobaciones de entradas
                if (usuarioVar.length() > 2 && usuarioVar.length() < 50) {
                    if (passwordVar.equals(password_confirmVar)) {
                        if (verificarPassword(passwordVar)) {
                            if (!dbhelper.userExists(usuarioVar)) {

                                //Creamos el usuario en la base de datos
                                dbhelper.createUser(usuarioVar, Cifrador.generateHASH(passwordVar));

                                //Obtenemos los datos de la BD, guardamos el log y llamamos a otra actividad
                                String[] userData = null;
                                try {
                                    userData = dbhelper.allDataUser(usuarioVar, passwordVar);
                                    dbhelper.insertLog(userData[0], getText(R.string.REGISTERLOGregistrousuario).toString());
                                    Intent intent = new Intent(RegisterActivity.this, PanelActivity.class);
                                    intent.putExtra("id", userData[0]);
                                    intent.putExtra("last_login", userData[3]);
                                    intent.putExtra("key", Cifrador.createKey(passwordVar, passwordVar));
                                    intent.putExtra("first_login", true);
                                    RegisterActivity.this.startActivity(intent);
                                } catch (NoSuchPaddingException e) {
                                    Toast.makeText(RegisterActivity.this,getText(R.string.CIFRADOerror), Toast.LENGTH_SHORT).show();
                                    dbhelper.insertLog(userData[0],getText(R.string.CIFRADOerror).toString());
                                    throw new RuntimeException(e);
                                } catch (NoSuchAlgorithmException e) {
                                    Toast.makeText(RegisterActivity.this,getText(R.string.CIFRADOerror), Toast.LENGTH_SHORT).show();
                                    dbhelper.insertLog(userData[0],getText(R.string.CIFRADOerror).toString());
                                    throw new RuntimeException(e);
                                } catch (InvalidKeySpecException e) {
                                    Toast.makeText(RegisterActivity.this,getText(R.string.CIFRADOerror), Toast.LENGTH_SHORT).show();
                                    dbhelper.insertLog(userData[0],getText(R.string.CIFRADOerror).toString());
                                    throw new RuntimeException(e);
                                }

                            } else {
                                Toast.makeText(RegisterActivity.this, getText(R.string.REGISTERlogusuarioexiste), Toast.LENGTH_SHORT).show();
                            }
                        } else {

                            popupWindow.showAtLocation(button_register, Gravity.CENTER, 0, 0);
                            popupWindow.setOutsideTouchable(true);
                            popupWindow.setFocusable(true);
                            popupWindow.setTouchable(true);
                            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, getText(R.string.REGISTERlogcontrasenianocoincide), Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(RegisterActivity.this, getText(R.string.REGISTERlognousuario), Toast.LENGTH_SHORT).show();
                }

            }


        });
    }

    /**
     * Metodo que gestiona la accion si se presiona el boton de retroceso
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        RegisterActivity.this.startActivity(intent);
    }

    /**
     * Metodo que gestiona la accion de la vista si esta queda en segundo plano
     */
    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
}