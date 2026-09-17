package models;

public class EmpregadoHorista extends Empregado {
    private String salarioPorHora;

    public EmpregadoHorista(String id, String nome, String endereco, String salarioPorHora) {
        super(id, nome, endereco, "horista");
        this.salarioPorHora = salarioPorHora;
    }

    @Override
    public String getSalario() {
        return this.salarioPorHora;
    }
}