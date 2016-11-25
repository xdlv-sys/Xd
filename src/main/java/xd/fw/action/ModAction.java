package xd.fw.action;

import org.springframework.beans.factory.annotation.Autowired;
import xd.fw.bean.Mod;
import xd.fw.bean.Role;
import xd.fw.service.FwService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ModAction extends BaseAction{
    final static int ADMIN_ID = -10;
    @Autowired
    FwService fwService;
    List<Mod> mods;

    Role role;

    Mod mod;
    public String obtainMods() throws Exception {
        total = fwService.getAllCount(Mod.class);
        mods = fwService.getList(Mod.class,null,start, limit);
        return SUCCESS;
    }

    public String obtainModsByRole() throws Exception{
        Set<Mod> mods = fwService.get(Role.class,role.getId()).getMods();
        this.mods = new ArrayList<>(mods.size());
        this.mods.addAll(mods);
        return SUCCESS;
    }

    public String delMod() throws Exception{
        if (mods != null){
            for (Mod mod : mods){
                fwService.delete(Mod.class,mod.getId());
            }
        }
        return FINISH;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String saveMod() throws Exception{
        fwService.saveOrUpdate(mod);
        return FINISH;
    }

    public void setMod(Mod mod) {
        this.mod = mod;
    }

    public Mod getMod() {
        return mod;
    }

    public List<Mod> getMods() {
        return mods;
    }

    public void setMods(List<Mod> mods) {
        this.mods = mods;
    }
}
