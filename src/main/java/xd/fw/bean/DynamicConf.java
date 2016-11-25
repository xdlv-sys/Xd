package xd.fw.bean;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "t_dynamic_conf")
public class DynamicConf {
    @Id
    private String confName;
    private String confValue;
    private String confDesc;
    private Byte dirty;

    public String getConfName() {
        return confName;
    }

    public void setConfName(String confName) {
        this.confName = confName;
    }

    public String getConfValue() {
        return confValue;
    }

    public void setConfValue(String confValue) {
        this.confValue = confValue;
    }

    public String getConfDesc() {
        return confDesc;
    }

    public void setConfDesc(String confDesc) {
        this.confDesc = confDesc;
    }

    public Byte getDirty() {
        return dirty;
    }

    public void setDirty(Byte dirty) {
        this.dirty = dirty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DynamicConf that = (DynamicConf) o;

        if (confName != null ? !confName.equals(that.confName) : that.confName != null) return false;
        if (confValue != null ? !confValue.equals(that.confValue) : that.confValue != null) return false;
        if (confDesc != null ? !confDesc.equals(that.confDesc) : that.confDesc != null) return false;
        return dirty != null ? dirty.equals(that.dirty) : that.dirty == null;

    }

    @Override
    public int hashCode() {
        int result = confName != null ? confName.hashCode() : 0;
        result = 31 * result + (confValue != null ? confValue.hashCode() : 0);
        result = 31 * result + (confDesc != null ? confDesc.hashCode() : 0);
        result = 31 * result + (dirty != null ? dirty.hashCode() : 0);
        return result;
    }
}
