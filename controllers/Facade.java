package controllers;

import models.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Facade {
    private Map<String, Empregado> empregados;
    private int nextId;

    public Facade() {
        zerarSistema();
    }

    public void zerarSistema() {
        this.empregados = new LinkedHashMap<>();
        this.nextId = 1;
    }

    public void encerrarSistema() { }

    // --- MÉTODOS AUXILIARES ---
    private String formatarValor(String valor) {
        double num = Double.parseDouble(valor.replace(",", "."));
        return String.format(java.util.Locale.US, "%.2f", num).replace(".", ",");
    }

    private String formatarHoras(double horas) {
        // Se a hora for exata (ex: 8.0), retorna "8". Se for quebrada, retorna com virgula.
        if (horas == (long) horas) return String.valueOf((long) horas);
        return String.valueOf(horas).replace(".", ",");
    }

    private boolean isDataNoIntervalo(String dataAlvo, String dataInicial, String dataFinal) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate alvo = LocalDate.parse(dataAlvo, formatter);
        LocalDate inicial = LocalDate.parse(dataInicial, formatter);
        LocalDate fInal = LocalDate.parse(dataFinal, formatter);
        return !alvo.isBefore(inicial) && !alvo.isAfter(fInal);
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

    // --- US 1: Criação de Empregados ---
    public String criarEmpregado(String nome, String endereco, String tipo, String salario) throws Exception {
        validarDadosIniciais(nome, endereco);
        if (tipo.equals("comissionado")) throw new Exception("Tipo nao aplicavel.");
        if (!tipo.equals("horista") && !tipo.equals("assalariado")) throw new Exception("Tipo invalido.");
        validarSalario(salario);

        String salarioFormatado = formatarValor(salario);
        String id = String.valueOf(nextId++);
        Empregado emp;
        
        if (tipo.equals("horista")) {
            emp = new EmpregadoHorista(id, nome, endereco, salarioFormatado);
        } else {
            emp = new EmpregadoAssalariado(id, nome, endereco, salarioFormatado);
        }
        
        empregados.put(id, emp);
        return id;
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao) throws Exception {
        validarDadosIniciais(nome, endereco);
        if (!tipo.equals("comissionado")) throw new Exception("Tipo nao aplicavel.");
        validarSalario(salario);
        
        if (comissao == null || comissao.isEmpty()) throw new Exception("Comissao nao pode ser nula.");
        try {
            double com = Double.parseDouble(comissao.replace(",", "."));
            if (com < 0) throw new Exception("Comissao deve ser nao-negativa.");
        } catch (NumberFormatException e) {
            throw new Exception("Comissao deve ser numerica.");
        }

        String salarioFormatado = formatarValor(salario);
        String id = String.valueOf(nextId++);
        
        Empregado emp = new EmpregadoComissionado(id, nome, endereco, salarioFormatado, comissao);
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
        if (atributo.equals("comissao") && emp instanceof EmpregadoComissionado) {
            return ((EmpregadoComissionado) emp).getComissao();
        }
        
        throw new Exception("Atributo nao existe.");
    }

    // --- US 2: Remoção e Busca ---
    public void removerEmpregado(String empId) throws Exception {
        if (empId == null || empId.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        if (!empregados.containsKey(empId)) throw new Exception("Empregado nao existe.");
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
        if (!empregados.containsKey(empId)) throw new Exception("Empregado nao existe.");
        Empregado emp = empregados.get(empId);
        if (!(emp instanceof EmpregadoHorista)) throw new Exception("Empregado nao eh horista.");
        
        double horasVal = Double.parseDouble(horas.replace(",", "."));
        ((EmpregadoHorista) emp).addCartao(new CartaoDePonto(data, horasVal));
    }

    public String getHorasTrabalhadas(String empId, String dataInicial, String dataFinal) throws Exception {
        if (!empregados.containsKey(empId)) throw new Exception("Empregado nao existe.");
        Empregado emp = empregados.get(empId);
        if (!(emp instanceof EmpregadoHorista)) throw new Exception("Empregado nao eh horista.");
        
        double normais = 0;
        for (CartaoDePonto cartao : ((EmpregadoHorista) emp).getCartoes()) {
            if (isDataNoIntervalo(cartao.getData(), dataInicial, dataFinal)) {
                double h = cartao.getHoras();
                normais += (h > 8) ? 8 : h;
            }
        }
        return formatarHoras(normais);
    }

    public String getHorasExtrasTrabalhadas(String empId, String dataInicial, String dataFinal) throws Exception {
        if (!empregados.containsKey(empId)) throw new Exception("Empregado nao existe.");
        Empregado emp = empregados.get(empId);
        if (!(emp instanceof EmpregadoHorista)) throw new Exception("Empregado nao eh horista.");
        
        double extras = 0;
        for (CartaoDePonto cartao : ((EmpregadoHorista) emp).getCartoes()) {
            if (isDataNoIntervalo(cartao.getData(), dataInicial, dataFinal)) {
                double h = cartao.getHoras();
                if (h > 8) extras += (h - 8);
            }
        }
        return formatarHoras(extras);
    }

    // --- US 4: Vendas ---
    public void lancaVenda(String empId, String data, String valor) throws Exception {
        if (!empregados.containsKey(empId)) throw new Exception("Empregado nao existe.");
        Empregado emp = empregados.get(empId);
        if (!(emp instanceof EmpregadoComissionado)) throw new Exception("Empregado nao eh comissionado.");
        
        double valorVal = Double.parseDouble(valor.replace(",", "."));
        ((EmpregadoComissionado) emp).addVenda(new ResultadoVenda(data, valorVal));
    }

    public String getVendasRealizadas(String empId, String dataInicial, String dataFinal) throws Exception {
        if (!empregados.containsKey(empId)) throw new Exception("Empregado nao existe.");
        Empregado emp = empregados.get(empId);
        if (!(emp instanceof EmpregadoComissionado)) throw new Exception("Empregado nao eh comissionado.");
        
        double total = 0;
        for (ResultadoVenda venda : ((EmpregadoComissionado) emp).getVendas()) {
            if (isDataNoIntervalo(venda.getData(), dataInicial, dataFinal)) {
                total += venda.getValor();
            }
        }
        return formatarValor(String.valueOf(total));
    }
}