import java.util.ArrayList;
import java.util.List;

/**
 * @author Liuhaifeng
 * @date 2021/8/29 - 13:53
 */
public class ViewPoint {
    private Point viewPoint;
    private Vector u;
    private Vector v;
    private Vector n;

    public static List<ViewPoint> viewPoints = new ArrayList<>();
    public static List<Matrix> matrixList = new ArrayList<>();
    public static void initialViewPoints(int xNumber,int yNumber){
        int yAngle = 0, xAngle = 0;
        for(yAngle = -90; yAngle <= 90; yAngle += 180/yNumber ){
            if (yAngle != -90 && yAngle != 90) {
                for (xAngle = 0; xAngle < 360; xAngle += 360 / xNumber) {
                    addViewPoint(xAngle, yAngle);
                }
            }else{
                xAngle = 0;
                addViewPoint(xAngle, yAngle);
            }
        }
        for (ViewPoint viewPoint : viewPoints){
            Matrix M = new Matrix(4,4);
            M.setData(0,0,viewPoint.u.getX());
            M.setData(0,1,viewPoint.u.getY());
            M.setData(0,2,viewPoint.u.getZ());
            M.setData(1,0,viewPoint.v.getX());
            M.setData(1,1,viewPoint.v.getY());
            M.setData(1,2,viewPoint.v.getZ());
            M.setData(2,0,viewPoint.n.getX());
            M.setData(2,1,viewPoint.n.getY());
            M.setData(2,2,viewPoint.n.getZ());
            Vector P = new Vector(viewPoint.viewPoint.getX()
                    ,viewPoint.viewPoint.getY(),viewPoint.viewPoint.getZ());
            M.setData(0,3,-1 * viewPoint.u.dot(P));
            M.setData(1,3,-1 * viewPoint.v.dot(P));
            M.setData(2,3,-1 * viewPoint.n.dot(P));
            M.setData(3,3,1);
            matrixList.add(M);
        }
    }
    private static void addViewPoint(int xAngle, int yAngle){
        ViewPoint temp = new ViewPoint();
        temp.viewPoint = new Point(Math.cos(yAngle * Math.PI / 180) * Math.cos(xAngle * Math.PI /180)
                , Math.cos(yAngle * Math.PI / 180) * Math.sin(xAngle * Math.PI / 180)
                , (Math.sin(yAngle * Math.PI / 180)));
        Vector n = new Vector(temp.viewPoint.getX() ,temp.viewPoint.getY(), temp.viewPoint.getZ());
        n.unify();
        temp.n = n;
        Vector u = new Vector();
        if (n.getY() != 0){
            u.setX(1);
            u.setY( -1.0 * n.getX() / n.getY());
            u.setZ(0);
        }else{
            u.setX(0);
            u.setY(1);
            u.setZ(0);
        }
        Vector crossVec = n.cross(u);
        if (crossVec.getZ() < 0)
            u.reverse();
        u.unify();
        temp.u = u;
        temp.v = n.cross(u);
        viewPoints.add(temp);
    }

    public ViewPoint(){
    }

    @Override
    public String toString() {
        return "ViewPoint{" +
                "viewPoint=" + viewPoint +
                ", u=" + u +
                ", v=" + v +
                ", n=" + n +
                '}';
    }

    public Point getViewPoint() {
        return viewPoint;
    }

    public void setViewPoint(Point viewPoint) {
        this.viewPoint = viewPoint;
    }

    public Vector getU() {
        return u;
    }

    public void setU(Vector u) {
        this.u = u;
    }

    public Vector getV() {
        return v;
    }

    public void setV(Vector v) {
        this.v = v;
    }

    public Vector getN() {
        return n;
    }

    public void setN(Vector n) {
        this.n = n;
    }

    public static List<ViewPoint> getViewPoints() {
        return viewPoints;
    }

    public static void setViewPoints(List<ViewPoint> viewPoints) {
        ViewPoint.viewPoints = viewPoints;
    }
}
