package guilherme.com.br.contactpikerlist.pojos;

/**
 * Created by guilh on 17/02/2016.
 */
public class Contatos {

    private int id;
    private String nome;
    private String telefone;
    private String email;
    private Boolean sentInvite;
    private int type;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getSentInvite() {
        return sentInvite;
    }

    public void setSentInvite(Boolean sentInvite) {
        this.sentInvite = sentInvite;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
