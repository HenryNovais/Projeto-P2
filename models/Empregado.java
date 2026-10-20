package models;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Dados e comportamentos compartilhados pelos tres tipos de empregado. */
public abstract class Empregado implements Serializable {
    private static final long serialVersionUID = -215896937901389876L;
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
    private LocalDate dataUltimoPagamento;
    private LocalDate dataContratacao;
    private AgendaPagamento agendaPagamento;

    protected Empregado(String id, String nome, String endereco, String tipo) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.tipo = tipo;
        this.sindicalizado = false;
        this.taxasServico = new ArrayList<>();
        this.metodoPagamento = "emMaos";
        this.agendaPagamento = AgendaPagamento.padraoPara(tipo);
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

    /** Agenda de pagamento escolhida, independentemente do tipo. */
    public AgendaPagamento getAgendaPagamento() {
        // Resguarda dados legados que ainda nao possuem o campo.
        return agendaPagamento != null ? agendaPagamento : AgendaPagamento.padraoPara(tipo);
    }

    public void setAgendaPagamento(AgendaPagamento agenda) {
        this.agendaPagamento = Objects.requireNonNull(agenda, "Agenda de pagamento obrigatoria");
    }

    public abstract String getSalario();
    public abstract void setSalario(String salario);
    public boolean devePagar(LocalDate data) { return getAgendaPagamento().devePagar(data); }
    public abstract DadosPagamento calcularBruto(LocalDate dataPagamento);
    public abstract int diasParaTaxaSindical(LocalDate dataPagamento);
    public abstract String getCategoriaFolha();

    // Operacoes especificas, mantidas para compatibilidade com a US 1-8.
    // Uma futura refatoracao deve retirar estas operacoes da classe-base.
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

    protected static double parseValor(String valor) {
        return Double.parseDouble(valor.replace(",", "."));
    }

    protected static double truncar(double valor) {
        return Math.floor(valor * 100 + 1e-9) / 100.0;
    }
}
