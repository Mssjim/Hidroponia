package br.mssjim.hidroponia.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import br.mssjim.hidroponia.Dados;
import br.mssjim.hidroponia.Hidroponia;
import br.mssjim.hidroponia.R;
import br.mssjim.hidroponia.Roles;
import br.mssjim.hidroponia.User;
import br.mssjim.hidroponia.utils.Hash;


public class Profile extends Activity {
    private User user;
    private Roles roles;
    private Dados dados;

    private ImageView ivImage;
    private TextView tvUsername;
    private TextView tvRole;

    private TextView tvEmail;

    private TextView lDados;
    private LinearLayout llDados;
    private TextView tvNameField;
    private TextView tvName;
    private TextView tvDataField;
    private TextView tvData;
    private TextView tvCpfField;
    private TextView tvCpf;
    private TextView tvPhone;
    private TextView tvAddress;
    private TextView tvCep;

    private AlertDialog.Builder alertDialog;
    LinearLayout.LayoutParams lp;

    private int i, j, mensagens; // Variável utilizada para a contagem de documentos no método de exclusão de conta

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_profile);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        user = Hidroponia.getUser();
        roles = Hidroponia.getRoles();
        dados = Hidroponia.getDados();

        ivImage = findViewById(R.id.ivImage);
        tvUsername = findViewById(R.id.tvUsername);
        tvRole = findViewById(R.id.tvRole);

        tvUsername.setText(user.getUsername());
        tvRole.setText(roles.getRole());
        Picasso.get().load(user.getProfileImage()).placeholder(R.drawable.default_profile).into(ivImage);

        tvEmail = findViewById(R.id.tvEmail);

        tvEmail.setText(user.getEmail());

        // Dados
        lDados = findViewById(R.id.lDados);
        llDados = findViewById(R.id.llDados);
        tvNameField = findViewById(R.id.tvNameField);
        tvName = findViewById(R.id.tvName);
        tvDataField = findViewById(R.id.tvDataField);
        tvData = findViewById(R.id.tvData);
        tvCpfField = findViewById(R.id.tvCpfField);
        tvCpf = findViewById(R.id.tvCpf);
        tvPhone = findViewById(R.id.tvPhone);
        tvAddress = findViewById(R.id.tvAddress);
        tvCep = findViewById(R.id.tvCep);

        if (roles.isOrganic() || roles.isStore()) {
            tvDataField.setVisibility(View.GONE);
            tvData.setVisibility(View.GONE);
            tvNameField.setText(getString(R.string.razao));
            tvCpfField.setText(getString(R.string.cnpj));
        } else if (roles.isFarm() || roles.isSale()) {
            tvNameField.setText(getString(R.string.fullName));
            tvCpfField.setText(getString(R.string.cpf));
        } else {
            lDados.setVisibility(View.GONE);
            llDados.setVisibility(View.GONE);
        }

        // TODO Formatar campos
        tvName.setText(dados.getName());
        tvData.setText(dados.getDate());
        tvCpf.setText(dados.getCpf());
        tvPhone.setText(String.valueOf(dados.getPhone()));
        tvAddress.setText(dados.getAddress());
        tvCep.setText(dados.getCep());

        lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
    }

    public void createAlertDialog(String s, View view, View view2, DialogInterface.OnClickListener listener) {
        // TODO Atualizar 'user', 'roles' e 'dados' para não exibir valores antigos
        alertDialog = null;
        TextView title = new TextView(this);
        title.setText(s);
        title.setPadding(20, 30, 20, 30);
        title.setTextSize(20F);
        title.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        title.setTextColor(getResources().getColor(R.color.colorWhite));
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);

        final LinearLayout layout = new LinearLayout(this);
        layout.setLayoutParams(lp);
        layout.setPadding(10, 40, 10, 0);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(view);
        if (view2 != null) {
            TextView space = new TextView(this);
            space.setLayoutParams(lp);
            space.setPadding(0, 10, 0, 0);

            layout.addView(space);
            layout.addView(view2);
        }

        new AlertDialog.Builder(Profile.this)
                .setView(layout)
                .setPositiveButton(android.R.string.yes, listener)
                .setNegativeButton(android.R.string.no, null)
                .setCustomTitle(title)
                .show()
                .getWindow().setBackgroundDrawable(getDrawable(R.drawable.bg_alertdialog));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (data == null) {
                return;
            }
            Log.i("AppLog", "Alterando Imagem do usuário");
            Uri imageUri = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                ivImage.setImageDrawable(new BitmapDrawable(bitmap));

                Log.i("AppLog", "Fazendo upload de imagem...");
                // TODO Animação durante upload;
                final StorageReference ref = FirebaseStorage.getInstance().getReference("/profile-images/" + user.getUserId());
                ref.putFile(imageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Log.i("AppLog", "Upload de imagem concluído! Url pública: " + uri.toString());
                                        FirebaseFirestore.getInstance().collection("users")
                                                .document(user.getUserId())
                                                .update("profileImage", uri.toString())
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(getApplicationContext(), getString(R.string.changeImageSuccess), Toast.LENGTH_SHORT).show();
                                                        Log.i("AppLog", getString(R.string.changeImageSuccess));
                                                    }
                                                });
                                    }
                                });
                            }
                        });
            } catch (IOException e) {
                // TODO Catch Block
            }
        }
    }

    public void changeImage(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 0);
    }

    public void changeUsername(View view) {
        final EditText input = new EditText(this);
        input.setBackground(getDrawable(R.drawable.bg_edittext));
        input.setText(user.getUsername());
        input.setPadding(15, 20, 15, 20);
        input.setLayoutParams(lp);
        createAlertDialog(getString(R.string.changeUsername), input, null, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                tvUsername.setText(input.getText().toString());
                Log.i("AppLog", "Alterando nome de usuário...");
                FirebaseFirestore.getInstance().collection("users")
                        .document(user.getUserId()).update("username", input.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getApplicationContext(), getString(R.string.changeUsernameSuccess), Toast.LENGTH_SHORT).show();
                                Log.i("AppLog", getString(R.string.changeUsernameSuccess));
                            }
                        });
            }
        });
    }

    public void changePassword(View view) {
        final EditText inputPassword = new EditText(this);
        inputPassword.setBackground(getDrawable(R.drawable.bg_edittext));
        inputPassword.setHint(getString(R.string.currentPassword));
        inputPassword.setPadding(15, 20, 15, 20);
        inputPassword.setLayoutParams(lp);

        final EditText inputNewPassword = new EditText(Profile.this);
        inputNewPassword.setBackground(getDrawable(R.drawable.bg_edittext));
        inputNewPassword.setHint(getString(R.string.newPassword));
        inputNewPassword.setPadding(15, 20, 15, 20);
        inputNewPassword.setLayoutParams(lp);

        createAlertDialog(getString(R.string.changePassword), inputPassword, inputNewPassword, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i("AppLog", "Alterando senha...");
                final String password = inputPassword.getText().toString();
                final String newPassword = inputNewPassword.getText().toString();
                if (!newPassword.isEmpty()) {
                    FirebaseAuth.getInstance().getCurrentUser()
                            .reauthenticate(EmailAuthProvider.getCredential(user.getEmail(), password))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    FirebaseAuth.getInstance().getCurrentUser()
                                            .updatePassword(newPassword)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    FirebaseFirestore.getInstance().collection("users")
                                                            .document(user.getUserId())
                                                            .update("password", Hash.code(newPassword))
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Toast.makeText(getApplicationContext(), getString(R.string.changePasswordSuccess), Toast.LENGTH_SHORT).show();
                                                                    Log.i("AppLog", getString(R.string.changePasswordSuccess));
                                                                }
                                                            });
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // TODO Exibir novamente o AlertDialog
                                                    Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                                    Log.i("AppLog", e.getLocalizedMessage());
                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // TODO Exibir novamente o AlertDialog
                                    Toast.makeText(getApplicationContext(), getString(R.string.invalidPassword), Toast.LENGTH_SHORT).show();
                                    Log.i("AppLog", getString(R.string.invalidPassword) + e.getLocalizedMessage());
                                }
                            });
                } else {
                    // TODO Exibir novamente o AlertDialog
                    Toast.makeText(getApplicationContext(), getString(R.string.emptyPassword), Toast.LENGTH_SHORT).show();
                    Log.i("AppLog", getString(R.string.emptyPassword));
                }
            }
        });
    }

    public void changeDados(View view) {
        Intent intent = new Intent(Profile.this, Join.class);
        startActivity(intent);
    }

    public void deleteAccount(View view) {
        // TODO Arrumar sabagaça kkkkkk Separar em funções e apagar dados apenas se existirem
        final TextView message = new TextView(this);
        message.setText(getString(R.string.deleteAccountConfirm));
        message.setTextSize(16F);
        message.setLayoutParams(lp);
        createAlertDialog(getString(R.string.deleteAccount), message, null, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Deletar conta
                deleteData();
            }
        });
    }

    public void deleteData() {
        Log.i("AppLog", "1/5 - Apagando data...");
        FirebaseFirestore.getInstance().collection("data").document(user.getUserId())
                .collection("data").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        final List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                        i = 0;
                        for (DocumentSnapshot doc : docs) {
                            Log.i("AppLog", "    [" + ++i + "/" + docs.size() + "] - " + doc.getReference().getPath());
                            doc.getReference().delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (docs.size() == i) {
                                                deleteMessages();
                                            }
                                        }
                                    });
                        }
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (!task.isSuccessful()) {
                            Log.i("AppLog", "Erro: " + task.getException().getLocalizedMessage());
                        }
                    }
                });
    }

    public void deleteMessages() {
        Log.i("AppLog", "2/5 - Apagando mensagens...");
        FirebaseFirestore.getInstance().collection("data").document(user.getUserId())
                .collection("last-messages").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        i = 0;
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()) {
                                deleteUser();
                            }
                            final List<DocumentSnapshot> docs = task.getResult().getDocuments();
                            mensagens = Math.max(mensagens, docs.size());
                            for (DocumentSnapshot doc : docs) {
                                Log.i("AppLog", "    [" + ++i + "/" + docs.size() + "] - " + doc.getReference().getPath());
                                final String path = doc.getReference().getPath()
                                        .substring(doc.getReference().getPath().lastIndexOf("/"));
                                doc.getReference().delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                FirebaseFirestore.getInstance().collection("data")
                                                        .document(user.getUserId()).collection("messages")
                                                        .document(path).collection("all").get()
                                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onSuccess(final QuerySnapshot queryDocumentSnapshots) {
                                                                final List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                                                                j = 0;
                                                                for (final DocumentSnapshot doc : docs) {
                                                                    Log.i("AppLog", "        [" + ++j + "/" + docs.size() + "] - " + doc.getReference().getPath());
                                                                    doc.getReference().delete()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    FirebaseFirestore.getInstance().collection("data")
                                                                                            .document(user.getUserId()).get()
                                                                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull final Task<DocumentSnapshot> task) {
                                                                                                    new Handler().postDelayed(new Runnable() {
                                                                                                        @Override
                                                                                                        public void run() {
                                                                                                            if (!task.getResult().exists()) {
                                                                                                                deleteUser();
                                                                                                            }
                                                                                                        }
                                                                                                    }, 5000);
                                                                                                }
                                                                                            });
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        });
                            }
                        } else {
                            Log.i("AppLog", "Erro: " + task.getException().getLocalizedMessage());
                            deleteUser();
                        }
                    }
                });
    }

    public void deleteUser() {
        Log.i("AppLog", "3/5 - Apagando usuário...");
        FirebaseFirestore.getInstance().collection("users").document(user.getUserId())
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Log.i("AppLog", "Erro: " + task.getException().getLocalizedMessage());
                        }

                        deleteMedia();
                    }
                });

    }

    public void deleteMedia() {
        Log.i("AppLog", "4/5 - Apagando arquivos...");
        FirebaseStorage.getInstance().
                getReference("/profile-images/" + user.getUserId()).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Log.i("AppLog", "Erro: " + task.getException().getLocalizedMessage());
                        }

                        deleteAuth();
                    }
                });
    }

    public void deleteAuth() {
        Log.i("AppLog", "5/5 - Finalizando...");
        // TODO Autenticação não é excluída
        FirebaseAuth.getInstance().getCurrentUser().delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Log.i("AppLog", "Erro: " + task.getException().getLocalizedMessage());
                        }
                        FirebaseAuth.getInstance().signOut();
                        Log.i("AppLog", "Conta excluída com sucesso!");
                        Intent intent = new Intent(Profile.this, Login.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        System.exit(0); // TODO Arrumar isso pq ainda não entendi pq não bugou 2
                    }
                });
    }
}
