package com.example.publiclibrary;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ViewReservedBook extends AppCompatActivity {

    private ReservedBookAdapter adapter;
    private List<ReservedCardItem> cardItemList;
    private DatabaseReference reservationsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_reserved_book);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewReservedBook);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cardItemList = new ArrayList<>();
        adapter = new ReservedBookAdapter(this, cardItemList);
        recyclerView.setAdapter(adapter);

        // Firebase
        reservationsRef = FirebaseDatabase.getInstance().getReference("Reservations");
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshAdaptersFromFirebase();
    }

    private void refreshAdaptersFromFirebase() {
        cardItemList.clear();

        reservationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                cardItemList.clear();
                if(snapshot.exists()){
                    for(DataSnapshot reservationSnapshot : snapshot.getChildren()){
                        String reservationId = reservationSnapshot.getKey();
                        String bookName = reservationSnapshot.child("bookName").getValue(String.class);
                        String memberName = reservationSnapshot.child("memberName").getValue(String.class);

                        FirebaseDatabase.getInstance().getReference("Books")
                                .orderByChild("name").equalTo(bookName)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot bookSnapshot) {
                                        String author = "";
                                        String publisher = "";
                                        for (DataSnapshot b : bookSnapshot.getChildren()) {
                                            author = b.child("author").getValue(String.class);
                                            publisher = b.child("publication").getValue(String.class);
                                            break;
                                        }
                                        cardItemList.add(new ReservedCardItem(reservationId, bookName, author, publisher, memberName));
                                        adapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError error) {
                                        Toast.makeText(ViewReservedBook.this, "Error fetching book info: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                } else {
                    Toast.makeText(ViewReservedBook.this, "No reserved books found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ViewReservedBook.this, "Error fetching reservations: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Modèle
    private static class ReservedCardItem {
        String reservationId;
        String name;
        String author;
        String publisher;
        String reservedBy;

        ReservedCardItem(String reservationId, String name, String author, String publisher, String reservedBy) {
            this.reservationId = reservationId;
            this.name = name;
            this.author = author;
            this.publisher = publisher;
            this.reservedBy = reservedBy;
        }
    }

    // Adapter
    private class ReservedBookAdapter extends RecyclerView.Adapter<ReservedBookAdapter.ViewHolder> {
        private final List<ReservedCardItem> cardItems;
        private final Context context;

        ReservedBookAdapter(Context context, List<ReservedCardItem> cardItems) {
            this.context = context;
            this.cardItems = cardItems;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.reserved_book_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ReservedCardItem cardItem = cardItems.get(position);
            holder.ViewReservedBookName.setText(cardItem.name);
            holder.ViewReservedBookAuthor.setText(cardItem.author);
            holder.ViewReservedBookPublisher.setText(cardItem.publisher);
            holder.ViewReservedBy.setText(cardItem.reservedBy);

            // Click sur la carte
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, BookDetailsActivity.class);
                intent.putExtra("bookName", cardItem.name);
                intent.putExtra("bookAuthor", cardItem.author);
                intent.putExtra("bookPublisher", cardItem.publisher);
                intent.putExtra("member", cardItem.reservedBy);
                context.startActivity(intent);
            });

            // Click sur Return
            holder.btnReturn.setOnClickListener(v -> {
                reservationsRef.child(cardItem.reservationId).removeValue()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(context, "Book returned successfully", Toast.LENGTH_SHORT).show();
                            cardItems.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, cardItems.size());
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(context, "Failed to return book: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
            });
        }

        @Override
        public int getItemCount() {
            return cardItems.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView ViewReservedBookName, ViewReservedBookAuthor, ViewReservedBookPublisher, ViewReservedBy;
            Button btnReturn;

            ViewHolder(View itemView) {
                super(itemView);
                ViewReservedBookName = itemView.findViewById(R.id.txtReservedBookName);
                ViewReservedBookAuthor = itemView.findViewById(R.id.txtReservedBookAuthor);
                ViewReservedBookPublisher = itemView.findViewById(R.id.txtReservedBookPublisher);
                ViewReservedBy = itemView.findViewById(R.id.txtReservedBy);

                // 🔹 IMPORTANT : le bouton btnReturn doit exister dans reserved_book_item.xml
                btnReturn = itemView.findViewById(R.id.btnReturn);
            }
        }
    }
}
