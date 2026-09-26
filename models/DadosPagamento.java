package models;

/**
 * Carrega o resultado do calculo de salario bruto de um empregado num
 * periodo, feito por ele mesmo (ver Empregado.calcularBruto). Existe pra
 * o Facade conseguir montar o relatorio de folha (que mostra colunas
 * diferentes pra cada tipo: horas para horista, fixo/vendas/comissao para
 * comissionado) sem precisar descobrir de que subtipo cada Empregado eh.
 */
public class DadosPagamento {
    public final double bruto;
    public final double fixo;
    public final double vendas;
    public final double comissaoValor;
    public final double horasNormais;
    public final double horasExtras;

    public DadosPagamento(double bruto, double fixo, double vendas, double comissaoValor,
                           double horasNormais, double horasExtras) {
        this.bruto = bruto;
        this.fixo = fixo;
        this.vendas = vendas;
        this.comissaoValor = comissaoValor;
        this.horasNormais = horasNormais;
        this.horasExtras = horasExtras;
    }
}