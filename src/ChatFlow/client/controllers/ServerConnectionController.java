package ChatFlow.client.controllers;


import ChatFlow.client.views.FatalExceptionView;
import ChatFlow.client.views.ServerConnectionView;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ChatFlow.client.controllers.MainController.log;


public class ServerConnectionController {

    private final ServerConnectionView view;
    private Socket serverSocket;
    private final ExecutorService viewExecutorService;
    private ObjectOutputStream serverOOS;
    private ObjectInputStream serverOIS;

    public ServerConnectionController() {
        this.view = new ServerConnectionView();

        this.view.addConnectButtonListener(e -> handleConnectButton());


        viewExecutorService = Executors.newSingleThreadExecutor();

        viewExecutorService.submit(() -> {
            this.view.setVisible(true);
        });

        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                String message = "Exception on client side, regarding threads: " + e.getMessage();
                fatalException(message);
            }
        }

    }

    private void handleConnectButton() {
        try {
            String ipAddress = view.getIpAddress();
            int port = Integer.parseInt(view.getPort());
            serverSocket = new Socket(InetAddress.getByName(ipAddress), port);
            serverOOS = new ObjectOutputStream(serverSocket.getOutputStream());
            serverOIS = new ObjectInputStream(serverSocket.getInputStream());
            closeView();
            synchronized (this) {
                notify();
            }
        } catch (UnknownHostException e) {
            log("Unknown host: " + e.getMessage());
            view.showErrorMessage(e.getMessage());
        } catch (IOException e) {
            log("IO exception while connecting to server: " + e.getMessage());
            view.showErrorMessage(e.getMessage());
        } catch (NumberFormatException e) {
            log("Invalid port number: " + e.getMessage());
            view.showErrorMessage("Invalid Port Number");
        }
    }

    private void closeView() {
        viewExecutorService.shutdownNow();
        view.setVisible(false);
        view.dispose();
    }

    public Socket getServerSocket() {
        return serverSocket;
    }

    private void closeApp() {
        System.exit(0);
    }

    private void fatalException(String message) {
        log(message);
        closeView();
        FatalExceptionView.showErrorDialog(null, message);
        closeApp();
    }

    public ObjectOutputStream getServerOOS() {
        return serverOOS;
    }

    public ObjectInputStream getServerOIS() {
        return serverOIS;
    }
}
