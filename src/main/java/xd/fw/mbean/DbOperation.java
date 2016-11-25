package xd.fw.mbean;

import org.apache.derby.tools.ij;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import xd.fw.FwUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@ManagedResource(objectName = "xapp:name=DbOperation", description = "derby db operations")
public class DbOperation {
    Logger logger = LoggerFactory.getLogger(DbOperation.class);

    @Autowired
    JdbcTemplate derbyTpl;

    @ManagedOperation(description = "Select Sql")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name="sql",description = "sql"),
            @ManagedOperationParameter(name="rowNumber",description = "rowNumber")
    })
    public String selectSql(String sql, int rowNumber){
        if (!sql.toLowerCase().startsWith("select")){
            return "invalidate sql";
        }
        StringBuilder buffer = new StringBuilder();
        derbyTpl.query(sql,(rs)->{
            for (int i=0;i<rowNumber;i++){
                buffer.append(rs.getString(i + 1)).append(",");
            }
            buffer.append("\n");
        });
        return buffer.toString();
    }

    @ManagedOperation(description = "Run Sql")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name="sql",description = "sql")
    })
    public String runSql(String sql){
        derbyTpl.execute(sql);
        return "OK";
    }

    @ManagedOperation(description = "Run File")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name="file",description = "file")
    })
    public String runFile(String file){
        File scriptFile = new File(file);
        if (!scriptFile.exists()){
            return "file not exists";
        }
        try(InputStream inputStream = new FileInputStream(scriptFile)){
            ij.runScript(derbyTpl.getDataSource().getConnection(), inputStream, FwUtil.UTF8, System.out, FwUtil.UTF8);
        } catch (Exception e){
            logger.error("fail to run script: " + file , e);
        }
        return "OK";
    }


}
