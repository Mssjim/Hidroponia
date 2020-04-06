package br.mssjim.hidroponia;

import android.app.Activity;
import android.content.res.Resources;

public class Roles {
    private boolean farm;
    private boolean sale;
    private boolean organic;
    private boolean store;
    private boolean staff;

    public Roles() {
    }

    public Roles(boolean farm, boolean sale, boolean organic, boolean store) {
        this.farm = farm;
        this.sale = sale;
        this.organic = organic;
        this.store = store;
    }

    public String getRole() {
        if(staff)
            return Resources.getSystem().getString(R.string.role_staff);
        if(farm)
            return Resources.getSystem().getString(R.string.role_farm);
        if(sale)
            return Resources.getSystem().getString(R.string.role_sale);
        if(organic)
            return Resources.getSystem().getString(R.string.role_organic);
        if(store)
            return Resources.getSystem().getString(R.string.role_store);
        return Resources.getSystem().getString(R.string.role_default);
    }

    public boolean isFarm() {
        return farm;
    }

    public void setFarm(boolean farm) {
        this.farm = farm;
    }

    public boolean isSale() {
        return sale;
    }

    public void setSale(boolean sale) {
        this.sale = sale;
    }

    public boolean isOrganic() {
        return organic;
    }

    public void setOrganic(boolean organic) {
        this.organic = organic;
    }

    public boolean isStore() {
        return store;
    }

    public void setStore(boolean store) {
        this.store = store;
    }

    public boolean isStaff() {
        return staff;
    }

    public void setStaff(boolean staff) {
        this.staff = staff;
    }
}
