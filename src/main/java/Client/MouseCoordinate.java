package Client;

import java.io.Serializable;

public class MouseCoordinate implements Serializable {
    private final int x, y;

    public MouseCoordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

}
