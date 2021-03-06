package br.mssjim.hidroponia;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Hidroponia extends Application implements Application.ActivityLifecycleCallbacks {
    private static User user;
    private static Roles roles;
    private static Dados dados;

    private int i;

    @Override
    public void onCreate() {
        Log.e("AppLog", "Aplicação Iniciada");
        registerActivityLifecycleCallbacks(this); // TODO Talvez seja desnecessário
        super.onCreate();
    }

    public static void logout() {
        Hidroponia.setStatus(false);
        setUser(null);
        setRoles(null);
        setDados(null);
        FirebaseAuth.getInstance().signOut();
    }

    public static void setStatus(final boolean online) {
        if(user != null) { // TODO Serve para quando a conta for excluida e ele voltar aki não bugar
            Log.i("AppLog", "Atualizando status do usuário...");
            FirebaseFirestore.getInstance().collection("/data")
                    .document(user.getUserId()).collection("data")
                    .document("status")
                    .set(new Status(online, System.currentTimeMillis()))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i("AppLog", "Status atualizado com sucesso! (" + online + ")");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i("AppLog", "Erro: " + e.getLocalizedMessage());
                            // TODO Catch Block
                        }
                    });
        } else {
            Log.i("AppLog", "user == null"); // TODO Excluir (Teste)
        }
    }

    @Override
    public void onTerminate() {
        Log.e("AppLog", "Aplicação Terminada");
        super.onTerminate();
    }

    @Override
    public void onLowMemory() {
        Log.e("AppLog", "onLowMemory");
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        // TODO Garbage collector
        switch (level) {
            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:
                Log.e("AppLog", "onTrimMemory: UI_Hidden (20)");
                break;
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
                Log.e("AppLog", "onTrimMemory: Running_Moderate (5)");
                break;
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
                Log.e("AppLog", "onTrimMemory: Running_Low (10)");
                break;
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:
                Log.e("AppLog", "onTrimMemory: Running_Critical (15)");
                break;
            case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
                Log.e("AppLog", "onTrimMemory: Background (40)");
                break;
            case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
                Log.e("AppLog", "onTrimMemory: Moderate (60)");
                break;
            case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:
                Log.e("AppLog", "onTrimMemory: Complete (80)");
                break;
        }
    }

    public static User getUser() {
        // TODO Evitar Null Exceptions
        return user;
    }

    public static Roles getRoles() {
        // TODO Evitar Null Exceptions
        return roles;
    }

    public static Dados getDados() {
        // TODO Evitar Null Exceptions
        return dados;
    }

    public static void setUser(User user) {
        Hidroponia.user = user;
    }

    public static void setRoles(Roles roles) {
        Hidroponia.roles = roles;
    }

    public static void setDados(Dados dados) {
        Hidroponia.dados = dados;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if (i == 0) {
            Log.e("AppLog", "Aplicação em primeiro plano");
            if(Hidroponia.getUser() != null) {
                Hidroponia.setStatus(true);
            }
        }
        i++;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        i--;
        if (i == 0) {
            Log.e("AppLog", "Aplicação em segundo plano");
            if(Hidroponia.getUser() != null) {
                Hidroponia.setStatus(false);
            }
        }
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }
}
