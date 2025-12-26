package com.example.publiclibrary;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ViewMember extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MemberAdapter adapter;
    private List<CardItem> cardItemList;
    private List<CardItem> fullList;  // Pour le filtre
    private EditText searchMember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_member);

        searchMember = findViewById(R.id.searchMember);
        recyclerView = findViewById(R.id.recyclerViewviewmember);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        cardItemList = new ArrayList<>();
        fullList = new ArrayList<>();
        adapter = new MemberAdapter(this, cardItemList);
        recyclerView.setAdapter(adapter);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadMembersFromFirebase();

        //  Recherche en temps réel
        searchMember.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMembers(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void loadMembersFromFirebase() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Members");

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                cardItemList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot memberSnapshot : snapshot.getChildren()) {
                        String key = memberSnapshot.getKey();
                        String name = memberSnapshot.child("name").getValue(String.class);
                        String email = memberSnapshot.child("email").getValue(String.class);
                        String phone = memberSnapshot.child("phone").getValue(String.class);
                        String address = memberSnapshot.child("address").getValue(String.class);

                        cardItemList.add(new CardItem(key, name, email, phone, address));
                    }

                    // TRI PAR NOM
                    Collections.sort(cardItemList, (a, b) -> a.name.compareToIgnoreCase(b.name));

                    //  Copie complète pour la recherche
                    fullList.clear();
                    fullList.addAll(cardItemList);

                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(ViewMember.this, "No members found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ViewMember.this, "Error loading members: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //  Filtre en temps réel
    private void filterMembers(String text) {
        cardItemList.clear();
        for (CardItem item : fullList) {
            if (item.name.toLowerCase().contains(text.toLowerCase())) {
                cardItemList.add(item);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private static class CardItem {
        String key;
        String name;
        String email;
        String phone;
        String address;

        CardItem(String key, String name, String email, String phone, String address) {
            this.key = key;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.address = address;
        }
    }

    private static class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {
        private final List<CardItem> cardItems;
        private final Context context;

        MemberAdapter(Context context, List<CardItem> cardItems) {
            this.context = context;
            this.cardItems = cardItems;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.allmember, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            CardItem cardItem = cardItems.get(position);
            holder.txtMemberName.setText(cardItem.name);
            holder.txtMemberEmail.setText(cardItem.email);
            holder.txtMemberPhone.setText(cardItem.phone);
            holder.txtMemberAddress.setText(cardItem.address);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, EditMember.class);
                intent.putExtra("memberKey", cardItem.key);
                context.startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return cardItems.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final TextView txtMemberName;
            final TextView txtMemberEmail;
            final TextView txtMemberPhone;
            final TextView txtMemberAddress;

            ViewHolder(View itemView) {
                super(itemView);
                txtMemberName = itemView.findViewById(R.id.txt_member_name);
                txtMemberEmail = itemView.findViewById(R.id.txt_member_email);
                txtMemberPhone = itemView.findViewById(R.id.txt_member_phone);
                txtMemberAddress = itemView.findViewById(R.id.txt_member_address);
            }
        }
    }
}
