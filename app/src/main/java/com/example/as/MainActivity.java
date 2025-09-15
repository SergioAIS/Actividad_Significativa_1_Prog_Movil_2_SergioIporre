package com.example.as;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private LinearLayout containerItems;
    private TextView tvProgress;
    private TextView tvMensaje;
    private Button btnDownloadAll;

    private Handler handler = new Handler();
    private ExecutorService executorService = Executors.newFixedThreadPool(3);

    private int totalItems = 5;
    private boolean[] downloadedItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        containerItems = findViewById(R.id.containerItems);
        tvProgress = findViewById(R.id.tvProgress);
        tvMensaje = findViewById(R.id.tvMensaje);
        btnDownloadAll = findViewById(R.id.btnDownloadAll);

        downloadedItems = new boolean[totalItems];

        for (int i = 0; i < totalItems; i++) {
            int index = i;

            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setGravity(Gravity.CENTER_VERTICAL);
            itemLayout.setPadding(0,10,0,10);

            TextView tvItem = new TextView(this);
            tvItem.setText("Item " + (index+1) + ": Esperando");
            tvItem.setTextSize(16f);
            tvItem.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            Button btnItem = new Button(this);
            btnItem.setText("Descargar");
            btnItem.setOnClickListener(v -> startDownloadThread(index, tvItem));

            itemLayout.addView(tvItem);
            itemLayout.addView(btnItem);
            containerItems.addView(itemLayout);
        }

        btnDownloadAll.setOnClickListener(v -> {
            for (int i = 0; i < totalItems; i++) {
                if (!downloadedItems[i]) {
                    int index = i;
                    TextView tvItem = (TextView)((LinearLayout)containerItems.getChildAt(index)).getChildAt(0);
                    executorService.submit(() -> startDownloadExecutor(index, tvItem));
                }
            }
        });

        handler.post(new Runnable() {
            @Override
            public void run() {
                int count = 0;
                for (boolean b : downloadedItems) {
                    if (b) count++;
                }
                tvProgress.setText("Progreso: " + count + "/" + totalItems + " items descargados");
                if (count < totalItems)
                {
                    handler.postDelayed(this, 500);
                }
            }
        });
    }

    private void startDownloadThread(int index, TextView tvItem) {
        if (downloadedItems[index]) return;

        new Thread(() -> {
            runOnUiThread(() -> tvItem.setText("Item " + (index+1) + ": Descargando..."));
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            downloadedItems[index] = true;
            runOnUiThread(() -> tvItem.setText("Item " + (index+1) + ": Descargado"));
            runOnUiThread(() -> tvMensaje.setText(("Gorra")));
        }).start();
    }

    private void startDownloadExecutor(int index, TextView tvItem) {
        runOnUiThread(() -> tvItem.setText("Item " + (index+1) + ": Descargando..."));
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        downloadedItems[index] = true;
        runOnUiThread(() -> tvItem.setText("Item " + (index+1) + ": Descargado"));

        boolean allDone = true;
        for (boolean b : downloadedItems) if (!b) allDone = false;
        if (allDone) runOnUiThread(() -> tvProgress.setText("Todos los items descargados!"));
    }
}
