package com.example.ingles_app.repository;

import android.app.DownloadManager;
import android.content.Context;
import android.util.Log;

import com.example.ingles_app.api.ApiCliente;

import java.io.Console;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Request;

public class ImageStore {
    private final File dir;

    public ImageStore(Context context) {
        dir = new File(context.getFilesDir(), "flashcards");
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    // getName() discards any route in the name
    public File fileFor(String img) {
        return new File(dir, new File(img).getName());
    }

    public boolean exists(String img) {
        File f = fileFor(img);
        return f.exists() && f.length() > 0;
    }

    //call just from backgroung thread
    public boolean download(String img) {
        if (img == null || img.isEmpty()) return false;
        if (exists(img)) return true;

        File target = fileFor(img);
        File tmp = new File(target.getPath() + ".tmp");
        Request request = new Request.Builder().url(ApiCliente.imageUrl(img)).build();

        try (okhttp3.Response response = ApiCliente.getClient().newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                Log.e("SYNC", "Imagen " + img + ": HTTP " + response.code());
                return false;
            }
            try (InputStream in = response.body().byteStream();
                 FileOutputStream out = new FileOutputStream(tmp)) {
                byte[] buffer = new byte[8192];
                int n;
                while ((n = in.read(buffer)) != -1) {
                    out.write(buffer, 0, n);
                }
            }
            return tmp.renameTo(target);
        } catch (IOException e) {
            Log.e("SYNC", "No se pudo descargar " + img + ": " + e.getMessage());
            tmp.delete();
            return false;
        }
    }

    //delete images when another student logs in on the same device
    public void clear() {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
    }
}
