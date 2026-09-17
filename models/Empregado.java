package models;

public abstract class Empregado {
    private String id;
    private String nome;
    private String endereco;
    private String tipo;

    public Empregado(String id, String nome, String endereco, String tipo) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.tipo = tipo;
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getEndereco() { return endereco; }
    public String getTipo() { return tipo; }

    public abstract String getSalario();
}