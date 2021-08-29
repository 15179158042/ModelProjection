import java.util.List;

/**
 * @author Liuhaifeng
 * @date 2021/8/28 - 20:25
 */
public class Vector {
    private double x;
    private double y;
    private double z;

    public Vector(){}
    public Vector(double x,double y,double z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector cross(Vector another){
        double newX = this.y * another.z - this.z * another.y;
        double newY = this.z * another.x - this.x * another.z;
        double newZ = this.x * another.y - this.y * another.x;
        Vector newVector = new Vector(newX, newY, newZ);
        return newVector;
    }

    public double dot(Vector another){
        return this.x * another.x +this.y * another.y + this.z * another.z;
    }

    public void reverse(){
        this.x = -1 * this.x;
        this.y = -1 * this.y;
        this.z = -1 * this.z;
    }

    public void unify(){
        double norm = getNorm();
        x = x/norm;
        y = y/norm;
        z = z/norm;
    }

    public double getNorm(){
        return Math.sqrt(Math.pow(x,2)+Math.pow(y,2)+Math.pow(z,2));
    }

    public double getAngle(Vector another){
        double dot = this.dot(another);
        double arch = Math.acos(dot/(this.getNorm()*another.getNorm()));
        double angle = 180 * arch / Math.PI;
        return Math.floor(angle);
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
        return "Vector{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
