package Client;

import Network.TCPConnection;
import Network.TCPConnectionListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.IOException;

public class ClientWindow extends JFrame implements ActionListener, TCPConnectionListener, MouseMotionListener {

    private static final String IP_ADDR = "127.0.0.1";
    private static final int PORT = 8888;
    private static final int WIDTH = 600;
    private static final int HEIGHT = 400;

    private TCPConnection connection;
    private TCPConnection connectionPainter;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private String json;


    public static void main(String[] args){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientWindow();
            }
        });
    }

    private final JTextArea textArea = new JTextArea();
    private final JTextField fieldNickname = new JTextField("Введите ник");
    private final JTextField fieldInput = new JTextField();
    //private int x = 1, y = 1;


    private ClientWindow(){

        super("Window");
        getContentPane().setLayout(new GridLayout());
        JSplitPane splitPane = new JSplitPane();
        getContentPane().add(splitPane);
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(200);
        JPanel topPanel = new JPanel();
        splitPane.setTopComponent(topPanel);
        JPanel bottomPanel = new JPanel();
        splitPane.setBottomComponent(bottomPanel);

        topPanel.addMouseMotionListener(this);

        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane();
        bottomPanel.add(scrollPane);
        scrollPane.setViewportView(textArea);
        JPanel inputPanel = new JPanel();
        bottomPanel.add(inputPanel);

        inputPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));
        inputPanel.add(fieldNickname);
        fieldNickname.setMaximumSize(new Dimension(200, 75));
        inputPanel.add(fieldInput);
        fieldInput.addActionListener(this);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        setVisible(true);


        try {
            connection = new TCPConnection(this, IP_ADDR, PORT);
            connectionPainter = new TCPConnection(this, IP_ADDR, PORT);
        } catch (IOException e) {
            printMsg("Connection exception: " + e);
        }
    }

    // Фиксирует нажатие кнопки и отправляет сообщение на сервер
    @Override
    public void actionPerformed(ActionEvent e) {
        String msg = fieldInput.getText();
        if (msg.equals("")) return;
        fieldInput.setText(null);
        connection.sendString(fieldNickname.getText() + ": " + msg);
    }

    //Фиксирует нажатие кнопки мыши и отправляет на сервер
    @Override
    public void mouseDragged(MouseEvent e){
        MouseCoordinate coordinate = new MouseCoordinate(e.getX(), e.getY());
        json = GSON.toJson(coordinate);
        connectionPainter.sendCoordinate(json);
        //Graphics graphics = getGraphics();
        //graphics.fillOval(e.getX(), e.getY(), 4 , 4);
    }

    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        printMsg("Connection ready");
    }

    //Фиксирует событие получение сообщения и передаёт методу, чтобы вывести
    @Override
    public void onReceiveString(TCPConnection tcpConnection, String value) {
        printMsg(value);
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        printMsg("Connection close");
    }

    @Override
    public void onException(TCPConnection tcpConnection, Exception e) {
        printMsg("Connection exception: " + e);
    }

    @Override
    public void onPaintXY(TCPConnection tcpConnection, String coordinate) {
        MouseCoordinate mouseCoordinate = GSON.fromJson(json, MouseCoordinate.class);
        paint(mouseCoordinate.getX(), mouseCoordinate.getY());
    }

    //выводит сообщение в текстовое поле
    private synchronized void printMsg (String msg){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                textArea.append(msg + "\n");
                textArea.setCaretPosition(textArea.getDocument().getLength());
            }
        });
    }

    public synchronized void paint(int x, int y){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Graphics g = getGraphics();
                g.fillOval(x,y, 4, 4);
            }
        });
    }

    public void mouseMoved(MouseEvent e){
        //
    }
}