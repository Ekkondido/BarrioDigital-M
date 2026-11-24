package cl.duoc.barriodigital.adminmq.dto;

import java.util.Map;

public class QueueInfoDto {

    private String name;
    private int messageCount;
    private int consumerCount;
    private boolean durable;
    private boolean autoDelete;
    private boolean isDlq;
    private Map<String, Object> arguments;

    public QueueInfoDto() {
    }

    public QueueInfoDto(String name, int messageCount, int consumerCount, boolean durable, boolean autoDelete, boolean isDlq, Map<String, Object> arguments) {
        this.name = name;
        this.messageCount = messageCount;
        this.consumerCount = consumerCount;
        this.durable = durable;
        this.autoDelete = autoDelete;
        this.isDlq = isDlq;
        this.arguments = arguments;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getMessageCount() { return messageCount; }
    public void setMessageCount(int messageCount) { this.messageCount = messageCount; }

    public int getConsumerCount() { return consumerCount; }
    public void setConsumerCount(int consumerCount) { this.consumerCount = consumerCount; }

    public boolean isDurable() { return durable; }
    public void setDurable(boolean durable) { this.durable = durable; }

    public boolean isAutoDelete() { return autoDelete; }
    public void setAutoDelete(boolean autoDelete) { this.autoDelete = autoDelete; }

    public boolean isDlq() { return isDlq; }
    public void setDlq(boolean dlq) { isDlq = dlq; }

    public Map<String, Object> getArguments() { return arguments; }
    public void setArguments(Map<String, Object> arguments) { this.arguments = arguments; }
}
