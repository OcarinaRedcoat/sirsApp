package tk.sirsbank;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    Log.d("log", "Antes da Socket");
                    Socket s = new Socket("192.168.1.87", 1234);
                    DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                    Log.d("log", "Depois do dos");
                    dos.writeUTF("Hello Server");
                    DataInputStream dis = new DataInputStream(s.getInputStream());
                    dis.read();
                    s.close();

                } catch (
                        IOException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }
}