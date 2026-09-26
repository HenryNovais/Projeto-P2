package models;

/**
 * Empregado com salario fixo mensal, pago no ultimo dia util do mes.
 * EmpregadoComissionado herda desta classe porque um comissionado tambem
 * tem um salario mensal fixo como base -- ele so soma comissao de vendas
 * em cima disso -- entao reaproveitar o campo salarioMensal evita
 * duplicar essa parte da modelagem.
 */
public class EmpregadoAssalariado extends Empregado{
    protected String salarioMensal;

    public EmpregadoAssalariado(String id, String nome, String endereco, String salarioMensal) {
        super(id, nome, endereco, "assalariado");
        this.salarioMensal = salarioMensal;
    }

    @Override
    public String getSalario() { return this.salarioMensal; }
    @Override
    public void setSalario(String salario) { this.salarioMensal = salario; }
}