package org.argus.ai.chat.runner;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

/**
 *
 *
 * @author junjie.cheng
 * @version 1.0
 * @date 2025/9/24 10:40
 */
public class ArgusAiRunner {
    @Bean
    public CommandLineRunner runner(ChatClient.Builder builder) {
        return args -> {
            ChatClient chatClient = builder.build();
            String response = chatClient.prompt("Tell me a joke").call().content();
            System.out.println(response);
        };
    }

}
