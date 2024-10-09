package com.papeleria;

import android.content.Context;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class JsonHelper {
    private static final String FILE_NAME = "productos.json";
    private static final String VENTAS_FILE_NAME = "ventas.json";

    // Guardar productos en JSON
    public static void guardarProductos(Context context, JSONArray productos) {
        try (FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)) {
            fos.write(productos.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Leer productos de JSON
    public static JSONArray cargarProductos(Context context) {
        try (FileInputStream fis = context.openFileInput(FILE_NAME)) {
            byte[] data = new byte[(int) fis.available()];
            fis.read(data);
            String jsonString = new String(data, StandardCharsets.UTF_8);
            return new JSONArray(jsonString);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return new JSONArray(); // Retorna un array vacío si hay un error
    }

    // Guardar ventas en JSON
    public static void guardarVentas(Context context, JSONArray ventas) {
        try (FileOutputStream fos = context.openFileOutput(VENTAS_FILE_NAME, Context.MODE_PRIVATE)) {
            fos.write(ventas.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Leer ventas en JSON
    public static JSONArray cargarVentas(Context context) {
        try (FileInputStream fis = context.openFileInput(VENTAS_FILE_NAME)) {
            byte[] data = new byte[(int) fis.available()];
            fis.read(data);
            String jsonString = new String(data, StandardCharsets.UTF_8);
            return new JSONArray(jsonString);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return new JSONArray(); // Retorna un array vacío si hay un error
    }

}
