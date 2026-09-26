package models;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class Empregado implements Serializable {
    private String id;
    private String nome;
    private String endereco;
    private String tipo;
    private boolean sindicalizado;
    private String idSindicato;
    private double taxaSindical;
    private List<TaxaServico> taxasServico;
    private String metodoPagamento;
    private String banco;
    private String agencia;
    private String contaCorrente;
    // Data do ultimo pagamento efetivado (null = nunca foi pago).
    // NAO apagamos cartoes/vendas/taxas ao pagar: usamos essa data como
    // marca d'agua para saber o que ja entrou em folha, mantendo o
    // historico consultavel (getHorasTrabalhadas, getVendasRealizadas, ...)
    // e o undo/redo consistentes.
    private LocalDate dataUltimoPagamento;
    // Data de contratacao (regra simplificada, ja que ainda nao existe um
    // comando para informa-la): horista = data do primeiro
    // cartao lancado; assalariado/comissionado = 1/1/2005, atribuido pela
    // Facade na criacao.
    private LocalDate dataContratacao;

    public Empregado(String id, String nome, String endereco, String tipo) {
        this.id = id; this.nome = nome; this.endereco = endereco; this.tipo = tipo;
        this.sindicalizado = false; this.taxasServico = new ArrayList<>(); this.metodoPagamento = "emMaos";
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getEndereco() { return endereco; }
    public String getTipo() { return tipo; }
    public boolean isSindicalizado() { return sindicalizado; }

    public void setNome(String nome) { this.nome = nome; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setSindicalizado(boolean sindicalizado) { this.sindicalizado = sindicalizado; }

    public String getIdSindicato() { return idSindicato; }
    public void setIdSindicato(String idSindicato) { this.idSindicato = idSindicato; }
    public double getTaxaSindical() { return taxaSindical; }
    public void setTaxaSindical(double taxaSindical) { this.taxaSindical = taxaSindical; }
    public void addTaxaServico(TaxaServico taxa) { this.taxasServico.add(taxa); }
    public List<TaxaServico> getTaxasServico() { return this.taxasServico; }

    public String getMetodoPagamento() { return metodoPagamento; }
    public void setMetodoPagamento(String metodoPagamento) { this.metodoPagamento = metodoPagamento; }
    public String getBanco() { return banco; }
    public void setBanco(String banco) { this.banco = banco; }
    public String getAgencia() { return agencia; }
    public void setAgencia(String agencia) { this.agencia = agencia; }
    public String getContaCorrente() { return contaCorrente; }
    public void setContaCorrente(String contaCorrente) { this.contaCorrente = contaCorrente; }

    public LocalDate getDataUltimoPagamento() { return dataUltimoPagamento; }
    public void setDataUltimoPagamento(LocalDate data) { this.dataUltimoPagamento = data; }
    public LocalDate getDataContratacao() { return dataContratacao; }
    public void setDataContratacao(LocalDate data) { this.dataContratacao = data; }

    public abstract String getSalario();
    public abstract void setSalario(String salario);

 

    /** Este empregado deve ser pago na data informada? (regra de agenda de cada tipo) */
    public abstract boolean devePagar(LocalDate data);

    /** Calcula o salario bruto (e detalhes: horas/vendas/comissao/fixo) deste
     *  empregado no periodo que termina em dataPagamento. */
    public abstract DadosPagamento calcularBruto(LocalDate dataPagamento);

    /** Quantos dias contam para a taxa sindical deste pagamento (a duracao
     *  do "periodo" e diferente pra cada tipo de agenda). */
    public abstract int diasParaTaxaSindical(LocalDate dataPagamento);

    /** Secao do relatorio de folha em que este empregado deve ser listado
     *  ("HORISTAS", "ASSALARIADOS" ou "COMISSIONADOS"). */
    public abstract String getCategoriaFolha();

    // Operacoes que so fazem sentido para alguns subtipos (cartao de ponto
    // e so de horista; venda e comissao sao so de comissionado). Por
    // padrao todas recusam com a mensagem de erro correta; o subtipo que
    // realmente suporta a operacao sobrescreve o metodo. Assim o Facade
    // so chama emp.addCartao(...)/emp.getVendas()/etc. sem precisar saber
    // (nem checar) o tipo concreto por tras da referencia Empregado.
    public void addCartao(CartaoDePonto cartao) throws Exception {
        throw new Exception("Empregado nao eh horista.");
    }
    public List<CartaoDePonto> getCartoes() throws Exception {
        throw new Exception("Empregado nao eh horista.");
    }
    public void addVenda(ResultadoVenda venda) throws Exception {
        throw new Exception("Empregado nao eh comissionado.");
    }
    public List<ResultadoVenda> getVendas() throws Exception {
        throw new Exception("Empregado nao eh comissionado.");
    }
    public String getComissao() throws Exception {
        throw new Exception("Empregado nao eh comissionado.");
    }
    public void setComissao(String comissao) throws Exception {
        throw new Exception("Empregado nao eh comissionado.");
    }

    // Helpers de calculo compartilhados por quem precisar (Horista e
    // Comissionado), evitando duplicar parsing/arredondamento em cada um.
    protected static double parseValor(String valor) {
        return Double.parseDouble(valor.replace(",", "."));
    }

    // Trunca (nao arredonda) para 2 casas decimais, usado no calculo do
    // salario fixo/comissao do comissionado, onde a divisao gera dizima.
    protected static double truncar(double valor) {
        return Math.floor(valor * 100 + 1e-9) / 100.0;
    }
}