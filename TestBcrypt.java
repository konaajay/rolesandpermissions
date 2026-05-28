import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestBcrypt {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        boolean matches = encoder.matches("superadmin", "$2a$10$et8HmjQGcKnB4Ta4Nr.hi.rbxYMFTKM5mW9qgcGnEaYtKCr16s5b2");
        System.out.println("Matches: " + matches);
    }
}
