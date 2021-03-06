package br.mssjim.hidroponia.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

import br.mssjim.hidroponia.R;
import br.mssjim.hidroponia.User;
import br.mssjim.hidroponia.utils.Hash;

public class Cadastrar extends Activity {

    private Button btImage;
    private ImageView ivImage;
    private EditText etUser;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etPasswordConfirm;

    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_cadastrar);

        btImage = findViewById(R.id.btImage);
        ivImage = findViewById(R.id.ivImage);
        etUser = findViewById(R.id.etUser);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPasswordConfirm = findViewById(R.id.etPasswordConfirm);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (data == null) {
                return;
            }
            imageUri = data.getData();
            Bitmap bitmap;

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                ivImage.setImageDrawable(new BitmapDrawable(bitmap));
                btImage.setAlpha(0);
            } catch (IOException e) {
                // TODO Catch Block
            }
        }
    }

    public void imagem(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 0);
    }

    public void cadastrar(final View v) {
        final String username = etUser.getText().toString().trim();
        final String email = etEmail.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();
        final String passwordConfirm = etPasswordConfirm.getText().toString().trim();

        if (username.isEmpty()) {
            etUser.setError(getString(R.string.emptyUsername));
            return;
        }
        if (email.isEmpty()) {
            etEmail.setError(getString(R.string.emptyEmail));
            return;
        }
        if (!email.contains("@")) {
            etEmail.setError(getString(R.string.invalidEmail));
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError(getString(R.string.emptyPassword));
            return;
        }
        if (password.length() < 6) {
            etPassword.setError(getString(R.string.invalidPasswordLength));
            return;
        }
        if (passwordConfirm.isEmpty()) {
            etPasswordConfirm.setError(getString(R.string.unconfirmedPassword));
            return;
        }
        if (!passwordConfirm.equals(password)) {
            etPasswordConfirm.setError(getString(R.string.unmatchedPassword));
            return;
        }

        v.setEnabled(false); // Bloqueia futuros pedidos de cadastro

        Log.i("AppLog", "Cadastrando usuário...");
        // TODO Progress Dialog / Ou feedback de cadastramento
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull final Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.i("AppLog", "Usuário cadastrado com Sucesso! User: " + etUser.getText() + " - Email: " + etEmail.getText());
                            Log.i("AppLog", "Fazendo upload de imagem...");
                            // TODO Animação durante upload
                            final String id = FirebaseAuth.getInstance().getUid();
                            final StorageReference ref = FirebaseStorage.getInstance().getReference("/profile-images/" + id);
                            if (imageUri != null) {
                                ref.putFile(imageUri)
                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        Log.i("AppLog", "Upload de imagem concluído! Url pública: " + uri.toString());
                                                        User user = new User(id, username, email, Hash.code(password), uri.toString());

                                                        Log.i("AppLog", "Adicionando usuário ao Firestore...");

                                        FirebaseFirestore.getInstance().collection("users").document(id)
                                                .set(user)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.i("AppLog", "Adicionado ao Firestore com sucesso!");
                                                        Intent intent = new Intent(Cadastrar.this, Splash.class);
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        startActivity(intent);
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.i("AppLog", "Erro: " + e.getLocalizedMessage());
                                                        Toast.makeText(Cadastrar.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                                        // TODO Catch Block
                                                        v.setEnabled(true);
                                                    }
                                                });
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i("AppLog", "Erro: " + e.getLocalizedMessage());
                                Toast.makeText(Cadastrar.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                // TODO Catch Block
                                v.setEnabled(true);
                            }
                        });
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i("AppLog", "Erro: " + e.getLocalizedMessage());
                    Toast.makeText(Cadastrar.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    // TODO Catch Block
                    v.setEnabled(true);
                }
            });
    }
}
