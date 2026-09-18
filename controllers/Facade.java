package controllers;

import models.*;
import commands.UndoRedoManager;
import java.util.LinkedHashMap;
import java.util.Map;
import java.time.LocalDate;

public class Facade {
    private Map<String, Empregado> empregados;
    private int nextId;
    private UndoRedoManager urManager;
    private boolean sistemaEncerrado;

    // Construtor original que cria as pilhas apenas UMA VEZ
    public Facade() {
        this.urManager = new UndoRedoManager();
        this.empregados = new LinkedHashMap<>();
        this.nextId = 1;
        this.sistemaEncerrado = false;
    }

    // O pulo do gato: zerarSistema é uma ação desfazível!
    public void zerarSistema() throws Exception {
        salvarEstado(); 
        this.empregados = new LinkedHashMap<>();
        this.nextId = 1;
        this.sistemaEncerrado = false;
        // Não recriamos o urManager aqui, para preservar o histórico!
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
    }

    @SuppressWarnings("unchecked")
    public void redo() throws Exception {
        if (sistemaEncerrado) throw new Exception("Nao pode dar comandos depois de encerrarSistema.");
        Object[] estadoRefeito = (Object[]) urManager.redo(new Object[] { this.empregados, this.nextId });
        this.empregados = (Map<String, Empregado>) estadoRefeito[0];
        this.nextId = (int) estadoRefeito[1];
    }

    // --- MÉTODOS PENDENTES US 7 ---
    public String getNumeroDeEmpregados() throws Exception {
        return String.valueOf(this.empregados.size());
    }

    public void rodaFolha(String data, String saida) throws Exception { 
        salvarEstado(); // A folha também é desfazível
    }

    public String totalFolha(String data) throws Exception { 
        return "0,00"; 
    }

    // --- MÉTODOS AUXILIARES E VALIDAÇÕES ---
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

    private boolean isDataNoIntervalo(String dataAlvo, LocalDate inicial, LocalDate fFinal) {
        try {
            LocalDate alvo = parseData(dataAlvo, "Erro");
            return !alvo.isBefore(inicial) && alvo.isBefore(fFinal);
        } catch(Exception e) { return false; }
    }

    private void copiarDados(Empregado velho, Empregado novo) {
        novo.setMetodoPagamento(velho.getMetodoPagamento());
        novo.setBanco(velho.getBanco());
        novo.setAgencia(velho.getAgencia());
        novo.setContaCorrente(velho.getContaCorrente());
        novo.setSindicalizado(velho.isSindicalizado());
        novo.setIdSindicato(velho.getIdSindicato());
        novo.setTaxaSindical(velho.getTaxaSindical());
    }

