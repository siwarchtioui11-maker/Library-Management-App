package com.example.publiclibrary;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class Login extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        EditText email = findViewById(R.id.txtloginemail);
        EditText password = findViewById(R.id.txtloginpassword);
        Button btnLogin = findViewById(R.id.btnlogin);

        btnLogin.setOnClickListener(v -> {
            String Email = email.getText().toString().trim();
            String Password = password.getText().toString().trim();

            if (Email.isEmpty() || Password.isEmpty()) {
                Toast.makeText(this, "Enter email & password", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.signInWithEmailAndPassword(Email, Password)
                    .addOnSuccessListener(authResult -> {
                        String uid = auth.getCurrentUser().getUid();

                        db.getReference("Users")
                                .child(uid)
                                .get()
                                .addOnSuccessListener(snapshot -> {

                                    String role = snapshot.child("role").getValue(String.class);

                                    if (role == null) {
                                        Toast.makeText(this, "Role missing", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    if (role.equals("Admin")) {
                                        startActivity(new Intent(Login.this, AdminHome.class));
                                    } else {
                                        startActivity(new Intent(Login.this, Home.class));
                                    }
                                    finish();
                                });

                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                    });
        });

        TextView btnSwapRegister = findViewById(R.id.btnswapregister);
        btnSwapRegister.setOnClickListener(v ->
                startActivity(new Intent(Login.this, Register.class)));
    }
}
