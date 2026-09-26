package controllers;

import models.*;
import commands.UndoRedoManager;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class Facade {
    private static final String ARQUIVO_DADOS = "wepayu.dat";

    private Map<String, Empregado> empregados;
    private int nextId;
    private UndoRedoManager urManager;
    private boolean sistemaEncerrado;

    public Facade() {
        this.urManager = new UndoRedoManager();
        this.sistemaEncerrado = false;
        carregarEstado();
    }

    @SuppressWarnings("unchecked")
    private void carregarEstado() {
        File arquivo = new File(ARQUIVO_DADOS);
        if (arquivo.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(arquivo))) {
                Object[] estado = (Object[]) ois.readObject();
                this.empregados = (Map<String, Empregado>) estado[0];
                this.nextId = (int) estado[1];
                return;
            } catch (Exception e) {
                // arquivo corrompido/incompativel: comeca vazio
            }
        }
        this.empregados = new LinkedHashMap<>();
        this.nextId = 1;
    }

    private void persistirEstado() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARQUIVO_DADOS))) {
            oos.writeObject(new Object[] { this.empregados, this.nextId });
        } catch (IOException e) {
            // nao interrompe o fluxo; idealmente logar
        }
    }

    public void zerarSistema() throws Exception {
        salvarEstado();
        this.empregados = new LinkedHashMap<>();
        this.nextId = 1;
        this.sistemaEncerrado = false;
        persistirEstado();
    }

    public void encerrarSistema() throws Exception {
        this.sistemaEncerrado = true;
    }

    // --- UNDO / REDO ---
    private void salvarEstado() {
        urManager.saveState(new Object[] { this.empregados, this.nextId });
    }

    @SuppressWarnings("unchecked")
    public void undo() throws Exception {
        if (sistemaEncerrado) throw new Exception("Nao pode dar comandos depois de encerrarSistema.");
        Object[] estadoAnterior = (Object[]) urManager.undo(new Object[] { this.empregados, this.nextId });
        this.empregados = (Map<String, Empregado>) estadoAnterior[0];
        this.nextId = (int) estadoAnterior[1];
        persistirEstado();
    }

    @SuppressWarnings("unchecked")
    public void redo() throws Exception {
        if (sistemaEncerrado) throw new Exception("Nao pode dar comandos depois de encerrarSistema.");
        Object[] estadoRefeito = (Object[]) urManager.redo(new Object[] { this.empregados, this.nextId });
        this.empregados = (Map<String, Empregado>) estadoRefeito[0];
        this.nextId = (int) estadoRefeito[1];
        persistirEstado();
    }

    public String getNumeroDeEmpregados() throws Exception {
        return String.valueOf(this.empregados.size());
    }

    // ==================================================================
    // US 7: REGRAS DE FOLHA DE PAGAMENTO
    
    // quem sabe se e dia de pagar (emp.devePagar), quanto e o bruto (emp.calcularBruto) e por
    // quantos dias cobrar taxa sindical (emp.diasParaTaxaSindical) e o
    // proprio Empregado: cada subtipo (Horista/Assalariado/Comissionado)
    // sabe responder por si mesmo. O Facade so orquestra.
    // ==================================================================

    // Indices do array de retorno de calcularPagamento:
    private static final int I_BRUTO = 0, I_DESCONTOS = 1, I_LIQUIDO = 2,
            I_FIXO = 3, I_VENDAS = 4, I_COMISSAO = 5, I_H_NORMAIS = 6, I_H_EXTRAS = 7;

    // aplicarPagamento=true efetiva o pagamento (avanca a "marca d'agua"
    // do empregado); aplicarPagamento=false so simula (usado por totalFolha).
    private double[] calcularPagamento(Empregado emp, LocalDate dataPagamento, boolean aplicarPagamento) {
        DadosPagamento dp = emp.calcularBruto(dataPagamento);

        // So existe um "evento de pagamento" de verdade se ha algo a pagar.
        // Assalariado/comissionado sempre tem (salario fixo garante bruto>0).
        // Horista pode nao ter (nenhum cartao lancado no periodo): nesse
        // caso ele aparece no relatorio com tudo zerado, mas NAO e "pago".
        boolean pagamentoReal = dp.bruto > 0;

        double descontos = 0;
        if (pagamentoReal && emp.isSindicalizado()) {
            descontos += emp.getTaxaSindical() * emp.diasParaTaxaSindical(dataPagamento);
            LocalDate desde = emp.getDataUltimoPagamento();
            for (TaxaServico t : emp.getTaxasServico()) {
                LocalDate d = t.getDataComoLocalDate();
                if ((desde == null || d.isAfter(desde)) && !d.isAfter(dataPagamento)) {
                    descontos += t.getValor();
                }
            }
        }
        // Regra de negocio: horista nao pode ter contracheque negativo.
        double liquido = Math.max(0, dp.bruto - descontos);

        if (aplicarPagamento && pagamentoReal) {
            emp.setDataUltimoPagamento(dataPagamento);
        }
        return new double[] { dp.bruto, descontos, liquido, dp.fixo, dp.vendas, dp.comissaoValor,
                dp.horasNormais, dp.horasExtras };
    }

    public String totalFolha(String data) throws Exception {
        LocalDate d = parseData(data, "Data invalida.");
        double total = 0;
        for (Empregado emp : empregados.values()) {
            if (emp.devePagar(d)) {
                // "TOTAL FOLHA" soma o BRUTO de cada pago no dia, nao o liquido
                // (confirmado comparando contra os arquivos ok/folha-*.txt).
                total += calcularPagamento(emp, d, false)[I_BRUTO];
            }
        }
        return formatarValor(String.valueOf(total));
    }

    private String metodoTexto(Empregado emp) {
        switch (emp.getMetodoPagamento()) {
            case "emMaos": return "Em maos";
            case "correios": return "Correios, " + emp.getEndereco();
            case "banco": return emp.getBanco() + ", Ag. " + emp.getAgencia() + " CC " + emp.getContaCorrente();
            default: return "";
        }
    }

    public void rodaFolha(String data, String saida) throws Exception {
        LocalDate d = parseData(data, "Data invalida.");
        salvarEstado(); // snapshot para undo, ANTES de mutar qualquer coisa

        Map<String, List<Empregado>> porCategoria = new LinkedHashMap<>();
        porCategoria.put("HORISTAS", new ArrayList<>());
        porCategoria.put("ASSALARIADOS", new ArrayList<>());
        porCategoria.put("COMISSIONADOS", new ArrayList<>());
        Map<String, double[]> resultados = new HashMap<>();

        for (Empregado emp : empregados.values()) {
            if (!emp.devePagar(d)) continue;
            double[] r = calcularPagamento(emp, d, true); // true = efetiva o pagamento
            resultados.put(emp.getId(), r);
            porCategoria.get(emp.getCategoriaFolha()).add(emp);
        }

        List<Empregado> horistas = porCategoria.get("HORISTAS");
        List<Empregado> assalariados = porCategoria.get("ASSALARIADOS");
        List<Empregado> comissionados = porCategoria.get("COMISSIONADOS");

        Comparator<Empregado> porNome = Comparator.comparing(Empregado::getNome);
        horistas.sort(porNome);
        assalariados.sort(porNome);
        comissionados.sort(porNome);

        String NL = "\r\n";
        StringBuilder sb = new StringBuilder();
        sb.append("FOLHA DE PAGAMENTO DO DIA ").append(d.toString()).append(NL);
        sb.append("====================================").append(NL).append(NL);

        // ---------- HORISTAS ----------
        sb.append("===============================================================================================================================").append(NL);
        sb.append("===================== HORISTAS ================================================================================================").append(NL);
        sb.append("===============================================================================================================================").append(NL);
        sb.append("Nome                                 Horas Extra Salario Bruto Descontos Salario Liquido Metodo").append(NL);
        sb.append("==================================== ===== ===== ============= ========= =============== ======================================").append(NL);
        double totHBruto = 0, totHDesc = 0, totHLiq = 0, totHNormais = 0, totHExtras = 0;
        for (Empregado emp : horistas) {
            double[] r = resultados.get(emp.getId());
            totHBruto += r[I_BRUTO]; totHDesc += r[I_DESCONTOS]; totHLiq += r[I_LIQUIDO];
            totHNormais += r[I_H_NORMAIS]; totHExtras += r[I_H_EXTRAS];
            sb.append(String.format("%-36s %5s %5s %13s %9s %15s %s",
                    emp.getNome(), formatarHoras(r[I_H_NORMAIS]), formatarHoras(r[I_H_EXTRAS]),
                    formatarValor(String.valueOf(r[I_BRUTO])), formatarValor(String.valueOf(r[I_DESCONTOS])),
                    formatarValor(String.valueOf(r[I_LIQUIDO])), metodoTexto(emp))).append(NL);
        }
        sb.append(NL);
        sb.append(String.format("%-36s %5s %5s %13s %9s %15s",
                "TOTAL HORISTAS", formatarHoras(totHNormais), formatarHoras(totHExtras),
                formatarValor(String.valueOf(totHBruto)), formatarValor(String.valueOf(totHDesc)),
                formatarValor(String.valueOf(totHLiq)))).append(NL).append(NL);

        // ---------- ASSALARIADOS ----------
        sb.append("===============================================================================================================================").append(NL);
        sb.append("===================== ASSALARIADOS ============================================================================================").append(NL);
        sb.append("===============================================================================================================================").append(NL);
        sb.append("Nome                                             Salario Bruto Descontos Salario Liquido Metodo").append(NL);
        sb.append("================================================ ============= ========= =============== ======================================").append(NL);
        double totABruto = 0, totADesc = 0, totALiq = 0;
        for (Empregado emp : assalariados) {
            double[] r = resultados.get(emp.getId());
            totABruto += r[I_BRUTO]; totADesc += r[I_DESCONTOS]; totALiq += r[I_LIQUIDO];
            sb.append(String.format("%-48s %13s %9s %15s %s",
                    emp.getNome(), formatarValor(String.valueOf(r[I_BRUTO])), formatarValor(String.valueOf(r[I_DESCONTOS])),
                    formatarValor(String.valueOf(r[I_LIQUIDO])), metodoTexto(emp))).append(NL);
        }
        sb.append(NL);
        sb.append(String.format("%-48s %13s %9s %15s",
                "TOTAL ASSALARIADOS", formatarValor(String.valueOf(totABruto)), formatarValor(String.valueOf(totADesc)),
                formatarValor(String.valueOf(totALiq)))).append(NL).append(NL);

        // ---------- COMISSIONADOS ----------
        sb.append("===============================================================================================================================").append(NL);
        sb.append("===================== COMISSIONADOS ===========================================================================================").append(NL);
        sb.append("===============================================================================================================================").append(NL);
        sb.append("Nome                  Fixo     Vendas   Comissao Salario Bruto Descontos Salario Liquido Metodo").append(NL);
        sb.append("===================== ======== ======== ======== ============= ========= =============== ======================================").append(NL);
        double totCBruto = 0, totCDesc = 0, totCLiq = 0, totCFixo = 0, totCVendas = 0, totCComissao = 0;
        for (Empregado emp : comissionados) {
            double[] r = resultados.get(emp.getId());
            totCBruto += r[I_BRUTO]; totCDesc += r[I_DESCONTOS]; totCLiq += r[I_LIQUIDO];
            totCFixo += r[I_FIXO]; totCVendas += r[I_VENDAS]; totCComissao += r[I_COMISSAO];
            sb.append(String.format("%-21s %8s %8s %8s %13s %9s %15s %s",
                    emp.getNome(), formatarValor(String.valueOf(r[I_FIXO])), formatarValor(String.valueOf(r[I_VENDAS])),
                    formatarValor(String.valueOf(r[I_COMISSAO])), formatarValor(String.valueOf(r[I_BRUTO])),
                    formatarValor(String.valueOf(r[I_DESCONTOS])), formatarValor(String.valueOf(r[I_LIQUIDO])),
                    metodoTexto(emp))).append(NL);
        }
        sb.append(NL);
        sb.append(String.format("%-21s %8s %8s %8s %13s %9s %15s",
                "TOTAL COMISSIONADOS", formatarValor(String.valueOf(totCFixo)), formatarValor(String.valueOf(totCVendas)),
                formatarValor(String.valueOf(totCComissao)), formatarValor(String.valueOf(totCBruto)),
                formatarValor(String.valueOf(totCDesc)), formatarValor(String.valueOf(totCLiq)))).append(NL).append(NL);

        double totalGeral = totHBruto + totABruto + totCBruto;
        sb.append("TOTAL FOLHA: ").append(formatarValor(String.valueOf(totalGeral))).append(NL);

        try (FileOutputStream fos = new FileOutputStream(saida)) {
            fos.write(sb.toString().getBytes("ISO-8859-1"));
        } catch (IOException e) {
            throw new Exception("Erro ao salvar o arquivo.");
        }

        persistirEstado();
    }

    // =================================
    // METODOS AUXILIARES E VALIDACOES
    // =================================
    private String formatarValor(String valor) {
        double num = Double.parseDouble(valor.replace(",", "."));
        return String.format(java.util.Locale.US, "%.2f", num).replace(".", ",");
    }

    private String formatarHoras(double horas) {
        if (horas == (long) horas) return String.valueOf((long) horas);
        return String.valueOf(horas).replace(".", ",");
    }

    private void validarDadosIniciais(String nome, String endereco) throws Exception {
        if (nome == null || nome.isEmpty()) throw new Exception("Nome nao pode ser nulo.");
        if (endereco == null || endereco.isEmpty()) throw new Exception("Endereco nao pode ser nulo.");
    }

    private void validarSalario(String salario) throws Exception {
        if (salario == null || salario.isEmpty()) throw new Exception("Salario nao pode ser nulo.");
        try {
            double sal = Double.parseDouble(salario.replace(",", "."));
            if (sal < 0) throw new Exception("Salario deve ser nao-negativo.");
        } catch (NumberFormatException e) {
            throw new Exception("Salario deve ser numerico.");
        }
    }

    private void validarComissao(String comissao) throws Exception {
        if (comissao == null || comissao.isEmpty()) throw new Exception("Comissao nao pode ser nula.");
        try {
            double com = Double.parseDouble(comissao.replace(",", "."));
            if (com < 0) throw new Exception("Comissao deve ser nao-negativa.");
        } catch (NumberFormatException e) {
            throw new Exception("Comissao deve ser numerica.");
        }
    }

    private LocalDate parseData(String dataStr, String erroMsg) throws Exception {
        try {
            String[] partes = dataStr.split("/");
            if (partes.length != 3) throw new Exception();
            return LocalDate.of(Integer.parseInt(partes[2]), Integer.parseInt(partes[1]), Integer.parseInt(partes[0]));
        } catch (Exception e) { throw new Exception(erroMsg); }
    }

    private boolean isDataNoIntervalo(LocalDate alvo, LocalDate inicial, LocalDate fFinal) {
        return !alvo.isBefore(inicial) && alvo.isBefore(fFinal);
    }

    private void copiarDados(Empregado velho, Empregado novo) {
        novo.setMetodoPagamento(velho.getMetodoPagamento());
        novo.setBanco(velho.getBanco());
        novo.setAgencia(velho.getAgencia());
        novo.setContaCorrente(velho.getContaCorrente());
        novo.setSindicalizado(velho.isSindicalizado());
        novo.setIdSindicato(velho.getIdSindicato());
        novo.setTaxaSindical(velho.getTaxaSindical());
        novo.setDataUltimoPagamento(velho.getDataUltimoPagamento());
        novo.setDataContratacao(velho.getDataContratacao());
    }

    // Regra simplificada para o calculo da folha: como ainda nao existe um
    // parametro de data de contratacao, assalariados e comissionados sao
    // sempre considerados contratados em 1/1/2005.
    private static final LocalDate DATA_CONTRATACAO_PADRAO = LocalDate.of(2005, 1, 1);

    // --- US 1: Criacao de Empregados ---
    public String criarEmpregado(String nome, String endereco, String tipo, String salario) throws Exception {
        validarDadosIniciais(nome, endereco);
        if (tipo.equals("comissionado")) throw new Exception("Tipo nao aplicavel.");
        if (!tipo.equals("horista") && !tipo.equals("assalariado")) throw new Exception("Tipo invalido.");
        validarSalario(salario);

        salvarEstado();
        String id = String.valueOf(nextId++);
        Empregado emp;
        if (tipo.equals("horista")) {
            emp = new EmpregadoHorista(id, nome, endereco, formatarValor(salario));
            // dataContratacao fica null ate o primeiro lancaCartao
        } else {
            emp = new EmpregadoAssalariado(id, nome, endereco, formatarValor(salario));
            emp.setDataContratacao(DATA_CONTRATACAO_PADRAO);
        }
        empregados.put(id, emp);
        persistirEstado();
        return id;
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao) throws Exception {
        validarDadosIniciais(nome, endereco);
        if (!tipo.equals("comissionado")) throw new Exception("Tipo nao aplicavel.");
        validarSalario(salario);
        validarComissao(comissao);

        salvarEstado();
        String id = String.valueOf(nextId++);
        Empregado emp = new EmpregadoComissionado(id, nome, endereco, formatarValor(salario), comissao);
        emp.setDataContratacao(DATA_CONTRATACAO_PADRAO);
        empregados.put(id, emp);
        persistirEstado();
        return id;
    }

    public String getAtributoEmpregado(String empId, String atributo) throws Exception {
        if (empId == null || empId.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        if (!empregados.containsKey(empId)) throw new Exception("Empregado nao existe.");
        Empregado emp = empregados.get(empId);

        if (atributo.equals("nome")) return emp.getNome();
        if (atributo.equals("endereco")) return emp.getEndereco();
        if (atributo.equals("tipo")) return emp.getTipo();
        if (atributo.equals("salario")) return emp.getSalario();
        if (atributo.equals("sindicalizado")) return String.valueOf(emp.isSindicalizado());
        if (atributo.equals("metodoPagamento")) return emp.getMetodoPagamento();

        // emp.getComissao() ja lanca "Empregado nao eh comissionado." por
        // padrao (ver Empregado) se o empregado nao for EmpregadoComissionado.
        if (atributo.equals("comissao")) {
            return emp.getComissao();
        }

        if (atributo.equals("banco") || atributo.equals("agencia") || atributo.equals("contaCorrente")) {
            if (!"banco".equals(emp.getMetodoPagamento())) throw new Exception("Empregado nao recebe em banco.");
            if (atributo.equals("banco")) return emp.getBanco();
            if (atributo.equals("agencia")) return emp.getAgencia();
            if (atributo.equals("contaCorrente")) return emp.getContaCorrente();
        }

        if (atributo.equals("idSindicato") || atributo.equals("taxaSindical")) {
            if (!emp.isSindicalizado()) throw new Exception("Empregado nao eh sindicalizado.");
            if (atributo.equals("idSindicato")) return emp.getIdSindicato();
            if (atributo.equals("taxaSindical")) return formatarValor(String.valueOf(emp.getTaxaSindical()));
        }
        throw new Exception("Atributo nao existe.");
    }

    // --- US 2: Remocao e Busca ---
    public void removerEmpregado(String empId) throws Exception {
        if (empId == null || empId.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        if (!empregados.containsKey(empId)) throw new Exception("Empregado nao existe.");
        salvarEstado();
        empregados.remove(empId);
        persistirEstado();
    }

    public String getEmpregadoPorNome(String nome, int indice) throws Exception {
        int currentIndex = 1;
        for (Empregado emp : empregados.values()) {
            if (emp.getNome().equals(nome)) {
                if (currentIndex == indice) return emp.getId();
                currentIndex++;
            }
        }
        throw new Exception("Nao ha empregado com esse nome.");
    }

    // --- US 3: Cartao de Ponto ---
    public void lancaCartao(String empId, String data, String horas) throws Exception {
        if (empId == null || empId.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        if (!empregados.containsKey(empId)) throw new Exception("Empregado nao existe.");
        Empregado emp = empregados.get(empId);

        parseData(data, "Data invalida.");
        double horasVal = Double.parseDouble(horas.replace(",", "."));
        if (horasVal <= 0) throw new Exception("Horas devem ser positivas.");

        salvarEstado();
        // emp.addCartao ja lanca "Empregado nao eh horista." por padrao se
        // nao for EmpregadoHorista; e o proprio EmpregadoHorista que cuida
        // de registrar a data de contratacao no primeiro cartao lancado.
        emp.addCartao(new CartaoDePonto(data, horasVal));
        persistirEstado();
    }

    public String getHorasNormaisTrabalhadas(String empId, String dataInicial, String dataFinal) throws Exception {
        if (empId == null || empId.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        if (!empregados.containsKey(empId)) throw new Exception("Empregado nao existe.");
        Empregado emp = empregados.get(empId);

        LocalDate inicio = parseData(dataInicial, "Data inicial invalida.");
        LocalDate fim = parseData(dataFinal, "Data final invalida.");
        if (inicio.isAfter(fim)) throw new Exception("Data inicial nao pode ser posterior aa data final.");

        double normais = 0;
        for (CartaoDePonto cartao : emp.getCartoes()) { // lanca se nao for horista
            if (isDataNoIntervalo(cartao.getDataComoLocalDate(), inicio, fim)) {
                double h = cartao.getHoras();
                normais += (h > 8) ? 8 : h;
            }
        }
        return formatarHoras(normais);
    }

    public String getHorasExtrasTrabalhadas(String empId, String dataInicial, String dataFinal) throws Exception {
        if (empId == null || empId.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        if (!empregados.containsKey(empId)) throw new Exception("Empregado nao existe.");
        Empregado emp = empregados.get(empId);

        LocalDate inicio = parseData(dataInicial, "Data inicial invalida.");
        LocalDate fim = parseData(dataFinal, "Data final invalida.");
        if (inicio.isAfter(fim)) throw new Exception("Data inicial nao pode ser posterior aa data final.");

        double extras = 0;
        for (CartaoDePonto cartao : emp.getCartoes()) { // lanca se nao for horista
            if (isDataNoIntervalo(cartao.getDataComoLocalDate(), inicio, fim)) {
                double h = cartao.getHoras();
                if (h > 8) extras += (h - 8);
            }
        }
        return formatarHoras(extras);
    }

    // --- US 4: Vendas ---
    public void lancaVenda(String empId, String data, String valor) throws Exception {
        if (empId == null || empId.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        if (!empregados.containsKey(empId)) throw new Exception("Empregado nao existe.");
        Empregado emp = empregados.get(empId);

        parseData(data, "Data invalida.");
        double valorVal = Double.parseDouble(valor.replace(",", "."));
        if (valorVal <= 0) throw new Exception("Valor deve ser positivo.");

        salvarEstado();
        emp.addVenda(new ResultadoVenda(data, valorVal)); // lanca se nao for comissionado
        persistirEstado();
    }

    public String getVendasRealizadas(String empId, String dataInicial, String dataFinal) throws Exception {
        if (empId == null || empId.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        if (!empregados.containsKey(empId)) throw new Exception("Empregado nao existe.");
        Empregado emp = empregados.get(empId);

        LocalDate inicio = parseData(dataInicial, "Data inicial invalida.");
        LocalDate fim = parseData(dataFinal, "Data final invalida.");
        if (inicio.isAfter(fim)) throw new Exception("Data inicial nao pode ser posterior aa data final.");

        double total = 0;
        for (ResultadoVenda venda : emp.getVendas()) { // lanca se nao for comissionado
            if (isDataNoIntervalo(venda.getDataComoLocalDate(), inicio, fim)) {
                total += venda.getValor();
            }
        }
        return formatarValor(String.valueOf(total));
    }

    // --- US 5: Taxas de Servico ---
    public void lancaTaxaServico(String membroId, String data, String valor) throws Exception {
        if (membroId == null || membroId.isEmpty()) throw new Exception("Identificacao do membro nao pode ser nula.");
        Empregado emp = null;
        for (Empregado e : empregados.values()) {
            if (membroId.equals(e.getIdSindicato())) {
                emp = e;
                break;
            }
        }
        if (emp == null) throw new Exception("Membro nao existe.");

        parseData(data, "Data invalida.");
        double valorVal = Double.parseDouble(valor.replace(",", "."));
        if (valorVal <= 0) throw new Exception("Valor deve ser positivo.");

        salvarEstado();
        emp.addTaxaServico(new TaxaServico(data, valorVal));
        persistirEstado();
    }

    public String getTaxasServico(String empId, String dataInicial, String dataFinal) throws Exception {
        if (empId == null || empId.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        if (!empregados.containsKey(empId)) throw new Exception("Empregado nao existe.");
        Empregado emp = empregados.get(empId);
        if (!emp.isSindicalizado()) throw new Exception("Empregado nao eh sindicalizado.");

        LocalDate inicio = parseData(dataInicial, "Data inicial invalida.");
        LocalDate fim = parseData(dataFinal, "Data final invalida.");
        if (inicio.isAfter(fim)) throw new Exception("Data inicial nao pode ser posterior aa data final.");

        double total = 0;
        for (TaxaServico taxa : emp.getTaxasServico()) {
            if (isDataNoIntervalo(taxa.getDataComoLocalDate(), inicio, fim)) {
                total += taxa.getValor();
            }
        }
        return formatarValor(String.valueOf(total));
    }

    // --- US 6: Alterar Detalhes do Empregado ---
    public void alteraEmpregado(String empId, String atributo, String valor) throws Exception {
        if (empId == null || empId.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        if (!empregados.containsKey(empId)) throw new Exception("Empregado nao existe.");
        Empregado emp = empregados.get(empId);

        if (atributo.equals("nome")) {
            if (valor == null || valor.isEmpty()) throw new Exception("Nome nao pode ser nulo.");
            salvarEstado();
            emp.setNome(valor);
        } else if (atributo.equals("endereco")) {
            if (valor == null || valor.isEmpty()) throw new Exception("Endereco nao pode ser nulo.");
            salvarEstado();
            emp.setEndereco(valor);
        } else if (atributo.equals("metodoPagamento")) {
            if (!valor.equals("correios") && !valor.equals("emMaos") && !valor.equals("banco")) throw new Exception("Metodo de pagamento invalido.");
            salvarEstado();
            emp.setMetodoPagamento(valor);
        } else if (atributo.equals("sindicalizado")) {
            if (!valor.equals("true") && !valor.equals("false")) throw new Exception("Valor deve ser true ou false.");
            if (valor.equals("false")) {
                salvarEstado();
                emp.setSindicalizado(false);
                emp.setIdSindicato(null);
                emp.setTaxaSindical(0.0);
                emp.getTaxasServico().clear();
            }
        } else if (atributo.equals("salario")) {
            validarSalario(valor);
            salvarEstado();
            emp.setSalario(formatarValor(valor));
        } else if (atributo.equals("comissao")) {
            validarComissao(valor);
            salvarEstado();
            emp.setComissao(valor); // lanca "Empregado nao eh comissionado." se nao for
        } else if (atributo.equals("tipo")) {
            if (valor.equals("assalariado")) {
                salvarEstado();
                EmpregadoAssalariado novo = new EmpregadoAssalariado(emp.getId(), emp.getNome(), emp.getEndereco(), emp.getSalario());
                copiarDados(emp, novo);
                empregados.put(empId, novo);
            } else {
                throw new Exception("Tipo invalido.");
            }
        } else {
            throw new Exception("Atributo nao existe.");
        }
        persistirEstado();
    }

    public void alteraEmpregado(String empId, String atributo, String valor, String ext) throws Exception {
        if (empId == null || empId.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        if (!empregados.containsKey(empId)) throw new Exception("Empregado nao existe.");
        Empregado emp = empregados.get(empId);

        if (atributo.equals("tipo")) {
            if (valor.equals("comissionado")) {
                validarComissao(ext);
                salvarEstado();
                EmpregadoComissionado novo = new EmpregadoComissionado(emp.getId(), emp.getNome(), emp.getEndereco(), emp.getSalario(), ext);
                copiarDados(emp, novo);
                empregados.put(empId, novo);
            } else if (valor.equals("horista")) {
                validarSalario(ext);
                salvarEstado();
                EmpregadoHorista novo = new EmpregadoHorista(emp.getId(), emp.getNome(), emp.getEndereco(), formatarValor(ext));
                copiarDados(emp, novo);
                empregados.put(empId, novo);
            } else {
                throw new Exception("Tipo invalido.");
            }
        } else {
            throw new Exception("Atributo nao existe.");
        }
        persistirEstado();
    }

    public void alteraEmpregado(String empId, String atributo, String valor, String idSindicato, String taxaSindical) throws Exception {
        if (empId == null || empId.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        if (!empregados.containsKey(empId)) throw new Exception("Empregado nao existe.");

        if (atributo.equals("sindicalizado") && valor.equals("true")) {
            if (idSindicato == null || idSindicato.isEmpty()) throw new Exception("Identificacao do sindicato nao pode ser nula.");
            if (taxaSindical == null || taxaSindical.isEmpty()) throw new Exception("Taxa sindical nao pode ser nula.");
            double taxa;
            try {
                taxa = Double.parseDouble(taxaSindical.replace(",", "."));
            } catch (NumberFormatException e) {
                throw new Exception("Taxa sindical deve ser numerica.");
            }
            if (taxa < 0) throw new Exception("Taxa sindical deve ser nao-negativa.");

            for (Empregado e : empregados.values()) {
                if (idSindicato.equals(e.getIdSindicato()) && !e.getId().equals(empId)) {
                    throw new Exception("Ha outro empregado com esta identificacao de sindicato");
                }
            }

            salvarEstado();
            Empregado emp = empregados.get(empId);
            emp.setSindicalizado(true);
            emp.setIdSindicato(idSindicato);
            emp.setTaxaSindical(taxa);
            persistirEstado();
        }
    }

    public void alteraEmpregado(String empId, String atributo, String valor, String banco, String agencia, String contaCorrente) throws Exception {
        if (empId == null || empId.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        if (!empregados.containsKey(empId)) throw new Exception("Empregado nao existe.");
        Empregado emp = empregados.get(empId);

        if (atributo.equals("metodoPagamento") && valor.equals("banco")) {
            if (banco == null || banco.isEmpty()) throw new Exception("Banco nao pode ser nulo.");
            if (agencia == null || agencia.isEmpty()) throw new Exception("Agencia nao pode ser nulo.");
            if (contaCorrente == null || contaCorrente.isEmpty()) throw new Exception("Conta corrente nao pode ser nulo.");

            salvarEstado();
            emp.setMetodoPagamento("banco");
            emp.setBanco(banco);
            emp.setAgencia(agencia);
            emp.setContaCorrente(contaCorrente);
            persistirEstado();
        }
    }
}