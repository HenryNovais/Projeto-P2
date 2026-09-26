package models;
import java.util.ArrayList;
import java.util.List;

/**
 * Empregado pago por hora trabalhada, com base num salario por hora
 * (salarioPorHora) e numa lista de cartoes de ponto batidos ao longo do
 * tempo. E o unico tipo de empregado cuja folha de pagamento pode dar
 * zero num periodo (quando nao ha cartao lancado), diferente do
 * assalariado e do comissionado, que sempre recebem um valor fixo.
 */
public class EmpregadoHorista extends Empregado{
    private String salarioPorHora;
    private List<CartaoDePonto> cartoes;

    public EmpregadoHorista(String id, String nome, String endereco, String salarioPorHora) {
        super(id, nome, endereco, "horista");
        this.salarioPorHora = salarioPorHora;
        this.cartoes = new ArrayList<>();
    }

    @Override
    public String getSalario() { return this.salarioPorHora; }
    @Override
    public void setSalario(String salario) { this.salarioPorHora = salario; }

    public void addCartao(CartaoDePonto cartao) { this.cartoes.add(cartao); }
    public List<CartaoDePonto> getCartoes() { return this.cartoes; }
}