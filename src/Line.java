import java.util.Objects;

/**
 * @author Liuhaifeng
 * @date 2021/8/28 - 20:30
 */
public class Line {
    private Point begin;
    private Point end;
    double[] ans = new double[0];

    @Override
    public String toString() {
        return "Line{" +
                "begin=" + begin +
                ", end=" + end +
                '}';
    }

    public Line(){}

    public Line(Point begin, Point end){
        this.begin = begin;
        this.end = end;
    }

    public double getLength(){
        Vector vec = new Vector(this);
        return vec.getNorm();
    }
    //两条线是否平行
    public boolean isParallel(Line line2) {
        Vector v1 = new Vector(this);
        Vector v2 = new Vector(line2);
        v1.unify();
        v2.unify();
        boolean result = v1.equals(v2);
        v2.reverse();
        boolean result2 = v1.equals(v2);
        return result || result2;
    }

    public boolean isCross(Line another, int type){
        boolean ans = false;
        double[] start1 = null;
        double[] end1 = null;
        double[] start2 = null;
        double[] end2 = null;
        switch (type){
            case 0:{
                start1 = new double[]{this.begin.getX(),this.begin.getY()};
                end1 = new double[]{this.end.getX(),this.end.getY()};
                start2 = new double[]{another.begin.getX(),another.begin.getY()};
                end2 = new double[]{another.end.getX(),another.end.getY()};
                break;
            }
            case 1:{
                start1 = new double[]{this.begin.getX(),this.begin.getZ()};
                end1 = new double[]{this.end.getX(),this.end.getZ()};
                start2 = new double[]{another.begin.getX(),another.begin.getZ()};
                end2 = new double[]{another.end.getX(),another.end.getZ()};
                break;
            }
            case 2:{
                start1 = new double[]{this.begin.getY(),this.begin.getZ()};
                end1 = new double[]{this.end.getY(),this.end.getZ()};
                start2 = new double[]{another.begin.getY(),another.begin.getZ()};
                end2 = new double[]{another.end.getY(),another.end.getZ()};
                break;
            }
        }
        ans = intersection(start1,end1,start2,end2);
        return ans;
    }

    private boolean intersection(double[] start1, double[] end1, double[] start2, double[] end2) {
        double x1 = start1[0], y1 = start1[1], x2 = end1[0], y2 = end1[1];
        double x3 = start2[0], y3 = start2[1], x4 = end2[0], y4 = end2[1];
        double d = cross(x1-x2,x4-x3,y1-y2,y4-y3);
        double p = cross(x4-x2,x4-x3,y4-y2,y4-y3);
        double q = cross(x1-x2,x4-x2,y1-y2,y4-y2);
        if (d != 0){
            double lam = p/d;
            double eta = q/d;
            if (!(0 <= lam && lam<=1 && 0 <= eta && eta <= 1 ))
                return false;
            else
                return true;
        }
        if (p != 0 || q != 0)
            return false;
        double t1 = Double.min(Double.max(y1,y2),Double.max(y3,y4));
        double t2 = Double.max(Double.min(y1,y2),Double.min(y3,y4));
        if (t1 >= t2)
            return true;
        return false;
    }
    // 叉积运算
    private double cross(double ux, double uy, double vx, double vy) {
        return ux * vy - vx * uy;
    }

    public Point getBegin() {
        return begin;
    }

    public void setBegin(Point begin) {
        this.begin = begin;
    }

    public Point getEnd() {
        return end;
    }

    public void setEnd(Point end) {
        this.end = end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Line line = (Line) o;
        return Objects.equals(begin, line.begin) && Objects.equals(end, line.end)
                || Objects.equals(end, line.begin) && Objects.equals(begin, line.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(begin, end) + Objects.hash(end, begin);
    }
}
