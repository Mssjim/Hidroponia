package br.mssjim.hidroponia.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import br.mssjim.hidroponia.Dados;
import br.mssjim.hidroponia.Hidroponia;
import br.mssjim.hidroponia.R;
import br.mssjim.hidroponia.Roles;
import br.mssjim.hidroponia.User;

public class Splash extends Activity {
    private ImageView ivLogo, ivLogoShadow;
    private TextView tvLogo;
    private ProgressBar pb;

    Animation topAnimation, bottomAnimation, shadowAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.act_splash);

        ivLogo = findViewById(R.id.ivLogo);
        ivLogoShadow = findViewById(R.id.ivLogoShadow);
        tvLogo = findViewById(R.id.tvLogo);
        pb = findViewById(R.id.pb);

        topAnimation = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnimation = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);
        shadowAnimation = AnimationUtils.loadAnimation(this, R.anim.shadow_animation);

        // Animations
        ivLogo.setAnimation(topAnimation);
        tvLogo.setAnimation(bottomAnimation);

        Log.i("AppLog", "Verificando sessão...");
        final String id = FirebaseAuth.getInstance().getUid();

        if (id != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    pb.setVisibility(View.VISIBLE);
                    ivLogoShadow.setAnimation(shadowAnimation);
                    ivLogoShadow.setVisibility(View.VISIBLE);
                    getDados(id);
                }
            }, 2100);
        } else {
            Log.i("AppLog", "Nenhum usuário logado!");
            login();
        }
    }

    public void login() {
        Intent intent = new Intent(Splash.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void inicio() {
        Intent intent = new Intent(Splash.this, Inicio.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void getDados(final String id) {
        Log.i("AppLog", "Obtendo dados do Firestore...");
        FirebaseFirestore.getInstance().collection("/users").document(id).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot == null) {
                            login();
                            return;
                        }
                        User user = documentSnapshot.toObject(User.class);
                        if (user == null) {
                            login();
                        }
                        Hidroponia.setUser(user);

                        FirebaseFirestore.getInstance().collection("/data").document(id)
                                .collection("data").document("roles").get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        Roles roles = documentSnapshot.toObject(Roles.class);
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
                                                        Dados dados = documentSnapshot.toObject(Dados.class);
                                                        if (dados == null) {
                                                            dados = new Dados();
                                                        }
                                                        Hidroponia.setDados(dados);

                                                        Log.i("AppLog", "Dados obtidos com sucesso!");

                                                        inicio();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.i("AppLog", "Erro: " + e.getLocalizedMessage());
                                                        Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                                        tvLogo.setText(getString(R.string.reconnecting));
                                                        getDados(id);
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.i("AppLog", "Erro: " + e.getLocalizedMessage());
                                        Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                        tvLogo.setText(getString(R.string.reconnecting));
                                        getDados(id);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("AppLog", "Erro: " + e.getLocalizedMessage());
                        Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        tvLogo.setText(getString(R.string.reconnecting));
                        getDados(id);
                    }
                });
    }
}
