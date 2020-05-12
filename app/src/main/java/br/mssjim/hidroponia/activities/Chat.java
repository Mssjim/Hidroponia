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

public class Chat extends Activity {

    private GroupAdapter adapter;
    private EditText etMsg;
    private User user;
    private User userSend;

    private Message lastMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_chat);

        user = getIntent().getExtras().getParcelable("user");
        etMsg = findViewById(R.id.etMsg);
        userSend = Hidroponia.getUser();
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(user.getUsername());

        LinearLayoutManager layout = new LinearLayoutManager(this);
        layout.setStackFromEnd(true);
        adapter = new GroupAdapter();
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
                // TODO Se bugar: [if(userSend != null)]
                FirebaseFirestore.getInstance().collection("/data").document(userSend.getUserId())
                        .collection("messages").document(user.getUserId()).collection("all")
                        .orderBy("time", Query.Direction.ASCENDING)
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                List<DocumentChange> docs = queryDocumentSnapshots.getDocumentChanges();

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

        String text = etMsg.getText().toString().trim();
        final String userSendId = userSend.getUserId();
        final String userId = user.getUserId();
        long time = System.currentTimeMillis();
        etMsg.setText(null);

        final Message message = new Message(text, userId, userSendId, time);

        Log.i("AppLog", "1/2 - Adicionando mensagem ao Firestore...");
        FirebaseFirestore.getInstance().collection("/data").document(userSendId)
                .collection("messages").document(userId)
                .collection("all").document(Long.toString(time)).set(message)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("AppLog", "1/2 - Mensagem adicionada com sucesso!");

                        LastMessage lastMessage = new LastMessage(message, userId, user.getUsername(), user.getProfileImage());
                        Log.i("AppLog", "1/2 - Adicionando mensagem rápida ao Firestore...");
                        FirebaseFirestore.getInstance().collection("/data")
                                .document(userSend.getUserId()).collection("last-messages")
                                .document(user.getUserId()).set(lastMessage)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.i("AppLog", "1/2 - Mensagem rápida adicionada com sucesso!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.i("AppLog", "1/2 - Erro: " + e.getLocalizedMessage());
                                        // TODO Catch Block
                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("AppLog", "1/2 - Erro: " + e.getLocalizedMessage());
                // TODO Catch Block
            }
        });

        Log.i("AppLog", "2/2 - Adicionando mensagem ao Firestore...");
        FirebaseFirestore.getInstance().collection("/data").document(userId)
                .collection("messages").document(userSendId)
                .collection("all").document(Long.toString(time)).set(message)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("AppLog", "2/2 - Mensagem adicionada com sucesso!");

                        LastMessage lastMessage = new LastMessage(message, userSendId, userSend.getUsername(), userSend.getProfileImage());
                        Log.i("AppLog", "2/2 - Adicionando mensagem rápida ao Firestore...");
                        FirebaseFirestore.getInstance().collection("/data")
                                .document(user.getUserId()).collection("last-messages")
                                .document(userSend.getUserId()).set(lastMessage)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.i("AppLog", "2/2 - Mensagem rápida adicionada com sucesso!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.i("AppLog", "2/2 - Erro: " + e.getLocalizedMessage());
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("AppLog", "2/2 - Erro: " + e.getLocalizedMessage());
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
            ImageView ivImage = viewHolder.itemView.findViewById(R.id.ivImage);
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
                Picasso.get().load(user.getProfileImage()).placeholder(R.drawable.default_profile).into(ivImage);
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
