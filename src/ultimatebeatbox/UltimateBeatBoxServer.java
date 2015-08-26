/**
 *
 * @author 
 * Rayed Bin Wahed - 12201114 
 * BRAC University 
 * CSE310 Final Project:
 * Client-Server Beat Box Application
 * 
 * Server Code
 */

package ultimatebeatbox;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UltimateBeatBoxServer {

    ArrayList<ObjectOutputStream> clientOutputStreams;
    
    public static void main(String[] args) {
        new UltimateBeatBoxServer().start();
    }
    
    public UltimateBeatBoxServer(){
        this.clientOutputStreams = new ArrayList<>();
    }

    private void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(7777);
            while (true){
                Socket clientSocket = serverSocket.accept();
                ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
                clientOutputStreams.add(oos);
                Thread thread = new Thread(new ClientHandler(clientSocket));
                thread.start();
                System.out.println("Got a connection");
            }
        } catch (IOException ex) {
            Logger.getLogger(UltimateBeatBoxServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void spread(Object o1, Object o2){
        Iterator it = clientOutputStreams.iterator();
        while (it.hasNext()){
            try {
                ObjectOutputStream outputStream = (ObjectOutputStream) it.next();
                outputStream.writeObject(o1);
                outputStream.writeObject(o2);
            } catch (IOException ex) {
                Logger.getLogger(UltimateBeatBoxServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private class ClientHandler implements Runnable {

        ObjectInputStream ois;
        Socket clientSocket;
        
        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
            try {
                ois = new ObjectInputStream(clientSocket.getInputStream());
            } catch (IOException ex) {
                Logger.getLogger(UltimateBeatBoxServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public void run() {
            Object o1;
            Object o2;
            try {
                while ((o1 = ois.readObject()) != null){
                    o2 = ois.readObject();
                    System.out.println("Read 2 objects");
                    spread(o1, o2);
                }
            } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(UltimateBeatBoxServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
