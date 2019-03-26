package com.myst.networking.clientside;

import com.myst.audio.Audio;
import com.myst.networking.Codes;
import com.myst.networking.EntityData;
import com.myst.networking.Message;
import com.myst.networking.serverside.PlayAudioData;
import com.myst.world.entities.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ClientReceiver extends Thread {
    private ClientSender toServer;
    private ObjectInputStream fromServer;
    private ConcurrentHashMap<String,ConcurrentHashMap<Integer, Entity>> entities;
    private ConcurrentHashMap<String,ConcurrentHashMap<Integer, EntityData>> toRender;
    private String clientID;

//    convert entity[] into hash map potentially currently the arrays indexes corresponds to the entity's ID
    public ClientReceiver(ObjectInputStream fromServer,
                          ClientSender toServer,
                          ConcurrentHashMap<String,ConcurrentHashMap<Integer, Entity>> entities,
                          ConcurrentHashMap<String,ConcurrentHashMap<Integer, EntityData>> toRender,
                          String clientID){
        this.fromServer = fromServer;
        this.toServer = toServer;
        this.entities = entities;
        this.clientID = clientID;
        this.toRender = toRender;
    }

    @Override
    public void run(){
        while(true){
            try {
                Message msg = (Message) fromServer.readObject();
                switch(msg.header){
                    case ENTITY_UPDATE:
                        readInEntities(msg.data);
                        break;
                    case UPDATE_SERVER:
                        sendEntities();
                        break;
                    case NO_AVAILABLE_SPACES:
                        System.out.println("no more spaces on the server");
                        System.exit(1);
                        break;
                    case PLAY_AUDIO:
                        playAudio((PlayAudioData) msg.data);
                    default:
                        break;
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void playAudio(PlayAudioData playAudioData) {
        Audio.getAudio().play(playAudioData.clipName, playAudioData.location);
    }

//    sends the entities positions to the server
    private void sendEntities(){
//        this needs refactoring
        ConcurrentHashMap<Integer, Entity> myEntities = this.entities.get(clientID);
        ArrayList<EntityData> toSend = new ArrayList<EntityData>();
        if(myEntities ==  null){
//            this is sloppy and needs tidying once debugging has finished
            return;
        }
        for(Integer i: myEntities.keySet()){
            if (myEntities.get(i) != null){
                EntityData data= myEntities.get(i).getData();
                if(myEntities.get(i).visibleToEnemy){
                    data.lightSource = true;
                } else{
                    data.lightSource = false;
                }
                toSend.add(data);
            }
        }
        Message msg = new Message(Codes.UPDATE_SERVER,toSend);
        toServer.addToQueue(msg);
//        this tells the Server sender to empty it's queue to the server
        toServer.sendQueue();
    }

//    this can be modified
    private void readInEntities(Object data) {
        ArrayList<EntityData> entityData = (ArrayList<EntityData>) data;
        for (int i = 0; i < entityData.size(); i++) {
            if (entityData.get(i) != null) {
                EntityData entity = entityData.get(i);
                if(!toRender.containsKey(entity.ownerID)){
                    toRender.put(entity.ownerID, new ConcurrentHashMap<Integer,EntityData>());
                }
                if(!entities.containsKey(entity.ownerID)){
                    entities.put(entity.ownerID, new ConcurrentHashMap<Integer,Entity>());
                }
                if(!entities.get(entity.ownerID).containsKey(entity.localID)){
                        toRender.get(entity.ownerID).put(entity.localID, entity);
                }
                else if (!entity.ownerID.equals(clientID)) {
                    entities.get(entity.ownerID).get(entity.localID).readInEntityData(entityData.get(i));
                }
            }
        }
    }
}
