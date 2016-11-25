package xd.fw.bean;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "t_user")
public class User {
    @Id
    private Integer id;
    @Column(name = "name")
    private String name;

    @Column(name = "password")
    private String password;

    @Column(name="mail")
    private String mail;

    @ManyToMany(cascade={CascadeType.PERSIST},fetch=FetchType.EAGER)
    @JoinTable(name="t_user_role" ,joinColumns={@JoinColumn(name="user_id")}
            ,inverseJoinColumns={@JoinColumn(name="role_id")})
    private Set<Role> roles;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    @Transient
    private List<Role> rolesL;

    public List<Role> getRolesL() {
        return rolesL;
    }

    public void setRolesL(List<Role> rolesL) {
        this.rolesL = rolesL;
    }

}