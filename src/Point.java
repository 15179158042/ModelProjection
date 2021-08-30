import java.util.Objects;

/**
 * @author Liuhaifeng
 * @date 2021/8/28 - 20:20
 */
public class Point {
    private double x;
    private double y;
    private double z;

    public Point(){}
    public Point(double x,double y,double z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point(Point another){
        x = another.getX();
        y = another.getY();
        z = another.getZ();
    }

    public Vector minus(Point another){
        return new Vector(this.x - another.x,this.y - another.y,this.z - another.z);
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    @Override
    public String toString() {
        return "Point{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return Double.compare(point.x, x) == 0 && Double.compare(point.y, y) == 0 && Double.compare(point.z, z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
}
