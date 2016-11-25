package xd.fw.mbean;

import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;
import xd.fw.FwUtil;

import java.lang.reflect.Method;

@Service
@ManagedResource(objectName = "xapp:name=RunTest", description = "Run Test cases")
public class RunTest {

    @ManagedOperation(description = "Run Test case")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name="className",description = "class name"),
            @ManagedOperationParameter(name="methodName", description = "method Name"),
            @ManagedOperationParameter(name="parameters", description = "ip,port,action example:localhost,48011,1")
    })
    public String runCase(String className, String methodName, String params) throws Exception{
        Object obj;
        Method method;
        if (className.indexOf(".") < 0){
            obj = FwUtil.getBean(className);
        } else {
            obj = Class.forName(className).newInstance();
        }
        method = obj.getClass().getDeclaredMethod(methodName,String.class);
        return String.valueOf(method.invoke(obj,params));
    }
}
