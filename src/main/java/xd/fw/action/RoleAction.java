package xd.fw.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import xd.fw.bean.Mod;
import xd.fw.bean.Role;
import xd.fw.bean.User;
import xd.fw.service.FwService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RoleAction extends BaseAction {
    @Autowired
    FwService fwService;
    Role role;
    List<Role> roles;

    User user;

    public String obtainUserRoles(){
        Set<Role> roles = fwService.get(User.class,user.getId()).getRoles();
        this.roles = new ArrayList<>(roles.size());
        this.roles.addAll(roles);
        return SUCCESS;
    }

    public String obtainRoles() {
        total = fwService.getAllCount(Role.class);
        roles = fwService.getList(Role.class,null,start, limit);
        return SUCCESS;
    }

    public String deleteRole() {

        for (int i=0; roles != null && i<roles.size();i++){
            fwService.delete(Role.class,roles.get(i).getId());
        }
        return FINISH;
    }

    public String saveRole() {
        fwService.saveOrUpdateRole(role);
        return FINISH;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Role getRole() {
        return role;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
