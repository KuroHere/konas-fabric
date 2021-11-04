package com.konasclient.konas.util.friend;

public class Friend {

    private final String name;

    public Friend(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


    @Override
    public boolean equals(Object input) {
        if(!(input instanceof Friend)) return false;
        Friend friend = (Friend) input;
        return friend.name.equals(this.name);
    }

}
