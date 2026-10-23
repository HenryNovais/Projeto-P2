package models;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/** Repositorio serializavel das agendas habilitadas pelo administrador. */
public final class CatalogoAgendas implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Set<String> descricoes = new LinkedHashSet<String>();

    public CatalogoAgendas() {
        for (AgendaPagamento agenda : AgendaPagamento.values()) {
            descricoes.add(agenda.getDescricao());
        }
    }

    public void criar(String descricao) {
        AgendaConfigurada agenda = AgendaConfigurada.porDescricao(descricao);
        if (!descricoes.add(agenda.getDescricao())) {
            throw new AgendaPagamentoJaExisteException();
        }
    }

    public String exigirDisponivel(String descricao) {
        if (descricao == null || !descricoes.contains(descricao)) {
            throw new AgendaPagamentoIndisponivelException();
        }
        return descricao;
    }

    public Set<String> listar() {
        return Collections.unmodifiableSet(descricoes);
    }
}
