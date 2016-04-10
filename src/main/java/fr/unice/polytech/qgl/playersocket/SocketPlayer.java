package fr.unice.polytech.qgl.playersocket;


import eu.ace_design.island.bot.IExplorerRaid;


import java.util.Queue;
import java.util.concurrent.CountDownLatch;

import java.util.concurrent.SynchronousQueue;

import com.corundumstudio.socketio.listener.*;
import com.corundumstudio.socketio.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * PolyTech Nice / SI3 / Module POO-Java
 * Annee 2015 - IslandWorking - Lab 3
 * Package fr.unice.polytech.qgl.playersocket
 *
 * @author Flavian Jacquot
 * @version 09/04/2016
 * @since 1.8.0_60
 */
public class SocketPlayer implements IExplorerRaid{
    private static final Logger LOGGER = LogManager.getLogger(SocketPlayer.class);

    private SynchronousQueue<String> incoming;
    private SynchronousQueue<String> outcomming;
    private SocketIOClient socketClient;

    private SocketIOServer server;
    private boolean _connected;


    public SocketPlayer()
    {


    }

    private String reception()
    {

        try {
            return incoming.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void envoi(String toSend)
    {


        outcomming.offer(toSend);


    }

    @Override
    public void initialize(String s) {
        incoming = new SynchronousQueue<>();
        outcomming = new SynchronousQueue<>();
        Configuration config = new Configuration();
        config.setHostname("localhost");
        config.setPort(15141);
        server = new SocketIOServer(config);

        server.addConnectListener(new ConnectListener() {
            @Override
            public void onConnect(SocketIOClient socketIOClient) {
                if(socketClient==null)
                    socketClient = socketIOClient;
                LOGGER.error("Client connectd");


            }
        });

        server.addEventListener("action",String.class, new DataListener<String>() {
            @Override
            public void onData(SocketIOClient socketIOClient, String s, AckRequest ackRequest) throws Exception {
                incoming.put(s);

                ackRequest.sendAckData(outcomming.take());

            }
        });

        server.startAsync();
        LOGGER.error("Server started, waiting for client ...");

        while(socketClient==null)
        {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        LOGGER.error("Client Co, sending init ...");

        socketClient.sendEvent("init",s);

        LOGGER.error(" init  OK...");
    }

    @Override
    public String takeDecision() {
        LOGGER.error("Asking for descision");

        return reception();
    }

    @Override
    public void acknowledgeResults(String s) {
        LOGGER.error("Ak of"+s);

        envoi(s);
    }

    @Override
    protected void finalize() throws Throwable {
        server.stop();
        super.finalize();
    }
}
