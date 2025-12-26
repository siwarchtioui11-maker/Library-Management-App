package com.example.publiclibrary;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ViewBook extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BookAdapter adapter;
    private List<CardItem> cardItemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_book);

        recyclerView = findViewById(R.id.recyclerViewviewbook);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cardItemList = new ArrayList<>();
        adapter = new BookAdapter(this, cardItemList);
        recyclerView.setAdapter(adapter);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadBooksFromFirebase();
    }

    private void loadBooksFromFirebase() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Books");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                cardItemList.clear();
                for (DataSnapshot bookSnapshot : snapshot.getChildren()) {
                    String name = bookSnapshot.child("name").getValue(String.class);
                    String author = bookSnapshot.child("author").getValue(String.class);
                    String publisher = bookSnapshot.child("publication").getValue(String.class);
                    Long quantityLong = bookSnapshot.child("quantity").getValue(Long.class);
                    Boolean available = bookSnapshot.child("available").getValue(Boolean.class);
                    String returnDate = bookSnapshot.child("returnDate").getValue(String.class);

                    String quantity = (quantityLong != null) ? String.valueOf(quantityLong) : "0";
                    boolean isAvailable = (available != null) ? available : false;

                    cardItemList.add(new CardItem(name, author, publisher, quantity, isAvailable, returnDate));
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // erreur Firebase
            }
        });
    }

    private static class CardItem {
        String name, author, publisher, quantity, returnDate;
        boolean available;

        CardItem(String name, String author, String publisher, String quantity, boolean available, String returnDate) {
            this.name = name;
            this.author = author;
            this.publisher = publisher;
            this.quantity = quantity;
            this.available = available;
            this.returnDate = returnDate;
        }
    }

    private static class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder> {
        private final List<CardItem> cardItems;
        private final Context context;

        BookAdapter(Context context, List<CardItem> cardItems) {
            this.context = context;
            this.cardItems = cardItems;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.allbook, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            CardItem cardItem = cardItems.get(position);
            holder.ViewBOOkName.setText(cardItem.name);
            holder.ViewBOOkAuthor.setText(cardItem.author);
            holder.ViewBookPublisher.setText(cardItem.publisher);
            holder.ViewBOOkQuantity.setText(cardItem.quantity);

            // Stopper ancien timer si recyclé
            if (holder.countDownTimer != null) {
                holder.countDownTimer.cancel();
            }

            // Timer
            if (cardItem.returnDate != null && !cardItem.returnDate.isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    Date returnDateObj = sdf.parse(cardItem.returnDate);
                    long diffMillis = returnDateObj.getTime() - System.currentTimeMillis();

                    if (diffMillis <= 0) {
                        holder.ViewBOOkAvailable.setText("OVERDUE");
                        holder.ViewBOOkAvailable.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                    } else {
                        holder.countDownTimer = new CountDownTimer(diffMillis, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                long days = millisUntilFinished / (1000 * 60 * 60 * 24);
                                long hours = (millisUntilFinished / (1000 * 60 * 60)) % 24;
                                long minutes = (millisUntilFinished / (1000 * 60)) % 60;
                                long seconds = (millisUntilFinished / 1000) % 60;

                                holder.ViewBOOkAvailable.setText(
                                        String.format(Locale.getDefault(), "%d d %02d h %02d m %02d s", days, hours, minutes, seconds)
                                );
                                holder.ViewBOOkAvailable.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                            }

                            @Override
                            public void onFinish() {
                                holder.ViewBOOkAvailable.setText("OVERDUE");
                                holder.ViewBOOkAvailable.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                            }
                        }.start();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    holder.ViewBOOkAvailable.setText(cardItem.available ? "Available" : "Not Available");
                    holder.ViewBOOkAvailable.setTextColor(context.getResources().getColor(android.R.color.black));
                }
            } else {
                holder.ViewBOOkAvailable.setText(cardItem.available ? "Available" : "Not Available");
                holder.ViewBOOkAvailable.setTextColor(context.getResources().getColor(android.R.color.black));
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, EditBook.class);
                intent.putExtra("bookName", cardItem.name);
                context.startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return cardItems.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final TextView ViewBOOkName, ViewBOOkAuthor, ViewBookPublisher, ViewBOOkQuantity, ViewBOOkAvailable;
            CountDownTimer countDownTimer;

            ViewHolder(View itemView) {
                super(itemView);
                ViewBOOkName = itemView.findViewById(R.id.txtallbookcardtitle);
                ViewBOOkAuthor = itemView.findViewById(R.id.txtallbookcardauthor);
                ViewBookPublisher = itemView.findViewById(R.id.txtallbookcardpublisher);
                ViewBOOkQuantity = itemView.findViewById(R.id.txtallbookcardquantity);
                ViewBOOkAvailable = itemView.findViewById(R.id.txtcardavailable);
            }
        }
    }
}
