package models;
import java.io.Serializable;

/**
 * Registro de um dia trabalhado por um empregado horista: a data e a
 * quantidade de horas batidas no cartao de ponto naquele dia.
 * Fica guardado indefinidamente na lista do EmpregadoHorista -- nunca e
 * apagado, mesmo depois de entrar numa folha de pagamento, para que o
 * historico (getHorasNormaisTrabalhadas/getHorasExtrasTrabalhadas) continue
 * correto mesmo apos varios pagamentos.
 */
public class CartaoDePonto implements Serializable {
    private String data;
    private double horas;
    public CartaoDePonto(String data, double horas) { this.data = data; this.horas = horas; }
    public String getData() { return data; }
    public double getHoras() { return horas; }
}