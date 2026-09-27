import easyaccept.EasyAccept;

//Classe principal responsável por inicializar a execução dos testes de aceitação.
public class Main {
    public static void main(String[] args) {
        // Caminho referenciando a classe Facade, que centraliza as chamadas do sistema
        String facade = "controllers.Facade"; 
        // Executa o EasyAccept passando o caminho e o script de teste específico
        EasyAccept.main(new String[]{
            facade,
            "tests/us7.txt"
        });
    }
}