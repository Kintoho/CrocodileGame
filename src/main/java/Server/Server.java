package Server;

import Network.TCPConnection;
import Network.TCPConnectionListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class Server implements TCPConnectionListener {

    public static void main(String[] args){
        new Server();
    }

    private final ArrayList<TCPConnection> connections = new ArrayList<>();

    private Server(){
        System.out.println("Server running...");
        try(ServerSocket serverSocket = new ServerSocket(8888)) {
            while (true){
                try{
                    new TCPConnection(this, serverSocket.accept());
                } catch (IOException e){
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
        sendToAllConnection("Client connected " + tcpConnection);
    }

    @Override
    public synchronized void onReceiveString(TCPConnection tcpConnection, String value) {
        sendToAllConnection(value);
    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        connections.remove(tcpConnection);
        sendToAllConnection("Client disconnected " + tcpConnection);
    }

    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception e) {
        System.out.println("TCPConnection exception: " + e);
    }

    @Override
    public synchronized void onPaintXY(TCPConnection tcpConnection, String coordinate) {
        paintToAllConnection(coordinate);
    }

    private void paintToAllConnection(String str){
        final int cnt = connections.size();
        for(int i = 0; i < cnt; i++){
            connections.get(i).sendCoordinate(str);
        }
    }

    private void sendToAllConnection(String value) {
        System.out.println(value);
        final int cnt = connections.size();
        for(int i = 0; i < cnt; i++){
            connections.get(i).sendString(value);
        }
    }
}
