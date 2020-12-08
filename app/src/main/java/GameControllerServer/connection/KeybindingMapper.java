package GameControllerServer.connection;

import co.m1ke.basic.events.listener.Listener;

import java.util.concurrent.ConcurrentMap;

import GameControllerServer.connection.bindings.Keybinding;

public class KeybindingMapper extends Listener {

    private ConcurrentMap<String, Keybinding> bindings;

    public KeybindingMapper(Connection connection) {
        super("Keybindings", connection.getManager());
    }

    @Override
    public void init() {
        this.registerSelf();
    }

}
