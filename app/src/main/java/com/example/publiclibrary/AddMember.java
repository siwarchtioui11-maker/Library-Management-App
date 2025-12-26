package com.example.publiclibrary;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class AddMember extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_member);

        EditText memberName = findViewById(R.id.txtmname);
        EditText memberEmail = findViewById(R.id.txtmemail);
        EditText memberPhone = findViewById(R.id.txtmphone);
        EditText memberAddress = findViewById(R.id.txtmaddress);
        Button addMember = findViewById(R.id.btnaddmember);

        //  Bouton ajouter un membre
        addMember.setOnClickListener(v -> {
            String name = memberName.getText().toString().trim();
            String email = memberEmail.getText().toString().trim();
            String phone = memberPhone.getText().toString().trim();
            String address = memberAddress.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            //  Création d’un HashMap pour le membre
            HashMap<String, Object> memberMap = new HashMap<>();
            memberMap.put("name", name);
            memberMap.put("email", email);
            memberMap.put("phone", phone);
            memberMap.put("address", address);

            //  Référence Firebase
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Members");
            String memberId = dbRef.push().getKey();
            dbRef.child(memberId).setValue(memberMap)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Member added successfully", Toast.LENGTH_SHORT).show();
                        memberName.setText("");
                        memberEmail.setText("");
                        memberPhone.setText("");
                        memberAddress.setText("");
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error adding member: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        //  Gestion marges système
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
