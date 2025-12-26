package com.example.publiclibrary;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;
import android.view.View;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditBook extends AppCompatActivity {

    private EditText bookNameEdit, bookAuthorEdit, bookPublicationEdit, bookQuantityEdit;
    private Switch availabilitySwitch;
    private TextView bookIdText;
    private Button btnUpdate, btnDelete;

    private DatabaseReference booksRef;
    private String originalBookName;
    private String bookKey; // clé Firebase
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_book);

        //  Récupérer les vues
        bookIdText = findViewById(R.id.txtbookid);
        bookNameEdit = findViewById(R.id.txtupdatebookname);
        bookAuthorEdit = findViewById(R.id.txtupdatebookauthor);
        bookPublicationEdit = findViewById(R.id.txtupdatepublication);
        bookQuantityEdit = findViewById(R.id.txtupdatequantity);
        availabilitySwitch = findViewById(R.id.switchavailability);

        btnUpdate = findViewById(R.id.btnupdate);
        btnDelete = findViewById(R.id.btndelete);

        //  Référence Firebase
        booksRef = FirebaseDatabase.getInstance().getReference("Books");


        originalBookName = getIntent().getStringExtra("bookName");
        if (originalBookName != null) {
            loadBookDetails(originalBookName);
        }


        btnDelete.setOnClickListener(v -> deleteBook());
        btnUpdate.setOnClickListener(v -> updateBook());

        progressBar = findViewById(R.id.progressBar6);


        progressBar.setVisibility(View.VISIBLE);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            booksRef.get().addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);

                if (task.isSuccessful()) {

                } else {
                    Toast.makeText(this, "Failed to load book", Toast.LENGTH_SHORT).show();
                }
            });

        }, 1500);

    }

    private void loadBookDetails(String bookName) {
        booksRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                boolean found = false;
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    String nameInDb = snapshot.child("name").getValue(String.class);
                    if (nameInDb != null && nameInDb.equalsIgnoreCase(bookName)) {
                        bookKey = snapshot.getKey(); // on récupère la clé unique
                        bookIdText.setText(bookKey);
                        bookNameEdit.setText(nameInDb);
                        bookAuthorEdit.setText(snapshot.child("author").getValue(String.class));
                        bookPublicationEdit.setText(snapshot.child("publication").getValue(String.class));
                        bookQuantityEdit.setText(String.valueOf(snapshot.child("quantity").getValue(Integer.class)));
                        Boolean available = snapshot.child("available").getValue(Boolean.class);
                        availabilitySwitch.setChecked(available != null && available);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    Toast.makeText(this, "Book not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to load book", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteBook() {
        if (bookKey == null) {
            Toast.makeText(this, "Book key not found", Toast.LENGTH_SHORT).show();
            return;
        }
        booksRef.child(bookKey).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Book deleted successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show());
    }


        private void updateBook() {

            if (bookKey == null) {
                Toast.makeText(this, "Book key not found", Toast.LENGTH_SHORT).show();
                return;
            }

            // 🔥 Afficher ProgressBar
            progressBar.setVisibility(View.VISIBLE);

            String name = bookNameEdit.getText().toString().trim();
            String author = bookAuthorEdit.getText().toString().trim();
            String publication = bookPublicationEdit.getText().toString().trim();
            String quantityStr = bookQuantityEdit.getText().toString().trim();
            int quantity = quantityStr.isEmpty() ? 0 : Integer.parseInt(quantityStr);
            boolean available = availabilitySwitch.isChecked();

            booksRef.child(bookKey).child("name").setValue(name);
            booksRef.child(bookKey).child("author").setValue(author);
            booksRef.child(bookKey).child("publication").setValue(publication);
            booksRef.child(bookKey).child("quantity").setValue(quantity);

            booksRef.child(bookKey).child("available").setValue(available)
                    .addOnSuccessListener(aVoid -> {

                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(EditBook.this,
                                    "Book updated successfully!",
                                    Toast.LENGTH_SHORT).show();
                        }, 1500);

                    })
                    .addOnFailureListener(e -> {

                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(EditBook.this,
                                    "Update failed",
                                    Toast.LENGTH_SHORT).show();
                        }, 1500);

                    });
        }

}




