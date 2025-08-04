package me.tjsh.utils;

public class GroupServer{
    public String server;
    public int online;
    public boolean canJoin;
    public GroupServer(String server, int online, boolean canJoin) {
        this.server = server;
        this.online = online;
        this.canJoin = canJoin;
    }
}
