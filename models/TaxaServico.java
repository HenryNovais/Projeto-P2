package models;

public class TaxaServico {
    private String data;
    private double valor;

    public TaxaServico(String data, double valor) {
        this.data = data;
        this.valor = valor;
    }

    public String getData() { return data; }
    public double getValor() { return valor; }
}