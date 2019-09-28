package com.pronin.chat.server;

import com.pronin.chat.network.TCPConnection;
import com.pronin.chat.network.TCPConnectionListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

//Имплементируем интерфей TCPConnectionListener и переопределяем все его методы
public class ChatServer implements TCPConnectionListener {

    public static void main(String[] args) {
        new ChatServer();
    }

    private final ArrayList<TCPConnection> connections = new ArrayList<>();

    private ChatServer() {
        System.out.println("Server running... ");
        //создаем сервер сокет, который слушает порт 8090
        try (ServerSocket serverSocket = new ServerSocket(9085)) {
            while (true) {
                try {
                    //В бесконечном цикле мы ждем новое соединение
                    new TCPConnection(serverSocket.accept(), this);
                }catch (IOException e){
                    System.out.println("TCPConnection exception: " + e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void onConnectionReady(TCPConnection tcpConnection) {
        connections.add(tcpConnection);
        sendToAllConnections("Client connected: " + tcpConnection);
    }

    @Override
    public synchronized void onReceiveString(TCPConnection tcpConnection, String value) {
        sendToAllConnections(value);
    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        connections.remove(tcpConnection);
        sendToAllConnections("Client disconnected: " + tcpConnection);
    }

    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception e) {
        System.out.println("TCPConnection exception: " + e);
    }

    //Метод, для отправки сообщений всем соединениям
    private void sendToAllConnections (String value) {
        System.out.println(value);
        for (int i = 0; i <connections.size() ; i++) {
            connections.get(i).sendString(value);
        }
    }
}
