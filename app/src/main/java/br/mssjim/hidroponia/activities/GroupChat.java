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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.ViewHolder;

import java.net.URL;
import java.util.List;

import br.mssjim.hidroponia.Hidroponia;
import br.mssjim.hidroponia.LastMessage;
import br.mssjim.hidroponia.Message;
import br.mssjim.hidroponia.R;
import br.mssjim.hidroponia.User;
import br.mssjim.hidroponia.utils.Url;

public class GroupChat extends Activity {

    // TODO permitir outros grupos além do "Hidroponia"

    private GroupAdapter adapter;
    private EditText etMsg;
    private User userSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_chat);

        etMsg = findViewById(R.id.etMsg);
        userSend = Hidroponia.getUser();
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(getResources().getString(R.string.app_name));

        adapter = new GroupAdapter();
        RecyclerView rv = findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        loadMessages();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // TODO Atualizar mensagens
    }

    public void loadMessages() {
        Log.i("AppLog", "Carregando mensagens...");
        // TODO Carregamento de mensagens
        int delay = 0;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() { // Handler pra não travar tudo
                // TODO Se bugar: [if(userSend != null)]
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
                                            adapter.add(new MessageItem(message));
                                            // TODO Agrupar mensagens do mesmo usuário
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

                        LastMessage lastMessage = new LastMessage(message, userSend.getUsername(), userSend.getProfileImage());
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

        private MessageItem(Message message) {
            this.message = message;
        }

        @Override
        public void bind(@NonNull ViewHolder viewHolder, int position) {
            TextView tvMsg = viewHolder.itemView.findViewById(R.id.tvMsg);
            ImageView ivImage = viewHolder.itemView.findViewById(R.id.ivImage);

            // TODO Exibir horário nas mensagens

            tvMsg.setText(message.getText());
            if(message.getUserSendId().equals(userSend.getUserId())) {
                Picasso.get().load(userSend.getProfileImage()).placeholder(R.drawable.default_profile).into(ivImage);
            } else {
                Picasso.get().load(Url.getGroupImage).into(ivImage);
                // TODO Carregar imagem do usuário que enviou a mensagem
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
