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

public class AdminHome extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_home);

        // Gestion des marges système (ne pas toucher)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🔹 Récupération des boutons Admin
        Button btnBooks = findViewById(R.id.btnManageBooks);
        Button btnmember = findViewById(R.id.btnmember);

        // 🔹 Click listeners






        btnBooks.setOnClickListener(v ->
                startActivity(new Intent(this, ManageBooksActivity.class))
        );
        Button member = findViewById(R.id.btnmember);
        member.setOnClickListener(v -> {
            startActivity(new Intent(AdminHome.this, Member.class));
        });


    }
}
