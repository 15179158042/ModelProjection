import javax.swing.text.View;
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

    public Model(String modelPath) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(modelPath));
        initialMinMaxValue();
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
                updateMinMaxValue("x",temp.getX());
                updateMinMaxValue("y",temp.getY());
                updateMinMaxValue("z",temp.getZ());
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

    public int[][] getPictureData(Matrix viewMatrix){
        int[][] pictureData = new int[256][256];
        for (int i = 0 ;i < 256; i++){
            for(int j = 0; j < 256; j++){
                pictureData[i][j] = 255;
            }
        }
        for (Triangle triangle : triangleList){
            Point a = viewMatrix.transform(triangle.getA());
            Point b = viewMatrix.transform(triangle.getB());
            Point c = viewMatrix.transform(triangle.getC());
        }

        return pictureData;
    }

    private void initialMinMaxValue(){
        minMaxValue.put("minX",Double.MAX_VALUE);
        minMaxValue.put("minY",Double.MAX_VALUE);
        minMaxValue.put("minZ",Double.MAX_VALUE);
        minMaxValue.put("maxX",Double.MIN_VALUE);
        minMaxValue.put("maxY",Double.MIN_VALUE);
        minMaxValue.put("maxZ",Double.MIN_VALUE);
    }

    private void updateMinMaxValue(String item,double value){
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
                point.setX((point.getX() - midX)/scale);
                point.setY((point.getY() - midY)/scale);
                point.setZ((point.getZ() - midZ)/scale);
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
