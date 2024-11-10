package com.papeleria;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class VerVentas extends Activity {
    TextView txtFechaActual, txtVentasPorFecha, txtSemana, txtMes, txtArticuloMasVendidoDelMes;
    Button btnDia, btnSemana, btnMes;
    Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_ventas);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ver_ventas), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtFechaActual = findViewById(R.id.txtFechaActual);
        txtSemana = findViewById(R.id.txtSemana);
        txtMes = findViewById(R.id.txtMes);
        txtArticuloMasVendidoDelMes = findViewById(R.id.txtArticuloMasVendidoDelMes);
        txtVentasPorFecha = findViewById(R.id.txtVentasPorFecha);
        btnDia = findViewById(R.id.btnDia);
        btnSemana = findViewById(R.id.btnSemana);
        btnMes = findViewById(R.id.btnMes);

        calendar = Calendar.getInstance();

        // Llamar al método para mostrar las ventas al iniciar la actividad
        mostrarVentas();

        // Botón para llamar las ventas efectuadas en un día
        btnDia.setOnClickListener(v -> abrirCalendario());

        // Botón para mostrar ventas de la semana
        btnSemana.setOnClickListener(v -> mostrarVentasPorSemana());

        // Botón para mostrar ventas del mes
        btnMes.setOnClickListener(v -> mostrarVentasPorMes());
    }


    // Abrir diálogo de calendario
    private void abrirCalendario() {
        // Obtener la fecha actual del calendario
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Mostrar el DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(VerVentas.this, (view, yearSelected, monthSelected, daySelected) -> {
            // Cuando el usuario selecciona una fecha, se actualiza el calendario
            calendar.set(Calendar.YEAR, yearSelected);
            calendar.set(Calendar.MONTH, monthSelected);
            calendar.set(Calendar.DAY_OF_MONTH, daySelected);

            // Formatear la fecha seleccionada al formato d/M/yyyy
            SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
            String fechaSeleccionada = sdf.format(calendar.getTime());

            // Guardar la fecha seleccionada en el TextView
            txtFechaActual.setText(fechaSeleccionada);

            // Mostrar las ventas filtradas por la fecha seleccionada
            mostrarVentas();

        }, year, month, day);

        datePickerDialog.show();
    }

    // Mostrar las ventas efectuadas en una fecha específica guardadas en JsonHelper.java
    private void mostrarVentas() {
        // Leer las ventas guardadas en el archivo JSON
        JSONArray ventas = JsonHelper.cargarVentas(this);

        // Obtener la fecha seleccionada del TextView
        String fechaSeleccionada = txtFechaActual.getText().toString();

        // Crear un StringBuilder para construir el texto formateado
        StringBuilder ventasFormateadas = new StringBuilder();

        // Recorrer el JSONArray de ventas
        for (int i = 0; i < ventas.length(); i++) {
            try {
                // Obtener cada venta como un JSONObject
                JSONObject venta = ventas.getJSONObject(i);

                // Extraer la información de la venta
                String fecha = venta.getString("fecha");
                double total = venta.getDouble("total");
                JSONArray productos = venta.getJSONArray("productos");

                // Verificar si la fecha de la venta coincide con la fecha seleccionada
                if (fecha.equals(fechaSeleccionada)) {
                    // Formatear la información de la venta
                    ventasFormateadas.append("Fecha: ").append(fecha).append("\n");
                    ventasFormateadas.append("Total con IVA: $").append(total).append("\n");
                    ventasFormateadas.append("Productos:\n");

                    // Recorrer el array de productos
                    for (int j = 0; j < productos.length(); j++) {
                        JSONObject producto = productos.getJSONObject(j);
                        String nombre = producto.getString("nombre");
                        int cantidad = producto.getInt("cantidad");
                        double precio = producto.getDouble("precio");

                        // Agregar información del producto al formato
                        ventasFormateadas.append("  - Nombre: ").append(nombre)
                                .append(", Cantidad: ").append(cantidad)
                                .append(", Precio: $").append(precio).append("\n");
                    }

                    // Separador entre ventas
                    ventasFormateadas.append("\n----------------------\n");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Si no hay ventas en la fecha seleccionada
        if (ventasFormateadas.length() == 0) {
            ventasFormateadas.append("No hay ventas para la fecha seleccionada.");
        }

        // Mostrar las ventas formateadas en el TextView
        txtVentasPorFecha.setText(ventasFormateadas.toString());
    }

    // Mostrar ventas de la semana en la que cae la fecha seleccionada
    private void mostrarVentasPorSemana() {
        // Leer las ventas guardadas en el archivo JSON
        JSONArray ventas = JsonHelper.cargarVentas(this);

        // Obtener la semana del año de la fecha seleccionada
        int semanaSeleccionada = calendar.get(Calendar.WEEK_OF_YEAR);

        // Crear un StringBuilder para construir el texto formateado
        StringBuilder ventasFormateadas = new StringBuilder();

        // Recorrer el JSONArray de ventas
        for (int i = 0; i < ventas.length(); i++) {
            try {
                // Obtener cada venta como un JSONObject
                JSONObject venta = ventas.getJSONObject(i);

                // Extraer la fecha de la venta
                String fechaVenta = venta.getString("fecha");

                // Convertir la fecha de la venta a un objeto Calendar
                Calendar calVenta = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
                calVenta.setTime(sdf.parse(fechaVenta));

                // Verificar si la venta pertenece a la semana seleccionada
                int semanaVenta = calVenta.get(Calendar.WEEK_OF_YEAR);
                if (semanaVenta == semanaSeleccionada) {
                    // Formatear la información de la venta
                    formatearVenta(venta, ventasFormateadas);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Mostrar las ventas de la semana seleccionada en el TextView
        if (ventasFormateadas.length() == 0) {
            ventasFormateadas.append("No hay ventas para la semana seleccionada.");
        }

        txtSemana.setText(ventasFormateadas.toString());
    }

    // Mostrar ventas del mes en el que cae la fecha seleccionada y encontrar el artículo más vendido
    private void mostrarVentasPorMes() {
        // Leer las ventas guardadas en el archivo JSON
        JSONArray ventas = JsonHelper.cargarVentas(this);

        // Obtener el mes de la fecha seleccionada
        int mesSeleccionado = calendar.get(Calendar.MONTH);

        // Crear un StringBuilder para construir el texto formateado
        StringBuilder ventasFormateadas = new StringBuilder();

        // Crear un HashMap para contar las cantidades de cada producto vendido en el mes
        HashMap<String, Integer> productosVendidos = new HashMap<>();

        // Recorrer el JSONArray de ventas
        for (int i = 0; i < ventas.length(); i++) {
            try {
                // Obtener cada venta como un JSONObject
                JSONObject venta = ventas.getJSONObject(i);

                // Extraer la fecha de la venta
                String fechaVenta = venta.getString("fecha");

                // Convertir la fecha de la venta a un objeto Calendar
                Calendar calVenta = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
                calVenta.setTime(sdf.parse(fechaVenta));

                // Verificar si la venta pertenece al mes seleccionado
                int mesVenta = calVenta.get(Calendar.MONTH);
                if (mesVenta == mesSeleccionado) {
                    // Formatear la información de la venta
                    formatearVenta(venta, ventasFormateadas);

                    // Recorrer los productos vendidos en la venta
                    JSONArray productos = venta.getJSONArray("productos");
                    for (int j = 0; j < productos.length(); j++) {
                        JSONObject producto = productos.getJSONObject(j);
                        String nombreProducto = producto.getString("nombre");
                        int cantidadVendida = producto.getInt("cantidad");

                        // Contar la cantidad de productos vendidos
                        productosVendidos.put(nombreProducto, productosVendidos.getOrDefault(nombreProducto, 0) + cantidadVendida);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Mostrar las ventas del mes seleccionado en el TextView
        if (ventasFormateadas.length() == 0) {
            ventasFormateadas.append("No hay ventas para el mes seleccionado.");
        }
        txtMes.setText(ventasFormateadas.toString());

        // Encontrar el producto más vendido
        String productoMasVendido = null;
        int maxCantidad = 0;
        for (Map.Entry<String, Integer> entry : productosVendidos.entrySet()) {
            if (entry.getValue() > maxCantidad) {
                maxCantidad = entry.getValue();
                productoMasVendido = entry.getKey();
            }
        }

        // Mostrar el producto más vendido en el TextView específico
        if (productoMasVendido != null) {
            txtArticuloMasVendidoDelMes.setText("Artículo más vendido del mes: " + productoMasVendido + " (Cantidad: " + maxCantidad + ")");
        } else {
            txtArticuloMasVendidoDelMes.setText("No se vendieron artículos este mes.");
        }
    }


    private void formatearVenta(JSONObject venta, StringBuilder ventasFormateadas) throws JSONException {
        // Extraer la información de la venta
        String fecha = venta.getString("fecha");
        double total = venta.getDouble("total");
        JSONArray productos = venta.getJSONArray("productos");

        // Formatear la información de la venta
        ventasFormateadas.append("Fecha: ").append(fecha).append("\n");
        ventasFormateadas.append("Total con IVA: $").append(total).append("\n");
        ventasFormateadas.append("Productos:\n");

        // Recorrer el array de productos
        for (int j = 0; j < productos.length(); j++) {
            JSONObject producto = productos.getJSONObject(j);
            String nombre = producto.getString("nombre");
            int cantidad = producto.getInt("cantidad");
            double precio = producto.getDouble("precio");

            // Agregar información del producto al formato
            ventasFormateadas.append("  - Nombre: ").append(nombre)
                    .append(", Cantidad: ").append(cantidad)
                    .append(", Precio: $").append(precio).append("\n");
        }

        // Separador entre ventas
        ventasFormateadas.append("\n----------------------\n");
    }

}
