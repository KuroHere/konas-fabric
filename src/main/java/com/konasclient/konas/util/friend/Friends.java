package com.konasclient.konas.util.friend;

import java.util.concurrent.CopyOnWriteArrayList;

public class Friends {
    public static CopyOnWriteArrayList<Friend> friends = new CopyOnWriteArrayList<>();

    public static boolean addFriend(String name) {
        if(!friends.contains(new Friend(name))) {
            friends.add(new Friend(name));
            return true;
        }
        return false;
    }

    public static boolean addFriend(Friend friend) {
        if(!friends.contains(friend)) {
            friends.add(friend);
            return true;
        }
        return false;
    }

    public static void delFriend(String name) {
        friends.removeIf(friend -> friend.getName().equalsIgnoreCase(name));
    }

    public static boolean delFriend(Friend friend) {
        return friends.remove(friend);
    }

    public static void clear() {
        friends.clear();
    }

    public static boolean isFriend(String name) {
        if (name == null) return false;
        for (Friend friend : friends) {
            if (friend.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public static CopyOnWriteArrayList<Friend> getFriends() {
        return friends;
    }
}
