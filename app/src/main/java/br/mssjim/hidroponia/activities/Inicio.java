package br.mssjim.hidroponia.activities;

import android.app.Activity;
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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.ViewHolder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import br.mssjim.hidroponia.Dados;
import br.mssjim.hidroponia.Hidroponia;
import br.mssjim.hidroponia.Post;
import br.mssjim.hidroponia.R;
import br.mssjim.hidroponia.Roles;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_inicio);

        btnPublish = findViewById(R.id.btPublish);
        tvUsername = findViewById(R.id.tvUsername);
        tvRole = findViewById(R.id.tvRole);
        ivImage = findViewById(R.id.ivImage);
        RecyclerView rv = findViewById(R.id.rv);

        adapter = new GroupAdapter();
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        verifyLogin();
    }

    private void verifyLogin() {
        user = Hidroponia.getUser();
        roles = Hidroponia.getRoles();
        dados = Hidroponia.getDados();

        if(user == null || roles == null || dados == null) {
            Intent intent = new Intent(Inicio.this, Splash.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            Hidroponia.setStatus("Online"); // TODO Refatorar status
            fillViews();
            loadCards();
            updateDados();
        }
    }

    private void loadCards() {
        Log.i("AppLog", "Carregando publicações...");
        // TODO alimentar recycle view
        FirebaseFirestore.getInstance().collection("/publish")
                .orderBy("time", Query.Direction.DESCENDING).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        adapter.clear();
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
    private void updateDados() {
        Log.i("AppLog", "Atualizando dados...");
        final String id = user.getUserId();
        FirebaseFirestore.getInstance().collection("/users").document(id).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        user = documentSnapshot.toObject(User.class);
                        Hidroponia.setUser(user);

                        FirebaseFirestore.getInstance().collection("/data").document(id)
                                .collection("data").document("roles").get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        roles = documentSnapshot.toObject(Roles.class);
                                        if (roles == null) {
                                            roles = new Roles();
                                        }
                                        Hidroponia.setRoles(roles);

                                        FirebaseFirestore.getInstance().collection("/data")
                                                .document(id).collection("data")
                                                .document("dados").get()
                                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                        dados = documentSnapshot.toObject(Dados.class);
                                                        if (dados == null) {
                                                            dados = new Dados();
                                                        }
                                                        Hidroponia.setDados(dados);

                                                        Log.i("AppLog", "Dados atualizados com sucesso!");
                                                        fillViews();
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

    private void fillViews() {
        tvUsername.setText(user.getUsername());
        tvUsername.setShadowLayer(10, 0, 0, Color.BLACK);
        Picasso.get().load(user.getProfileImage()).placeholder(R.drawable.default_profile).into(ivImage);

        tvRole.setText(roles.getRole());
        tvRole.setShadowLayer(4, 0, 0, Color.BLACK);

        // TODO Só exibir aqui 'btnComercio' (BtnPublish)
        if (roles.isVisitor()) {
            btnPublish.setText(getString(R.string.join));
        } else {
            btnPublish.setText(getString(R.string.publish));
        }
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
                Hidroponia.logout();
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
            final ImageView ivUser = viewHolder.itemView.findViewById(R.id.ivUser);
            final TextView tvUsername = viewHolder.itemView.findViewById(R.id.tvUsername);
            TextView tvTime = viewHolder.itemView.findViewById(R.id.tvTime);
            TextView tvText = viewHolder.itemView.findViewById(R.id.tvText);
            ImageView ivImage = viewHolder.itemView.findViewById(R.id.ivImage);

            FirebaseFirestore.getInstance().collection("users")
                    .document(post.getUserId()).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            String username = documentSnapshot.getString("username");
                            String image = documentSnapshot.getString("profileImage");

                            Picasso.get().load(image).placeholder(R.drawable.default_profile).into(ivUser);
                            tvUsername.setText(username);

                            if(username.isEmpty()) {
                                tvUsername.setText(getString(R.string.deletedUser));
                            }
                        }
                    });
            tvTime.setText(new SimpleDateFormat("dd/mm - hh:mm").format(new Date(post.getTime())));
            tvText.setText(post.getText());
            if(post.getImage() != null)
                Picasso.get().load(post.getImage()).placeholder(R.drawable.default_image).into(ivImage);
        }

        @Override
        public int getLayout() {
            return R.layout.item_card;
        }
    }
}
