package org.zheng.fds;

import lombok.Data;

@Data
public class Message {
    private String transactionId;
    private double amount;
    private String merchant;
    private String accountId;
    private int riskScore;
}
