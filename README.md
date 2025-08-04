# ChatFlow - A Secure Java Chat Application

## Overview

ChatFlow is a chat application written in **Java 23**, using **Swing** for the graphical user interface (GUI). It allows multiple clients to connect to a central server using **Java Sockets**. The communication between the client and server is **encrypted using RSA encryption**, ensuring secure message transmission.

## Features

- **Client-Server Model:** Clients connect to the server using an IP address and port.
- **Username System:** Users must enter a username that is not being used to join the chat.
- **RSA Encryption:** Messages exchanged between client and server are encrypted for security.
- **Message History:** Upon connection, a client automatically receives all previous messages.
- **Shared Models and Encryption:** The models and encryption logic are shared between the client and server to maintain consistency.
- **MVC Architecture:** The client follows the **Model-View-Controller (MVC) pattern** for better organization and separation of concerns.
- **Multi-Threading:** Both the client and server handle sending and receiving messages in separate threads.
- **Server-Side Logging:** The server logs each operation and exception.
- **Built-in Bot:** A server bot provides predefined commands to interact with users.

## Application Structure
### Shared Code
Some classes, especially the models, are shared between client and server. That reduce the possibility of making mistakes, while actively apply the DRY principle (Don't Repeat Yourself!). Apart from the models, classes used for encryption, serialization and in general communication, are included there.

### Client-Side

#### **Models**

1. **ChatModel** - Stores all messages sent in the chat.
2. **MessageModel** - Defines a message with:
   - `message`: The message text.
   - `sender`: A **UserModel** instance representing the sender.
   - `date`: The timestamp of the message.
3. **UserModel** - Represents a user with a username.

#### **Views**

1. **ServerConnectionView** - Allows users to enter the server IP and port.

![image](https://github.com/user-attachments/assets/10c951dc-6472-4686-be72-2bfc5f02cc04)

2. **LoginView** - Allows users to enter a unique username.

![image](https://github.com/user-attachments/assets/2e259c1d-bfea-4c33-8dcd-965a1f331dd4)

3. **ChatView** - Displays all messages and provides an input field for sending new ones.

![image](https://github.com/user-attachments/assets/735626ec-99cf-4f9f-ba0a-f21c28e18eaf)


#### **Controllers**

1. **ServerConnectionController** - Manages the server connection process.
2. **LoginController** - Handles user authentication (unique username requirement).
3. **ChatController** - Manages chat interactions, message encryption, and UI updates.

#### **Threading & Communication**

- **Sending and receiving messages** are handled in **separate threads**.
- Messages are serialized and **encrypted before transmission**.
- Incoming messages are decrypted, deserialized, and displayed in the UI.
- Errors and exceptions are displayed via **popup alerts**. 

### Server-Side

#### **Main Components**

1. **ServerMain** - Controls the **ServerSocket** and listens for incoming client connections.
2. **ClientHandler** - Manages communication with a single connected client. Each client has a dedicated `ClientHandler` instance.
3. **ServerBot** - Provides automated responses to user commands.

#### **Threading & Communication**

- Each client has two separate threads on the server:
  - One for receiving messages.
  - One for sending messages.
- Messages are serialized, encrypted, and transmitted securely.

### **Bot Commands**

The **ServerBot** responds to the following commands:

```
/date - Get the current date and time
/random - Generate a random number (1-100)
/whoami - Find out your username
/users - Get a list of connected users
/coin - Flip a coin (heads or tails)
/size - Get the chat size
/help - Show the help message
```

## How to Run
Download ChatFlow_client.jar & ChatFlow_server.jar from the release section!

### **Running the Server**

1. Run `java -jar ChatFlow_server.jar`.
2. The server will start listening for client connections.
3. Logs will be generated for each operation.

### **Running the Client**

1. Run `java -jar ChatFlow_client.jar`.
2. Enter the **server IP address** and **port**.
3. Choose a **unique username**.
4. Start chatting!

## Security

- **RSA Encryption:** All messages are encrypted before transmission.
- **Serialized Object Encryption:** Objects are **serialized and encrypted** before being sent.
- **Unique Usernames:** The same user can only be actively used by one client simultaneously.


## License

ChatFlow is an **open-source project**, created as an educational project!

