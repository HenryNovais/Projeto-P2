package models;

/** Erro de regra de negocio: agenda ainda nao cadastrada no sistema. */
public class AgendaPagamentoIndisponivelException extends IllegalArgumentException {
    public AgendaPagamentoIndisponivelException() {
        super("Agenda de pagamento nao esta disponivel");
    }
}
