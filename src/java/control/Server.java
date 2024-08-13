package control;

import moose.Memo;
import moose.Moose;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;
import tool.Constants;
import tool.Constants.STR;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public class Server {
    private final TaggedLogger conLog = Logger.tag(getClass().getSimpleName());

    private static Server instance; // Singelton

    public static final int PORT = 8000; // always the same

    private ServerSocket serverSocket;
    private Socket openSocket;
    private PrintWriter outPW;
    private BufferedReader inBR;
    private final ExecutorService executor;

    private Moose moose;

    private PropertyChangeSupport support;

    //----------------------------------------------------------------------------------------
    /**
     * Get the instance
     * @return single instance
     */
    public static Server get() {
        if (instance == null) {
            instance = new Server();
        }
        return instance;
    }

    /**
     * Constructor
     */
    private Server() {
        // Init executerService for running threads
        executor = Executors.newCachedThreadPool();
        support = new PropertyChangeSupport(this);
    }

    /**
     * Set the Moose
     * @param moose Moose
     */
    public void setMoose(Moose moose) {
        this.moose = moose;
    }

    //----------------------------------------------------------------------------------------

    //-- Runnable for waiting for incoming connections
    private class ConnWaitRunnable implements Runnable {
        @Override
        public void run() {
            try {
                conLog.debug("Opening socket...");
                if (serverSocket == null) {
                    conLog.debug("Socket was null");
                    serverSocket = new ServerSocket(PORT);
                }
                conLog.debug("Accepting connections...");
                openSocket = serverSocket.accept();

                // Create streams
                inBR = new BufferedReader(new InputStreamReader(openSocket.getInputStream()));
                outPW = new PrintWriter(
                        new BufferedWriter(new OutputStreamWriter(openSocket.getOutputStream())),
                        true);
                conLog.trace("Ready! Listening to incoming messages...");
                // Start receiving
                executor.execute(new InRunnable());

            } catch (IOException e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        }
    }

    //-- Runnable for sending messages to Moose
    private class OutRunnable implements Runnable {
        private final Memo message;

        public OutRunnable(Memo msg) {
            this.message = msg;
        }

        @Override
        public void run() {
            if (message != null && outPW != null) {
                outPW.println(message);
                outPW.flush();
            }
        }
    }

    //-- Runnable for receiving messages from Moose
    private class InRunnable implements Runnable {
        @Override
        public void run() {
            while (!Thread.interrupted() && inBR != null) {
                try {
                    String message = inBR.readLine();
                    conLog.info("Message: {}", message);
                    if (message != null) {
                        Memo memo = Memo.valueOf(message);

                        switch (memo.getAction()) {
                            case STR.CLICK, STR.SCROLL, Constants.STR.ZOOM,
                                 STR.GRAB, Constants.STR.REL, Constants.STR.PAN, STR.FLING -> {
//                                moose.processMooseEvent(memo);
                                support.firePropertyChange(STR.MOOSE, null, memo);
                            }

                            case Constants.STR.CONNECTION -> {
                                if (memo.getMode().equals(STR.KEEP_ALIVE)) {
                                    // Send back the message (as confirmation)
                                    send(memo);
                                }
                            }
                        }
                    } else {
                        conLog.trace("Moose Disconnected");
                        start();
                        break;
                    }
                } catch (IOException e) {
                    conLog.warn("Error in reading from Moose");
                    start();
                }
            }

            conLog.trace("inBR: {}", inBR);
        }
    }

    //----------------------------------------------------------------------------------------
    /**
     * Start the server
     */
    public void start() {
        try {
            executor.execute(new ConnWaitRunnable());
        } catch (RejectedExecutionException exp) {
            conLog.warn("Connection task rejected.");
        }
    }

    /**
     * Shut down the server
     */
    public void shutDown() {
        try {
            // Send end message to the Moose
            send(new Memo(STR.CONNECTION, STR.END, ""));

            // Close the socket, etc.
            if (serverSocket != null && openSocket != null) {
                conLog.trace("Closing the socket...");
                serverSocket.close();
                openSocket.close();
            }
            conLog.trace("Shutting down the executer...");
            if (executor != null) executor.shutdownNow();
        } catch (IOException e) {
            conLog.trace("Couldn't close the socket!");
            e.printStackTrace();
        }
    }

    /**
     * Send a Memo to the Moose
     * Called from outside
     *
     * @param msg Memo message
     */
    public void send(Memo msg) {
        executor.execute(new OutRunnable(msg));
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        support.removePropertyChangeListener(pcl);
    }
}
