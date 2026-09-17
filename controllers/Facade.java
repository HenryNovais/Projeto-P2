package controllers;

import models.*;
import java.util.LinkedHashMap;
import java.util.Map;

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

    // --- MÉTODOS AUXILIARES DE VALIDAÇÃO E FORMATAÇÃO ---
    private String formatarValor(String valor) {
        double num = Double.parseDouble(valor.replace(",", "."));
        return String.format(java.util.Locale.US, "%.2f", num).replace(".", ",");
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
}