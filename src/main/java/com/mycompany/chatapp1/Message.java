package com.mycompany.chatapp1;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class Message {
    
    // encapsulating variables
    private String messageId;
    private int messageNumber;
    private String recipientNumber;
    private String content;
    private String messageHash;
    private String status;

    private static final String JSON_FILE = "messages.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    //sending a message
    public Message(String messageId, int messageNumber, String recipientNumber, String content, String messageHash, String status) {
        this.messageId = messageId;
        this.messageNumber = messageNumber;
        this.recipientNumber = recipientNumber;
        this.content = content;
        this.messageHash = messageHash;
        this.status = status;
    }

    // Validation for Message ID (Should be 10 digits)
    public boolean checkMessageID() {
        return messageId != null && messageId.matches("\\d{10}");
    }

    // Validation for Recipient Cell
    public String checkRecipientCell() {
        if (recipientNumber.length() > 10 && recipientNumber.startsWith("+")) {
            return "Recipient number is valid.";
        } else {
            return "Invalid recipient number. Must contain an international code (+) and be longer than 10 characters.";
        }
    }

    // Creates the Message Hash
    public String createMessageHash() {
        String firstTwoId = messageId.substring(0, 2);
        String[] words = content.trim().split("\\s+");
        String firstWord = words.length > 0 ? words[0] : "";
        String lastWord = words.length > 0 ? words[words.length - 1] : "";
        return (firstTwoId + ":" + messageNumber + ":" + firstWord + " " + lastWord).toUpperCase();
    }

    // Returns a summary line
    public String sentMessages() {
        return "Msg #" + messageNumber + " to " + recipientNumber + " [" + status + "]";
    }

    // Returns a detailed print out
    public String printMessage() {
        return "-----------------------------------\n" +
               "MESSAGE ID:     " + messageId + "\n" +
               "MESSAGE NO:     " + messageNumber + "\n" +
               "RECIPIENT:      " + recipientNumber + "\n" +
               "HASH:           " + messageHash + "\n" +
               "STATUS:         " + status + "\n" +
               "CONTENT:        " + content + "\n" +
               "-----------------------------------";
    }

    // Static helper for list size
    public static int returnTotalMessages(List<Message> list) {
        return list.size();
    }

    // Static helper for JSON storage
    public static void storeMessage(List<Message> list) {
        try (Writer writer = new FileWriter(JSON_FILE)) {
            gson.toJson(list, writer);
        } catch (IOException e) {
            System.out.println("Error saving to JSON: " + e.getMessage());
        }
    }

    // Getters
    public String getMessageId() { return messageId; }
    public int getMessageNumber() { return messageNumber; }
    public String getRecipientNumber() { return recipientNumber; }
    public String getContent() { return content; }
    public String getMessageHash() { return messageHash; }
    public String getStatus() { return status; }

    // Operations for Part 3 Unit Tests and Menu Option 4
    public static String getLongestMessage(List<Message> list) {
        String longest = "";
        for (Message m : list) {
            if (m.getContent() != null && m.getContent().length() > longest.length()) {
                longest = m.getContent();
            }
        }
        return longest;
    }

    public static String searchByMessageId(List<Message> list, String messageId) {
        for (Message m : list) {
            if (m.getMessageId() != null && m.getMessageId().equals(messageId)) {
                return m.getContent();
            }
        }
        return "Message not found.";
    }

    public static List<String> searchByRecipient(List<Message> list, String recipient) {
        List<String> results = new ArrayList<>();
        for (Message m : list) {
            if (m.getRecipientNumber() != null && m.getRecipientNumber().equals(recipient)) {
                if ("SENT".equalsIgnoreCase(m.getStatus()) || "STORED".equalsIgnoreCase(m.getStatus())) {
                    results.add(m.getContent());
                }
            }
        }
        return results;
    }

    public static Message deleteByHash(List<Message> list, String hash) {
        for (int i = 0; i < list.size(); i++) {
            Message m = list.get(i);
            if (m.getMessageHash() != null && m.getMessageHash().equalsIgnoreCase(hash)) {
                list.remove(i);
                storeMessage(list);
                return m;
            }
        }
        return null;
    }
}
