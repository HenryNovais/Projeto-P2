package models;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Empregado pago por hora trabalhada, com base num salario por hora
 * (salarioPorHora) e numa lista de cartoes de ponto batidos ao longo do
 * tempo. E o unico tipo de empregado cuja folha de pagamento pode dar
 * zero num periodo (quando nao ha cartao lancado), diferente do
 * assalariado e do comissionado, que sempre recebem um valor fixo.
 */
public class EmpregadoHorista extends Empregado {
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

    @Override
    public void addCartao(CartaoDePonto cartao) {
        // "empregados horistas sao contratados no primeiro dia em que lancarem um cartao"
        if (getDataContratacao() == null) {
            setDataContratacao(cartao.getDataComoLocalDate());
        }
        this.cartoes.add(cartao);
    }

    @Override
    public List<CartaoDePonto> getCartoes() { return this.cartoes; }

    @Override
    public boolean devePagar(LocalDate data) {
        return data.getDayOfWeek().getValue() == 5; // sexta-feira
    }

    @Override
    public int diasParaTaxaSindical(LocalDate dataPagamento) {
        // Horista pode "pular" semanas sem cartao (calcularBruto da bruto=0
        // nelas, entao o Facade nao efetiva pagamento). Quando finalmente
        // ha um pagamento real de novo, a taxa sindical das semanas
        // puladas tem que ser cobrada junto, por isso usamos dias
        // corridos desde o ultimo pagamento REAL (nao um valor fixo de 7),
        // com fallback pra data de contratacao (+1, pois o dia da
        // contratacao conta) na primeira vez que ele e pago.
        LocalDate desde = getDataUltimoPagamento();
        long dias = (desde != null)
                ? ChronoUnit.DAYS.between(desde, dataPagamento)
                : ChronoUnit.DAYS.between(getDataContratacao(), dataPagamento) + 1;
        return (int) dias;
    }

    @Override
    public String getCategoriaFolha() { return "HORISTAS"; }

    @Override
    public DadosPagamento calcularBruto(LocalDate dataPagamento) {
        LocalDate desde = getDataUltimoPagamento();
        double taxa = parseValor(getSalario());
        double horasNormais = 0, horasExtras = 0, bruto = 0;
        for (CartaoDePonto card : cartoes) {
            LocalDate d = card.getDataComoLocalDate();
            boolean dentroDoPeriodo = (desde == null || d.isAfter(desde)) && !d.isAfter(dataPagamento);
            if (dentroDoPeriodo) {
                double horas = card.getHoras();
                double normais = Math.min(horas, 8);
                double extras = Math.max(0, horas - 8);
                horasNormais += normais;
                horasExtras += extras;
                bruto += normais * taxa + extras * taxa * 1.5;
            }
        }
        return new DadosPagamento(bruto, 0, 0, 0, horasNormais, horasExtras);
    }
}