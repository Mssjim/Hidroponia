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
import android.widget.CheckBox;
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
    private Button btCadastrar;

    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.w("AppLog", "Cadastrar - onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_cadastrar);

        btImage = findViewById(R.id.btImage);
        ivImage = findViewById(R.id.ivImage);
        etUser=findViewById(R.id.etUser);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPasswordConfirm = findViewById(R.id.etPasswordConfirm);
        btCadastrar = findViewById(R.id.btCadastrar);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.w("AppLog", "Cadastrar - onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0) {
            imageUri = data.getData();
            Bitmap bitmap = null;

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

    public void termos(View v) {
        CheckBox cb = findViewById(v.getId());
        if(cb.isChecked()) {
            btCadastrar.setClickable(true);
            btCadastrar.setBackground(getDrawable(R.drawable.bg_button));
        } else {
            btCadastrar.setClickable(false);
            btCadastrar.setBackground(getDrawable(R.drawable.bg_buttondisabled));
        }
    }

    public void cadastrar(View v) {
        final String username = etUser.getText().toString();
        final String email = etEmail.getText().toString();
        final String password = etPassword.getText().toString();
        final String passwordConfirm = etPasswordConfirm.getText().toString();

        // TODO Solicitar uma imagem de perfil OU adicionar imagem padrão

        if(username.isEmpty()) {
            // TODO Animação de campo inválido
            Toast.makeText(this, "É necessário informar um nome de Usuário!", Toast.LENGTH_SHORT).show(); // TODO Exibir erro no campo
            return;
        }
        if(email.isEmpty()) {
            // TODO Animação de campo inválido
            Toast.makeText(this, "É necessário informar um endereço de E-mail!", Toast.LENGTH_SHORT).show(); // TODO Exibir erro no campo
            return;
        }
        if(password.isEmpty()) {
            // TODO Animação de campo inválido
            Toast.makeText(this, "É necessário informar uma Senha!", Toast.LENGTH_SHORT).show(); // TODO Exibir erro no campo
            return;
        }
        if(passwordConfirm.isEmpty()) {
            // TODO Animação de campo inválido
            Toast.makeText(this, "É necessário confirmar a sua Senha!", Toast.LENGTH_SHORT).show(); // TODO Exibir erro no campo
            return;
        }
        if(!email.contains("@")) { // TODO Melhorar verificação de e-mail
            // TODO Animação de campo inválido
            Toast.makeText(this, "Informe um E-mail válido!", Toast.LENGTH_SHORT).show(); // TODO Exibir erro no campo
            return;
        }
        if(!passwordConfirm.equals(password)) {
            // TODO Animação de campo inválido
            Toast.makeText(this, "As senhas não coincidem!", Toast.LENGTH_SHORT).show(); // TODO Exibir erro no campo
            return;
        }

        // TODO Exigir senha > 6 caracteres

        Log.i("AppLog", "Cadastrando usuário...");
        // TODO Não permitir novas instâncias
        // TODO Progress Dialog
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull final Task<AuthResult> task) {
                    if(task.isSuccessful()) {
                        Log.i("AppLog", "Usuário cadastrado com Sucesso! User: " + etUser.getText() + " - Email: " + etEmail.getText());
                        Log.i("AppLog", "Fazendo upload de imagem...");
                        // TODO Animação durante upload
                        String filename = username + "(" + email + ")";
                        final StorageReference ref = FirebaseStorage.getInstance().getReference("/profile-images/" + filename);
                        ref.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Log.i("AppLog", "Upload de imagem concluído! Url pública: " + uri.toString());
                                        String id = FirebaseAuth.getInstance().getUid();
                                        User user = new User(id, username, Hash.code(password), uri.toString());

                                        Log.i("AppLog", "Adicionando usuário ao Firestore...");

                                        FirebaseFirestore.getInstance().collection("users").document(id)
                                                .set(user)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.i("AppLog", "Adicionado ao Firestore com sucesso!");
                                                        Intent intent = new Intent(Cadastrar.this, Inicio.class);
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
                }
            });
    }
}
