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

    public void encerrarSistema() {
    }

    // --- US 1: Criação de Empregados ---
    public String criarEmpregado(String nome, String endereco, String tipo, String salario) throws Exception {
        String id = String.valueOf(nextId++);
        Empregado emp;
        
        if (tipo.equals("horista")) {
            emp = new EmpregadoHorista(id, nome, endereco, salario);
        } else if (tipo.equals("assalariado")) {
            emp = new EmpregadoAssalariado(id, nome, endereco, salario);
        } else {
            throw new Exception("Tipo invalido.");
        }
        
        empregados.put(id, emp);
        return id;
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao) throws Exception {
        if (!tipo.equals("comissionado")) {
            throw new Exception("Tipo invalido.");
        }
        
        String id = String.valueOf(nextId++);
        Empregado emp = new EmpregadoComissionado(id, nome, endereco, salario, comissao);
        empregados.put(id, emp);
        return id;
    }

    public String getAtributoEmpregado(String empId, String atributo) throws Exception {
        if (!empregados.containsKey(empId)) {
            throw new Exception("Empregado nao existe.");
        }
        
        Empregado emp = empregados.get(empId);
        
        if (atributo.equals("nome")) return emp.getNome();
        if (atributo.equals("endereco")) return emp.getEndereco();
        if (atributo.equals("tipo")) return emp.getTipo();
        if (atributo.equals("salario")) return emp.getSalario();
        if (atributo.equals("comissao") && emp instanceof EmpregadoComissionado) {
            return ((EmpregadoComissionado) emp).getComissao();
        }
        
        throw new Exception("Atributo nao existe.");
    }

    // --- US 2: Remoção e Busca ---
    public void removerEmpregado(String empId) throws Exception {
        if (!empregados.containsKey(empId)) {
            throw new Exception("Empregado nao existe.");
        }
        empregados.remove(empId);
    }

    public String getEmpregadoPorNome(String nome, int indice) throws Exception {
        int currentIndex = 1;
        for (Empregado emp : empregados.values()) {
            if (emp.getNome().equals(nome)) {
                if (currentIndex == indice) {
                    return emp.getId();
                }
                currentIndex++;
            }
        }
        throw new Exception("Empregado nao existe.");
    }
}