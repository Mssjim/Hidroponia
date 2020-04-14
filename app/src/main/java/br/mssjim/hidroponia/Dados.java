package br.mssjim.hidroponia;

public class Dados {
    private String userId;
    private String name;
    private String date;
    private int cpf;
    private int phone;
    private String address;
    private int cep;

    public Dados() {
    }

    public Dados(String userId, String name, String date, int cpf, int phone, String address, int cep) {
        this.userId = userId;
        this.name = name;
        this.date = date;
        this.cpf = cpf;
        this.phone = phone;
        this.address = address;
        this.cep = cep;
    }
    public Dados(String userId, String name, int cpf, int phone, String address, int cep) {
        this.userId = userId;
        this.name = name;
        this.date = "";
        this.cpf = cpf;
        this.phone = phone;
        this.address = address;
        this.cep = cep;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public int getCpf() {
        return cpf;
    }

    public int getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public int getCep() {
        return cep;
    }
}
