package com.pronin.chat.network;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TCPConnection {

    private final Socket socket;
    private final Thread connectionThread;
    private final BufferedReader messageIn;
    private final BufferedWriter messageOut;
    private final TCPConnectionListener eventListener;

    public TCPConnection(TCPConnectionListener eventListener, String ipAddress, int port) throws IOException {
        this(new Socket(ipAddress, port), eventListener);
    }

    public TCPConnection(Socket socket, TCPConnectionListener eventListener) throws IOException {
        this.eventListener = eventListener;
        this.socket = socket;
       //Читаем и пишем
        messageIn = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        messageOut = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        //Создаем поток соединения. Используем интерфейс TCPConnectionListener как мы будем работать со входящим соединением
        connectionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    eventListener.onConnectionReady(TCPConnection.this);
                    while (!connectionThread.isInterrupted()) {
                        eventListener.onReceiveString(TCPConnection.this, messageIn.readLine());
                    }
                } catch (IOException e) {
                    eventListener.onException(TCPConnection.this, e);
                } finally {
                    eventListener.onDisconnect(TCPConnection.this);
                }
            }
        });
        connectionThread.start();
    }

    //Синхронизованный метод отправки сообщения
    public synchronized void sendString(String value) {
        try {
            messageOut.write(value + "\r\n");
            messageOut.flush();
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
            disconnect();
        }
    }

    //Метод для прерывания соединения
    public synchronized void disconnect() {
        connectionThread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
        }
    }

    @Override
    public String toString() {
        return "TCP Connection: " + socket.getInetAddress() + ": " + socket.getPort();
    }
}
