package models;

public abstract class Empregado {
    private String id;
    private String nome;
    private String endereco;
    private String tipo;
    private boolean sindicalizado; 

    public Empregado(String id, String nome, String endereco, String tipo) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.tipo = tipo;
        this.sindicalizado = false; 
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getEndereco() { return endereco; }
    public String getTipo() { return tipo; }
    public boolean isSindicalizado() { return sindicalizado; } 

    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setNome(String nome) { this.nome = nome; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public void setSindicalizado(boolean sindicalizado) { this.sindicalizado = sindicalizado; }

    public abstract String getSalario();
}