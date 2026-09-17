package models;

public class CartaoDePonto {
    private String data;
    private double horas;

    public CartaoDePonto(String data, double horas) {
        this.data = data;
        this.horas = horas;
    }

    public String getData() { return data; }
    public double getHoras() { return horas; }
}