package models;

public class EmpregadoAssalariado extends Empregado {
    protected String salarioMensal;

    public EmpregadoAssalariado(String id, String nome, String endereco, String salarioMensal) {
        super(id, nome, endereco, "assalariado");
        this.salarioMensal = salarioMensal;
    }

    @Override
    public String getSalario() {
        return this.salarioMensal;
    }
}