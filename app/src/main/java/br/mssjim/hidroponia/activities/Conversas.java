package br.mssjim.hidroponia.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.ViewHolder;

import java.util.List;

import br.mssjim.hidroponia.Hidroponia;
import br.mssjim.hidroponia.LastMessage;
import br.mssjim.hidroponia.R;

public class Conversas extends Activity {

    private GroupAdapter adapter;
    private RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.w("AppLog", "Conversas - onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_conversas);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        adapter = new GroupAdapter();
        rv = findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        loadLastMessages();
    }

    @Override
    protected void onResume() {
        Log.w("AppLog", "Conversas - onResume");
        super.onResume();
        // TODO Atualizar conversas
    }

    public void loadLastMessages() {
        Log.i("AppLog", "Carregando conversas...");
        // TODO Carregamento de mensagens
        int delay = 0;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() { // Handler pra não travar tudo
                // TODO Se bugar: [if(userSend != null]
                FirebaseFirestore.getInstance().collection("/data")
                        .document(Hidroponia.getUser().getUserId()).collection("last-messages")
                        .orderBy("message", Query.Direction.DESCENDING) // TODO Ordenar conversas
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                List<DocumentChange> docs = queryDocumentSnapshots.getDocumentChanges();

                                if(docs != null) {
                                    for(DocumentChange doc : docs) {
                                        // Lista apenas as alterações
                                        if(doc.getType() == DocumentChange.Type.ADDED) {
                                            LastMessage lastMessage = doc.getDocument().toObject(LastMessage.class);
                                            adapter.add(new LastMessageItem(lastMessage));
                                        }
                                    }
                                }
                            }
                        });
            }
        }, delay);
        Log.i("AppLog", "Todas as conversas foram carregadas!");
    }

    private class LastMessageItem extends Item<ViewHolder> {

        private final LastMessage lastMessage;

        private LastMessageItem(LastMessage lastMessage) {
            this.lastMessage = lastMessage;
        }

        @Override
        public void bind(@NonNull ViewHolder viewHolder, int position) {
            ImageView ivImage = viewHolder.itemView.findViewById(R.id.ivImage);
            TextView tvUsername = viewHolder.itemView.findViewById(R.id.tvUsername);
            TextView tvMsg = viewHolder.itemView.findViewById(R.id.tvMsg);

            tvUsername.setText(lastMessage.getUser().getUsername());
            tvMsg.setText(lastMessage.getMessage().getText());
            Picasso.get().load(lastMessage.getUser().getProfileImage()).into(ivImage);
        }

        @Override
        public int getLayout() {
            return R.layout.item_last_message;
        }
    }
}
