package com.soluto.carouselnotification;


import java.util.Map;

public interface AnalyticsWriter {
    void write(String eventName, Map<String, Object> extraData);
}
