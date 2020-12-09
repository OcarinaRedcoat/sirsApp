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
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executor;

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

                        //Log.d("Android ID", getDeviceID((getBaseContext())));

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
    }


    protected static String getDeviceID(Context Ctx) {

        String android_id = Secure.getString(Ctx.getContentResolver(), Secure.ANDROID_ID);
        return android_id;
    }

    protected void sendAuth(){
        Thread t = new Thread(() -> {
            try {

                Socket s = new Socket("192.168.1.106", 1234);
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());

                JSONObject json = new JSONObject();
                json.put("androidID", getDeviceID(getBaseContext())); //FIXME: Falta dar hash do androidID
                //json.put("QRcode", "");

                dos.writeUTF(json.toString());
                s.close();

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        });
        t.start();
    }

/*    public static String getImeiNumber(Context Ctx) {
        final TelephonyManager telephonyManager = (TelephonyManager) Ctx.getSystemService(Context.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //getDeviceId() is Deprecated so for android O we can use getImei() method
            return telephonyManager.getImei();
        } else {
            return getDeviceID(Ctx);
        }

    }*/
}