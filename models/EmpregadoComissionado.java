package models;

public class EmpregadoComissionado extends EmpregadoAssalariado {
    private String comissao;

    public EmpregadoComissionado(String id, String nome, String endereco, String salarioMensal, String comissao) {
        super(id, nome, endereco, salarioMensal);
        super.setTipo("comissionado"); // Sobrescreve o tipo da classe pai
        this.comissao = comissao;
    }
    
    public String getComissao() {
        return this.comissao;
    }
}