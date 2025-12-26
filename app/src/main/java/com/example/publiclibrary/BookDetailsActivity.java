package com.example.publiclibrary;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class BookDetailsActivity extends AppCompatActivity {

    private String bookName;
    private String bookAuthor;
    private String bookPublisher;
    private String member;

    private DatabaseReference booksRef;
    private DatabaseReference reserveRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        // 🔹 Références Firebase
        booksRef = FirebaseDatabase.getInstance().getReference("books"); // si tu as une table 'books'
        reserveRef = FirebaseDatabase.getInstance().getReference("book_reserve"); // table réservations

        // 🔹 Récupération des données de l'intent
        bookName = getIntent().getStringExtra("bookName");
        bookAuthor = getIntent().getStringExtra("bookAuthor");
        bookPublisher = getIntent().getStringExtra("bookPublisher");
        member = getIntent().getStringExtra("member");

        // 🔹 Affichage
        TextView txtBookName = findViewById(R.id.txtBookName);
        TextView txtBookAuthor = findViewById(R.id.txtBookAuthor);
        TextView txtBookPublisher = findViewById(R.id.txtBookPublisher);
        TextView txtMember = findViewById(R.id.txtBookQuantity);

        txtBookName.setText(bookName);
        txtBookAuthor.setText(bookAuthor);
        txtBookPublisher.setText(bookPublisher);
        txtMember.setText(member);

        // 🔹 Bouton retourner le livre
        Button btnReturnBook = findViewById(R.id.btnReturnBook);
        btnReturnBook.setOnClickListener(v -> returnBook());
    }

    private void returnBook() {
        // 🔹 Chercher la réservation correspondante
        Query query = reserveRef.orderByChild("bookName").equalTo(bookName);
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                boolean found = false;
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    String reservedMember = snapshot.child("memberName").getValue(String.class);
                    if (reservedMember != null && reservedMember.equals(member)) {
                        // Supprimer cette réservation
                        snapshot.getRef().removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Book returned successfully!", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("ReturnActivity", "Error returning book", e);
                                    Toast.makeText(this, "Error returning book", Toast.LENGTH_SHORT).show();
                                });
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    Toast.makeText(this, "Reservation not found!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("ReturnActivity", "Error fetching reservation", task.getException());
                Toast.makeText(this, "Error fetching reservation", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
