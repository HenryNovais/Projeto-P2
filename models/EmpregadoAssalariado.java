package models;
import java.time.LocalDate;

/**
 * Empregado com salario fixo mensal, pago no ultimo dia util do mes.
 * EmpregadoComissionado herda desta classe porque um comissionado tambem
 * tem um salario mensal fixo como base, ele so soma comissao de vendas
 * em cima disso, entao reaproveitar o campo salarioMensal evita
 * duplicar essa parte da modelagem.
 */
public class EmpregadoAssalariado extends Empregado {
    protected String salarioMensal;

    public EmpregadoAssalariado(String id, String nome, String endereco, String salarioMensal) {
        super(id, nome, endereco, "assalariado");
        this.salarioMensal = salarioMensal;
    }

    @Override
    public String getSalario() { return this.salarioMensal; }
    @Override
    public void setSalario(String salario) { this.salarioMensal = salario; }

    @Override
    public boolean devePagar(LocalDate data) {
        return isUltimoDiaUtil(data);
    }

    private boolean isUltimoDiaUtil(LocalDate data) {
        LocalDate ultimo = data.withDayOfMonth(data.lengthOfMonth());
        while (ultimo.getDayOfWeek().getValue() >= 6) { // sabado=6, domingo=7
            ultimo = ultimo.minusDays(1);
        }
        return data.equals(ultimo);
    }

    @Override
    public int diasParaTaxaSindical(LocalDate dataPagamento) {
        // Assalariado nunca "pula" pagamento (bruto sempre > 0, e' o
        // salario fixo), entao usamos a duracao real do mes. Isso tambem
        // garante que rodar a folha duas vezes na MESMA data da o mesmo
        // resultado, uma formula baseada em "dias desde o ultimo
        // pagamento" zeraria o desconto na segunda chamada, ja que a data
        // do ultimo pagamento passaria a ser igual a data atual.
        return dataPagamento.lengthOfMonth();
    }

    @Override
    public String getCategoriaFolha() { return "ASSALARIADOS"; }

    @Override
    public DadosPagamento calcularBruto(LocalDate dataPagamento) {
        double bruto = parseValor(getSalario());
        return new DadosPagamento(bruto, 0, 0, 0, 0, 0);
    }
}