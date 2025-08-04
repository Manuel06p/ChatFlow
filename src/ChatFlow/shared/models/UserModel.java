package ChatFlow.shared.models;

import java.io.Serializable;

public class UserModel implements Serializable {
    private final String username;

    public UserModel(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

}