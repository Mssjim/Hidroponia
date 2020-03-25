package br.mssjim.hidroponia;

import android.app.Activity;
import android.content.res.Resources;

public class Roles {
    // TODO Achar nomes mais especificos pros atributos
    // TODO Alterar valores de strings caso altere os Atributos

    private boolean cultivador;
    private boolean comerciante;
    private boolean hortifruti;
    private boolean staff;

    public Roles() {
    }

    public Roles(boolean cultivador, boolean mercador, boolean hortifruti) {
        this.cultivador = cultivador;
        this.comerciante = mercador;
        this.hortifruti = hortifruti;
    }
    
    public String getRole() {
        if(staff)
            return Resources.getSystem().getString(R.string.role_staff);
        if(hortifruti)
            return Resources.getSystem().getString(R.string.role_hortifruti);
        if(comerciante)
            return Resources.getSystem().getString(R.string.role_comerciante);
        if(cultivador)
            return Resources.getSystem().getString(R.string.role_cultivador);

        return Resources.getSystem().getString(R.string.role_default);
    }

    public boolean isCultivador() {
        return cultivador;
    }

    public void setCultivador(boolean cultivador) {
        this.cultivador = cultivador;
    }

    public boolean isMercador() {
        return comerciante;
    }

    public void setMercador(boolean mercador) {
        this.comerciante = mercador;
    }

    public boolean isHortifruti() {
        return hortifruti;
    }

    public void setHortifruti(boolean hortifruti) {
        this.hortifruti = hortifruti;
    }

    public boolean isStaff() {
        return staff;
    }

    public void setStaff(boolean staff) {
        this.staff = staff;
    }
}
