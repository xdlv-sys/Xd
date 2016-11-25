package xd.fw.derby;

import org.apache.derby.tools.ij;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import xd.fw.FwUtil;
import xd.fw.I18n;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * initialize the derby database
 */
public class DerbyInit {

    static Logger logger = LoggerFactory.getLogger(DerbyInit.class);

    //String name;
    @Autowired
    JdbcTemplate derbyTpl;

    @PostConstruct
    public void init() throws Exception{
        logger.info("start to initialize memory database");

        File dbDir = new File(I18n.getWebInfDir(),"db");
        File dbFile = new File(dbDir, "derby.bin");
        String dbName = dbFile.getCanonicalPath();
        String connectionURL = "jdbc:derby:" + dbName + ";create=true";
        /*
         * it's important to set url for spring bean jdbcTemplate, otherwise the derbyTpl don't work
         * because the path of database dynamically is created in runtime
          */
        ((DriverManagerDataSource)derbyTpl.getDataSource()).setUrl(connectionURL);

        if (dbFile.exists()){
            return;
        }
        // run the initialized sql scripts
        try(Connection connection = DriverManager.getConnection(connectionURL)){
            dbDir.listFiles((f)->{
                String fileName = f.getName();
                if (fileName.startsWith("derby") && fileName.endsWith(".sql")){
                    logger.info("start to run scripts");
                    try(InputStream inputStream = new FileInputStream(f)){
                        ij.runScript(connection,inputStream, FwUtil.UTF8,System.out,FwUtil.UTF8);
                    } catch (IOException e){
                        logger.error("fail to run script: " + f , e);
                    }
                }
                return true;
            });
        }
    }

    @PreDestroy
    public void close() throws Exception{
        boolean gotSQLExc = false;
        try {
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException se)  {
            if ( se.getSQLState().equals("XJ015") ) {
                gotSQLExc = true;
            }
        }
        if (!gotSQLExc) {
            logger.info("Database did not shut down normally");
        }  else  {
            logger.info("shutdown memory database completely");
        }
    }
}
