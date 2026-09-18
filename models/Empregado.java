package models;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Empregado implements Serializable {
    private String id;
    private String nome;
    private String endereco;
    private String tipo;
    private boolean sindicalizado;
    private String idSindicato;
    private double taxaSindical;
    private List<TaxaServico> taxasServico;
    private String metodoPagamento;
    private String banco;
    private String agencia;
    private String contaCorrente;

    public Empregado(String id, String nome, String endereco, String tipo) {
        this.id = id; this.nome = nome; this.endereco = endereco; this.tipo = tipo;
        this.sindicalizado = false; this.taxasServico = new ArrayList<>(); this.metodoPagamento = "emMaos";
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getEndereco() { return endereco; }
    public String getTipo() { return tipo; }
    public boolean isSindicalizado() { return sindicalizado; }

    public void setNome(String nome) { this.nome = nome; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setSindicalizado(boolean sindicalizado) { this.sindicalizado = sindicalizado; }

    public String getIdSindicato() { return idSindicato; }
    public void setIdSindicato(String idSindicato) { this.idSindicato = idSindicato; }
    public double getTaxaSindical() { return taxaSindical; }
    public void setTaxaSindical(double taxaSindical) { this.taxaSindical = taxaSindical; }
    public void addTaxaServico(TaxaServico taxa) { this.taxasServico.add(taxa); }
    public List<TaxaServico> getTaxasServico() { return this.taxasServico; }

    public String getMetodoPagamento() { return metodoPagamento; }
    public void setMetodoPagamento(String metodoPagamento) { this.metodoPagamento = metodoPagamento; }
    public String getBanco() { return banco; }
    public void setBanco(String banco) { this.banco = banco; }
    public String getAgencia() { return agencia; }
    public void setAgencia(String agencia) { this.agencia = agencia; }
    public String getContaCorrente() { return contaCorrente; }
    public void setContaCorrente(String contaCorrente) { this.contaCorrente = contaCorrente; }

    public abstract String getSalario();
    public abstract void setSalario(String salario);
}