package com.papeleria;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.Locale;

public class Inventario extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventario);

        cargarProductos();

        // Asignar evento al botón "AGREGAR PRODUCTO"
        Button agregarProductoButton = findViewById(R.id.btn_agregar_producto);
        agregarProductoButton.setOnClickListener(v -> showProductoDialog());

        // Asignar evento al botón "Volver"
        Button volverButton = findViewById(R.id.btn_volver);
        volverButton.setOnClickListener(v -> {
            Intent intent = new Intent(Inventario.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Ajuste para manejar el padding del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.inventario), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void showProductoDialog() {
        final Dialog dialog = new Dialog(Inventario.this);
        dialog.setContentView(R.layout.dialog_producto);

        // Referencias a los campos de texto del diálogo
        EditText editProducto = dialog.findViewById(R.id.edit_producto);
        EditText editDescripcion = dialog.findViewById(R.id.edit_descripcion);
        EditText editPrecio = dialog.findViewById(R.id.edit_precio);

        // Botón para guardar
        dialog.findViewById(R.id.btn_guardar).setOnClickListener(v -> {
            String nombre = editProducto.getText().toString();
            String descripcion = editDescripcion.getText().toString();
            String precioTexto = editPrecio.getText().toString();

            if (nombre.isEmpty() || descripcion.isEmpty() || precioTexto.isEmpty()) {
                Toast.makeText(Inventario.this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            double precio;
            try {
                precio = Double.parseDouble(precioTexto);
            } catch (NumberFormatException e) {
                Toast.makeText(Inventario.this, "El precio debe ser un número válido", Toast.LENGTH_SHORT).show();
                return;
            }

            agregarProducto(nombre, descripcion, precio);
            dialog.dismiss(); // Cerrar el diálogo
        });

        // Botón para salir
        dialog.findViewById(R.id.btn_cancelar).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void agregarProducto(String nombre, String descripcion, double precio) {
        JSONArray productos = JsonHelper.cargarProductos(this);

        // Crear un nuevo objeto JSON para el producto
        JSONObject nuevoProducto = new JSONObject();
        try {
            nuevoProducto.put("id", productos.length() + 1); // Generar un ID simple
            nuevoProducto.put("nombre", nombre);
            nuevoProducto.put("descripcion", descripcion);
            nuevoProducto.put("precio", precio);

            productos.put(nuevoProducto);

            // Guardar la lista actualizada de productos en el archivo JSON
            JsonHelper.guardarProductos(this, productos);

            // Mostrar Toast con los datos del JSON
            Toast.makeText(this, "Producto registrado correctamente. JSON guardado: " + productos.toString(), Toast.LENGTH_LONG).show();

            cargarProductos(); // Actualizar la lista de productos
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("Range")
    private void cargarProductos() {
        JSONArray productos = JsonHelper.cargarProductos(this);
        TableLayout tableLayout = findViewById(R.id.table_productos);
        tableLayout.removeAllViews();

        NumberFormat formatoActual = NumberFormat.getCurrencyInstance(Locale.getDefault());

        // Recorrer los productos y agregarlos a la tabla
        for (int i = 0; i < productos.length(); i++) {
            try {
                JSONObject producto = productos.getJSONObject(i);
                int idProducto = producto.getInt("id");
                String nombreProducto = producto.getString("nombre");
                String descripcionProducto = producto.getString("descripcion");
                double precioProducto = producto.getDouble("precio");

                TableRow row = new TableRow(this);

                TextView nombre = new TextView(this);
                nombre.setText(nombreProducto);
                nombre.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));

                TextView descripcion = new TextView(this);
                descripcion.setText(descripcionProducto);
                descripcion.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2));

                TextView precio = new TextView(this);
                precio.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
                precio.setText(formatoActual.format(precioProducto));

                // Botón Editar
                ImageButton btn_editar = new ImageButton(this);
                btn_editar.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
                btn_editar.setImageResource(R.drawable.playlist_edit);
                btn_editar.setBackgroundColor(ContextCompat.getColor(this, R.color.celeste));
                btn_editar.setOnClickListener(v -> showEditarProductoDialog(idProducto, nombreProducto, descripcionProducto, precioProducto));

                // Botón Eliminar
                ImageButton btn_eliminar = new ImageButton(this);
                btn_eliminar.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
                btn_eliminar.setImageResource(R.drawable.delete);
                btn_eliminar.setBackgroundColor(ContextCompat.getColor(this, R.color.celeste));
                btn_eliminar.setOnClickListener(v -> eliminarProducto(idProducto));

                row.addView(nombre);
                row.addView(descripcion);
                row.addView(precio);
                row.addView(btn_editar);
                row.addView(btn_eliminar);

                tableLayout.addView(row);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void eliminarProducto(int idProducto) {
        JSONArray productos = JsonHelper.cargarProductos(this);
        JSONArray productosActualizados = new JSONArray();

        for (int i = 0; i < productos.length(); i++) {
            try {
                JSONObject producto = productos.getJSONObject(i);
                if (producto.getInt("id") != idProducto) {
                    productosActualizados.put(producto);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        JsonHelper.guardarProductos(this, productosActualizados);

        // Mostrar Toast con los datos del JSON
        Toast.makeText(this, "Producto eliminado correctamente. JSON actualizado: " + productosActualizados.toString(), Toast.LENGTH_LONG).show();

        cargarProductos(); // Actualizar la lista de productos
    }

    private void showEditarProductoDialog(int idProducto, String nombreActual, String descripcionActual, double precioActual) {
        final Dialog dialog = new Dialog(Inventario.this);
        dialog.setContentView(R.layout.dialog_producto);

        EditText editProducto = dialog.findViewById(R.id.edit_producto);
        EditText editDescripcion = dialog.findViewById(R.id.edit_descripcion);
        EditText editPrecio = dialog.findViewById(R.id.edit_precio);

        editProducto.setText(nombreActual);
        editDescripcion.setText(descripcionActual);
        editPrecio.setText(String.valueOf(precioActual));

        dialog.findViewById(R.id.btn_guardar).setOnClickListener(v -> {
            String nuevoNombre = editProducto.getText().toString();
            String nuevaDescripcion = editDescripcion.getText().toString();
            String nuevoPrecioTexto = editPrecio.getText().toString();

            if (nuevoNombre.isEmpty() || nuevaDescripcion.isEmpty() || nuevoPrecioTexto.isEmpty()) {
                Toast.makeText(Inventario.this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            double nuevoPrecio;
            try {
                nuevoPrecio = Double.parseDouble(nuevoPrecioTexto);
            } catch (NumberFormatException e) {
                Toast.makeText(Inventario.this, "El precio debe ser un número válido", Toast.LENGTH_SHORT).show();
                return;
            }

            editarProducto(idProducto, nuevoNombre, nuevaDescripcion, nuevoPrecio);
            dialog.dismiss();
        });

        dialog.findViewById(R.id.btn_cancelar).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void editarProducto(int idProducto, String nuevoNombre, String nuevaDescripcion, double nuevoPrecio) {
        JSONArray productos = JsonHelper.cargarProductos(this);

        for (int i = 0; i < productos.length(); i++) {
            try {
                JSONObject producto = productos.getJSONObject(i);
                if (producto.getInt("id") == idProducto) {
                    producto.put("nombre", nuevoNombre);
                    producto.put("descripcion", nuevaDescripcion);
                    producto.put("precio", nuevoPrecio);
                    break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        JsonHelper.guardarProductos(this, productos);

        // Mostrar Toast con los datos del JSON
        Toast.makeText(this, "Producto modificado correctamente. JSON actualizado: " + productos.toString(), Toast.LENGTH_LONG).show();

        cargarProductos();
    }
}
