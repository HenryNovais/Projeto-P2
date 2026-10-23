package models;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Regra imutavel para uma agenda de pagamento, predefinida ou criada na US 10.
 * Dia da semana: segunda = 1, ..., domingo = 7.
 * Para agendas a cada N semanas, a primeira ocorre na N-esima semana de
 * referencia iniciada em janeiro de 2005, e se repete a cada N semanas.
 */
public final class AgendaConfigurada {
    private static final Pattern SEMANAL = Pattern.compile("semanal ([1-7])");
    private static final Pattern SEMANAL_INTERVALO = Pattern.compile("semanal (\\d+) ([1-7])");
    private static final Pattern MENSAL = Pattern.compile("mensal (\\$|\\d+)");
    private static final LocalDate REFERENCIA = LocalDate.of(2005, 1, 1);

    private final String descricao;
    private final int semanas;
    private final int diaSemana;
    private final int diaMes;
    private final boolean mensal;
    private final boolean ultimoDiaUtil;

    private AgendaConfigurada(String descricao, int semanas, int diaSemana,
                             int diaMes, boolean mensal, boolean ultimoDiaUtil) {
        this.descricao = descricao;
        this.semanas = semanas;
        this.diaSemana = diaSemana;
        this.diaMes = diaMes;
        this.mensal = mensal;
        this.ultimoDiaUtil = ultimoDiaUtil;
    }

    /** Valida a sintaxe e os limites numericos com erro especifico. */
    public static AgendaConfigurada porDescricao(String texto) {
        if (texto == null) throw new DescricaoAgendaInvalidaException();
        String descricao = texto.trim().replaceAll("\\s+", " ");
        Matcher m = SEMANAL.matcher(descricao);
        if (m.matches()) {
            return new AgendaConfigurada(descricao, 1, Integer.parseInt(m.group(1)), 0, false, false);
        }
        m = SEMANAL_INTERVALO.matcher(descricao);
        if (m.matches()) {
            try {
                int semanas = Integer.parseInt(m.group(1));
                if (semanas < 1 || semanas > 52) throw new DescricaoAgendaInvalidaException();
                return new AgendaConfigurada(descricao, semanas, Integer.parseInt(m.group(2)), 0, false, false);
            } catch (NumberFormatException e) {
                throw new DescricaoAgendaInvalidaException();
            }
        }
        m = MENSAL.matcher(descricao);
        if (m.matches()) {
            if ("$".equals(m.group(1))) {
                return new AgendaConfigurada(descricao, 0, 0, 0, true, true);
            }
            try {
                int dia = Integer.parseInt(m.group(1));
                if (dia < 1 || dia > 28) throw new DescricaoAgendaInvalidaException();
                return new AgendaConfigurada(descricao, 0, 0, dia, true, false);
            } catch (NumberFormatException e) {
                throw new DescricaoAgendaInvalidaException();
            }
        }
        throw new DescricaoAgendaInvalidaException();
    }

    public String getDescricao() { return descricao; }
    public boolean isMensal() { return mensal; }

    private LocalDate ultimoDiaUtil(LocalDate data) {
        LocalDate ultimo = data.with(TemporalAdjusters.lastDayOfMonth());
        while (ultimo.getDayOfWeek() == DayOfWeek.SATURDAY ||
               ultimo.getDayOfWeek() == DayOfWeek.SUNDAY) {
            ultimo = ultimo.minusDays(1);
        }
        return ultimo;
    }

    private LocalDate primeiroDiaDaAgenda() {
        LocalDate primeiro = REFERENCIA;
        while (primeiro.getDayOfWeek().getValue() != diaSemana) {
            primeiro = primeiro.plusDays(1);
        }
        return primeiro.plusWeeks(semanas - 1L);
    }

    public boolean devePagar(LocalDate data) {
        if (mensal) {
            return ultimoDiaUtil ? data.equals(ultimoDiaUtil(data)) : data.getDayOfMonth() == diaMes;
        }
        if (data.getDayOfWeek().getValue() != diaSemana) return false;
        if (semanas == 1) return true;
        long dias = ChronoUnit.DAYS.between(primeiroDiaDaAgenda(), data);
        return dias >= 0 && dias % (7L * semanas) == 0;
    }

    /** Limite inferior exclusivo do ciclo, preservando a regra da US 9. */
    public LocalDate inicioPeriodo(LocalDate dataPagamento) {
        if (!mensal) return dataPagamento.minusWeeks(semanas);
        if (ultimoDiaUtil) return ultimoDiaUtil(dataPagamento.minusMonths(1));
        return dataPagamento.minusMonths(1);
    }

    /** Mensalistas recebem 12 salarios anuais distribuidos nos ciclos. */
    public double fatorSalarioMensal() {
        return mensal ? 1.0 : 12.0 * semanas / 52.0;
    }

    public int diasParaTaxaSindical(LocalDate dataPagamento) {
        return mensal ? dataPagamento.lengthOfMonth() : semanas * 7;
    }
}
