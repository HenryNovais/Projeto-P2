package models;

/** Tentativa de cadastrar novamente uma agenda ja disponivel. */ 
public final class AgendaPagamentoJaExisteException extends IllegalArgumentException {
    public AgendaPagamentoJaExisteException() {
        super("Agenda de pagamentos ja existe");
    }
}
