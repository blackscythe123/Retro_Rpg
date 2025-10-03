package utils;

import gameobjects.GameObject;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Generic list for managing game objects
 */
public class GameObjectList<T extends GameObject> implements Iterable<T> {
    private CopyOnWriteArrayList<T> objects;

    public GameObjectList() {
        objects = new CopyOnWriteArrayList<>();
    }

    public void add(T object) {
        objects.add(object);
    }

    public void remove(T object) {
        objects.remove(object);
    }

    public void clear() {
        objects.clear();
    }

    public int size() {
        return objects.size();
    }

    public T get(int index) {
        return objects.get(index);
    }

    public boolean isEmpty() {
        return objects.isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        return objects.iterator();
    }

    /**
     * Updates all objects in the list
     */
    public void updateAll() {
        for (T object : objects) {
            object.update();
        }
    }

    /**
     * Draws all objects in the list
     */
    public void drawAll(java.awt.Graphics g) {
        for (T object : objects) {
            object.draw(g);
        }
    }
}