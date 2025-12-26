package com.example.publiclibrary;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Register extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        // Déclaration des views
        EditText txtName = findViewById(R.id.txtname);
        EditText txtEmail = findViewById(R.id.txtemail);
        EditText txtPassword = findViewById(R.id.txtpassword);
        EditText txtConfirmPassword = findViewById(R.id.txtcomfirmpassword);
        Spinner spinnerRole = findViewById(R.id.spinnerRole); // à ajouter dans ton XML
        Button btnRegister = findViewById(R.id.btnregister);
        TextView btnSwapLogin = findViewById(R.id.btnswaplogin);

        // Remplir le spinner avec les rôles
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.roles_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        // Clic sur Register
        btnRegister.setOnClickListener(v -> {
            String name = txtName.getText().toString().trim();
            String email = txtEmail.getText().toString().trim();
            String password = txtPassword.getText().toString().trim();
            String confirmPassword = txtConfirmPassword.getText().toString().trim();
            String role = spinnerRole.getSelectedItem().toString();

            if(name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()){
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if(!password.equals(confirmPassword)){
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Créer l'utilisateur Firebase
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        String uid = auth.getCurrentUser().getUid();

                        // Enregistrer les infos supplémentaires dans Firebase Realtime Database
                        HashMap<String, Object> userMap = new HashMap<>();
                        userMap.put("name", name);
                        userMap.put("email", email);
                        userMap.put("role", role);

                        db.getReference("Users").child(uid).setValue(userMap)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Register successful!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(Register.this, Login.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Database error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Register failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        });

        // Clic sur "Already have an account"
        btnSwapLogin.setOnClickListener(v ->
                startActivity(new Intent(Register.this, Login.class)));

    }
}
