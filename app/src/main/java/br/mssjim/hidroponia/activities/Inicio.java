package br.mssjim.hidroponia.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;

import java.util.Objects;

import br.mssjim.hidroponia.Hidroponia;
import br.mssjim.hidroponia.R;
import br.mssjim.hidroponia.User;

public class Inicio extends Activity {

    private User user;

    private GroupAdapter adapter;
    private Button btComercio;
    private TextView tvUsername;
    private ImageView ivImage;
    private RecyclerView rv;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.w("AppLog", "Inicio - onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_inicio);

        btComercio = findViewById(R.id.btComercio);
        tvUsername = findViewById(R.id.tvUsername);
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
        Log.w("AppLog", "Inicio - onResume");
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

        FirebaseFirestore.getInstance().collection("/users").document(id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(e != null) {
                    Log.i("AppLog", "Erro: " + e.getLocalizedMessage());
                    // TODO Catch Block
                    return;
                }
                if (documentSnapshot == null) {
                    Log.i("AppLog", "Erro: Null Exception");
                    // TODO Catch Block
                    return;
                }
                Hidroponia.setUser(documentSnapshot.toObject(User.class));
                user = Hidroponia.getUser();

                // TODO Remover esse e arrumar o 'getUser' da Application
                try {
                    tvUsername.setText(user.getUsername());
                } catch (Exception err) {
                    Log.i("AppLog", "Erro: " + err.getLocalizedMessage());
                    // TODO Catch Block
                    return;
                }

                Log.i("AppLog", "Dados obtidos com sucesso!");

                // TODO animação durante carregamento da imagem
                Log.i("AppLog", "Obtendo imagem de perfil...");
                Picasso.get().load(user.getProfileImage()).into(ivImage);
                Log.i("AppLog", "Imagem carregada com sucesso!");

                loadCards();
            }
        });
    }

    public void loadCards() {
        // TODO alimentar recycle view
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
                Hidroponia.setStatus("Offline");
                verifyLogin();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void conversas(View view) {
        Intent intent = new Intent(this, Conversas.class);
        startActivity(intent);
    }

    public void comercio(View view) {
        // TODO Ação ao pressionar botão
    }
}
