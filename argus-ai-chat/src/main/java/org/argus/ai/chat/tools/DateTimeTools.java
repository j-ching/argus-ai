package org.argus.ai.chat.tools;

import java.time.LocalDateTime;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 *
 *
 * @author junjie.cheng
 * @version 1.0
 * @date 2025/9/25 11:09
 */
public class DateTimeTools {
    @Tool(description = "获取用户时区的当前日期时间")
    String getCurrentDateTime() {
        System.out.println("获取当前日期时间");
        return LocalDateTime.now().toString();
    }

    @Tool(description = "设置闹钟")
    void setAlarm(@ToolParam(description = "ISO-8601 格式时间") String time) {
        System.out.println("设置闹钟");
        System.out.println("闹钟已设为: " + time);
    }

}
