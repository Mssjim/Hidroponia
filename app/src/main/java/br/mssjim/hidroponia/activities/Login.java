package br.mssjim.hidroponia.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import br.mssjim.hidroponia.R;

public class Login extends Activity {

    private EditText etEmail;
    private EditText etPassword;
    private Button btLogin;
    private TextView tvCadastrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.w("AppLog", "Login - onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btLogin = findViewById(R.id.btLogin);
        tvCadastrar = findViewById(R.id.tvCadastrar);
    }

    public void login(View v) {
        final String email = etEmail.getText().toString();
        final String password = etPassword.getText().toString();

        if(email.isEmpty()) {
            // TODO Animação de campo inválido
            Toast.makeText(this, "É necessário informar um endereço de E-mail!", Toast.LENGTH_SHORT).show();
            return;
        }
        if(password.isEmpty()) {
            // TODO Animação de campo inválido
            Toast.makeText(this, "É necessário informar uma Senha!", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.i("AppLog", "Autenticando usuário...");
        // TODO Não permitir novas instâncias
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.i("AppLog", "Usuário autenticado com sucesso!");
                        Intent intent = new Intent(Login.this, Inicio.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("AppLog", "Erro: " + e.getLocalizedMessage());
                        Toast.makeText(Login.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        // TODO Catch Block
                    }
                });
    }

    public void cadastrar(View v) {
        Intent intent = new Intent(this, Cadastrar.class);
        startActivity(intent);
    }
}
