package org.zheng.fds;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class MessageProcessorTest {

    private MessageProcessor messageProcessor = new MessageProcessor();

    @Test
    public void testProcessMessage_AmmountEq100() throws Exception {
        // 正常输入测试
        String messageStr = "{\"transactionId\":\"12345\",accountId=\"12345\",\"amount\":100.0,\"merchant\":\"ABC Store\"}";
        int expectedResult = 0; // 假设正常处理后的预期结果
        int actualResult = messageProcessor.processMessage(messageStr);
        assertEquals(0, actualResult);
    }

    @Test
    public void testProcessMessage_AmmountGr100() throws Exception {
        // 空字符串输入测试
        String messageStr = "{\"transactionId\":\"12345\",accountId=\"12345\",\"amount\":150.0,\"merchant\":\"ABC Store\"}";
        assertEquals(messageProcessor.processMessage(messageStr), 100);
    }

    @Test
    public void testProcessMessage_AccountId1234() throws Exception {
        // 空字符串输入测试
        String messageStr = "{\"transactionId\":\"12345\",accountId=\"1234\",\"amount\":150.0,\"merchant\":\"DCBA Store\"}";
        assertEquals(messageProcessor.processMessage(messageStr), 100);
    }

    @Test
    public void testProcessMessage_InvalidInput() {
        // 无效输入测试
        String messageStr = null;
        assertThrows(Exception.class, () -> {
            messageProcessor.processMessage(messageStr);
        });
    }
}
