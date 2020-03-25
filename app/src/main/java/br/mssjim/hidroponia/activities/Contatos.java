package br.mssjim.hidroponia.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.OnItemClickListener;
import com.xwray.groupie.ViewHolder;

import java.util.List;

import br.mssjim.hidroponia.R;
import br.mssjim.hidroponia.Status;
import br.mssjim.hidroponia.User;

public class Contatos extends Activity {

    private ImageView ivImage;
    private TextView tvUsername;
    private TextView tvStatus;

    private GroupAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.w("AppLog", "Contatos - onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_contatos);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        ivImage = findViewById(R.id.ivImage);
        tvUsername = findViewById(R.id.tvUsername);
        tvStatus = findViewById(R.id.tvStatus);

        adapter = new GroupAdapter();
        RecyclerView rv = findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull Item item, @NonNull View view) {
                UserItem userItem = (UserItem) item;
                Intent intent = new Intent(Contatos.this, Chat.class);
                intent.putExtra("user", userItem.user);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        Log.w("AppLog", "Contatos - onResume");
        super.onResume();

        // TODO Recarregar contatos != Adicionar mais contatos
        // TODO Ordernar contatos Alfabeticamente/Roles
        Log.i("AppLog", "Carregando lista de contatos...");
        FirebaseFirestore.getInstance().collection("/users").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(e != null) {
                    Log.i("AppLog", "Erro: " + e.getLocalizedMessage());
                    // TODO Catch Block
                    return;
                }

                List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                for (DocumentSnapshot doc : docs) {
                    final User user = doc.toObject(User.class);
                    // TODO Carregar contatos separadamente
                    int delay = 0;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() { // Handler pra não travar tudo
                            // TODO Ocultar usuário logado
                            adapter.add(new UserItem(user));
                        }
                    }, delay);
                }
                Log.i("AppLog", "Todos os contatos foram carregados!");
            }
        });
    }

    private class UserItem extends Item<ViewHolder> {
        private final User user;

        private UserItem(User user) {
            this.user = user;
        }

        @Override
        public void bind(@NonNull ViewHolder viewHolder, int position) {
            ImageView ivImage = viewHolder.itemView.findViewById(R.id.ivImage);
            TextView tvUsername = viewHolder.itemView.findViewById(R.id.tvUsername);
            final TextView tvStatus = viewHolder.itemView.findViewById(R.id.tvStatus);

            tvUsername.setText(user.getUsername());
            Picasso.get().load(user.getProfileImage()).into(ivImage);
            FirebaseFirestore.getInstance().collection("/data").document(user.getUserId())
                    .collection("data").document("status")
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if(e != null) {
                            Log.i("AppLog", "Erro: " + e.getLocalizedMessage());
                            // TODO Catch Block
                            Toast.makeText(getApplicationContext(), "e != null", Toast.LENGTH_SHORT).show(); // TODO Toast Teste
                            return;
                        }

                        Status status = documentSnapshot.toObject(Status.class);

                        if (status != null) {
                            tvStatus.setText(status.getStatus());
                            // TODO Exibir última vez visto
                        }
                    }
            });
        }

        @Override
        public int getLayout() {
            return R.layout.item_user;
        }
    }
}
