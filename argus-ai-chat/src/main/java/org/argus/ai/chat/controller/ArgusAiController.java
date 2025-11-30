package org.argus.ai.chat.controller;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.argus.ai.chat.controller.entity.ActorsFilms;
import org.argus.ai.chat.tools.DateTimeTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.ai.converter.StructuredOutputConverter;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.ollama.management.ModelManagementOptions;
import org.springframework.ai.ollama.management.OllamaModelManager;
import org.springframework.ai.ollama.management.PullModelStrategy;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
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

    @GetMapping("/chatmodel/call1")
    String chatModelCall1(String userInput) {
        PromptTemplate template = new PromptTemplate("Tell me a {adjective} joke about {topic}");
        Prompt prompt = template.create(Map.of("adjective", "funny", "topic", "AI"));

        ChatResponse chatResponse = this.chatModel.call(prompt);
        return chatResponse.getResult().getOutput().getText();
    }

    @GetMapping("/chatmodel/call2")
    String chatModelCall2(String userInput) {
        String systemText = "You are {name}, reply in the style of a {voice}.";
        SystemPromptTemplate systemTemplate = new SystemPromptTemplate(systemText);
        Message systemMsg = systemTemplate.createMessage(Map.of("name", "Jack", "voice", "pirate"));
        Message userMsg = new UserMessage("Tell me a joke");
        Prompt prompt = new Prompt(List.of(systemMsg, userMsg));
        ChatResponse chatResponse = this.chatModel.call(prompt);
        return chatResponse.getResult().getOutput().getText();
    }

    @GetMapping("/chatmodel/call3")
    String chatModelCall3(String userInput) {
        PromptTemplate template = PromptTemplate.builder()
            .renderer(StTemplateRenderer.builder()
                .startDelimiterToken('<')
                .endDelimiterToken('>')
                .build())
            .template("Generate JSON: <data>")
            .build();
        Prompt prompt = template.create(Map.of("adjective", "funny", "topic", "AI"));

        ChatResponse chatResponse = this.chatModel.call(prompt);
        return chatResponse.getResult().getOutput().getText();
    }

    @GetMapping("/chatclient/call")
    String call(String userInput) {
        return this.chatClient.prompt()
            .user(userInput)
            .call()
            .content();
    }

    @GetMapping("/chatclient/format/call")
    String formatCall(String userInput) {
        StructuredOutputConverter<Map<String, Object>> outputConverter = new MapOutputConverter();
        String userInputTemplate = "{format}";
        Prompt prompt = new Prompt(
            PromptTemplate.builder()
                .template(userInputTemplate)
                .variables(Map.of("format", outputConverter.getFormat()))
                .build().createMessage()
        );

        ChatResponse chatResponse = this.chatModel.call(prompt);
        return chatResponse.getResult().getOutput().getText();
    }
    @GetMapping("/chatclient/format/call1")
    String formatCall1(String userInput) {
        //ActorsFilms actorsFilms = this.chatClient.prompt()
        //    .user(u -> u.text("Generate the filmography of 5 movies for {actor}.")
        //        .param("actor", "Tom Hanks"))
        //    .call()
        //    .entity(ActorsFilms.class);
        return null;
    }

    @GetMapping("/chatclient/memory/call")
    String callWithMemory() {
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
            .maxMessages(10)
            .chatMemoryRepository(new InMemoryChatMemoryRepository())
            .build();
        chatMemory.add("user-123", new UserMessage("My name is Bond"));

        ChatClient chatClient = ChatClient.builder(chatModel)
            .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
            .build();

        // 调用时指定会话 ID
        return chatClient.prompt()
            .user("What’s my name?")
            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "user-123"))
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
            .system("You are a helpful assistant.")
            .user(userInput)
            .call()
            .content();
    }

    @GetMapping("/chatclient/tool/call")
    String toolCalling(String userInput) {

        ToolCallback toolCallback = FunctionToolCallback
            .builder("currentWeather", (Function<WeatherRequest, WeatherResponse>)request -> {
                System.out.println("调用天气函数");
                return new WeatherResponse(25.0, "C");
            })
            .description("获取某地天气")
            .inputType(WeatherRequest.class)
            .build();

        return this.chatClient.prompt()
            .system("You are a helpful assistant.")
            .user(userInput)
            .tools(new DateTimeTools())
            .toolCallbacks(toolCallback)
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

    public record WeatherRequest(String location, String unit) {
    }

    public record WeatherResponse(double temp, String unit) {
    }
}
