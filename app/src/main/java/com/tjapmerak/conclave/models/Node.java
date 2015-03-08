package com.tjapmerak.conclave.models;

import java.io.Serializable;

/**
 * Created by bahrunnur on 3/8/15.
 */
public class Node implements Serializable {

    private String id;
    private String name;
    private String command;
    private int power;

    public Node(String id, String name, String command, int power) {
        this.id = id;
        this.name = name;
        this.command = command;
        this.power = power;
    }

    public String interpolateCommand() {
        String ret =  "python conclave.py"
                + " " + "--node=" + name
                + " " + "--id=" + id
                + " " + "--power=" + power;
        return ret;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
