package models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Empregado que recebe parcela fixa mais comissoes das vendas do periodo. */
public class EmpregadoComissionado extends EmpregadoAssalariado {
    private static final long serialVersionUID = 6999299652534402279L;
    private String comissao;
    private List<ResultadoVenda> vendas;

    public EmpregadoComissionado(String id, String nome, String endereco,
                                String salarioMensal, String comissao) {
        super(id, nome, endereco, salarioMensal);
        setTipo("comissionado");
        setAgendaPagamento(AgendaPagamento.QUINZENAL);
        this.comissao = comissao;
        this.vendas = new ArrayList<>();
    }

    @Override
    public String getComissao() { return comissao; }
    @Override
    public void setComissao(String comissao) { this.comissao = comissao; }

    @Override
    public void addVenda(ResultadoVenda venda) { vendas.add(venda); }
    @Override
    public List<ResultadoVenda> getVendas() { return vendas; }

    @Override
    public String getCategoriaFolha() { return "COMISSIONADOS"; }

    @Override
    public DadosPagamento calcularBruto(LocalDate dataPagamento) {
        LocalDate desde = getDataUltimoPagamento();
        LocalDate limiteAgenda = getAgendaPagamento().inicioPeriodo(dataPagamento);
        if (desde == null || desde.isBefore(limiteAgenda)) desde = limiteAgenda;
        double fixo = calcularSalarioFixo();
        double vendasPeriodo = 0;
        for (ResultadoVenda venda : vendas) {
            LocalDate dataVenda = venda.getDataComoLocalDate();
            if ((desde == null || dataVenda.isAfter(desde))
                    && !dataVenda.isAfter(dataPagamento)) {
                vendasPeriodo += venda.getValor();
            }
        }
        double valorComissao = truncar(vendasPeriodo * parseValor(comissao));
        return new DadosPagamento(fixo + valorComissao, fixo, vendasPeriodo,
                                  valorComissao, 0, 0);
    }
}
