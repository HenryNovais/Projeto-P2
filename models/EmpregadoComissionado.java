package models;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Empregado assalariado que tambem recebe comissao sobre vendas (por isso
 * estende EmpregadoAssalariado em vez de Empregado diretamente, ver
 * comentario em EmpregadoAssalariado). Guarda o percentual de comissao e
 * a lista de vendas realizadas, usada para calcular a parte variavel do
 * salario na folha de pagamento.
 */
public class EmpregadoComissionado extends EmpregadoAssalariado {
    private String comissao;
    private List<ResultadoVenda> vendas;

    public EmpregadoComissionado(String id, String nome, String endereco, String salarioMensal, String comissao) {
        super(id, nome, endereco, salarioMensal);
        this.setTipo("comissionado");
        this.comissao = comissao;
        this.vendas = new ArrayList<>();
    }

    @Override
    public String getComissao() { return this.comissao; }
    @Override
    public void setComissao(String comissao) { this.comissao = comissao; }

    @Override
    public void addVenda(ResultadoVenda venda) { this.vendas.add(venda); }
    @Override
    public List<ResultadoVenda> getVendas() { return this.vendas; }

    @Override
    public boolean devePagar(LocalDate data) {
        if (data.getDayOfWeek().getValue() != 5) return false; // sexta-feira
        int dia = data.getDayOfMonth();
        return (dia > 7 && dia <= 14) || (dia > 21 && dia <= 28);
    }

    @Override
    public int diasParaTaxaSindical(LocalDate dataPagamento) {
        // Diferente do horista, o comissionado nunca fica sem bruto (o
        // fixo garante isso), entao o periodo e sempre a quinzena cheia,
        // nao precisa de "dias desde o ultimo pagamento" pra acumular
        // periodo perdido, porque nunca ha periodo perdido.
        return 14;
    }

    @Override
    public String getCategoriaFolha() { return "COMISSIONADOS"; }

    @Override
    public DadosPagamento calcularBruto(LocalDate dataPagamento) {
        LocalDate desde = getDataUltimoPagamento();
        double salario = parseValor(getSalario());
        double pct = parseValor(comissao);
        // ano tem 52 semanas / 26 quinzenas => base por quinzena = salario*12/52*2
        // truncado (nao arredondado) a centavos, para bater com o valor
        // esperado no relatorio (ex.: 1500*24/52 = 692,3076... -> 692,30)
        double fixo = truncar(salario * 24.0 / 52.0);
        double vendasPeriodo = 0;
        for (ResultadoVenda v : vendas) {
            LocalDate d = v.getDataComoLocalDate();
            if ((desde == null || d.isAfter(desde)) && !d.isAfter(dataPagamento)) {
                vendasPeriodo += v.getValor();
            }
        }
        double comissaoValor = truncar(vendasPeriodo * pct);
        double bruto = fixo + comissaoValor;
        return new DadosPagamento(bruto, fixo, vendasPeriodo, comissaoValor, 0, 0);
    }
}