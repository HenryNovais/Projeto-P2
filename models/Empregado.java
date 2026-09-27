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
    // Data do ultimo pagamento efetivado: nula quando o empregado nunca
    // foi pago. Nao apagamos cartoes, vendas nem taxas ao pagar, entao
    // usamos essa data como marca d'agua para saber o que ja entrou em
    // folha, mantendo o historico consultavel e o undo e redo consistentes.
    private LocalDate dataUltimoPagamento;
    // Data de contratacao. Regra simplificada, ja que ainda nao existe um
    // comando para informa-la diretamente. O horista usa a data do
    // primeiro cartao lancado, e o assalariado e o comissionado usam
    // 1/1/2005, atribuido pela Facade na criacao.
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

    // ======================================================================
    // Cada subtipo sabe responder por si mesmo se e dia de pagar, quanto
    // e o proprio salario bruto e por quantos dias cobrar taxa sindical.
    // ======================================================================

    /** Diz se este empregado deve ser pago na data informada, seguindo a regra de agenda do proprio tipo. */
    public abstract boolean devePagar(LocalDate data);

    /** Calcula o salario bruto deste empregado, com seus detalhes de horas, vendas, comissao ou fixo, no periodo que termina em dataPagamento. */
    public abstract DadosPagamento calcularBruto(LocalDate dataPagamento);

    /** Diz quantos dias contam para a taxa sindical deste pagamento, ja que a duracao do periodo muda de acordo com o tipo de agenda. */
    public abstract int diasParaTaxaSindical(LocalDate dataPagamento);

    /** Diz em qual secao do relatorio de folha este empregado deve ser listado, entre horistas, assalariados e comissionados. */
    public abstract String getCategoriaFolha();

    // Cartao de ponto so faz sentido para horista, e venda e comissao so
    // fazem sentido para comissionado. Por padrao essas operacoes recusam
    // com a mensagem de erro correta, e o subtipo que realmente suporta a
    // operacao sobrescreve o metodo. Assim o Facade so chama
    // emp.addCartao(), emp.getVendas() e assim por diante, sem precisar
    // saber qual e o tipo concreto por tras da referencia Empregado.
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

    // Helpers de calculo compartilhados por quem precisar, principalmente
    // Horista e Comissionado, evitando duplicar parsing e arredondamento.
    protected static double parseValor(String valor) {
        return Double.parseDouble(valor.replace(",", "."));
    }

    // Trunca, em vez de arredondar, para 2 casas decimais. Usado no
    // calculo do salario fixo e da comissao do comissionado, onde a
    // divisao gera dizima.
    protected static double truncar(double valor) {
        return Math.floor(valor * 100 + 1e-9) / 100.0;
    }
}