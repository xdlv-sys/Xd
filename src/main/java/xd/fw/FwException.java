package xd.fw;

/**
 * Created by xd on 2015/10/31.
 */
public class FwException extends RuntimeException {
    public FwException(String msg, Throwable e){
        super(msg,e);
    }

    public FwException(String msg){
        super(msg);
    }
}