    // --- US 1: Criação de Empregados ---
    public String criarEmpregado(String nome, String endereco, String tipo, String salario) throws Exception {
        validarDadosIniciais(nome, endereco);
        if (tipo.equals("comissionado")) throw new Exception("Tipo nao aplicavel.");
        if (!tipo.equals("horista") && !tipo.equals("assalariado")) throw new Exception("Tipo invalido.");
        validarSalario(salario);

        salvarEstado();
        String id = String.valueOf(nextId++);
        Empregado emp = tipo.equals("horista") ? 
            new EmpregadoHorista(id, nome, endereco, formatarValor(salario)) : 
            new EmpregadoAssalariado(id, nome, endereco, formatarValor(salario));
        empregados.put(id, emp);
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
        empregados.put(id, emp);
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

        if (atributo.equals("comissao")) {
            if (!(emp instanceof EmpregadoComissionado)) throw new Exception("Empregado nao eh comissionado.");
            return ((EmpregadoComissionado) emp).getComissao();
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

    // --- US 2: Remoção e Busca ---
    public void removerEmpregado(String empId) throws Exception {
        if (empId == null || empId.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        if (!empregados.containsKey(empId)) throw new Exception("Empregado nao existe.");
        salvarEstado();
        empregados.remove(empId);
    }

    public String getEmpregadoPorNome(String nome, int indice) throws Exception {
        int currentIndex = 1;
        for (Empregado emp : empregados.values()) {
            if (emp.getNome().equals(nome)) {
                if (currentIndex == indice) return emp.getId();
                currentIndex++;
            }
        }
        throw new Exception("Empregado nao existe.");
    }

    // --- US 3: Cartão de Ponto ---
    public void lancaCartao(String empId, String data, String horas) throws Exception {
        if (empId == null || empId.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        if (!empregados.containsKey(empId)) throw new Exception("Empregado nao existe.");
        Empregado emp = empregados.get(empId);
        if (!(emp instanceof EmpregadoHorista)) throw new Exception("Empregado nao eh horista.");
        
        parseData(data, "Data invalida.");
        double horasVal = Double.parseDouble(horas.replace(",", "."));
        if (horasVal <= 0) throw new Exception("Horas devem ser positivas.");
        
        salvarEstado();
        ((EmpregadoHorista) emp).addCartao(new CartaoDePonto(data, horasVal));
    }

    public String getHorasNormaisTrabalhadas(String empId, String dataInicial, String dataFinal) throws Exception {
        if (empId == null || empId.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        if (!empregados.containsKey(empId)) throw new Exception("Empregado nao existe.");
        Empregado emp = empregados.get(empId);
        if (!(emp instanceof EmpregadoHorista)) throw new Exception("Empregado nao eh horista.");
        
        LocalDate inicio = parseData(dataInicial, "Data inicial invalida.");
        LocalDate fim = parseData(dataFinal, "Data final invalida.");
        if (inicio.isAfter(fim)) throw new Exception("Data inicial nao pode ser posterior aa data final.");
        
        double normais = 0;
        for (CartaoDePonto cartao : ((EmpregadoHorista) emp).getCartoes()) {
            if (isDataNoIntervalo(cartao.getData(), inicio, fim)) {
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
        if (!(emp instanceof EmpregadoHorista)) throw new Exception("Empregado nao eh horista.");
        
        LocalDate inicio = parseData(dataInicial, "Data inicial invalida.");
        LocalDate fim = parseData(dataFinal, "Data final invalida.");
        if (inicio.isAfter(fim)) throw new Exception("Data inicial nao pode ser posterior aa data final.");
        
        double extras = 0;
        for (CartaoDePonto cartao : ((EmpregadoHorista) emp).getCartoes()) {
            if (isDataNoIntervalo(cartao.getData(), inicio, fim)) {
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
        if (!(emp instanceof EmpregadoComissionado)) throw new Exception("Empregado nao eh comissionado.");
        
        parseData(data, "Data invalida.");
        double valorVal = Double.parseDouble(valor.replace(",", "."));
        if (valorVal <= 0) throw new Exception("Valor deve ser positivo."); 
        
        salvarEstado();
        ((EmpregadoComissionado) emp).addVenda(new ResultadoVenda(data, valorVal));
    }

    public String getVendasRealizadas(String empId, String dataInicial, String dataFinal) throws Exception {
        if (empId == null || empId.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        if (!empregados.containsKey(empId)) throw new Exception("Empregado nao existe.");
        Empregado emp = empregados.get(empId);
        if (!(emp instanceof EmpregadoComissionado)) throw new Exception("Empregado nao eh comissionado.");
        
        LocalDate inicio = parseData(dataInicial, "Data inicial invalida.");
        LocalDate fim = parseData(dataFinal, "Data final invalida.");
        if (inicio.isAfter(fim)) throw new Exception("Data inicial nao pode ser posterior aa data final.");
        
        double total = 0;
        for (ResultadoVenda venda : ((EmpregadoComissionado) emp).getVendas()) {
            if (isDataNoIntervalo(venda.getData(), inicio, fim)) {
                total += venda.getValor();
            }
        }
        return formatarValor(String.valueOf(total));
    }

    // --- US 5: Taxas de Serviço ---
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
            if (isDataNoIntervalo(taxa.getData(), inicio, fim)) {
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
            }
        } else if (atributo.equals("salario")) {
            validarSalario(valor);
            salvarEstado();
            emp.setSalario(formatarValor(valor));
        } else if (atributo.equals("comissao")) {
            if (!(emp instanceof EmpregadoComissionado)) throw new Exception("Empregado nao eh comissionado.");
            validarComissao(valor);
            salvarEstado();
            ((EmpregadoComissionado) emp).setComissao(valor);
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
    }

    public void alteraEmpregado(String empId, String atributo, String valor, String idSindicato, String taxaSindical) throws Exception {
        if (empId == null || empId.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        if (!empregados.containsKey(empId)) throw new Exception("Empregado nao existe.");
        
        if (atributo.equals("sindicalizado") && valor.equals("true")) {
            if (idSindicato == null || idSindicato.isEmpty()) throw new Exception("Identificacao do sindicato nao pode ser nula.");
            if (taxaSindical == null || taxaSindical.isEmpty()) throw new Exception("Taxa sindical nao pode ser nula.");
            try {
                double taxa = Double.parseDouble(taxaSindical.replace(",", "."));
                if (taxa < 0) throw new Exception("Taxa sindical deve ser nao-negativa.");
            } catch (NumberFormatException e) {
                throw new Exception("Taxa sindical deve ser numerica.");
            }
            
            for (Empregado e : empregados.values()) {
                if (idSindicato.equals(e.getIdSindicato()) && !e.getId().equals(empId)) {
                    throw new Exception("Ha outro empregado com esta identificacao de sindicato");
                }
            }
            
            salvarEstado();
            Empregado emp = empregados.get(empId);
            emp.setSindicalizado(true);
            emp.setIdSindicato(idSindicato);
            emp.setTaxaSindical(Double.parseDouble(taxaSindical.replace(",", ".")));
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
        }
    }
}