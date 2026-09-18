package models;
import java.util.ArrayList;
import java.util.List;

public class EmpregadoComissionado extends EmpregadoAssalariado{
    private String comissao;
    private List<ResultadoVenda> vendas;

    public EmpregadoComissionado(String id, String nome, String endereco, String salarioMensal, String comissao) {
        super(id, nome, endereco, salarioMensal);
        this.setTipo("comissionado");
        this.comissao = comissao;
        this.vendas = new ArrayList<>();
    }
    
    public String getComissao() { return this.comissao; }
    public void setComissao(String comissao) { this.comissao = comissao; }

    public void addVenda(ResultadoVenda venda) { this.vendas.add(venda); }
    public List<ResultadoVenda> getVendas() { return this.vendas; }
}