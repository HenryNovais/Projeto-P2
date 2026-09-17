import easyaccept.EasyAccept;

public class Main {
    public static void main(String[] args) {
        String facade = "controllers.Facade"; 
        
        EasyAccept.main(new String[]{
            facade,
            "tests/us3.txt",
            "tests/us3_1.txt",
            "tests/us4.txt",
            "tests/us4_1.txt"
        });
    }
}