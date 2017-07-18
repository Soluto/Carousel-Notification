package com.soluto.carouselnotification;

public interface LoggingApi {
    public void debug(String msg);
    public void info(String msg);
    public void warn(String msg);
    public void warn(String msg, Throwable t);
    public void error(String msg);
    public void error(String msg, Throwable t);
}
