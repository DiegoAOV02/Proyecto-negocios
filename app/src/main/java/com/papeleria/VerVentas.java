package com.papeleria;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class VerVentas extends Activity {
    TextView txtFechaActual, txtVentasPorFecha, txtSemana, txtMes, txtArticuloMasVendidoDelMes;
    Button btnDia, btnSemana, btnMes, btnReport;
    Calendar calendar;
    float yPosition = 25f;

    Canvas canvas;
    PdfDocument pdfDocument;
    PdfDocument.Page page;
    String finalesDia = "", finalesSemana = "", finalesMes = "";

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
        btnReport = findViewById(R.id.btnReport);

        calendar = Calendar.getInstance();

        // Llamar al método para mostrar las ventas al iniciar la actividad
        //mostrarVentas();

        // Botón para llamar las ventas efectuadas en un día
        btnDia.setOnClickListener(v -> abrirCalendario());

        // Botón para mostrar ventas de la semana
        btnSemana.setOnClickListener(v -> mostrarVentasPorSemana());

        // Botón para mostrar ventas del mes
        btnMes.setOnClickListener(v -> mostrarVentasPorMes());

        btnReport.setOnClickListener(v -> crearPDF());
    }

    private void iniciarNuevaPagina() {
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pdfDocument.getPages().size() + 1).create();
        page = pdfDocument.startPage(pageInfo);
        canvas = page.getCanvas();
        yPosition = 25f;
    }

    private void nuevaPaginaSiNecesaria() {
        if (yPosition >= 845 - 50) { // Espacio de margen
            pdfDocument.finishPage(page);
            iniciarNuevaPagina();
        }
    }

    private void putText(String txt, float marginLeft, float locationY) {
        if (canvas == null) {
            return;
        }
        nuevaPaginaSiNecesaria();

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(12);
        paint.setAntiAlias(true);
        canvas.drawText(txt, marginLeft, locationY, paint);
        yPosition += paint.getTextSize() + 15;
    }

    private void drawTable(@NonNull String[][] data, float startX, float startY, float cellWidth, float cellHeight) {
        nuevaPaginaSiNecesaria();

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE); // Para dibujar líneas de la tabla
        paint.setStrokeWidth(2);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(12);
        textPaint.setAntiAlias(true);

        // Dibuja filas y columnas
        int rows = data.length;
        int cols = data[0].length;

        float y = startY;

        for (int i = 0; i <= rows; i++) { // Dibujar líneas horizontales
            canvas.drawLine(startX, y, startX + cellWidth * cols, y, paint);
            y += cellHeight;
        }

        float x = startX;
        for (int i = 0; i <= cols; i++) { // Dibujar líneas verticales
            canvas.drawLine(x, startY, x, startY + cellHeight * rows, paint);
            x += cellWidth;
        }

        // Dibuja el texto en cada celda
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                String text = data[row][col];
                float textX = startX + col * cellWidth + 10; // Margen para el texto dentro de la celda
                float textY = startY + row * cellHeight + cellHeight / 2 + textPaint.getTextSize() / 2;
                canvas.drawText(text, textX, textY, textPaint);
            }
        }
        yPosition += (cellHeight * rows) + 20;
    }


    private void crearPDF() {
        // Crear el documento PDF
        pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // Tamaño A4 en puntos
        page = pdfDocument.startPage(pageInfo);
        canvas = page.getCanvas();

        // Configuración del título
        Paint titlePaint = new Paint();
        titlePaint.setColor(Color.BLACK);
        titlePaint.setTextSize(20);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titlePaint.setAntiAlias(true);

        Paint subtitlePaint = new Paint();
        subtitlePaint.setColor(Color.BLACK);
        subtitlePaint.setTextSize(15);
        subtitlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        subtitlePaint.setAntiAlias(true);

        // Posiciones y configuraciones
        float marginLeft = 10f;
        float maxWidth = 575f;

        // Dibujar el título
        canvas.drawText("Reporte de productos vendidos", marginLeft, yPosition, titlePaint);
        yPosition += titlePaint.getTextSize() + 20; // Incrementar según el tamaño del título

        canvas.drawText("Ventas del dia:", marginLeft, yPosition, subtitlePaint);
        yPosition += subtitlePaint.getTextSize() + 20; // Incrementar según el tamaño del título
        mostrarVentas();

        canvas.drawText("Ventas de la semana:", marginLeft, yPosition, subtitlePaint);
        yPosition += subtitlePaint.getTextSize() + 20; // Incrementar según el tamaño del título
        mostrarVentasPorSemana();

        canvas.drawText("Ventas del mes:", marginLeft, yPosition, subtitlePaint);
        yPosition += subtitlePaint.getTextSize() + 20; // Incrementar según el tamaño del título
        mostrarVentasPorMes();

        pdfDocument.finishPage(page);

        yPosition = 25f;

        // Guardar el PDF en la carpeta de Descargas/PDFs
        String directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/PDFs";
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs(); // Crear directorio si no existe
        }
        String filePath = directoryPath + "/reporte_productos_vendidos.pdf";
        try (OutputStream outputStream = new FileOutputStream(filePath)) {
            pdfDocument.writeTo(outputStream);
            //Toast.makeText(this, "PDF generado en: " + filePath, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error al generar PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.i("debugiando", e.getMessage());
        } finally {
            pdfDocument.close();
        }
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



        }, year, month, day);

        datePickerDialog.show();
    }

    // Mostrar las ventas efectuadas en una fecha específica guardadas en JsonHelper.java
    private void mostrarVentas() {
        // Leer las ventas guardadas en el archivo JSON
        JSONArray ventas = JsonHelper.cargarVentas(this);

        LocalDateTime hoy = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            hoy = LocalDateTime.now();
        }

        // Obtener la fecha seleccionada del TextView
        String fechaSeleccionada = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            fechaSeleccionada = (!txtFechaActual.getText().toString().isEmpty()) ? txtFechaActual.getText().toString() : hoy.getDayOfMonth()+"/"+hoy.getMonthValue()+"/"+hoy.getYear();
        }

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
                    ventasFormateadas.append("Fecha: ").append(fecha).append("\n")
                            .append("Total con IVA: $").append(total).append("\n")
                            .append("Productos:\n");

                    putText( "Fecha: " + fecha, 10f, yPosition);
                    putText( "Total con IVA: $" + total, 10f, yPosition);
                    putText( "Productos:", 10f, yPosition);

                    String[][] data = new String[productos.length() + 1][3];
                    data[0][0] = "Nombre"; data[0][1] = "Cantidad"; data[0][2] = "Precio";

                    for (int j = 0; j < productos.length(); j++) {
                        JSONObject producto = productos.getJSONObject(j);
                        data[j+1][0] = producto.getString("nombre");
                        data[j+1][1] = String.valueOf(producto.getInt("cantidad"));
                        data[j+1][2] = String.valueOf(producto.getDouble("precio"));

                        ventasFormateadas.append("  - Nombre: ").append(data[j+1][0])
                                .append(", Cantidad: ").append(data[j+1][1])
                                .append(", Precio: $").append(data[j+1][2]).append("\n");
                    }

                    drawTable( data, 10f, yPosition, 120f, 20f);
                    //yPosition += 20f * (productos.length() + 1);  // Incrementa después de la tabla

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Si no hay ventas en la fecha seleccionada
        if (ventasFormateadas.length() == 0) {
            ventasFormateadas.append("No hay ventas para la fecha seleccionada.");
            putText( "No hay ventas para la fecha seleccionada.", 10f, yPosition);
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
            putText( "No hay ventas para la semana seleccionada.", 10f, yPosition);
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
            putText( "No hay ventas para el mes seleccionado.", 10f, yPosition);
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
            putText( "Artículo más vendido del mes: " + productoMasVendido + " (Cantidad: " + maxCantidad + ")", 10f, yPosition);
        } else {
            txtArticuloMasVendidoDelMes.setText("No se vendieron artículos este mes.");
            putText( "Artículo más vendido del mes: " + productoMasVendido + " (Cantidad: " + maxCantidad + ")", 10f, yPosition);
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

        putText( "Fecha: " + fecha, 10f, yPosition);
        putText( "Total con IVA: $" + total, 10f, yPosition);
        putText( "Productos: ", 10f, yPosition);

        // Recorrer el array de productos
        String[][] data = new String[productos.length() + 1][3];
        data[0][0] = "Nombre"; data[0][1] = "Cantidad"; data[0][2] = "Precio";

        for (int j = 0; j < productos.length(); j++) {
            JSONObject producto = productos.getJSONObject(j);
            String nombre = producto.getString("nombre");
            int cantidad = producto.getInt("cantidad");
            double precio = producto.getDouble("precio");

            // Agregar información del producto al formato
            ventasFormateadas.append("  - Nombre: ").append(nombre)
                    .append(", Cantidad: ").append(cantidad)
                    .append(", Precio: $").append(precio).append("\n");

            data[j+1][0] = nombre;
            data[j+1][1] = String.valueOf(cantidad);
            data[j+1][2] = String.valueOf(precio);

        }

        drawTable( data, 10f, yPosition, 120f, 20f);

        // Separador entre ventas
        ventasFormateadas.append("\n----------------------\n");
    }

}
