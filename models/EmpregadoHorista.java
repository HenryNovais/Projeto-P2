package models;
import java.util.ArrayList;
import java.util.List;

public class EmpregadoHorista extends Empregado {
    private String salarioPorHora;
    private List<CartaoDePonto> cartoes;

    public EmpregadoHorista(String id, String nome, String endereco, String salarioPorHora) {
        super(id, nome, endereco, "horista");
        this.salarioPorHora = salarioPorHora;
        this.cartoes = new ArrayList<>();
    }

    @Override
    public String getSalario() {
        return this.salarioPorHora;
    }

    public void addCartao(CartaoDePonto cartao) {
        this.cartoes.add(cartao);
    }

    public List<CartaoDePonto> getCartoes() {
        return this.cartoes;
    }
}