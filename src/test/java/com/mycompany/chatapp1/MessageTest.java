package com.mycompany.chatapp1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class MessageTest {

    private Message message1;
    private Message message2;
    private List<Message> testList;

    @BeforeEach
    public void setUp() {
        // Initial setup for existing tests
        message1 = new Message("0011223344", 1, "+27718693002",
                "Hi Mike, can you join us for dinner tonight?", "00:1:HI TONIGHT?", "Sent");

        message2 = new Message("9988776655", 2, "08575975889",
                "Hi Keegan, did you receive the payment?", "99:2:HI PAYMENT?", "Discard");

        // Set up the specific Test Data for Part 3 Unit Tests (Messages 1 to 5)
        testList = new ArrayList<>();
        
        // Message 1: Sent
        testList.add(new Message("0000000001", 1, "+27834557896", "Did you get the cake?", "00:1:DID CAKE?", "Sent"));
        
        // Message 2: Stored
        testList.add(new Message("0000000002", 2, "+27838884567", "Where are you? You are late! I have asked you to be on time.", "00:2:WHERE TIME.", "Stored"));
        
        // Message 3: Disregard
        // Note: Disregarded messages go to disregarded list, but we can add it to test list to check calculations
        testList.add(new Message("0000000003", 3, "+27834484567", "Yohoooo, I am at your gate!", "00:3:YOHOOOO GATE!", "Disregard"));
        
        // Message 4: Sent
        testList.add(new Message("0838884567", 4, "", "It is dinner time !", "08:4:IT !", "Sent"));
        
        // Message 5: Stored
        testList.add(new Message("0000000005", 5, "+27838884567", "Ok, I am leaving without you.", "00:5:OK YOU.", "Stored"));
    }

    // ==========================================
    // EXISTING TESTS (Converted to JUnit 5)
    // ==========================================

    @Test
    public void testCheckMessageID_Valid() {
        assertTrue(message1.checkMessageID());
    }

    @Test
    public void testCheckMessageID_Invalid() {
        Message badMessage = new Message("ABC", 3, "+27700000000", "Test message", "", "Sent");
        assertFalse(badMessage.checkMessageID());
    }

    @Test
    public void testCheckRecipientCell_Valid() {
        String result = message1.checkRecipientCell();
        assertEquals("Recipient number is valid.", result);
    }

    @Test
    public void testCheckRecipientCell_Invalid() {
        String result = message2.checkRecipientCell();
        assertEquals("Invalid recipient number. Must contain an international code (+) and be longer than 10 characters.", result);
    }

    @Test
    public void testMessageLength_Under250() {
        String content = message1.getContent();
        assertTrue(content.length() <= 250);
    }

    @Test
    public void testMessageLength_Over250() {
        String longContent = "A".repeat(260);
        Message longMessage = new Message("1234567890", 3, "+27700000000", longContent, "", "Sent");
        assertFalse(longMessage.getContent().length() <= 250);
    }

    @Test
    public void testCreateMessageHash_Message1() {
        String hash = message1.createMessageHash();
        assertEquals("00:1:HI TONIGHT?", hash);
    }

    @Test
    public void testCreateMessageHash_Message2() {
        String hash = message2.createMessageHash();
        assertEquals("99:2:HI PAYMENT?", hash);
    }

    @Test
    public void testReturnTotalMessages() {
        ArrayList<Message> list = new ArrayList<>();
        list.add(message1);
        list.add(message2);
        assertEquals(2, Message.returnTotalMessages(list));
    }

    @Test
    public void testMessageStatus_Sent() {
        assertEquals("Sent", message1.getStatus());
    }

    @Test
    public void testMessageStatus_Discard() {
        assertEquals("Discard", message2.getStatus());
    }

    @Test
    public void testMessageStatus_Stored() {
        Message storedMessage = new Message("5544332211", 3, "+27900000000", "Test store message", "", "Stored");
        assertEquals("Stored", storedMessage.getStatus());
    }

    @Test
    public void testSentMessages_ContainsRecipient() {
        String result = message1.sentMessages();
        assertTrue(result.contains("+27718693002"));
    }

    @Test
    public void testPrintMessage_ContainsMessageID() {
        String result = message1.printMessage();
        assertTrue(result.contains("0011223344"));
    }

    // ==========================================
    // PART 3 SPECIFIC UNIT TESTS (Required)
    // ==========================================

    @Test
    public void testSentMessagesArrayCorrectlyPopulated() {
        // Prepare Main class with our test messages
        Main.sentMessages.clear();
        Main.disregardedMessagesList.clear();

        // Load messages 1 to 4
        for (int i = 0; i < 4; i++) {
            Message m = testList.get(i);
            if ("Disregard".equalsIgnoreCase(m.getStatus())) {
                Main.disregardedMessagesList.add(m);
            } else {
                Main.sentMessages.add(m);
            }
        }

        // Populate arrays
        Main.populateArrays();

        // Assert that the Sent Messages array contains exactly the sent message contents:
        // Message 1: "Did you get the cake?"
        // Message 4: "It is dinner time !"
        assertEquals(2, Main.sentMessagesArray.length);
        assertEquals("Did you get the cake?", Main.sentMessagesArray[0]);
        assertEquals("It is dinner time !", Main.sentMessagesArray[1]);
    }

    @Test
    public void testDisplayLongestMessage() {
        // Test Data: message 1-4
        List<Message> subList = testList.subList(0, 4);
        String longest = Message.getLongestMessage(subList);
        assertEquals("Where are you? You are late! I have asked you to be on time.", longest);
    }

    @Test
    public void testSearchForMessageID() {
        // Test Data: message 4 ID: "0838884567"
        String result = Message.searchByMessageId(testList, "0838884567");
        assertEquals("It is dinner time !", result);
    }

    @Test
    public void testSearchAllMessagesByRecipient() {
        // Test Data: recipient "+27838884567"
        List<String> results = Message.searchByRecipient(testList, "+27838884567");
        
        assertEquals(2, results.size());
        assertEquals("Where are you? You are late! I have asked you to be on time.", results.get(0));
        assertEquals("Ok, I am leaving without you.", results.get(1));
    }

    @Test
    public void testDeleteMessageUsingHash() {
        // Test Data: Test Message 2. Hash: "00:2:WHERE TIME."
        Message deleted = Message.deleteByHash(testList, "00:2:WHERE TIME.");
        
        assertNotNull(deleted);
        assertEquals("Where are you? You are late! I have asked you to be on time.", deleted.getContent());
        
        // Ensure it was deleted from the list (so size should decrease from 5 to 4)
        assertEquals(4, testList.size());
        
        // Ensure searching for it again returns null
        assertNull(Message.deleteByHash(testList, "00:2:WHERE TIME."));
    }
    @Test
public void testPrintMessageContainsRequiredFields() {

    String report = testList.get(1).printMessage();

    assertTrue(report.contains("MESSAGE ID"));
    assertTrue(report.contains("RECIPIENT"));
    assertTrue(report.contains("HASH"));
    assertTrue(report.contains("STATUS"));
    assertTrue(report.contains("CONTENT"));
}
}
