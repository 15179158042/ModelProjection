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
    private List<Triangle> triangleList = new ArrayList<>();
    Map<Line,List<Integer>> lineOfTriangleMap = new HashMap<>();
    private List<Line> lineList;
    private String modelName;


    public Model(String modelPath) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(modelPath));
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
                        float x = Float.parseFloat(strings[2]);
                        float y = Float.parseFloat(strings[3]);
                        float z = Float.parseFloat(strings[4]);
                        n = new Vector(x, y, z);
                        break;
                    }
                    case 2:{
                        String[] strings = str.split(" ");
                        float x = Float.parseFloat(strings[1]);
                        float y = Float.parseFloat(strings[2]);
                        float z = Float.parseFloat(strings[3]);
                        a = new Point(x, y, z);
                        break;
                    }
                    case 3:{
                        String[] strings = str.split(" ");
                        float x = Float.parseFloat(strings[1]);
                        float y = Float.parseFloat(strings[2]);
                        float z = Float.parseFloat(strings[3]);
                        b = new Point(x, y, z);
                        break;
                    }
                    case 4:{
                        String[] strings = str.split(" ");
                        float x = Float.parseFloat(strings[1]);
                        float y = Float.parseFloat(strings[2]);
                        float z = Float.parseFloat(strings[3]);
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
        for (Line tempLine : lineOfTriangleMap.keySet()){
            List<Integer> triangleIndexList = lineOfTriangleMap.get(tempLine);
            Triangle leftTriangle = triangleList.get(triangleIndexList.get(0));
            Triangle rightTriangle = triangleList.get(triangleIndexList.get(1));
            Vector leftNormal = leftTriangle.getN();
            Vector rightNormal = rightTriangle.getN();

        }
    }


}
