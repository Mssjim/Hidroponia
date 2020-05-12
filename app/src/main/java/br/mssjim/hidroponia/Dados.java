package br.mssjim.hidroponia;

public class Dados {
    private String name;
    private String date;
    private String cpf;
    private int phone;
    private String address;
    private String cep;

    public Dados() {
    }

    public Dados(String name, String date, String cpf, int phone, String address, String cep) {
        this.name = name;
        this.date = date;
        this.cpf = cpf;
        this.phone = phone;
        this.address = address;
        this.cep = cep;
    }
    public Dados(String name, String cpf, int phone, String address, String cep) {
        this.name = name;
        this.date = "";
        this.cpf = cpf;
        this.phone = phone;
        this.address = address;
        this.cep = cep;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getCpf() {
        return cpf;
    }

    public int getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getCep() {
        return cep;
    }
}
