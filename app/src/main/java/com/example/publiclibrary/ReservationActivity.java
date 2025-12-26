package com.example.publiclibrary;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ReservationActivity extends AppCompatActivity {

    private AutoCompleteTextView searchBookAutoComplete;
    private TextView bookDetailsTextView;
    private Spinner memberSpinner;
    private Button reserveButton;

    private ArrayAdapter<String> memberAdapter;
    private ArrayAdapter<String> bookAdapter;

    private DatabaseReference booksRef;
    private DatabaseReference membersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        searchBookAutoComplete = findViewById(R.id.searchBookAutoComplete);
        bookDetailsTextView = findViewById(R.id.bookDetailsTextView);
        memberSpinner = findViewById(R.id.memberSpinner);
        reserveButton = findViewById(R.id.reserveButton);

        // 🔹 Firebase references
        booksRef = FirebaseDatabase.getInstance().getReference("Books");
        membersRef = FirebaseDatabase.getInstance().getReference("Members");

        // Load member names into spinner from Firebase
        loadMemberNamesFromFirebase();

        // Set up reserve button click listener
        reserveButton.setOnClickListener(v -> reserveBook());

        // Set up AutoCompleteTextView for book search
        setupBookAutoComplete();
    }

    private void setupBookAutoComplete() {
        List<String> bookNames = new ArrayList<>();
        bookAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, bookNames);
        searchBookAutoComplete.setAdapter(bookAdapter);
        searchBookAutoComplete.setThreshold(1);

        searchBookAutoComplete.setOnItemClickListener((parent, view, position, id) -> {
            String selectedBook = (String) parent.getItemAtPosition(position);
            fetchBookDetailsFromFirebase(selectedBook);
        });

        searchBookAutoComplete.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                fetchBookSuggestionsFromFirebase(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void fetchBookSuggestionsFromFirebase(String query) {
        booksRef.orderByChild("name").startAt(query).endAt(query + "\uf8ff")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<String> suggestions = new ArrayList<>();
                        for (DataSnapshot bookSnapshot : snapshot.getChildren()) {
                            String name = bookSnapshot.child("name").getValue(String.class);
                            if (name != null) suggestions.add(name);
                        }
                        bookAdapter.clear();
                        bookAdapter.addAll(suggestions);
                        bookAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(ReservationActivity.this, "Error fetching books: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchBookDetailsFromFirebase(String bookName) {
        booksRef.orderByChild("name").equalTo(bookName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot bookSnapshot : snapshot.getChildren()) {
                                String author = bookSnapshot.child("author").getValue(String.class);
                                String publisher = bookSnapshot.child("publication").getValue(String.class);
                                Long quantity = bookSnapshot.child("quantity").getValue(Long.class);

                                String details = "Book Name: " + bookName + "\n\n" +
                                        "Author: " + author + "\n\n" +
                                        "Publisher: " + publisher + "\n\n" +
                                        "Quantity: " + quantity;

                                bookDetailsTextView.setText(details);
                                break;
                            }
                        } else {
                            bookDetailsTextView.setText("");
                            Toast.makeText(ReservationActivity.this, "Book not found!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(ReservationActivity.this, "Error fetching book details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadMemberNamesFromFirebase() {
        membersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<String> memberNames = new ArrayList<>();
                for (DataSnapshot memberSnapshot : snapshot.getChildren()) {
                    String name = memberSnapshot.child("name").getValue(String.class);
                    if (name != null) memberNames.add(name);
                }
                memberAdapter = new ArrayAdapter<>(ReservationActivity.this,
                        android.R.layout.simple_spinner_item, memberNames);
                memberAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                memberSpinner.setAdapter(memberAdapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ReservationActivity.this, "Error loading members: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void reserveBook() {
        String bookName = searchBookAutoComplete.getText().toString().trim();
        String selectedMemberName = (String) memberSpinner.getSelectedItem();

        if (bookName.isEmpty() || selectedMemberName == null) {
            Toast.makeText(this, "Select book and member", Toast.LENGTH_SHORT).show();
            return;
        }

        // Find the book in Firebase
        booksRef.orderByChild("name").equalTo(bookName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot bookSnapshot : snapshot.getChildren()) {
                                String bookId = bookSnapshot.getKey();
                                Long quantity = bookSnapshot.child("quantity").getValue(Long.class);

                                if (quantity != null && quantity > 0) {
                                    // Decrease quantity
                                    bookSnapshot.getRef().child("quantity").setValue(quantity - 1);

                                    // Add reservation under "Reservations" node
                                    DatabaseReference reservationsRef = FirebaseDatabase.getInstance().getReference("Reservations");
                                    String reservationId = reservationsRef.push().getKey();
                                    reservationsRef.child(reservationId).setValue(new Reservation(bookName, selectedMemberName));

                                    Toast.makeText(ReservationActivity.this, "Book reserved successfully!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ReservationActivity.this, "Book not available!", Toast.LENGTH_SHORT).show();
                                }
                                break;
                            }
                        } else {
                            Toast.makeText(ReservationActivity.this, "Book not found!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(ReservationActivity.this, "Error reserving book: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Reservation class for Firebase
    public static class Reservation {
        public String bookName;
        public String memberName;

        public Reservation() {} // Needed for Firebase

        public Reservation(String bookName, String memberName) {
            this.bookName = bookName;
            this.memberName = memberName;
        }
    }
}
