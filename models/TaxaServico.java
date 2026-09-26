package models;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * Taxa de servico avulsa cobrada pelo sindicato de um empregado
 * sindicalizado (ex: uso de uma creche, um servico extra). Diferente da
 * taxa sindical fixa (que e um valor por dia guardado no Empregado), cada
 * taxa de servico e um lancamento pontual com data e valor proprios, e
 * entra como desconto na folha do periodo em que foi lancada.
 */
public class TaxaServico implements Serializable {
    private String data;
    private double valor;
    public TaxaServico(String data, double valor) { this.data = data; this.valor = valor; }
    public String getData() { return data; }
    public double getValor() { return valor; }

    public LocalDate getDataComoLocalDate() {
        String[] p = data.split("/");
        return LocalDate.of(Integer.parseInt(p[2]), Integer.parseInt(p[1]), Integer.parseInt(p[0]));
    }
}