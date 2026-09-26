package models;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * Registro de um dia trabalhado por um empregado horista: a data e a
 * quantidade de horas batidas no cartao de ponto naquele dia.
 * Fica guardado indefinidamente na lista do EmpregadoHorista: nunca e
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

    /** Interpreta o campo data (formato dd/mm/aaaa) como LocalDate, para
     *  quem for comparar/filtrar por periodo nao precisar parsear string. */
    public LocalDate getDataComoLocalDate() {
        String[] p = data.split("/");
        return LocalDate.of(Integer.parseInt(p[2]), Integer.parseInt(p[1]), Integer.parseInt(p[0]));
    }
}