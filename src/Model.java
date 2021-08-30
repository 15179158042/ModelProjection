
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Liuhaifeng
 * @date 2021/8/28 - 20:20
 */
public class Model {
    /**
     * 保存所有的三角形
     */
    private List<Triangle> triangleList = new ArrayList<>();

    /**
     * 中间数据结构
     */
    private Map<Line,List<Integer>> lineOfTriangleMap = new HashMap<>();

    /**
     * 零件边的集合
     */
    private List<Line> lineList = new ArrayList<>();

    /**
     * minX,maxX,minY,maxY,minZ,maxZ
     * midX,midY,midZ
     * scale
     */
    private Map<String,Double> minMaxValue = new HashMap<>();

    private static List<Matrix> matrixList = ViewPoint.matrixList;

    static {
        ViewPoint.initialViewPoints(12,6);
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public Model(String modelPath) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(modelPath));
        initialMinMaxValue(this.minMaxValue);
        String str;
        in.readLine();
        while ((str = in.readLine()) != null) {
            if (str.startsWith("endsolid"))
                break;
            Point a=null,b=null,c=null;
            Vector n=null;
            for (int index = 0; index < 7; index++) {
                switch (index){
                    case 0:{
                        String[] strings = str.split(" ");
                        double x = Double.parseDouble(strings[2]);
                        double y = Double.parseDouble(strings[3]);
                        double z = Double.parseDouble(strings[4]);
                        n = new Vector(x, y, z);
                        break;
                    }
                    case 2:{
                        String[] strings = str.split(" ");
                        double x = Double.parseDouble(strings[1]);
                        double y = Double.parseDouble(strings[2]);
                        double z = Double.parseDouble(strings[3]);
                        a = new Point(x, y, z);
                        break;
                    }
                    case 3:{
                        String[] strings = str.split(" ");
                        double x = Double.parseDouble(strings[1]);
                        double y = Double.parseDouble(strings[2]);
                        double z = Double.parseDouble(strings[3]);
                        b = new Point(x, y, z);
                        break;
                    }
                    case 4:{
                        String[] strings = str.split(" ");
                        double x = Double.parseDouble(strings[1]);
                        double y = Double.parseDouble(strings[2]);
                        double z = Double.parseDouble(strings[3]);
                        c = new Point(x, y, z);
                        break;
                    }
                    default:
                        break;
                }
                if(index != 6) {
                    str = in.readLine();
                }
            }
            Triangle tempTriangle = new Triangle(a, b, c, n);
            for (Point temp : new Point[]{a,b,c}){
                updateMinMaxValue("x", temp.getX(), this.minMaxValue);
                updateMinMaxValue("y", temp.getY(), this.minMaxValue);
                updateMinMaxValue("z", temp.getZ(), this.minMaxValue);
            }
            triangleList.add(tempTriangle);
            Line ab = new Line(a,b);
            Line bc = new Line(b,c);
            Line ca = new Line(c,a);
            int indexOfTriangle = triangleList.size() - 1;
            List<Integer> integerList = lineOfTriangleMap.getOrDefault(ab,new ArrayList<>());
            integerList.add(indexOfTriangle);
            lineOfTriangleMap.put(ab,integerList);
            integerList = lineOfTriangleMap.getOrDefault(bc,new ArrayList<>());
            integerList.add(indexOfTriangle);
            lineOfTriangleMap.put(bc,integerList);
            integerList = lineOfTriangleMap.getOrDefault(ca,new ArrayList<>());
            integerList.add(indexOfTriangle);
            lineOfTriangleMap.put(ca,integerList);
        }
        minMaxValue.put("midX",(minMaxValue.get("minX")+minMaxValue.get("maxX"))/2);
        minMaxValue.put("midY",(minMaxValue.get("minY")+minMaxValue.get("maxY"))/2);
        minMaxValue.put("midZ",(minMaxValue.get("minZ")+minMaxValue.get("maxZ"))/2);
        minMaxValue.put("scale",Double.max(minMaxValue.get("maxX")-minMaxValue.get("minX")
                        ,Double.max(minMaxValue.get("maxY")-minMaxValue.get("minY")
                        ,minMaxValue.get("maxZ")-minMaxValue.get("minZ"))));
        for (Line tempLine : lineOfTriangleMap.keySet()){
            List<Integer> triangleIndexList = lineOfTriangleMap.get(tempLine);
            Triangle leftTriangle = triangleList.get(triangleIndexList.get(0));
            Triangle rightTriangle = triangleList.get(triangleIndexList.get(1));
            Vector leftNormal = leftTriangle.getN();
            Vector rightNormal = rightTriangle.getN();
            double angle = leftNormal.getAngle(rightNormal);
            if (angle > 30)
                lineList.add(tempLine);
        }
        unify();
    }

    public Mat getPicture(int index){
        Matrix matrix = matrixList.get(index);
        int[][] pictureData = getPictureData(matrix);
        int[] data = new int[256 * 256];
        int index2 = 0;
        for (int i = 0;i<256;i++){
            for(int j=0;j<256;j++){
                data[index2++] = pictureData[i][j];
            }
        }
        Mat picture = new Mat(256,256, CvType.CV_32S);
        picture.put(0,0,data);
        Imgcodecs.imwrite("D:\\Desktop\\Picture1.jpg",picture);
        return picture;
    }

    public int[][] getPictureData(Matrix viewMatrix){
        int[][] pictureData = new int[256][256];
        for (int i = 0 ;i < 256; i++){
            for(int j = 0; j < 256; j++){
                pictureData[i][j] = 255;
            }
        }
        Map<String,Double> transformedMinMaxValue = new HashMap<>();
        initialMinMaxValue(transformedMinMaxValue);
        List<Triangle> transformedTriangleList = new ArrayList<>();
        for (Triangle triangle : triangleList){
            Point a = viewMatrix.transform(triangle.getA());
            Point b = viewMatrix.transform(triangle.getB());
            Point c = viewMatrix.transform(triangle.getC());
            transformedTriangleList.add(new Triangle(a,b,c,triangle.getN()));
            for (Point temp : new Point[]{a,b,c}){
                updateMinMaxValue("x", temp.getX(), transformedMinMaxValue);
                updateMinMaxValue("y", temp.getY(), transformedMinMaxValue);
                updateMinMaxValue("z", temp.getZ(), transformedMinMaxValue);
            }
        }
        double scale = Double.max(transformedMinMaxValue.get("maxX") - transformedMinMaxValue.get("minX")
                        , Double.max(transformedMinMaxValue.get("maxY") - transformedMinMaxValue.get("minY")
                        ,transformedMinMaxValue.get("maxZ") - transformedMinMaxValue.get("minZ")));
        double xScale = (transformedMinMaxValue.get("maxX") - transformedMinMaxValue.get("minX"))/scale;
        double yScale = (transformedMinMaxValue.get("maxY") - transformedMinMaxValue.get("minY"))/scale;
        double zScale = (transformedMinMaxValue.get("maxZ") - transformedMinMaxValue.get("minZ"))/scale;
        transformedMinMaxValue.put("scale", scale);
        transformedMinMaxValue.put("xScale", xScale);
        transformedMinMaxValue.put("yScale", yScale);
        transformedMinMaxValue.put("zScale", zScale);
        for (Triangle triangle : transformedTriangleList){
            printToPicture(triangle, pictureData,transformedMinMaxValue);
        }

        int count = 0;
        for (Line line : lineList){
            Point begin = viewMatrix.transform(line.getBegin());
            Point end = viewMatrix.transform(line.getEnd());
            Line transformedLine = new Line(begin, end);
            printToPicture(transformedLine, pictureData, transformedMinMaxValue);
            System.out.println(count++);
        }

        return pictureData;
    }

    private void printToPicture(Triangle triangle,int[][] pictureData,Map<String,Double> minMaxValue){
        Double minX = Double.min(triangle.getA().getX(),Double.min(triangle.getB().getX(),triangle.getC().getX()));
        Double maxX = Double.max(triangle.getA().getX(),Double.max(triangle.getB().getX(),triangle.getC().getX()));
        Double minY = Double.min(triangle.getA().getY(),Double.min(triangle.getB().getY(),triangle.getC().getY()));
        Double maxY = Double.max(triangle.getA().getY(),Double.max(triangle.getB().getY(),triangle.getC().getY()));
        Triangle triangleInScreen = new Triangle();
        int index = 0;
        for (Point point : new Point[]{triangle.getA(),triangle.getB(), triangle.getC()}){
            double x = 235 * (1 - minMaxValue.get("xScale")) / 2 + 10 + 235 * (point.getX() - minMaxValue.get("minX"))/minMaxValue.get("scale");
            double y = 235 * (1 - minMaxValue.get("yScale")) / 2 + 10 + 235 * (point.getY() - minMaxValue.get("minY"))/minMaxValue.get("scale");
            int z = (int)(10 + 235 * (point.getZ() - minMaxValue.get("minZ"))/(minMaxValue.get("maxZ")-minMaxValue.get("minZ")));
            switch (index){
                case 0: {
                    triangleInScreen.setA(new Point(x,y,z));
                    index++;
                    break;
                }
                case 1: {
                    triangleInScreen.setB(new Point(x,y,z));
                    index++;
                    break;
                }
                case 2:{
                    triangleInScreen.setC(new Point(x,y,z));
                    index++;
                    break;
                }
                default:
                    break;
            }
        }
        int xMinIndex = (int)(235 * (1 - minMaxValue.get("xScale")) / 2 + 10 + 235 * (minX - minMaxValue.get("minX"))/minMaxValue.get("scale"));
        int xMaxIndex = (int)(235 * (1 - minMaxValue.get("xScale")) / 2 + 10 + 235 * (maxX - minMaxValue.get("minX"))/minMaxValue.get("scale"));
        int yMinIndex = (int)(235 * (1 - minMaxValue.get("yScale")) / 2 + 10 + 235 * (minY - minMaxValue.get("minY"))/minMaxValue.get("scale"));
        int yMaxIndex = (int)(235 * (1 - minMaxValue.get("yScale")) / 2 + 10 + 235 * (maxY - minMaxValue.get("minY"))/minMaxValue.get("scale"));
        for (int i = xMinIndex; i <= xMaxIndex ; i++){
            for (int j = yMinIndex; j <= yMaxIndex ; j++){
                Point A = new Point(i + 0.5, j + 0.5, 0);
                Triangle triangle1 = new Triangle(triangleInScreen);
                if (isPointInTriangle(A,triangle1)){
                    pictureData[i][j] = Integer.min(getZValue(A,triangleInScreen),pictureData[i][j]);
                }
            }
        }
    }

    private void printToPicture(Line line, int[][] pictureData, Map<String,Double> minMaxValue) {
        Line lineInScreen = new Line();
        int index = 0;
        for (Point point : new Point[]{line.getBegin(), line.getEnd()}) {
            double x = (int)(235 * (1 - minMaxValue.get("xScale")) / 2 + 10 + 235 * (point.getX() - minMaxValue.get("minX")) / minMaxValue.get("scale"));
            double y = (int)(235 * (1 - minMaxValue.get("yScale")) / 2 + 10 + 235 * (point.getY() - minMaxValue.get("minY")) / minMaxValue.get("scale"));
            int z = (int) (10 + 235 * (point.getZ() - minMaxValue.get("minZ")) / (minMaxValue.get("maxZ") - minMaxValue.get("minZ")));
            switch (index) {
                case 0: {
                    lineInScreen.setBegin(new Point(x, y, z));
                    index++;
                    break;
                }
                case 1: {
                    lineInScreen.setEnd(new Point(x, y, z));
                    index++;
                    break;
                }
                default:
                    break;
            }
        }

        Point beginInScreen = lineInScreen.getBegin();
        Point endInScreen = lineInScreen.getEnd();

        if (lineInScreen.getBegin().getX() > lineInScreen.getEnd().getX()){
            beginInScreen = lineInScreen.getEnd();
            endInScreen = lineInScreen.getBegin();
        }else {
            beginInScreen = lineInScreen.getBegin();
            endInScreen = lineInScreen.getEnd();
        }

        double k = (endInScreen.getY() - beginInScreen.getY())/(endInScreen.getX() - beginInScreen.getX());
        if (Math.abs(k) <= 1){
            int xBegin = (int)beginInScreen.getX();
            int yBegin = (int)beginInScreen.getY();
            int zBegin = (int)beginInScreen.getZ();
            double Ratio = k;
            double zRatio = (endInScreen.getZ() - beginInScreen.getZ())/(endInScreen.getX() - beginInScreen.getX());
            for (int i = (int)beginInScreen.getX(); i <= (int)endInScreen.getX(); i++){
                int xSteps = i - xBegin;
                int yValue = yBegin + (int)Math.round(xSteps * Ratio);
                int zValue = zBegin + (int)Math.round(xSteps * zRatio);
                if(zValue <= pictureData[i][yValue] + 5){
                    pictureData[i][yValue] = 0;
                }
            }
        }else{
            if (lineInScreen.getBegin().getY() > lineInScreen.getEnd().getY()){
                beginInScreen = lineInScreen.getEnd();
                endInScreen = lineInScreen.getBegin();
            }else{
                beginInScreen = lineInScreen.getBegin();
                endInScreen = lineInScreen.getEnd();
            }

            int xBegin = (int)beginInScreen.getX();
            int yBegin = (int)beginInScreen.getY();
            int zBegin = (int)beginInScreen.getZ();
            double Ratio = (endInScreen.getX() - beginInScreen.getX())/(endInScreen.getY() - beginInScreen.getY());
            double zRatio = (endInScreen.getZ() - beginInScreen.getZ())/(endInScreen.getY() -beginInScreen.getY());
            for (int i = (int)beginInScreen.getY(); i <= (int)endInScreen.getY(); i++){
                int ySteps = i - yBegin;
                int xValue = xBegin + (int)Math.round(ySteps * Ratio);
                int zValue = zBegin + (int)Math.round(ySteps * zRatio);
                if(zValue <= pictureData[xValue][i] + 5){
                    pictureData[xValue][i] = 0;
                }
            }
        }










//        if (lineInScreen.getBegin().getX() > lineInScreen.getEnd().getX()){
//            Point temp = lineInScreen.getBegin();
//            lineInScreen.setBegin(lineInScreen.getEnd());
//            lineInScreen.setEnd(temp);
//        }
//
//        int xBegin = (int)lineInScreen.getBegin().getX();
//        int yBegin = (int)lineInScreen.getBegin().getY();
//        int zBegin = (int)lineInScreen.getBegin().getZ();
//        double Ratio = (lineInScreen.getEnd().getY() - lineInScreen.getBegin().getY())/(lineInScreen.getEnd().getX() - lineInScreen.getBegin().getX());
//        double zRatio = (lineInScreen.getEnd().getZ() - lineInScreen.getBegin().getZ())/(lineInScreen.getEnd().getX() - lineInScreen.getBegin().getX());
//
//        for (int i = (int)lineInScreen.getBegin().getX(); i <= (int)lineInScreen.getEnd().getX(); i++){
//            int xSteps = i - xBegin;
//            int yValue = yBegin + (int)Math.round(xSteps * Ratio);
//            int zValue = zBegin + (int)Math.round(xSteps * zRatio);
//            if(zValue <= pictureData[i][yValue] + 5){
//                pictureData[i][yValue] = 0;
//            }
//        }
//
//        if (lineInScreen.getBegin().getY() > lineInScreen.getEnd().getY()){
//            Point temp = lineInScreen.getBegin();
//            lineInScreen.setBegin(lineInScreen.getEnd());
//            lineInScreen.setEnd(temp);
//        }
//
//        xBegin = (int)lineInScreen.getBegin().getX();
//        yBegin = (int)lineInScreen.getBegin().getY();
//        zBegin = (int)lineInScreen.getBegin().getZ();
//        Ratio = (lineInScreen.getEnd().getY() - lineInScreen.getBegin().getY())/(lineInScreen.getEnd().getX() - lineInScreen.getBegin().getX());
//        zRatio = (lineInScreen.getEnd().getZ() - lineInScreen.getBegin().getZ())/(lineInScreen.getEnd().getY() - lineInScreen.getBegin().getY());
//
//
//        for (int i = (int)lineInScreen.getBegin().getY(); i <= (int)lineInScreen.getEnd().getY(); i++){
//            int ySteps = i - yBegin;
//            int xValue = xBegin + (int)Math.round(ySteps * Ratio);
//            int zValue = zBegin + (int)Math.round(ySteps * zRatio);
//            if(zValue <= pictureData[xValue][i] + 5){
//                pictureData[xValue][i] = 0;
//            }
//        }

    }

    private boolean isPointInTriangle(Point point, Triangle triangle){
        Point A = triangle.getA();
        Point B = triangle.getB();
        Point C = triangle.getC();
        Point P = point;
        A.setZ(0);
        B.setZ(0);
        C.setZ(0);
        P.setZ(0);
        Vector PA = A.minus(P);
        Vector PB = B.minus(P);
        Vector PC = C.minus(P);
        Vector t1 = PA.cross(PB);
        Vector t2 = PB.cross(PC);
        Vector t3 = PC.cross(PA);
        boolean ans = t1.dot(t2) >= 0 && t1.dot(t3) >= 0;
        return ans;
    }

    private int getZValue(Point point, Triangle triangle){
        Point A = triangle.getA();
        Point B = triangle.getB();
        Point C = triangle.getC();
        Vector AB = B.minus(A);
        Vector AC = C.minus(A);
        Vector n = AB.cross(AC);
        if (n.getZ() == 0)
            return (int)A.getZ();
        else {
            int ans = (int) (A.getZ() - (n.getX() * (point.getX() - A.getX()) + n.getY() * (point.getY() - A.getY())) / n.getZ());
            return ans;
        }
    }

    private void initialMinMaxValue(Map<String,Double> minMaxValue){
        minMaxValue.put("minX",Double.MAX_VALUE);
        minMaxValue.put("minY",Double.MAX_VALUE);
        minMaxValue.put("minZ",Double.MAX_VALUE);
        minMaxValue.put("maxX",Double.MIN_VALUE);
        minMaxValue.put("maxY",Double.MIN_VALUE);
        minMaxValue.put("maxZ",Double.MIN_VALUE);
    }

    private void updateMinMaxValue(String item, double value, Map<String,Double> minMaxValue){
        String min = null, max = null;
        if ("x".equals(item)) {
            min = "minX";
            max = "maxX";
        }else if ("y".equals(item)){
            min = "minY";
            max = "maxY";
        }else if ("z".equals(item)){
            min = "minZ";
            max = "maxZ";
        }else {
            System.out.println("item 输入错误，请输入 x,y,z");
            return;
        }
        double tempMinValue = minMaxValue.get(min);
        minMaxValue.put(min, tempMinValue > value ? value : tempMinValue);
        double tempMaxValue = minMaxValue.get(max);
        minMaxValue.put(max, tempMaxValue > value ? tempMaxValue : value);
    }

    private void unify(){
        double midX = minMaxValue.get("midX");
        double midY = minMaxValue.get("midY");
        double midZ = minMaxValue.get("midZ");
        double scale = minMaxValue.get("scale");
        for (Triangle triangle : triangleList){
            for(Point point : new Point[]{triangle.getA(), triangle.getB(), triangle.getC()}){
                point.setX(Util.remainPoint4((point.getX() - midX)/scale));
                point.setY(Util.remainPoint4((point.getY() - midY)/scale));
                point.setZ(Util.remainPoint4((point.getZ() - midZ)/scale));
            }
            triangle.getN().unify();
        }
        minMaxValue.put("minX",(minMaxValue.get("minX") - midX)/scale);
        minMaxValue.put("minY",(minMaxValue.get("minY") - midY)/scale);
        minMaxValue.put("minZ",(minMaxValue.get("minZ") - midZ)/scale);
        minMaxValue.put("maxX",(minMaxValue.get("maxX") - midX)/scale);
        minMaxValue.put("maxY",(minMaxValue.get("maxY") - midY)/scale);
        minMaxValue.put("maxZ",(minMaxValue.get("maxZ") - midZ)/scale);
    }

    public List<Triangle> getTriangleList() {
        return triangleList;
    }

    public void setTriangleList(List<Triangle> triangleList) {
        this.triangleList = triangleList;
    }

    public Map<Line, List<Integer>> getLineOfTriangleMap() {
        return lineOfTriangleMap;
    }

    public void setLineOfTriangleMap(Map<Line, List<Integer>> lineOfTriangleMap) {
        this.lineOfTriangleMap = lineOfTriangleMap;
    }

    public List<Line> getLineList() {
        return lineList;
    }

    public void setLineList(List<Line> lineList) {
        this.lineList = lineList;
    }

    public Map<String, Double> getMinMaxValue() {
        return minMaxValue;
    }

    public void setMinMaxValue(Map<String, Double> minMaxValue) {
        this.minMaxValue = minMaxValue;
    }
}
