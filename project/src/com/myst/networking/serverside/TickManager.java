package com.myst.networking.serverside;

import com.myst.networking.Codes;
import com.myst.networking.EntityData;
import com.myst.networking.Message;
import com.myst.networking.Report;
import com.myst.networking.serverside.model.WorldModel;

import java.util.ArrayList;

/**
 * Manages the ticks and events that occur on them
 */
public class TickManager extends Thread{
    private final int TICKRATE = 60;
    private ServerSender[] senders = new ServerSender[8];
    private Object senderListKey = new Object();
    private WorldModel wm;
    private ItemGenerator itemGenerator;
    TickManager(WorldModel wm){
        this.wm = wm;
        itemGenerator = new ItemGenerator(wm);
    }
    int ticks = 60;


    /**
     * Runs a tick manager
     */
    @Override
    public void run(){
        while(true){
            ticks++;
            ArrayList<EntityData> worldData = wm.getWorldData(false);
            for (int i=0; i< senders.length; i++){
                if(senders[i] != null) {
                    senders[i].requestClientUpdate();
                    senders[i].addMessage(new Message(Codes.ENTITY_UPDATE, worldData));
                }
            }
            wm.update();
            if(wm.playersAlive == 1 && wm.players > 1){
                System.out.println("I break here");
                break;
            }

            itemGenerator.update();
            if(ticks >= 60){
                ticks = 0;
            }

            try {
                Thread.sleep((long) 1000 / (long) TICKRATE);
            } catch (InterruptedException e){
                Report.error("Tick Manager is not running correctly");
            }
        }
        for(int i=0; i < senders.length; i++){
            if(senders[i] != null) {
                senders[i].endGame();
                senders[i].end();
            }
        }
        System.out.println("tick manager is ending");
    }

    /**
     * Adds a server sender to the tick manager
     * @param sender The server sender
     */
    public void addSender(ServerSender sender){
        Boolean added = false;
        synchronized (senderListKey){
            for(int i=0; i<senders.length; i++){
                if (senders[i] == null){
                    senders[i] = sender;
                    senders[i].start();
                    added = true;
                    break;
                }
            }
        }
        if (!added){
            throw new IllegalStateException("no more space to add a server sender");
        }
    }

}
