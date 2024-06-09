package kz.ori;

public interface AuthenticationProvider {
    String getUsernameByLoginAndPassword(String login, String password);
}
