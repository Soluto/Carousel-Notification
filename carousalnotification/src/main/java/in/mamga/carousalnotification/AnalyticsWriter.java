package in.mamga.carousalnotification;


import java.util.Map;

public interface AnalyticsWriter {
    void write(String eventName, Map<String, Object> extraData);
}
