package models;

import java.time.LocalDate;

/** Empregado que recebe uma parcela fixa do salario anual por agenda. */
public class EmpregadoAssalariado extends Empregado {
    private static final long serialVersionUID = -7494194105892041507L;
    protected String salarioMensal;

    public EmpregadoAssalariado(String id, String nome, String endereco, String salarioMensal) {
        super(id, nome, endereco, "assalariado");
        this.salarioMensal = salarioMensal;
    }

    @Override
    public String getSalario() { return salarioMensal; }
    @Override
    public void setSalario(String salario) { this.salarioMensal = salario; }

    /** Reutilizado pelos assalariados e pelos comissionados. */
    protected double calcularSalarioFixo() {
        double salario = parseValor(getSalario());
        if (getAgendaConfigurada().isMensal()) return salario;
        return truncar(salario * getAgendaConfigurada().fatorSalarioMensal());
    }

    @Override
    public int diasParaTaxaSindical(LocalDate dataPagamento) {
        return getAgendaConfigurada().diasParaTaxaSindical(dataPagamento);
    }

    @Override
    public String getCategoriaFolha() { return "ASSALARIADOS"; }

    @Override
    public DadosPagamento calcularBruto(LocalDate dataPagamento) {
        return new DadosPagamento(calcularSalarioFixo(), 0, 0, 0, 0, 0);
    }
}
