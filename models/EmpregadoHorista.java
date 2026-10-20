package models;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/** Empregado cuja remuneracao resulta dos cartoes de ponto. */
public class EmpregadoHorista extends Empregado {
    private static final long serialVersionUID = -4210639985962258050L;
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
        if (getDataContratacao() == null) {
            setDataContratacao(cartao.getDataComoLocalDate());
        }
        this.cartoes.add(cartao);
    }

    @Override
    public List<CartaoDePonto> getCartoes() { return this.cartoes; }

    @Override
    public int diasParaTaxaSindical(LocalDate dataPagamento) {
        LocalDate desde = getDataUltimoPagamento();
        LocalDate contratacao = getDataContratacao();
        if (desde != null) {
            return (int) ChronoUnit.DAYS.between(desde, dataPagamento);
        }
        if (contratacao == null) return 0;
        return (int) ChronoUnit.DAYS.between(contratacao, dataPagamento) + 1;
    }

    @Override
    public String getCategoriaFolha() { return "HORISTAS"; }

    @Override
    public DadosPagamento calcularBruto(LocalDate dataPagamento) {
        LocalDate desde = getDataUltimoPagamento();
        LocalDate limiteAgenda = getAgendaPagamento().inicioPeriodo(dataPagamento);
        if (desde == null || desde.isBefore(limiteAgenda)) desde = limiteAgenda;
        double taxa = parseValor(getSalario());
        double horasNormais = 0, horasExtras = 0, bruto = 0;
        for (CartaoDePonto card : cartoes) {
            LocalDate d = card.getDataComoLocalDate();
            boolean dentro = (desde == null || d.isAfter(desde)) && !d.isAfter(dataPagamento);
            if (dentro) {
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
