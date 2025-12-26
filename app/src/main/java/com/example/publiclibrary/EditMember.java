package com.example.publiclibrary;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditMember extends AppCompatActivity {

    private EditText memberNameEdit, memberEmailEdit, memberPhoneEdit, memberAddressEdit;
    private TextView memberIdView;
    private String memberKey;

    private DatabaseReference membersRef;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_member);

        // 🔹 Firebase reference
        membersRef = FirebaseDatabase.getInstance().getReference("Members");

        // 🔹 Views
        memberIdView = findViewById(R.id.txtmemberid);
        memberNameEdit = findViewById(R.id.txtupdatemembername);
        memberEmailEdit = findViewById(R.id.txtupdatememberemail);
        memberPhoneEdit = findViewById(R.id.txtupdatememberphone);
        memberAddressEdit = findViewById(R.id.txtupdatememberaddress);
        progressBar = findViewById(R.id.progressBar);

        Button btnUpdate = findViewById(R.id.btnupdatemember);
        Button btnDelete = findViewById(R.id.btndeletemember);

        // 🔹 Récupérer la clé du membre depuis l'intent
        memberKey = getIntent().getStringExtra("memberKey");
        if (memberKey != null) {
            loadMemberDetails(memberKey);
        } else {
            Toast.makeText(this, "Member key not found", Toast.LENGTH_SHORT).show();
        }

        btnUpdate.setOnClickListener(v -> updateMember());
        btnDelete.setOnClickListener(v -> deleteMember());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadMemberDetails(String key) {
        progressBar.setVisibility(View.VISIBLE); // afficher immédiatement

        // Récupération Firebase
        membersRef.child(key).get().addOnCompleteListener(task -> {
            // On veut que le ProgressBar reste au moins 1500 ms
            new android.os.Handler().postDelayed(() -> {
                progressBar.setVisibility(View.GONE);

                if (task.isSuccessful() && task.getResult().exists()) {
                    memberIdView.setText(key); // ou l'ID réel si différent
                    memberNameEdit.setText(task.getResult().child("name").getValue(String.class));
                    memberEmailEdit.setText(task.getResult().child("email").getValue(String.class));
                    memberPhoneEdit.setText(task.getResult().child("phone").getValue(String.class));
                    memberAddressEdit.setText(task.getResult().child("address").getValue(String.class));
                } else {
                    Toast.makeText(this, "Failed to load member details", Toast.LENGTH_SHORT).show();
                }
            }, 1500); // 1500 ms
        });
    }


    private void updateMember() {
        String name = memberNameEdit.getText().toString().trim();
        String email = memberEmailEdit.getText().toString().trim();
        String phone = memberPhoneEdit.getText().toString().trim();
        String address = memberAddressEdit.getText().toString().trim();

        if (memberKey == null) {
            Toast.makeText(this, "Member key not found", Toast.LENGTH_SHORT).show();
            return;
        }
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // 🔹 Mettre à jour tous les champs en même temps
        membersRef.child(memberKey).child("name").setValue(name);
        membersRef.child(memberKey).child("email").setValue(email);
        membersRef.child(memberKey).child("phone").setValue(phone);
        membersRef.child(memberKey).child("address").setValue(address)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Member updated successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
                    Log.e("EditMember", "Update failed", e);
                });
    }

    private void deleteMember() {
        if (memberKey == null) {
            Toast.makeText(this, "Member key not found", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        membersRef.child(memberKey).removeValue()
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Member deleted successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
                    Log.e("EditMember", "Delete failed", e);
                });
    }
}
