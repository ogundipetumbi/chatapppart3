package com.mycompany.chatapp1;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Main {
    public static List<Message> sentMessages = new ArrayList<>();
    public static List<Message> disregardedMessagesList = new ArrayList<>();

    // The 5 required arrays (no hard-coding)
    public static String[] sentMessagesArray = new String[0];
    public static String[] disregardedMessagesArray = new String[0];
    public static String[] storedMessagesArray = new String[0];
    public static String[] messageHashArray = new String[0];
    public static String[] messageIdArray = new String[0];

    private static final Random random = new Random();
    private static final String JSON_FILE = "messages.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) {
        System.out.println("Welcome to QuickChat\n");
        loadMessagesFromJson();

        Scanner input = new Scanner(System.in);
        Login login = new Login();

        String username, password, phoneNumber;
        String registrationMessage;

        System.out.println("=== REGISTRATION ===");

        while (true) {
            System.out.print("Enter username: ");
            username = input.nextLine();
            System.out.print("Enter password: ");
            password = input.nextLine();
            System.out.print("Enter phone number: ");
            phoneNumber = input.nextLine();

            registrationMessage = login.registerUser(username, password, phoneNumber);
            System.out.println(registrationMessage);

            if (registrationMessage.equals("User registered successfully.")) {
                break;
            }
            System.out.println("\nPlease try again...\n");
        }

        System.out.println("\n=== LOGIN ===");
        boolean isLoggedIn = false;
        while (!isLoggedIn) {
            System.out.print("Enter username: ");
            String loginUsername = input.nextLine();
            System.out.print("Enter password: ");
            String loginPassword = input.nextLine();
            isLoggedIn = login.loginUser(loginUsername, loginPassword);
            System.out.println(login.returnLoginStatus(isLoggedIn));
        }

        boolean running = true;
        while (running) {
            System.out.println("\n=== QUICKCHAT MENU ===");
            System.out.println("1. Send messages");
            System.out.println("2. Show previously sent messages");
            System.out.println("3. Stored Messages");
            System.out.println("4. Quit");
            System.out.print("Choose an option: ");
            
            String choiceStr = input.nextLine();
            int choice = 0;
            try {
                choice = Integer.parseInt(choiceStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1:
                    System.out.print("How many messages do you wish to enter and send? ");
                    String numStr = input.nextLine();
                    int numMessages = 0;
                    try {
                        numMessages = Integer.parseInt(numStr);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid number.");
                        break;
                    }
                    
                    if (numMessages <= 0) {
                        System.out.println("Please enter a positive number.");
                        break;
                    }

                    for (int i = 1; i <= numMessages; i++) {
                        System.out.println("\n--- Message Draft " + i + " ---");
                        String messageId = generate10DigitId();
                        int currentMessageNum = Message.returnTotalMessages(sentMessages) + 1;
                        
                        // Recipient Input
                        String recipient = "";
                        while (true) {
                            System.out.print("Enter recipient cell number (e.g., +27...): ");
                            recipient = input.nextLine().trim();
                            // Create dummy message to use its validation logic
                            Message temp = new Message(messageId, currentMessageNum, recipient, "", "", "");
                            String validation = temp.checkRecipientCell();
                            if (validation.equals("Recipient number is valid.")) {
                                break;
                            } else {
                                System.out.println(validation);
                            }
                        }
                        
                        // Content Input
                        String content = "";
                        while (true) {
                            System.out.print("Enter message content (max 250 chars): ");
                            content = input.nextLine().trim();
                            if (content.length() > 0 && content.length() <= 250) {
                                break;
                            } else if (content.length() > 250) {
                                System.out.println("Message too long (" + content.length() + " chars).");
                            } else {
                                System.out.println("Message cannot be empty.");
                            }
                        }
                        
                        // Create Message object to generate hash and handle actions
                        Message msg = new Message(messageId, currentMessageNum, recipient, content, "", "DRAFT");
                        String hash = msg.createMessageHash();
                        
                        boolean drafted = true;
                        while(drafted) {
                            System.out.println("\nDraft Ready. Hash: " + hash);
                            System.out.println("1 - Send message");
                            System.out.println("0 - Disregard message");
                            System.out.println("2 - Store message to send later");
                            System.out.print("Choice: ");
                            String action = input.nextLine().trim();
                            
                            if (action.equals("1")) {
                                Message finalMsg = new Message(messageId, currentMessageNum, recipient, content, hash, "SENT");
                                sentMessages.add(finalMsg);
                                Message.storeMessage(sentMessages);
                                populateArrays();
                                System.out.println("Message sent successfully.");
                                drafted = false;
                            } else if (action.equals("2")) {
                                Message finalMsg = new Message(messageId, currentMessageNum, recipient, content, hash, "STORED");
                                sentMessages.add(finalMsg);
                                Message.storeMessage(sentMessages);
                                populateArrays();
                                System.out.println("Message stored successfully.");
                                drafted = false;
                            } else if (action.equals("0")) {
                                Message disregardedMsg = new Message(messageId, currentMessageNum, recipient, content, hash, "DISREGARDED");
                                disregardedMessagesList.add(disregardedMsg);
                                populateArrays();
                                System.out.println("Message disregarded.");
                                drafted = false;
                            } else {
                                System.out.println("Invalid choice.");
                            }
                        }
                    }
                    break;
                case 2:
                    if (sentMessages.isEmpty()) {
                        System.out.println("No messages sent or stored yet.");
                    } else {
                        System.out.println("\n=== SENT/STORED MESSAGES ===");
                        for (Message m : sentMessages) {
                            System.out.println(m.printMessage());
                        }
                        System.out.println("Total Messages: " + Message.returnTotalMessages(sentMessages));
                    }
                    break;
                case 3:
                    handleStoredMessagesMenu(input, login);
                    break;
                case 4:
                    System.out.println("Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
        input.close();
    }
    
    private static String generate10DigitId() {
        StringBuilder sb = new StringBuilder();
        sb.append(random.nextInt(9) + 1);
        for (int i = 0; i < 9; i++) sb.append(random.nextInt(10));
        return sb.toString();
    }

    private static void loadMessagesFromJson() {
        try (Reader reader = new FileReader(JSON_FILE)) {
            Type listType = new TypeToken<ArrayList<Message>>(){}.getType();
            List<Message> loaded = gson.fromJson(reader, listType);
            if (loaded != null) {
                sentMessages = loaded;
                System.out.println("Loaded " + sentMessages.size() + " messages from history.");
                populateArrays();
            }
        } catch (IOException e) { }
    }

    public static void populateArrays() {
        List<String> sentList = new ArrayList<>();
        List<String> disregardList = new ArrayList<>();
        List<String> storedList = new ArrayList<>();
        List<String> hashList = new ArrayList<>();
        List<String> idList = new ArrayList<>();

        for (Message m : sentMessages) {
            if ("SENT".equalsIgnoreCase(m.getStatus())) {
                sentList.add(m.getContent());
            } else if ("STORED".equalsIgnoreCase(m.getStatus())) {
                storedList.add(m.getContent());
            }
            if (m.getMessageHash() != null && !m.getMessageHash().isEmpty()) {
                hashList.add(m.getMessageHash());
            }
            if (m.getMessageId() != null && !m.getMessageId().isEmpty()) {
                idList.add(m.getMessageId());
            }
        }

        for (Message m : disregardedMessagesList) {
            disregardList.add(m.getContent());
            if (m.getMessageHash() != null && !m.getMessageHash().isEmpty()) {
                hashList.add(m.getMessageHash());
            }
            if (m.getMessageId() != null && !m.getMessageId().isEmpty()) {
                idList.add(m.getMessageId());
            }
        }

        sentMessagesArray = sentList.toArray(new String[0]);
        disregardedMessagesArray = disregardList.toArray(new String[0]);
        storedMessagesArray = storedList.toArray(new String[0]);
        messageHashArray = hashList.toArray(new String[0]);
        messageIdArray = idList.toArray(new String[0]);
    }

    private static void handleStoredMessagesMenu(Scanner input, Login login) {
        boolean back = false;
        while (!back) {
            System.out.println("\n=== STORED MESSAGES MENU ===");
            System.out.println("a. Display the sender and recipient of all stored messages");
            System.out.println("b. Display the longest stored message");
            System.out.println("c. Search for a message ID and display the corresponding recipient and message");
            System.out.println("d. Search for all the messages stored for a particular recipient");
            System.out.println("e. Delete a message using the message hash");
            System.out.println("f. Display a report that lists the full details of all the stored messages");
            System.out.println("g. Back to main menu");
            System.out.print("Choose an option: ");
            String option = input.nextLine().trim().toLowerCase();

            switch (option) {
                case "a":
                    displayStoredSendersRecipients(login);
                    break;
                case "b":
                    displayLongestStoredMessage();
                    break;
                case "c":
                    searchMessageById(input);
                    break;
                case "d":
                    searchMessagesByRecipient(input);
                    break;
                case "e":
                    deleteMessageByHash(input);
                    break;
                case "f":
                    displayStoredMessagesReport();
                    break;
                case "g":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void displayStoredSendersRecipients(Login login) {
        System.out.println("\n--- Stored Message Senders & Recipients ---");
        boolean found = false;
        String sender = (login.getCellNumber() != null) ? login.getCellNumber() : "Unknown (Me)";
        for (Message m : sentMessages) {
            if ("STORED".equalsIgnoreCase(m.getStatus())) {
                System.out.println("Sender: " + sender + " | Recipient: " + m.getRecipientNumber());
                found = true;
            }
        }
        if (!found) {
            System.out.println("No stored messages found.");
        }
    }

    private static void displayLongestStoredMessage() {
        System.out.println("\n--- Longest Stored Message ---");
        List<Message> storedOnly = new ArrayList<>();
        for (Message m : sentMessages) {
            if ("STORED".equalsIgnoreCase(m.getStatus())) {
                storedOnly.add(m);
            }
        }
        String longest = Message.getLongestMessage(storedOnly);
        if (longest.isEmpty()) {
            System.out.println("No stored messages found.");
        } else {
            System.out.println("Longest stored message: \"" + longest + "\"");
        }
    }

    private static void searchMessageById(Scanner input) {
        System.out.print("Enter Message ID to search: ");
        String searchId = input.nextLine().trim();
        System.out.println("\n--- Search Result ---");
        boolean found = false;
        for (Message m : sentMessages) {
            if (m.getMessageId().equals(searchId)) {
                System.out.println("Recipient: " + m.getRecipientNumber());
                System.out.println("Message:   \"" + m.getContent() + "\"");
                found = true;
                break;
            }
        }
        if (!found) {
            System.out.println("Message not found.");
        }
    }

    private static void searchMessagesByRecipient(Scanner input) {
        System.out.print("Enter Recipient Cell Number: ");
        String recipient = input.nextLine().trim();
        System.out.println("\n--- Messages stored for " + recipient + " ---");
        List<String> results = Message.searchByRecipient(sentMessages, recipient);
        if (results.isEmpty()) {
            System.out.println("No messages stored for this recipient.");
        } else {
            for (String content : results) {
                System.out.println("- \"" + content + "\"");
            }
        }
    }

    private static void deleteMessageByHash(Scanner input) {
        System.out.print("Enter Message Hash to delete: ");
        String hash = input.nextLine().trim();
        System.out.println("\n--- Deletion Result ---");
        Message deleted = Message.deleteByHash(sentMessages, hash);
        if (deleted != null) {
            populateArrays();
            System.out.println("Message successfully deleted!");
            System.out.println("Message: \"" + deleted.getContent() + "\"");
        } else {
            System.out.println("Message with hash \"" + hash + "\" not found.");
        }
    }

    private static void displayStoredMessagesReport() {
        System.out.println("\n=== STORED MESSAGES REPORT ===");
        boolean found = false;
        for (Message m : sentMessages) {
            if ("STORED".equalsIgnoreCase(m.getStatus())) {
                System.out.println(m.printMessage());
                found = true;
            }
        }
        if (!found) {
            System.out.println("No stored messages found.");
        }
    }
}
