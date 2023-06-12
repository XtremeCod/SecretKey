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
import android.widget.TextView;
import android.widget.Toast;

import com.secretkey.cifrador.Cifrador;
import com.secretkey.db.DbHelper;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class MasterkeyActivity extends AppCompatActivity {

    private Button btn_change;
    private ImageView close_error, close_ok;
    private String id_usuario, key, last_login;
    private String new_key = null;
    private EditText password1;
    private EditText password2;
    private TextView popup_ok;
    private DbHelper dbhelper;

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
        setContentView(R.layout.activity_change_master_key);

        dbhelper = new DbHelper(MasterkeyActivity.this);

        //Obtenemos datos del contexto anterior
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            id_usuario = extras.getString("id");
            key = extras.getString("key");
            last_login = extras.getString("last_login");
        }

        //POPUP ERROR
        LayoutInflater inflater = (LayoutInflater) MasterkeyActivity.this
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_password, null);
        int width = ConstraintLayout.LayoutParams.MATCH_PARENT;
        int height = ConstraintLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        close_error = popupView.findViewById(R.id.popupCerrar);
        close_error.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        //POPUP OK
        LayoutInflater inflater2 = (LayoutInflater) MasterkeyActivity.this
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView2 = inflater2.inflate(R.layout.popup_realizado, null);
        popup_ok = popupView2.findViewById(R.id.popup1Texto);
        int width2 = ConstraintLayout.LayoutParams.MATCH_PARENT;
        int height2 = ConstraintLayout.LayoutParams.MATCH_PARENT;
        boolean focusable2 = true;
        final PopupWindow popupWindow2 = new PopupWindow(popupView2, width2, height2, focusable2);
        close_ok = popupView2.findViewById(R.id.popup1Cerrar);
        close_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow2.dismiss();
                Intent intent = new Intent(MasterkeyActivity.this, PanelActivity.class);
                intent.putExtra("id", id_usuario);
                intent.putExtra("key", new_key);
                intent.putExtra("last_login", last_login);
                MasterkeyActivity.this.startActivity(intent);
                finish();
            }
        });

        //Accion del boton cambiar
        btn_change = findViewById(R.id.changeChangeMK);
        btn_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                password1 = findViewById(R.id.changePassword);
                password2 = findViewById(R.id.changePassword2);

                String password3 = password1.getText().toString();
                String password4 = password2.getText().toString();

                //Comprobaciones
                if (password3.equals(password4)) {
                    if (verificarPassword(password3)) {

                        //Cambiamos la master key
                        dbhelper.changeMasterKey(id_usuario, Cifrador.generateHASH(password3));

                        //Log
                        dbhelper.insertLog(id_usuario, getText(R.string.MASTERKEYlog).toString());

                        //Si existen servicios, estos se desencriptan y se vuelven a encriptar con la nueva key
                        if (dbhelper.anyPassword(id_usuario)) {
                            try {
                                new_key = Cifrador.createKey(password3, password3);
                                int j = 0;
                                int cantidad = dbhelper.changeKeyPasswords(id_usuario).size();

                                while (j < cantidad) {
                                    int i = 0;
                                    String[] olddata = dbhelper.changeKeyPasswords(id_usuario).get(j);
                                    j++;
                                    String id = olddata[i];
                                    i++;
                                    String email = Cifrador.descifrar(key, olddata[i]);
                                    i++;
                                    String password = Cifrador.descifrar(key, olddata[i]);

                                    dbhelper.updateEncriptedData(id, Cifrador.cifrar(new_key, email), Cifrador.cifrar(new_key, password));
                                }
                            } catch (NoSuchPaddingException e) {
                                Toast.makeText(MasterkeyActivity.this, getText(R.string.CIFRADOerror), Toast.LENGTH_SHORT).show();
                                dbhelper.insertLog(id_usuario, getText(R.string.CIFRADOerror).toString());
                                throw new RuntimeException(e);
                            } catch (NoSuchAlgorithmException e) {
                                Toast.makeText(MasterkeyActivity.this, getText(R.string.CIFRADOerror), Toast.LENGTH_SHORT).show();
                                dbhelper.insertLog(id_usuario, getText(R.string.CIFRADOerror).toString());
                                throw new RuntimeException(e);
                            } catch (InvalidKeySpecException e) {
                                Toast.makeText(MasterkeyActivity.this, getText(R.string.CIFRADOerror), Toast.LENGTH_SHORT).show();
                                dbhelper.insertLog(id_usuario, getText(R.string.CIFRADOerror).toString());
                                throw new RuntimeException(e);
                            } catch (IllegalBlockSizeException e) {
                                Toast.makeText(MasterkeyActivity.this, getText(R.string.CIFRADOerror), Toast.LENGTH_SHORT).show();
                                dbhelper.insertLog(id_usuario, getText(R.string.CIFRADOerror).toString());
                                throw new RuntimeException(e);
                            } catch (BadPaddingException e) {
                                Toast.makeText(MasterkeyActivity.this, getText(R.string.CIFRADOerror), Toast.LENGTH_SHORT).show();
                                dbhelper.insertLog(id_usuario, getText(R.string.CIFRADOerror).toString());
                                throw new RuntimeException(e);
                            } catch (InvalidKeyException e) {
                                Toast.makeText(MasterkeyActivity.this, getText(R.string.CIFRADOerror), Toast.LENGTH_SHORT).show();
                                dbhelper.insertLog(id_usuario, getText(R.string.CIFRADOerror).toString());
                                throw new RuntimeException(e);
                            }

                        }

                        //Mostramos popup de exito
                        popup_ok.setText(getText(R.string.MASTERKEYpopup));
                        popupWindow2.showAtLocation(btn_change, Gravity.CENTER, 0, 0);
                        popupWindow2.setOutsideTouchable(true);
                        popupWindow2.setFocusable(true);
                        popupWindow2.setTouchable(true);
                        popupWindow2.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                    } else {
                        //Mostramos popup contraseña no valida
                        popupWindow.showAtLocation(btn_change, Gravity.CENTER, 0, 0);
                        popupWindow.setOutsideTouchable(true);
                        popupWindow.setFocusable(true);
                        popupWindow.setTouchable(true);
                        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    }
                } else {
                    Toast.makeText(MasterkeyActivity.this, getText(R.string.MASTERKEYlog2), Toast.LENGTH_SHORT).show();
                    dbhelper.insertLog(id_usuario, getText(R.string.MASTERKEYlog2).toString());
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
        if (new_key == null) {
            Intent intent = new Intent(MasterkeyActivity.this, PanelActivity.class);
            intent.putExtra("id", id_usuario);
            intent.putExtra("key", key);
            intent.putExtra("last_login", last_login);
            MasterkeyActivity.this.startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(MasterkeyActivity.this, PanelActivity.class);
            intent.putExtra("id", id_usuario);
            intent.putExtra("key", new_key);
            intent.putExtra("last_login", last_login);
            MasterkeyActivity.this.startActivity(intent);
            finish();
        }
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