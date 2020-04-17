package br.mssjim.hidroponia.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
    private Button btCadastrar;

    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

    public void cadastrar(View v) {
        final String username = etUser.getText().toString();
        final String email = etEmail.getText().toString();
        final String password = etPassword.getText().toString();
        final String passwordConfirm = etPasswordConfirm.getText().toString();

        // TODO Solicitar uma imagem de perfil OU adicionar imagem padrão

        if(username.isEmpty()) {
            // TODO Animação de campo inválido
            Toast.makeText(this, getString(R.string.emptyUsername), Toast.LENGTH_SHORT).show(); // TODO Exibir erro no campo
            return;
        }
        if(email.isEmpty()) {
            // TODO Animação de campo inválido
            Toast.makeText(this, getString(R.string.emptyEmail), Toast.LENGTH_SHORT).show(); // TODO Exibir erro no campo
            return;
        }
        if(password.isEmpty()) {
            // TODO Animação de campo inválido
            Toast.makeText(this, getString(R.string.emptyPassword), Toast.LENGTH_SHORT).show(); // TODO Exibir erro no campo
            return;
        }
        if(passwordConfirm.isEmpty()) {
            // TODO Animação de campo inválido
            Toast.makeText(this, getString(R.string.unconfirmedPassword), Toast.LENGTH_SHORT).show(); // TODO Exibir erro no campo
            return;
        }
        if(!email.contains("@")) { // TODO Melhorar verificação de e-mail
            // TODO Animação de campo inválido
            Toast.makeText(this, getString(R.string.invalidEmail), Toast.LENGTH_SHORT).show(); // TODO Exibir erro no campo
            return;
        }
        if(!passwordConfirm.equals(password)) {
            // TODO Animação de campo inválido
            Toast.makeText(this, getString(R.string.unmatchedPassword), Toast.LENGTH_SHORT).show(); // TODO Exibir erro no campo
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
                                        User user = new User(id, username, email, Hash.code(password), uri.toString());

                                        Log.i("AppLog", "Adicionando usuário ao Firestore...");

                                        FirebaseFirestore.getInstance().collection("users").document(id)
                                                .set(user)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.i("AppLog", "Adicionado ao Firestore com sucesso!");
                                                        // TODO Ações de usuário novo
                                                        new AlertDialog.Builder(Cadastrar.this)
                                                                .setTitle("")
                                                                .setMessage("")
                                                                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                                        // TODO Positive Action
                                                                    }
                                                                })
                                                                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                                        // TODO Negative Action
                                                                    }
                                                                })
                                                                .setIcon(getDrawable(R.mipmap.ic_launcher_round))
                                                                .show();

                                                        // TODO Só Iniciar intent após sair do AlertDialog (se bugar)
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
