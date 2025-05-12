package org.argus.ai.chat.controller;


import org.springframework.ai.model.ollama.autoconfigure.OllamaConnectionProperties;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.ollama.management.ModelManagementOptions;
import org.springframework.ai.ollama.management.OllamaModelManager;
import org.springframework.ai.ollama.management.PullModelStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * @author junjie.cheng
 */
@RestController
public class OllamaController {
    private static final String MODEL = "llama3.2";

    private static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(10);

    private static final int DEFAULT_MAX_RETRIES = 2;


    private final OllamaChatModel chatModel;


    public OllamaController(@Autowired OllamaConnectionProperties ollamaConnectionProperties) {
        //加载本地模型
        OllamaApi api = new OllamaApi(ollamaConnectionProperties.getBaseUrl());
        var modelManagementOptions = ModelManagementOptions.builder()
                .maxRetries(DEFAULT_MAX_RETRIES)
                .timeout(DEFAULT_TIMEOUT)
                .build();
        var ollamaModelManager = new OllamaModelManager(api, modelManagementOptions);
        ollamaModelManager.pullModel(MODEL, PullModelStrategy.WHEN_MISSING);

        //构建对话模型
        this.chatModel = OllamaChatModel.builder()
                .ollamaApi(api)
                .defaultOptions(OllamaOptions.builder().model(MODEL).temperature(0.9).build())
                .build();

    }


    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chat(@RequestBody String message) {
        //通过模型进行对话
        return chatModel.stream(message)
                .doOnError(e -> {
                    System.out.println("Error: " + e.getMessage());
                })
                .doOnNext(response -> {
                    System.out.println("Response: " + response);
                })
                .map(response -> ServerSentEvent.builder(response).build());
    }


}