package models;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Agendas disponiveis na US 9. Na US 10, agendas customizadas serao
 * criadas e validadas em um catalogo especifico, sem afetar o dominio.
 */
public enum AgendaPagamento {
    SEMANAL("semanal 5", 1),
    QUINZENAL("semanal 2 5", 2),
    MENSAL("mensal $", 0);

    private static final LocalDate PRIMEIRA_SEXTA_QUINZENAL = LocalDate.of(2005, 1, 14);

    private final String descricao;
    private final int semanas;

    AgendaPagamento(String descricao, int semanas) {
        this.descricao = descricao;
        this.semanas = semanas;
    }

    public String getDescricao() {
        return descricao;
    }

    /** Converte o texto do EasyAccept em uma agenda valida na US 9. */
    public static AgendaPagamento porDescricao(String descricao) {
        for (AgendaPagamento agenda : values()) {
            if (agenda.descricao.equals(descricao)) return agenda;
        }
        throw new AgendaPagamentoIndisponivelException();
    }

    public static AgendaPagamento padraoPara(String tipo) {
        if ("horista".equals(tipo)) return SEMANAL;
        if ("comissionado".equals(tipo)) return QUINZENAL;
        return MENSAL;
    }

    /** A agenda eh independente da categoria profissional do empregado. */
    public boolean devePagar(LocalDate data) {
        if (this == MENSAL) {
            LocalDate ultimoUtil = data.withDayOfMonth(data.lengthOfMonth());
            while (ultimoUtil.getDayOfWeek() == DayOfWeek.SATURDAY
                    || ultimoUtil.getDayOfWeek() == DayOfWeek.SUNDAY) {
                ultimoUtil = ultimoUtil.minusDays(1);
            }
            return data.equals(ultimoUtil);
        }
        if (data.getDayOfWeek() != DayOfWeek.FRIDAY) return false;
        if (this == SEMANAL) return true;
        long dias = ChronoUnit.DAYS.between(PRIMEIRA_SEXTA_QUINZENAL, data);
        return dias >= 0 && dias % (7L * semanas) == 0;
    }

    /** Limite inferior exclusivo do ciclo de pagamento do dia informado. */
    public LocalDate inicioPeriodo(LocalDate dataPagamento) {
        if (this != MENSAL) return dataPagamento.minusWeeks(semanas);
        LocalDate mesAnterior = dataPagamento.minusMonths(1);
        LocalDate ultimoUtil = mesAnterior.withDayOfMonth(mesAnterior.lengthOfMonth());
        while (ultimoUtil.getDayOfWeek() == DayOfWeek.SATURDAY
                || ultimoUtil.getDayOfWeek() == DayOfWeek.SUNDAY) {
            ultimoUtil = ultimoUtil.minusDays(1);
        }
        return ultimoUtil;
    }

    public double fatorSalarioMensal() {
        if (this == MENSAL) return 1;
        return 12.0 * semanas / 52.0;
    }

    public int diasParaTaxaSindical(LocalDate dataPagamento) {
        return this == MENSAL ? dataPagamento.lengthOfMonth() : semanas * 7;
    }
}
