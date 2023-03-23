import java.io.Serializable;
import java.util.Objects;

public class ClienteInfo implements Serializable {
    private String nome, username, password, email;

    public ClienteInfo() {
    }

    public ClienteInfo(String nome, String username, String email, String password) {
        this.nome = nome;
        this.password = password;
        this.username = username;
        this.email = email;
    }

    public String toString() {
        return "Cliente:" + nome + "/" + username + " Email: " + email + " Password: " + password;
    }

    public String getNome() {
        return nome;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
