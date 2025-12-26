package com.example.publiclibrary;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.MapController;
import org.osmdroid.api.IMapController;

public class Home extends AppCompatActivity {

    private MapView map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialisation de OSMDroid (obligatoire)
        Configuration.getInstance().setUserAgentValue(getPackageName());

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        // --- Initialisation des boutons ---
        Button btnbook = findViewById(R.id.btnbook);
        btnbook.setOnClickListener(v -> startActivity(new Intent(Home.this, Book.class)));

        Button lend = findViewById(R.id.btnlendbook);
        lend.setOnClickListener(v -> startActivity(new Intent(Home.this, ReservationActivity.class)));

        Button returnbook = findViewById(R.id.btnreturn);
        returnbook.setOnClickListener(v -> startActivity(new Intent(Home.this, ViewReservedBook.class)));

        Button home = findViewById(R.id.btnhome);
        home.setOnClickListener(v -> startActivity(getIntent())); // refresh

        Button search = findViewById(R.id.btnsearch);
        search.setOnClickListener(v -> startActivity(getIntent())); // placeholder

        Button notification = findViewById(R.id.btnnotification);
        notification.setOnClickListener(v ->
                Toast.makeText(this, "Notifications clicked", Toast.LENGTH_SHORT).show()
        );

        Button store = findViewById(R.id.btnstore);
        store.setOnClickListener(v -> startActivity(new Intent(Home.this, ViewBook.class)));





        // --- Gestion des marges pour les barres système ---
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
