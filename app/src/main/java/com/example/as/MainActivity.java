package com.example.as;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.as.adapter.FileAdapter;
import com.example.as.model.FileItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton btnDownloadAll;
    private TextView tvGlobalStatus;
    private FileAdapter adapter;
    private List<FileItem> fileList;

    // --- Concurrencia ---
    private ExecutorService executorService;
    private Handler progressHandler;
    private AtomicInteger completedDownloads = new AtomicInteger(0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar vistas
        recyclerView = findViewById(R.id.recyclerView);
        btnDownloadAll = findViewById(R.id.btnDownloadAll);
        tvGlobalStatus = findViewById(R.id.tvGlobalStatus);

        // Preparar datos y RecyclerView
        setupRecyclerView();

        // ---------------------------------
        // ⚙️ TÉCNICA 2: EXECUTOR SERVICE
        // ---------------------------------
        // Creamos un pool de 3 hilos. Descargará 3 archivos a la vez como máximo.
        executorService = Executors.newFixedThreadPool(3);

        btnDownloadAll.setOnClickListener(v -> downloadAllFiles());

        // ---------------------------------
        // ⚙️ TÉCNICA 3: HANDLER
        // ---------------------------------
        // Handler para actualizar el estado global de las descargas.
        progressHandler = new Handler(Looper.getMainLooper());
        startGlobalProgressChecker();
    }

    private void setupRecyclerView() {
        fileList = new ArrayList<>();
        // Datos de ejemplo
        fileList.add(new FileItem("Reporte_Anual.pdf", "5.2 MB"));
        fileList.add(new FileItem("Presentacion_Q3.pptx", "12.8 MB"));
        fileList.add(new FileItem("Dataset_Ventas.csv", "8.1 MB"));
        fileList.add(new FileItem("Logo_Empresa.zip", "1.5 MB"));
        fileList.add(new FileItem("Video_Tutorial.mp4", "55.0 MB"));
        fileList.add(new FileItem("Backup_Codigo.rar", "23.4 MB"));

        adapter = new FileAdapter(fileList, position -> {
            // ---------------------------------
            // ⚙️ TÉCNICA 1: THREAD TRADICIONAL
            // ---------------------------------
            // Al hacer clic en el ícono de descarga, se inicia un nuevo hilo.
            FileItem item = fileList.get(position);
            if (item.getStatus() == FileItem.DownloadStatus.NOT_STARTED) {
                startDownload(item, position);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    // Método para simular una descarga. Usado por el Thread y el Executor.
    private void simulateDownload(FileItem item, int position) {
        item.setStatus(FileItem.DownloadStatus.DOWNLOADING);

        // Actualizar UI para mostrar que la descarga ha comenzado
        runOnUiThread(() -> adapter.notifyItemChanged(position));

        try {
            for (int i = 0; i <= 100; i++) {
                item.setProgress(i);
                // Actualizamos la UI en el hilo principal
                runOnUiThread(() -> adapter.notifyItemChanged(position));
                Thread.sleep(50); // Simula el tiempo de descarga
            }
            item.setStatus(FileItem.DownloadStatus.COMPLETED);
            completedDownloads.incrementAndGet(); // Hilo seguro

            // Actualizar UI para mostrar que la descarga ha finalizado
            runOnUiThread(() -> adapter.notifyItemChanged(position));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Método que inicia la descarga para la TÉCNICA 1
    private void startDownload(FileItem item, int position) {
        new Thread(() -> simulateDownload(item, position)).start();
    }

    // Método que inicia la descarga para la TÉCNICA 2
    private void downloadAllFiles() {
        for (int i = 0; i < fileList.size(); i++) {
            FileItem item = fileList.get(i);
            if (item.getStatus() == FileItem.DownloadStatus.NOT_STARTED) {
                final int position = i;
                // Enviamos la tarea al ExecutorService
                executorService.submit(() -> simulateDownload(item, position));
            }
        }
    }

    // Runnable para la TÉCNICA 3
    private final Runnable globalProgressChecker = new Runnable() {
        @Override
        public void run() {
            int completed = completedDownloads.get();
            int total = fileList.size();
            tvGlobalStatus.setText(String.format("Descargas: %d/%d", completed, total));

            // Si no se han completado todas, vuelve a ejecutar esto en 1 segundo
            if (completed < total) {
                progressHandler.postDelayed(this, 1000);
            }
        }
    };

    private void startGlobalProgressChecker() {
        // Inicia el chequeo periódico
        progressHandler.post(globalProgressChecker);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Es MUY importante limpiar los recursos para evitar memory leaks
        executorService.shutdownNow(); // Detiene todas las tareas del executor
        progressHandler.removeCallbacks(globalProgressChecker); // Detiene el handler
    }
}