package com.pronin.chat.client;

import com.pronin.chat.network.TCPConnection;
import com.pronin.chat.network.TCPConnectionListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ClientWindow extends JFrame implements ActionListener, TCPConnectionListener {

    private static final String IP_ADDR = "127.0.0.1";
    private static final int port = 9085;
    private static final int WIGHT = 600;
    private static final int HEIGHT = 400;

    private TCPConnection connection;

    private ClientWindow() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIGHT, HEIGHT);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        log.setEditable(false);
        log.setLineWrap(true);
        add(log, BorderLayout.CENTER);
        fieldMessage.addActionListener(this);
        add(fieldNickName, BorderLayout.NORTH);
        add(fieldMessage, BorderLayout.SOUTH);
        setVisible(true);
        try {
            connection = new TCPConnection(this, IP_ADDR, port);
        } catch (IOException e) {
            printMessage("Connection exception: " + e);
        }
    }

    private final JTextArea log = new JTextArea();
    private final JTextField fieldNickName = new JTextField("Vova");
    private final JTextField fieldMessage = new JTextField();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientWindow();
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String message = fieldMessage.getText();
        if(message.equals("")) {
            return;
        }else {
            fieldMessage.setText(null);
            connection.sendString(fieldNickName.getText() + ": " + message);
        }
    }


    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        printMessage("Connection ready...");
    }

    @Override
    public void onReceiveString(TCPConnection tcpConnection, String value) {
        printMessage(value);
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        printMessage("Connection closed...");
    }

    @Override
    public void onException(TCPConnection tcpConnection, Exception e) {
        printMessage("Connection exception: " + e);
    }

    //метод логирования
    private synchronized void printMessage (String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(message + "\n");
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }
}
