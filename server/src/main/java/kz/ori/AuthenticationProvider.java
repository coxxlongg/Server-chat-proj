package kz.ori;

public interface AuthenticationProvider {
    String getUsernameByLoginAndPassword(String login, String password);

    boolean isAuthenticated(String login, String password);
}
