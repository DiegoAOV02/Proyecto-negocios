package com.papeleria;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class Vender extends Activity {

    private Button btnAgregarProducto, btnSeleccionarFecha, btnVender, btnVolver;
    private TextView txtFechaSeleccionada, txtIva, txtTotalFinal;
    private TableLayout tablaProductosVendidos;
    private AutoCompleteTextView editBuscarProducto;
    private Calendar fechaSeleccionada;
    private double subtotal = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ventas);

        // Referencias a los elementos de la interfaz
        editBuscarProducto = findViewById(R.id.editBuscarProducto);
        btnAgregarProducto = findViewById(R.id.btnAgregarProducto);
        btnSeleccionarFecha = findViewById(R.id.btnSeleccionarFecha);
        txtFechaSeleccionada = findViewById(R.id.txtFechaSeleccionada);
        txtIva = findViewById(R.id.txtIva);
        txtTotalFinal = findViewById(R.id.txtTotalFinal);
        tablaProductosVendidos = findViewById(R.id.tablaProductosVendidos);
        btnVolver = findViewById(R.id.btnVolver);
        btnVender = findViewById(R.id.btnVender);

        fechaSeleccionada = Calendar.getInstance();

        configurarAutoCompleteProducto();

        // Selección de fecha
        btnSeleccionarFecha.setOnClickListener(v -> mostrarDialogoFecha());

        btnVolver.setOnClickListener(v -> finish());

        btnVender.setOnClickListener(v -> {
            if (tablaProductosVendidos.getChildCount() > 1) {
                registrarVenta(); // Guardar venta en JSON
            } else {
                Toast.makeText(Vender.this, "No hay productos para vender", Toast.LENGTH_SHORT).show();
            }
        });

        btnAgregarProducto.setOnClickListener(v -> agregarProducto());
    }

    private void configurarAutoCompleteProducto() {
        // Cargar los productos desde el archivo JSON
        JSONArray productosJsonArray = JsonHelper.cargarProductos(this);
        ArrayList<String> listaProductos = new ArrayList<>();

        // Extraer los nombres de los productos del JSON
        for (int i = 0; i < productosJsonArray.length(); i++) {
            try {
                JSONObject producto = productosJsonArray.getJSONObject(i);
                String nombreProducto = producto.getString("nombre");  // Ajusta según la estructura de tu JSON
                listaProductos.add(nombreProducto);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Configurar el adaptador con los nombres de los productos
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                listaProductos
        );
        editBuscarProducto.setAdapter(adapter);
        editBuscarProducto.setThreshold(1); // Autocompletar a partir de 1 carácter
    }

    private void mostrarDialogoFecha() {
        int year = fechaSeleccionada.get(Calendar.YEAR);
        int month = fechaSeleccionada.get(Calendar.MONTH);
        int day = fechaSeleccionada.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            fechaSeleccionada.set(year1, month1, dayOfMonth);
            String fechaFormateada = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
            txtFechaSeleccionada.setText(fechaFormateada);
        }, year, month, day);
        datePickerDialog.show();
    }

    private void agregarProducto() {
        String nombreProducto = editBuscarProducto.getText().toString().trim();
        if (nombreProducto.isEmpty()) {
            Toast.makeText(this, "Por favor, ingrese el nombre del producto", Toast.LENGTH_SHORT).show();
            return;
        }

        double precioUnitario = obtenerPrecioProducto(nombreProducto); // Debes tener este método para obtener precios

        TableRow row = new TableRow(this);
        TextView nombreTextView = new TextView(this);
        nombreTextView.setText(nombreProducto);
        nombreTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
        row.addView(nombreTextView);

        TextView precioTextView = new TextView(this);
        precioTextView.setText(NumberFormat.getCurrencyInstance().format(precioUnitario));
        precioTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
        row.addView(precioTextView);

        EditText cantidadEditText = new EditText(this);
        cantidadEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        cantidadEditText.setText("1");
        cantidadEditText.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
        row.addView(cantidadEditText);

        TextView totalTextView = new TextView(this);
        totalTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
        totalTextView.setText(NumberFormat.getCurrencyInstance().format(precioUnitario));
        row.addView(totalTextView);

        cantidadEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int cantidad = Integer.parseInt(s.toString());
                    double total = cantidad * precioUnitario;
                    totalTextView.setText(NumberFormat.getCurrencyInstance().format(total));
                    actualizarTotal();
                } catch (NumberFormatException e) {
                    totalTextView.setText(NumberFormat.getCurrencyInstance().format(precioUnitario));
                }
            }
        });

        tablaProductosVendidos.addView(row);
        actualizarTotal();
    }

    private void registrarVenta() {
        String fecha = txtFechaSeleccionada.getText().toString();
        if (fecha.equals("Fecha no seleccionada")) {
            Toast.makeText(this, "Por favor, seleccione una fecha", Toast.LENGTH_SHORT).show();
            return;
        }

        double totalFinal = Double.parseDouble(txtTotalFinal.getText().toString().replace("$", "").replace(",", ""));

        // Crear un objeto JSON para la venta
        JSONObject venta = new JSONObject();
        try {
            venta.put("fecha", fecha);
            venta.put("total", totalFinal);

            JSONArray productosVendidos = new JSONArray();
            for (int i = 1; i < tablaProductosVendidos.getChildCount(); i++) {
                TableRow row = (TableRow) tablaProductosVendidos.getChildAt(i);
                TextView nombreTextView = (TextView) row.getChildAt(0);
                EditText cantidadEditText = (EditText) row.getChildAt(2);
                TextView totalTextView = (TextView) row.getChildAt(3);

                JSONObject producto = new JSONObject();
                producto.put("nombre", nombreTextView.getText().toString());
                producto.put("cantidad", Integer.parseInt(cantidadEditText.getText().toString()));
                producto.put("precio", Double.parseDouble(totalTextView.getText().toString().replace("$", "").replace(",", "")));

                productosVendidos.put(producto);
            }

            venta.put("productos", productosVendidos);

            // Guardar la venta en JSON
            JSONArray ventas = JsonHelper.cargarVentas(this);
            ventas.put(venta);
            JsonHelper.guardarVentas(this, ventas);

            Toast.makeText(this, "Venta registrada exitosamente", Toast.LENGTH_SHORT).show();
            finish();  // Cerrar la actividad
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void actualizarTotal() {
        subtotal = 0.0;

        for (int i = 1; i < tablaProductosVendidos.getChildCount(); i++) {
            TableRow row = (TableRow) tablaProductosVendidos.getChildAt(i);
            TextView totalTextView = (TextView) row.getChildAt(3);
            subtotal += Double.parseDouble(totalTextView.getText().toString().replace("$", "").replace(",", ""));
        }

        double iva = subtotal * 0.16;
        double totalFinal = subtotal + iva;

        txtIva.setText(String.format(Locale.getDefault(), "$%.2f", iva));
        txtTotalFinal.setText(String.format(Locale.getDefault(), "$%.2f", totalFinal));
    }

    // Métodos auxiliares para obtener productos de una base de datos simulada
    private ArrayList<String> obtenerNombresProductos() {
        // Simula la obtención de productos, aquí puedes conectarlo con JSON si lo necesitas
        ArrayList<String> productos = new ArrayList<>();
        productos.add("Producto A");
        productos.add("Producto B");
        return productos;
    }

    private double obtenerPrecioProducto(String nombre) {
        JSONArray productosJsonArray = JsonHelper.cargarProductos(this);
        for (int i = 0; i < productosJsonArray.length(); i++) {
            try {
                JSONObject producto = productosJsonArray.getJSONObject(i);
                if (producto.getString("nombre").equals(nombre)) {
                    return producto.getDouble("precio");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return 0.0; // Retorna 0 si no encuentra el producto
    }
}
