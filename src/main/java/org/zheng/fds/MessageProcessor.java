package org.zheng.fds;

import com.google.gson.Gson;

public class MessageProcessor {
    /**
     * Return fraud score for the message. 0-100
     * 0 pass 1-30 low risk 31-60 medium risk 61-100 high risk 100+ fraud
     *
     * @param messageStr
     * @return
     */
    public int processMessage(String messageStr) throws Exception {
        //String transactionData = "{\"transactionId\":\"12345\",\"amount\":100.0,\"merchant\":\"ABC Store\"}";
        //parse message in json format
        Gson gson = new Gson();
        Message message = gson.fromJson(messageStr, Message.class);
        return processMessage(message);
    }

    public int processMessage(Message message) {
        //String transactionData = "{\"transactionId\":\"12345\",\"amount\":100.0,\"merchant\":\"ABC Store\"}";
        //parse message in json format
        if (message.getAmount() > 100)
            return 100;
        if (message.getAccountId().equals("1234"))
            return 100;
        return 0;
    }
}
