package org.argus.ai.chat.controller;

import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.ollama.management.ModelManagementOptions;
import org.springframework.ai.ollama.management.OllamaModelManager;
import org.springframework.ai.ollama.management.PullModelStrategy;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 *
 *
 * @author junjie.cheng
 * @version 1.0
 * @date 2025/9/24 11:37
 */
@RestController
public class ArgusAiController {
    private static final String MODEL = "llama3.2";

    private static final int DEFAULT_MAX_RETRIES = 2;

    private final ChatClient chatClient;
    private final OllamaChatModel chatModel;

    public ArgusAiController() {
        //加载本地模型
        OllamaApi api = OllamaApi.builder().build();
        var modelManagementOptions = ModelManagementOptions.builder()
            .maxRetries(DEFAULT_MAX_RETRIES)
            .build();
        var ollamaModelManager = new OllamaModelManager(api, modelManagementOptions);
        ollamaModelManager.pullModel(MODEL, PullModelStrategy.WHEN_MISSING);

        //构建对话模型
        this.chatModel = OllamaChatModel.builder()
            .ollamaApi(api)
            .defaultOptions(OllamaOptions.builder().model(MODEL).temperature(0.9).build())
            .build();

        this.chatClient = ChatClient.builder(this.chatModel).build();

    }

    @GetMapping("/chatmodel/call")
    String chatModelCall(String userInput) {
        ChatResponse chatResponse = this.chatModel.call(new Prompt(userInput));
        return chatResponse.getResult().getOutput().getText();
    }

    @GetMapping("/chatclient/call")
    String call(String userInput) {
        return this.chatClient.prompt()
            .user(userInput)
            .call()
            .content();
    }

    @GetMapping("/chatclient/advisor/call")
    String generation(String userInput) {

        SimpleLoggerAdvisor customLogger = new SimpleLoggerAdvisor(
            request -> "Custom request: " + request.prompt().getUserMessage(),
            response -> "Custom response: " + response.getResult(),
            0
        );

        return this.chatClient.prompt()
            .advisors(
                customLogger
            )
            .user(userInput)
            .call()
            .content();
    }

    @PostMapping(value = "/chatclient/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chat(@RequestBody String userInput) {
        //通过模型进行对话
        return this.chatClient.prompt()
            .user(userInput)
            .stream()
            .content()
            .doOnError(e -> {
                System.out.println("Error: " + e.getMessage());
            })
            .doOnNext(response -> {
                System.out.println("Response: " + response);
            })
            .map(response -> ServerSentEvent.builder(response).build());
    }

}
