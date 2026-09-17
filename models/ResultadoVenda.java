package models;

public class ResultadoVenda {
    private String data;
    private double valor;

    public ResultadoVenda(String data, double valor) {
        this.data = data;
        this.valor = valor;
    }

    public String getData() { return data; }
    public double getValor() { return valor; }
}