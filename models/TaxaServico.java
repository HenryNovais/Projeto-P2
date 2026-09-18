package models;
import java.io.Serializable;

public class TaxaServico implements Serializable {
    private String data;
    private double valor;
    public TaxaServico(String data, double valor) { this.data = data; this.valor = valor; }
    public String getData() { return data; }
    public double getValor() { return valor; }
}