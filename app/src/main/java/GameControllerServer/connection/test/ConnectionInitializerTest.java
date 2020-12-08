package GameControllerServer.connection.test;

import co.m1ke.basic.events.EventManager;

import GameControllerServer.connection.Connection;

public class ConnectionInitializerTest {

    public static void main(String[] args) {
        new Connection(true, new EventManager(false));
    }

}