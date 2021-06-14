package Network;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TCPConnection {

    private final Socket socket;
    private final Thread rxThread;
    private final TCPConnectionListener eventListener;
    private final BufferedReader in;
    private final BufferedWriter out;
    private final BufferedWriter outXY;
    private final BufferedReader inXY;


    public TCPConnection(TCPConnectionListener eventListener, String ipAddr, int port) throws IOException{
        this(eventListener, new Socket(ipAddr, port));
    }

    public TCPConnection(TCPConnectionListener eventListener, Socket socket) throws IOException {

        this.socket = socket;
        this.eventListener = eventListener;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        inXY = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        outXY = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        rxThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    eventListener.onConnectionReady(TCPConnection.this);
                    while ((!rxThread.isInterrupted())) {
                        eventListener.onReceiveString(TCPConnection.this, in.readLine());
                        eventListener.onPaintXY(TCPConnection.this,  inXY.readLine());
                    }
                } catch (IOException e) {
                    eventListener.onException(TCPConnection.this, e);
                } finally {
                    eventListener.onDisconnect(TCPConnection.this);
                }
            }
        });
        rxThread.start();
    }

    //выводит сообщение из потока
    public synchronized void sendString(String value){
        try {
            out.write(value + "\r\n");
            out.flush();
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
            disconnect();
        }
    }

    //выводит координаты из потока
    public synchronized void sendCoordinate(String str){
        try {
            outXY.write(str);
            outXY.flush();
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
            disconnect();
        }
    }

    public synchronized void disconnect(){
        rxThread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
        }
    }

    @Override
    public String toString(){
        return "TCPConnection" + socket.getInetAddress() + ": " + socket.getPort();
    }
}


