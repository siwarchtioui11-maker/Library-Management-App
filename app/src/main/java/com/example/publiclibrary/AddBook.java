package com.example.publiclibrary;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class AddBook extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_book);

        //  Récupération des EditTexts et du bouton
        EditText bookName = findViewById(R.id.txtbname);
        EditText bookAuthor = findViewById(R.id.txtbauthor);
        EditText bookPublication = findViewById(R.id.txtbpublication);
        EditText bookQuantity = findViewById(R.id.txtquntity);
        EditText returnDateEdit = findViewById(R.id.edtReturnDate); // nouveau champ returnDate
        Button addBook = findViewById(R.id.btnadd);

        //  DatePicker + TimePicker pour choisir la date de retour
        returnDateEdit.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);

                        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                                (timeView, hourOfDay, minute) -> {
                                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    calendar.set(Calendar.MINUTE, minute);

                                    // Format "yyyy-MM-dd HH:mm"
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                                    returnDateEdit.setText(sdf.format(calendar.getTime()));
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                true);
                        timePickerDialog.show();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        //  Click listener pour ajouter un livre
        addBook.setOnClickListener(v -> {
            String name = bookName.getText().toString().trim();
            String author = bookAuthor.getText().toString().trim();
            String publication = bookPublication.getText().toString().trim();
            String quantityStr = bookQuantity.getText().toString().trim();
            String returnDate = returnDateEdit.getText().toString().trim();

            if (name.isEmpty() || author.isEmpty() || publication.isEmpty() || quantityStr.isEmpty() || returnDate.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int quantity;
            try {
                quantity = Integer.parseInt(quantityStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Quantity must be a number", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean available = quantity > 0;

            //  Créer la référence Firebase et enregistrer le livre
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Books");
            String bookId = dbRef.push().getKey();

            HashMap<String, Object> bookMap = new HashMap<>();
            bookMap.put("name", name);
            bookMap.put("author", author);
            bookMap.put("publication", publication);
            bookMap.put("quantity", quantity);
            bookMap.put("available", available);
            bookMap.put("returnDate", returnDate); //  ajout du champ returnDate

            dbRef.child(bookId).setValue(bookMap)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Book added successfully", Toast.LENGTH_SHORT).show();
                        bookName.setText("");
                        bookAuthor.setText("");
                        bookPublication.setText("");
                        bookQuantity.setText("");
                        returnDateEdit.setText("");
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error adding book: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        //  Gestion des marges système
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
