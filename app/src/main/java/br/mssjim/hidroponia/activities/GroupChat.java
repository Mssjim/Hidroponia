package br.mssjim.hidroponia.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
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
import br.mssjim.hidroponia.Message;
import br.mssjim.hidroponia.R;
import br.mssjim.hidroponia.User;

public class GroupChat extends Activity {
    private GroupAdapter adapter;
    private EditText etMsg;
    private User userSend;

    private Message lastMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_chat);

        etMsg = findViewById(R.id.etMsg);
        userSend = Hidroponia.getUser();
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(getResources().getString(R.string.app_name));

        adapter = new GroupAdapter();
        LinearLayoutManager layout = new LinearLayoutManager(this);
        layout.setStackFromEnd(true);
        RecyclerView rv = findViewById(R.id.rv);
        rv.setLayoutManager(layout);
        rv.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.clear();
        Log.i("AppLog", "Carregando mensagens...");
        int delay = 0;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() { // Handler pra não travar tudo
                FirebaseFirestore.getInstance().collection("/groups").document("hidroponia")
                        .collection("messages")
                        .orderBy("time", Query.Direction.ASCENDING)
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                List<DocumentChange> docs = queryDocumentSnapshots.getDocumentChanges();

                                if(docs != null) {
                                    for(DocumentChange doc : docs) {
                                        // Lista apenas as alterações
                                        if(doc.getType() == DocumentChange.Type.ADDED) {
                                            Message message = doc.getDocument().toObject(Message.class);
                                            Message previousMessage = new Message("", "", "", 0);
                                            if(doc.getNewIndex() > 0) {
                                                try {
                                                    previousMessage = docs.get(doc.getNewIndex() - 1)
                                                            .getDocument().toObject(Message.class);
                                                } catch (Exception error) {
                                                    if(lastMessage != null) {
                                                        previousMessage = lastMessage;
                                                    }
                                                }
                                            }
                                            lastMessage = message;
                                            adapter.add(new MessageItem(message, previousMessage));
                                        }
                                    }
                                }
                            }
                        });
            }
        }, delay);
        Log.i("AppLog", "Todas as mensagens foram carregadas!");
    }

    public void enviar(View view) {
        if(etMsg.getText().toString().isEmpty()) {
            Log.i("AppLog", "Campo de mensagem vazio!");
            return;
        }

        String text = etMsg.getText().toString();
        final String userSendId = userSend.getUserId();
        long time = System.currentTimeMillis();
        etMsg.setText(null);

        final Message message = new Message(text, null, userSendId, time);

        Log.i("AppLog", "Adicionando mensagem ao Firestore...");
        FirebaseFirestore.getInstance().collection("/groups").document("hidroponia")
                .collection("messages").document(Long.toString(time)).set(message)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("AppLog", "Mensagem adicionada com sucesso!");

                        LastMessage lastMessage = new LastMessage(message, userSendId, userSend.getUsername(), userSend.getProfileImage());
                        Log.i("AppLog", "Adicionando mensagem rápida ao Firestore...");
                        FirebaseFirestore.getInstance().collection("/groups")
                                .document("hidroponia").collection("data")
                                .document("last-message").set(lastMessage)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.i("AppLog", "Mensagem rápida adicionada com sucesso!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.i("AppLog", "Erro: " + e.getLocalizedMessage());
                                        // TODO Catch Block
                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("AppLog", "Erro: " + e.getLocalizedMessage());
                // TODO Catch Block
            }
        });
    }

    private class MessageItem extends Item<ViewHolder> {

        private final Message message;
        private final Message previousMessage;

        private MessageItem(Message message, Message previousMessage) {
            this.message = message;
            this.previousMessage = previousMessage;
        }

        @Override
        public void bind(@NonNull ViewHolder viewHolder, int position) {
            TextView tvMsg = viewHolder.itemView.findViewById(R.id.tvMsg);
            final ImageView ivImage = viewHolder.itemView.findViewById(R.id.ivImage);
            View space = viewHolder.itemView.findViewById(R.id.space);

            // TODO Exibir horário nas mensagens

            if(message.getUserSendId().equals(previousMessage.getUserSendId())) {
                ivImage.setVisibility(View.GONE);
                space.setVisibility(View.VISIBLE);
            } else {
                ivImage.setVisibility(View.VISIBLE);
                space.setVisibility(View.GONE);
            }

            tvMsg.setText(message.getText());
            if(message.getUserSendId().equals(userSend.getUserId())) {
                Picasso.get().load(userSend.getProfileImage()).placeholder(R.drawable.default_profile).into(ivImage);
            } else {
                FirebaseFirestore.getInstance().collection("users")
                        .document(message.getUserSendId()).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                String image = documentSnapshot.getString("profileImage");
                                Picasso.get().load(image).placeholder(R.drawable.default_profile).into(ivImage);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // TODO Testar se a imagem padrao sera carregada caso o usuario n exista mais
                                Picasso.get().load(R.drawable.default_profile).into(ivImage);
                            }
                        });
            }
        }

        @Override
        public int getLayout() {
            return message.getUserSendId().equals(userSend.getUserId())
                    ? R.layout.item_message_send
                    : R.layout.item_message;
        }
    }
}
