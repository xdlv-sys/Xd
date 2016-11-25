package xd.fw.service;


public interface IConst {
    int INVALIDATE_INT = -1000;

    String SUCCESS_FLAG = "SUCCESS";

    short PAY_WX = 0, PAY_ALI = 1;

    byte STATUS_INI = 0, STATUS_DONE = 1, STATUS_FAIL = 2;

    byte ORDER_STATUS_REFUND_DONE = 7, ORDER_STATUS_REFUND_FAIL = 8;

    byte ORDER_STATUS_CLOSE_DONE = 9, ORDER_STATUS_CLOSE_FAIL = 10;
}
