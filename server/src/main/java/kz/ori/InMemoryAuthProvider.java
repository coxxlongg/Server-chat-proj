package kz.ori;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InMemoryAuthProvider implements AuthenticationProvider{
    private static class User {
        private String login;
        private String password;
        private String username;

        public User(String login, String password, String username) {
            this.login = login;
            this.password = password;
            this.username = username;
        }
    }

    private List<User> list;

    public InMemoryAuthProvider() {
        this.list = new ArrayList<>(Arrays.asList(
                new User("Alex@gmail.com", "111", "Alex"),
                new User("Ben@gmail.com", "111", "Ben"),
                new User("John@gmail.com", "111", "John")
        ));
    }

    @Override
    public String getUsernameByLoginAndPassword(String login, String password) {
        for(User u : list) {
            if(u.login.equals(login) && u.password.equals(password)) {
                return u.username;
            }
        }
        return null;
    }
}
