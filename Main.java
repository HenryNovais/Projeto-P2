import easyaccept.EasyAccept;

public class Main {
    public static void main(String[] args) {
        String facade = "controllers.Facade"; 
        
        EasyAccept.main(new String[]{
            facade,
            "tests/us5.txt",
            "tests/us5_1.txt",
            "tests/us6.txt",
            "tests/us6_1.txt"
        });
    }
}