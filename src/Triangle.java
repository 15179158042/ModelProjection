import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    //两条相连的线的三角形
    public Triangle(Line line1,Line line2){
        Map<Point,Integer> pointCountMap = new HashMap<>();
        Point A = line1.getBegin();
        Point B = line1.getEnd();
        Point C = line2.getEnd();
        Point D = line2.getBegin();
        for (Point point : new Point[]{A,B,C,D})
            pointCountMap.put(point,pointCountMap.getOrDefault(point,0)+1);
        Point begin = null;
        Point mid = null;
        Point end = null;
        if (pointCountMap.get(A) == 1) {
            begin = A;
            mid = B;
        }else{
            begin = B;
            mid = A;
        }
        if (pointCountMap.get(C) == 1)
            end = C;
        else
            end = D;
        this.a = begin;
        this.b = mid;
        this.c = end;
        this.n = null;
    }

    public Triangle(Triangle another){
        if (null != another.getA())
            a = new Point(another.getA());
        if (null != another.getB())
            b = new Point(another.getB());
        if (null != another.getC())
            c = new Point(another.getC());
        if (null != another.getN())
            n = new Vector(another.getN());
    }

    public Triangle(Point a,Point b,Point c,Vector n){
        this.a = a;
        this.b = b;
        this.c = c;
        this.n = n;
    }

    //海伦公式求面积
    public double getArea(){
        Line ab = new Line(a,b);
        Line bc = new Line(b,c);
        Line ca = new Line(c,a);
        double abLength = ab.getLength();
        double bcLength = bc.getLength();
        double caLength = ca.getLength();
        double tmp = (abLength + bcLength + caLength)/2;
        double ans = Math.sqrt(tmp * (tmp - abLength) * (tmp - bcLength) * (tmp - caLength));
        return Util.remainPoint4(ans);
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
