package models;

/** A descricao nao obedece as regras de sintaxe de agenda da US 10. */
public final class DescricaoAgendaInvalidaException extends IllegalArgumentException {
    public DescricaoAgendaInvalidaException() {
        super("Descricao de agenda invalida");
    }
}
