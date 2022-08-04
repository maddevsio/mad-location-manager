package mad.location.manager.lib.enums;

import lombok.Getter;

public enum Direction {
    EAST(0),
    NORTH(1),
    UP(2);

    private final int code;

    public int getCode(){
        return code;
    }

    Direction(int code) {
        this.code = code;
    }
}
