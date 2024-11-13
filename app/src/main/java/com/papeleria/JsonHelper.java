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
    private static final String PRODUCTOS_FILE_NAME = "productos.json";
    private static final String VENTAS_PRODUCTOS_FILE_NAME = "ventas.json";

    // Guardar productos en JSON
    public static void guardarProductos(Context context, JSONArray productos) {
        try (FileOutputStream fos = context.openFileOutput(PRODUCTOS_FILE_NAME, Context.MODE_PRIVATE)) {
            fos.write(productos.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Leer productos de JSON
    public static JSONArray cargarProductos(Context context) {
        try (FileInputStream fis = context.openFileInput(PRODUCTOS_FILE_NAME)) { // Hace un intento de leer el archivo productos.json
            byte[] data = new byte[(int) fis.available()]; 
            fis.read(data); // Lee los bytes del archivo
            String jsonString = new String(data, StandardCharsets.UTF_8); // Los convierte a String
            return new JSONArray(jsonString); // Retorna un JSONArray con los datos del archivo visibles para el usuario
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return new JSONArray(); // Retorna un array vacío si hay un error
    }

    // Guardar ventas en JSON
    public static void guardarVentas(Context context, JSONArray ventas) {
        try (FileOutputStream fos = context.openFileOutput(VENTAS_PRODUCTOS_FILE_NAME, Context.MODE_PRIVATE)) { // Intenta abrir el archivo ventas.json en modo privado (esto quiere decir que solo la app puede acceder a él)
            fos.write(ventas.toString().getBytes(StandardCharsets.UTF_8)); // Escribe las ventas en formato JSON en el archivo
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Leer ventas en JSON
    public static JSONArray cargarVentas(Context context) {
        try (FileInputStream fis = context.openFileInput(VENTAS_PRODUCTOS_FILE_NAME)) { // Intenta abrir el archivo ventas.json
            byte[] data = new byte[(int) fis.available()];
            fis.read(data); // Lee los bytes del archivo
            String jsonString = new String(data, StandardCharsets.UTF_8); // Convierte los bytes del archivo a String
            return new JSONArray(jsonString); // Retorna un JSONArray con los datos del archivo visibles para el usuario
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return new JSONArray(); // Retorna un array vacío si hay un error
    }

}
