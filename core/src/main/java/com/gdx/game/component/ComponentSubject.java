package com.gdx.game.component;

import com.badlogic.gdx.utils.Array;

public class ComponentSubject {
    private final Array<ComponentObserver> observers;

    public ComponentSubject() {
        observers = new Array<>();
    }

    public void addObserver(ComponentObserver conversationObserver) {
        observers.add(conversationObserver);
    }

    public void removeAllObservers() {
        for(ComponentObserver observer: observers) {
            observers.removeValue(observer, true);
        }
    }

    protected void notify(final String value, ComponentObserver.ComponentEvent event) {
        for(ComponentObserver observer: observers) {
            observer.onNotify(value, event);
        }
    }
}
