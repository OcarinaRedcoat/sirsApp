package tk.sirsbank;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;
import java.security.MessageDigest;
import java.util.Base64;

public class MainActivity extends AppCompatActivity {

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnAuth = (Button) findViewById(R.id.btnAuth);
        executor = ContextCompat.getMainExecutor(this);
        btnAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Button", "Click on Auth button");

                biometricPrompt = new BiometricPrompt(MainActivity.this,
                        executor, new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode,
                                                      @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        Toast.makeText(getApplicationContext(),
                                "Authentication error: " + errString, Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onAuthenticationSucceeded(
                            @NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        Toast.makeText(getApplicationContext(),
                                "Authentication succeeded!", Toast.LENGTH_SHORT).show();

                        Log.d("Android ID", getDeviceID((getBaseContext())));

                        // Enviar aqui as coisas para o server (AndroidID)
                        sendAuth();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(getApplicationContext(), "Authentication failed",
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                });

                promptInfo = new BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Biometric login for my app")
                        .setSubtitle("Log in using your biometric credential")
                        .setNegativeButtonText("Use account password")
                        .build();

                biometricPrompt.authenticate(promptInfo);
            }


        });

        Button btnReg = (Button) findViewById(R.id.btnRegister);
        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView code = findViewById(R.id.code);
                Log.d("Code", code.getText().toString());

                if (code.getText().toString().length() != 6){
                    Toast.makeText(getApplicationContext(), "Register Code must have 6 digits, try again.",
                            Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                sendRegister(code.getText().toString());
            }
        });
    }


    protected static String getDeviceID(Context Ctx) {
        try {
            String android_id = Secure.getString(Ctx.getContentResolver(), Secure.ANDROID_ID);
            Log.d("Android ID", android_id);
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(android_id.getBytes());
            String stringHash = Base64.getEncoder().encodeToString(messageDigest.digest());
            Log.d("JSON hashed", stringHash);
            return stringHash;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void sendRegister(String code){
        Log.d("SendRegister Code", code);
        Thread t = new Thread(() -> {
            try {

                Socket s = new Socket("192.168.1.125", 1234);
                BufferedOutputStream dos = new BufferedOutputStream(s.getOutputStream());

                JSONObject json = new JSONObject();
                json.put("androidID", getDeviceID(getBaseContext()));
                json.put("registerCode", code);
                Log.d("JSON", json.toString());
                Log.d("JSON Bytes", json.toString().getBytes().toString());
                dos.write(json.toString().getBytes());
                dos.flush();
                s.close();

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        });
        t.start();
    }

    protected void sendAuth(){
        Thread t = new Thread(() -> {
            try {

                Socket s = new Socket("192.168.1.125", 1234);
                BufferedOutputStream dos = new BufferedOutputStream(s.getOutputStream());

                JSONObject json = new JSONObject();
                json.put("androidID", getDeviceID(getBaseContext()));
                Log.d("JSON", json.toString());
                Log.d("JSON Bytes", json.toString().getBytes().toString());
                dos.write(json.toString().getBytes());
                dos.flush();
                s.close();

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        });
        t.start();
    }

}