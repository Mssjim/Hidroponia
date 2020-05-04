package br.mssjim.hidroponia.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.ViewHolder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import br.mssjim.hidroponia.Dados;
import br.mssjim.hidroponia.Hidroponia;
import br.mssjim.hidroponia.Post;
import br.mssjim.hidroponia.R;
import br.mssjim.hidroponia.Roles;
import br.mssjim.hidroponia.Status;
import br.mssjim.hidroponia.User;

public class Inicio extends Activity {
    private User user;
    private Roles roles;
    private Dados dados;

    private GroupAdapter adapter;
    private Button btnPublish;
    private TextView tvUsername;
    private TextView tvRole;
    private ImageView ivImage;
    private RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_inicio);

        btnPublish = findViewById(R.id.btPublish);
        tvUsername = findViewById(R.id.tvUsername);
        tvRole = findViewById(R.id.tvRole);
        ivImage = findViewById(R.id.ivImage);
        rv = findViewById(R.id.rv);

        // TODO Definir 'btComercio' dinamicamente

        adapter = new GroupAdapter();
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        verifyLogin();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // TODO Atualizar cards
    }

    public void verifyLogin() {
        Log.i("AppLog", "Obtendo dados do Firestore...");
        // Verifica se o usuário está logado
        // TODO ProgressBar ou algo enquanto carrega os dados
        String id = FirebaseAuth.getInstance().getUid();
        if(id == null) {
            Log.i("AppLog", "Nenhum usuário logado!");
            Intent intent = new Intent(Inicio.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            System.exit(0); // TODO Arrumar isso pq ainda não entendi pq não bugou
            return;
        }

        FirebaseFirestore.getInstance().collection("/users").document(id)
            .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot == null) {
                        Log.i("AppLog", "Erro: Null Exception");
                        // TODO Catch Block
                        return;
                    }
                    Hidroponia.setUser(documentSnapshot.toObject(User.class));
                    Hidroponia.setStatus("Online"); // TODO Refatorar status
                    user = Hidroponia.getUser();

                    // TODO Remover esse e arrumar o 'getUser' da Application
                    try {
                        tvUsername.setText(user.getUsername());
                        // TODO Melhorar sombra no campo de texto (talvez)
                        tvUsername.setShadowLayer(10, 0, 0, Color.BLACK);
                    } catch (Exception err) {
                        Log.i("AppLog", "Erro: " + err.getLocalizedMessage());
                        Intent intent = new Intent(Inicio.this, Login.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        System.exit(0); // TODO Arrumar isso pq ainda não entendi pq não bugou
                        // TODO Catch Block
                        return;
                    }
                    // TODO Exibir uma imagem padrão ou animação de carregamento
                    Picasso.get().load(user.getProfileImage()).into(ivImage);

                    FirebaseFirestore.getInstance().collection("/data").document(user.getUserId())
                            .collection("data").document("roles").get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    roles = documentSnapshot.toObject(Roles.class);

                                    if(roles == null) {
                                        roles = new Roles();
                                    }

                                    Hidroponia.setRoles(roles);

                                    tvRole.setText(roles.getRole());
                                    // TODO Melhorar sombra no campo de texto (talvez)
                                    tvRole.setShadowLayer(4, 0, 0, Color.BLACK);

                                    // TODO Só exibir aqui 'btnComercio' (BtnPublish)
                                    if(roles.isVisitor()) {
                                        btnPublish.setText(getString(R.string.join));
                                    } else {
                                        btnPublish.setText(getString(R.string.publish));
                                    }

                                    FirebaseFirestore.getInstance().collection("/data")
                                            .document(user.getUserId()).collection("data")
                                            .document("dados").get()
                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    dados = documentSnapshot.toObject(Dados.class);

                                                    if(dados == null) {
                                                        dados = new Dados();
                                                    }
                                                    Hidroponia.setDados(dados);

                                                    Log.i("AppLog", "Dados obtidos com sucesso!");

                                                    loadCards();
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
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.i("AppLog", "Erro: " + e.getLocalizedMessage());
                                    // TODO Catch Block
                                }
                            });
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

    public void loadCards() {
        Log.i("AppLog", "Carregando publicações...");
        // TODO alimentar recycle view
        FirebaseFirestore.getInstance().collection("/publish").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot doc : docs) {
                            final Post post = doc.toObject(Post.class);
                            // TODO Carregar Posts separadamente
                            // TODO Definir limite de carregamento
                            int delay = 0;
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() { // Handler pra não travar tudo
                                    adapter.add(new Inicio.CardItem(post));
                                }
                            }, delay);
                        }
                        Log.i("AppLog", "Todas as publicações foram carregadas!");
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.update:
                Intent intent = new Intent(this, Update.class);
                startActivity(intent);
                break;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                Hidroponia.setStatus("Offline"); // TODO Refatorar status
                verifyLogin();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void profile(View view) {
        Intent intent = new Intent(Inicio.this, Profile.class);
        startActivity(intent);
    }

    public void conversas(View view) {
        Intent intent = new Intent(this, Conversas.class);
        startActivity(intent);
    }

    public void publish(View view) {
        if(roles.isVisitor()) {
            Intent intent = new Intent(Inicio.this, Join.class);
            startActivity(intent);
        } else {
            // TODO Iniciar novo post
            Intent intent = new Intent(Inicio.this, Publish.class);
            startActivity(intent);
        }
    }

    private class CardItem extends Item<ViewHolder> {
        private final Post post;

        private CardItem(Post post) {
            this.post = post;
        }

        @Override
        public void bind(@NonNull ViewHolder viewHolder, int position) {
            ImageView ivUser = viewHolder.itemView.findViewById(R.id.ivUser);
            TextView tvUsername = viewHolder.itemView.findViewById(R.id.tvUsername);
            TextView tvTime = viewHolder.itemView.findViewById(R.id.tvTime);
            TextView tvText = viewHolder.itemView.findViewById(R.id.tvText);
            ImageView ivImage = viewHolder.itemView.findViewById(R.id.ivImage);

            Picasso.get().load(post.getUser().getProfileImage()).placeholder(R.drawable.default_profile).into(ivUser);
            tvUsername.setText(post.getUser().getUsername());
            tvTime.setText(new SimpleDateFormat("dd/mm - hh:mm").format(new Date(post.getTime())));
            tvText.setText(post.getText());
            // TODO Definir placeholder
            Picasso.get().load(user.getProfileImage()).into(ivImage);
        }

        @Override
        public int getLayout() {
            return R.layout.item_card;
        }
    }
}
