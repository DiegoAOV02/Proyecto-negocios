package com.papeleria;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    Button BT1, BT2, BT3; // BT1: Vender Productos, BT2: Inventario, BT3: Ventas.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /**
         * Botones que dirigen a una nueva actividad.
         */

        // Botón que dirige a la actividad de "Inventario".
        BT1 = findViewById(R.id.btn_vender);
        BT1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent I = new Intent(MainActivity.this, VerVentas.class);
                startActivity(I);
            }
        });

        // Botón que dirige a la actividad de "Vender".
        BT2 = findViewById(R.id.btn_ver_inventario);
        BT2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent I2 = new Intent(MainActivity.this, Inventario.class);
                startActivity(I2);
            }
        });

        // Botón que dirige a la actividad de "Ver Ventas".
        BT3 = findViewById(R.id.btn_ventas);
        BT3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent I3 = new Intent(MainActivity.this, Vender.class);
                startActivity(I3);
            }
        });
    }
}