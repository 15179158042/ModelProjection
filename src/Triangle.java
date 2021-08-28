import java.util.ArrayList;
import java.util.List;

/**
 * @author Liuhaifeng
 * @date 2021/8/28 - 20:23
 */
public class Triangle {
    //点
    private Point a;
    private Point b;
    private Point c;
    //法向量
    private Vector n;


    private List<Triangle> neighborTriangles = new ArrayList<>();

    public Triangle(){}

    public Triangle(Point a,Point b,Point c,Vector n){
        this.a = a;
        this.b = b;
        this.c = c;
        this.n = n;
    }

    public Point getA() {
        return a;
    }

    public void setA(Point a) {
        this.a = a;
    }

    public Point getB() {
        return b;
    }

    public void setB(Point b) {
        this.b = b;
    }

    public Point getC() {
        return c;
    }

    public void setC(Point c) {
        this.c = c;
    }

    public Vector getN() {
        return n;
    }

    public void setN(Vector n) {
        this.n = n;
    }

    @Override
    public String toString() {
        return "Triangle{" +
                "a=" + a +
                ", b=" + b +
                ", c=" + c +
                ", n=" + n +
                ", neighborTriangles=" + neighborTriangles +
                '}';
    }
}
