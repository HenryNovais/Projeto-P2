package models;
import java.util.ArrayList;
import java.util.List;

/**
 * Empregado assalariado que tambem recebe comissao sobre vendas (por isso
 * estende EmpregadoAssalariado em vez de Empregado diretamente -- ver
 * comentario em EmpregadoAssalariado). Guarda o percentual de comissao e
 * a lista de vendas realizadas, usada para calcular a parte variavel do
 * salario na folha de pagamento.
 */
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