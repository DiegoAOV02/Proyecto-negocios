package com.papeleria;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class VerVentas extends Activity {
    TextView txtFechaActual, txtVentasPorFecha, txtPrueba;
    Button btnDia, btnImprimir;

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
        txtVentasPorFecha = findViewById(R.id.txtVentasPorFecha);  // TextEdit para mostrar las ventas en un día seleccionado
        txtPrueba = findViewById(R.id.txtPrueba);  // TextEdit que muestra todas las ventas realizadas (Borrar al concluir proyecto).
        btnDia = findViewById(R.id.btnDia);
        btnImprimir = findViewById(R.id.btnImprimir);
    }

    // Mostrar todas las ventas efectuadas
    private void mostrarTodasLasVentas() {}
}
