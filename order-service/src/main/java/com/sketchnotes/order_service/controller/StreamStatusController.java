package com.sketchnotes.order_service.controller;

import com.sketchnotes.order_service.dtos.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.config.BindingProperties;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@RestController
@RequestMapping("/internal/stream")
@RequiredArgsConstructor
@Slf4j
public class StreamStatusController {

    private final ApplicationContext ctx;
    private final Environment env;

    @Autowired(required = false)
    private BindingServiceProperties bindingServiceProperties;

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> status() {
        Map<String, Object> result = new HashMap<>();

        // list function consumer beans
        Map<String, Consumer> consumers = ctx.getBeansOfType(Consumer.class);
        Map<String, String> consumerInfo = new HashMap<>();
        for (Map.Entry<String, Consumer> e : consumers.entrySet()) {
            consumerInfo.put(e.getKey(), e.getValue().getClass().getName());
        }
        result.put("functionConsumers", consumerInfo);

        // binding properties (if available)
        if (bindingServiceProperties != null) {
            Map<String, BindingProperties> bindings = bindingServiceProperties.getBindings();
            Map<String, Object> bindingSummary = new HashMap<>();
            for (Map.Entry<String, BindingProperties> e : bindings.entrySet()) {
                BindingProperties bp = e.getValue();
                Map<String, Object> info = new HashMap<>();
                info.put("destination", bp.getDestination());
                info.put("group", bp.getGroup());
                bindingSummary.put(e.getKey(), info);
            }
            result.put("bindings", bindingSummary);
        } else {
            result.put("bindings", "BindingServiceProperties not available");
        }

        // kafka consumer group property
        String kafkaGroup = env.getProperty("spring.kafka.consumer.group-id");
        result.put("spring.kafka.consumer.group-id", kafkaGroup);

        return ResponseEntity.ok(ApiResponse.success(result, "stream-status"));
    }
}
