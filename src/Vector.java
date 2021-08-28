import java.util.List;

/**
 * @author Liuhaifeng
 * @date 2021/8/28 - 20:25
 */
public class Vector {
    private float x;
    private float y;
    private float z;

    public Vector(){}
    public Vector(float x,float y,float z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    @Override
    public String toString() {
        return "Vector{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
