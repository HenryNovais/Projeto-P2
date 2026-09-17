import easyaccept.EasyAccept;

public class Main {
    public static void main(String[] args) {
        String facade = "controllers.Facade"; 
        
        EasyAccept.main(new String[]{
            facade,
            "tests/us1.txt",
            "tests/us1_1.txt"
        });
    }
}