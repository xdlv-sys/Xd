package xd.fw.bean;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "t_role")
public class Role {
    @Id
    private Integer id;
    @Column(name="name")
    private String name;

    @ManyToMany(cascade={CascadeType.PERSIST},fetch=FetchType.EAGER)
    @JoinTable(name="t_role_mod" ,joinColumns={@JoinColumn(name="role_id")}
            ,inverseJoinColumns={@JoinColumn(name="mod_id")})
    private Set<Mod> mods;

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

    public void setMods(Set<Mod> mods) {
        this.mods = mods;
    }

    public Set<Mod> getMods() {
        return mods;
    }

    @Transient
    public List<Mod> modsL;

    public List<Mod> getModsL() {
        return modsL;
    }

    public void setModsL(List<Mod> modsL) {
        this.modsL = modsL;
    }
}