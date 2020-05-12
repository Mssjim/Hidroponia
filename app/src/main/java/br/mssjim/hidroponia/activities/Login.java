package br.mssjim.hidroponia.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
    }

    public void login(final View v) {
        final String email = etEmail.getText().toString();
        final String password = etPassword.getText().toString();

        if(email.isEmpty()) {
            etEmail.setError(getString(R.string.emptyEmail));
            return;
        }
        if(password.isEmpty()) {
            etPassword.setError(getString(R.string.emptyPassword));
            return;
        }

        v.setEnabled(false);
        Log.i("AppLog", "Autenticando usuário...");
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.i("AppLog", "Usuário autenticado com sucesso!");
                        Intent intent = new Intent(Login.this, Splash.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("AppLog", "Erro: " + e.getLocalizedMessage());
                        Toast.makeText(Login.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        // TODO Catch Block (Email/Senha invalidos)
                        v.setEnabled(true);
                    }
                });
    }

    public void cadastrar(View v) {
        Intent intent = new Intent(this, Cadastrar.class);
        startActivity(intent);
    }
}
