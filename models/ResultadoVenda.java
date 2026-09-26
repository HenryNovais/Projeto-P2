package models;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * Registro de uma venda realizada por um empregado comissionado: a data e
 * o valor da venda. Assim como o CartaoDePonto, nunca e apagado, fica
 * na lista do EmpregadoComissionado para consulta historica
 * (getVendasRealizadas) mesmo apos ja ter entrado em alguma folha.
 */
public class ResultadoVenda implements Serializable {
    private String data;
    private double valor;
    public ResultadoVenda(String data, double valor) { this.data = data; this.valor = valor; }
    public String getData() { return data; }
    public double getValor() { return valor; }

    public LocalDate getDataComoLocalDate() {
        String[] p = data.split("/");
        return LocalDate.of(Integer.parseInt(p[2]), Integer.parseInt(p[1]), Integer.parseInt(p[0]));
    }
}