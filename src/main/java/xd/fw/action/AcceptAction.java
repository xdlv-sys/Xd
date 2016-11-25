package xd.fw.action;

import java.io.File;

/**
 * Created by exiglvv on 9/2/2016.
 */
public class AcceptAction extends BaseAction{

    File file;
    String name;
    String fileFileName;
    String fileContentType;

    public String upload(){
        if (name.equals("中国") && file.exists()){
            return SUCCESS;
        }
        throw new IllegalArgumentException("fail");
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setFileFileName(String fileFileName) {
        this.fileFileName = fileFileName;
    }

    public void setFileContentType(String fileContentType) {
        this.fileContentType = fileContentType;
    }
}
