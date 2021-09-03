
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

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

    /**
     * 获得ViewPoint中的所有的变换矩阵
     */
    private static List<Matrix> matrixList = ViewPoint.matrixList;

    static {
        ViewPoint.initialViewPoints(12,6);
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    /**
     * 无参构造
     */
    public Model(){}

    /**
     * 读取模型的所有点的数据，提取其中的边，并且归一化模型数据
     */
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
                        int length = strings.length;
                        double x = Double.parseDouble(strings[length-3]);
                        double y = Double.parseDouble(strings[length-2]);
                        double z = Double.parseDouble(strings[length-1]);
                        n = new Vector(x, y, z);
                        break;
                    }
                    case 2:{
                        String[] strings = str.split(" ");
                        int length = strings.length;
                        double x = Double.parseDouble(strings[length-3]);
                        double y = Double.parseDouble(strings[length-2]);
                        double z = Double.parseDouble(strings[length-1]);
                        a = new Point(x, y, z);
                        break;
                    }
                    case 3:{
                        String[] strings = str.split(" ");
                        int length = strings.length;
                        double x = Double.parseDouble(strings[length-3]);
                        double y = Double.parseDouble(strings[length-2]);
                        double z = Double.parseDouble(strings[length-1]);
                        b = new Point(x, y, z);
                        break;
                    }
                    case 4:{
                        String[] strings = str.split(" ");
                        int length = strings.length;
                        double x = Double.parseDouble(strings[length-3]);
                        double y = Double.parseDouble(strings[length-2]);
                        double z = Double.parseDouble(strings[length-1]);
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
            if (angle > 10)
                lineList.add(tempLine);
        }
        unify();
    }

    /**
     * 剖面后得到的模型
     * 0 -- x-y平面剖 z = 0；
     * 1 -- x-z平面剖 y = 0；
     * 2 -- y-z平面剖 x = 0；
     */
    public Model Split(int type){
        Model afterSplitModel = new Model();
        List<Triangle> afterSplitTriangleList = new ArrayList<>();
        List<Triangle> unSplitTriangleList = new ArrayList<>();
        List<Line> afterSplitLineList = new ArrayList<>();
        List<Line> unSplitLineList = new ArrayList<>();
        List<Line> extraLines = new ArrayList<>();
        for (Triangle triangle : triangleList) {
            int negativeCount = 0;
            switch (type) {
                case 0: {
                    for (Point point : new Point[]{triangle.getA(), triangle.getB(), triangle.getC()}) {
                        if (point.getZ() < 0) {
                            negativeCount++;
                        }
                    }
                    break;
                }
                case 1:{
                    for (Point point : new Point[]{triangle.getA(), triangle.getB(), triangle.getC()}) {
                        if (point.getY() < 0) {
                            negativeCount++;
                        }
                    }
                    break;
                }
                case 2:{
                    for (Point point : new Point[]{triangle.getA(), triangle.getB(), triangle.getC()}) {
                        if (point.getX() < 0) {
                            negativeCount++;
                        }
                    }
                    break;
                }
            }
            switch (negativeCount) {
                case 0: {
                    unSplitTriangleList.add(triangle);
                    break;
                }
                case 1:
                    splitIntoTwoOrMorePart(triangle, afterSplitTriangleList, unSplitTriangleList, extraLines, type);
                    break;
                case 2: {
                    splitIntoTwoOrMorePart(triangle, afterSplitTriangleList, unSplitTriangleList, extraLines, type);
                    break;
                }
                case 3: {
                    afterSplitTriangleList.add(triangle);
                    break;
                }
            }
        }
        for (Line line : lineList){
            int negativeCount = 0;
            switch (type) {
                case 0: {
                    for (Point point : new Point[]{line.getBegin(),line.getEnd()}) {
                        if (point.getZ() < 0) {
                            negativeCount++;
                        }
                    }
                    break;
                }
                case 1:{
                    for (Point point : new Point[]{line.getBegin(),line.getEnd()}) {
                        if (point.getY() < 0) {
                            negativeCount++;
                        }
                    }
                    break;
                }
                case 2:{
                    for (Point point : new Point[]{line.getBegin(),line.getEnd()}) {
                        if (point.getX() < 0) {
                            negativeCount++;
                        }
                    }
                    break;
                }
            }
            switch (negativeCount) {
                case 0: {
                    unSplitLineList.add(line);
                    break;
                }
                case 1:
                    splitIntoTwoPart(line, afterSplitLineList, unSplitLineList, type);
                    break;
                case 2: {
                    afterSplitLineList.add(line);
                    break;
                }
            }
        }
        Map<Point,List<Line>> pointMap = new HashMap<>();
        for (int index = 0; index < extraLines.size(); index++){
            Line tempLine = extraLines.get(index);
            afterSplitLineList.add(tempLine);
            for (Point tempPoint : new Point[]{tempLine.getBegin(), tempLine.getEnd()}) {
                List<Line> lineList = pointMap.getOrDefault(tempPoint, new ArrayList<>());
                lineList.add(tempLine);
                pointMap.put(tempPoint, lineList);
            }
        }

        while(!extraLines.isEmpty()){
            List<Line> oneCircleLines = new ArrayList<>();
            Line firstLine = extraLines.get(0);
            extraLines.remove(firstLine);
            Line tempLine = firstLine;
            Set<Point> haveVisitedPoint = new HashSet<>();
            while (true) {
                Point node = tempLine.getEnd();
                if (haveVisitedPoint.contains(node))
                    node = tempLine.getBegin();
                haveVisitedPoint.add(node);
                List<Line> nextLineList = pointMap.get(node);
                Line nextLine = null;
                for (Line line : nextLineList) {
                    if (!line.equals(tempLine))
                        nextLine = line;
                }
                if (nextLine.equals(firstLine)) {
                    oneCircleLines.add(tempLine);
                    break;
                }
                oneCircleLines.add(tempLine);
                tempLine = nextLine;
                extraLines.remove(nextLine);
            }

            List<Line> oneCircleMergedLines = new ArrayList<>();
            List<Point> oneCircleMergedPoints = new ArrayList<>();
            Line lastLine = oneCircleLines.get(0);
            for (int index = 1; index < oneCircleLines.size(); index++){
                Line thisLine = oneCircleLines.get(index);
                Map<Point,Integer> pointCountMap = new HashMap<>();
                Point A = lastLine.getBegin();
                Point B = lastLine.getEnd();
                Point C = thisLine.getBegin();
                Point D = thisLine.getEnd();
                for (Point point : new Point[]{A,B,C,D})
                    pointCountMap.put(point,pointCountMap.getOrDefault(point,0)+1);
                Point begin = null;
                Point end = null;
                Point mid = null;
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

                if (lastLine.isParallel(thisLine) || lastLine.getLength()/ thisLine.getLength() < 0.01 || lastLine.getLength()/ thisLine.getLength() > 100 ){
                    lastLine = new Line(begin,end);
                }else{
                    oneCircleMergedLines.add(lastLine);
                    oneCircleMergedPoints.add(lastLine.getBegin());
                    lastLine = new Line(mid,end);
                }
                if (index == oneCircleLines.size() - 1) {
                    oneCircleMergedLines.add(lastLine);
                    oneCircleMergedPoints.add(lastLine.getBegin());
                }
            }
//            System.out.println(oneCircleMergedLines.size());
//            for (Line line : oneCircleMergedLines)
//                System.out.println(line);
//
//            System.out.println(oneCircleMergedPoints.size());

            List<Triangle> newTriangleList = splitIntoTriangles(oneCircleMergedLines,oneCircleMergedPoints,type);

            System.out.println(newTriangleList.size());
            System.out.println("+++");



            afterSplitTriangleList.addAll(newTriangleList);
        }
        afterSplitModel.setTriangleList(afterSplitTriangleList);
        afterSplitModel.setLineList(afterSplitLineList);
        return afterSplitModel;
    }

    List<Triangle> splitIntoTriangles(List<Line> tempLineList,List<Point> pointList,int type){
        List<Triangle> triangleList = new ArrayList<>();
        for (Point point : pointList)
            System.out.println(point);
        System.out.println(tempLineList.size());
        if (tempLineList.size() < 3)
            return triangleList;
        if (tempLineList.size() == 3){
            triangleList.add(new Triangle(tempLineList.get(0),tempLineList.get(1)));
            return triangleList;
        }
        List<Integer> pointIndexOfNewLine = null;
        for (int i = 0; i < pointList.size(); i++){
            pointIndexOfNewLine = new ArrayList<>();
            Point firstPoint = pointList.get(i);
            pointIndexOfNewLine.add(i);
            int count = 0;
            int index = -1;
            for (int j = i + 2; count < pointList.size() - 3; count++,j++){
                if (j >= pointList.size())
                    j = j - pointList.size();
                Point secondPoint = pointList.get(j);
                Line newLine = new Line(firstPoint, secondPoint);
                boolean success = false;
                for (int k = 0;k < tempLineList.size();k++){
                    int lastI = (i == 0 ? tempLineList.size()-1 : i-1);
                    int lastJ = (j == 0 ? tempLineList.size()-1 : j-1);
                    if(!(k == i || k == lastI || k == j || k == lastJ)) {
                        Line exitLine = tempLineList.get(k);
                        if (newLine.isCross(exitLine, type)) {
                            break;
                        }else{
                            System.out.println(exitLine);
                            System.out.println(newLine);
                            System.out.println("=====");

                        }
                    }
                    if (k == tempLineList.size() - 1)
                        success = true;
                }
                if (success) {
                    boolean isOnLeftSide = false;
                    boolean isOnRightSide = false;
                    for (int m = 0;m < pointList.size();m++){
                        if (m == i || m == j)
                            continue;
                        Vector v1 = new Vector(pointList.get(i),pointList.get(j));
                        Vector v2 = new Vector(pointList.get(i),pointList.get(m));
                        Vector crossResult = v1.cross(v2);
                        switch (type){
                            case 0:{
                                if (crossResult.getZ() > 0)
                                    isOnLeftSide = true;
                                else
                                    isOnRightSide = true;
                                break;
                            }
                            case 1:{
                                if (crossResult.getY() > 0)
                                    isOnLeftSide = true;
                                else
                                    isOnRightSide = true;
                                break;
                            }
                            case 2:{
                                if (crossResult.getX() > 0)
                                    isOnLeftSide = true;
                                else
                                    isOnRightSide = true;
                                break;
                            }
                        }
                    }
                    if (isOnLeftSide && isOnRightSide)
                        index = j;
                }
            }
            if (index != -1){
                pointIndexOfNewLine.add(index);
                break;
            }
        }

        if (pointIndexOfNewLine.size() < 2)
            return triangleList;

        int begin = pointIndexOfNewLine.get(0) > pointIndexOfNewLine.get(1) ? pointIndexOfNewLine.get(1) :pointIndexOfNewLine.get(0);
        int end = pointIndexOfNewLine.get(0) > pointIndexOfNewLine.get(1) ? pointIndexOfNewLine.get(0) :pointIndexOfNewLine.get(1);

        System.out.println("begin"+begin);
        System.out.println("end"+end);

        List<Line> leftLineList = new ArrayList<>();
        List<Line> rightLineList = new ArrayList<>();
        List<Point> leftPointList = new ArrayList<>();
        List<Point> rightPointList = new ArrayList<>();

        for (int i = begin;i<end;i++) {
            leftLineList.add(tempLineList.get(i));
            leftPointList.add(pointList.get(i));
        }
        leftPointList.add(pointList.get(end));
        leftLineList.add(new Line(pointList.get(end),pointList.get(begin)));

        int count = 0;
        for (int i = end;count < pointList.size() - (end - begin) ;count++,i++){
            if (i == tempLineList.size())
                i = 0;
            rightLineList.add(tempLineList.get(i));
            rightPointList.add(pointList.get(i));
        }
        rightPointList.add(pointList.get(begin));
        rightLineList.add(new Line(pointList.get(begin),pointList.get(end)));

        triangleList.addAll(splitIntoTriangles(leftLineList,leftPointList,type));

        triangleList.addAll(splitIntoTriangles(rightLineList,rightPointList,type));
        return triangleList;
    }



    private void splitIntoTwoOrMorePart(Triangle triangle, List<Triangle> afterSplitTriangleList, List<Triangle> unSplitTriangleList,List<Line> extraLines,int type){
        Point A = triangle.getA();
        Point B = triangle.getB();
        Point C = triangle.getC();
        List<Point> pointList = new ArrayList<>();
        List<Point> newPointList = new ArrayList<>();
        pointList.add(A);
        pointList.add(B);
        pointList.add(C);
        Line[] lines = new Line[]{new Line(A,B), new Line(B,C) ,new Line(C,A)};
        Map<Point,Integer> pointCount = new HashMap<>();
        switch (type){
            case 0:{
                for (Line temp : lines){
                    Point begin = temp.getBegin();
                    Point end = temp.getEnd();
                    if (begin.getZ() > end.getZ()){
                        begin = temp.getEnd();
                        end = temp.getBegin();
                    }
                    if (begin.getZ() < 0 && end.getZ() >0){
                        double xRation = (end.getX() - begin.getX()) / (end.getZ() - begin.getZ());
                        double yRation = (end.getY() - begin.getY()) / (end.getZ() - begin.getZ());
                        Point newPoint = new Point(begin.getX() + xRation * (0 - begin.getZ())
                                        ,begin.getY() + yRation * (0 - begin.getZ())
                                        ,0);
                        newPointList.add(begin);
                        newPointList.add(newPoint);
                        newPointList.add(end);
                        pointCount.put(begin,pointCount.getOrDefault(begin,0)+1);
                        pointCount.put(end,pointCount.getOrDefault(end,0)+1);
                    }
                }
                break;
            }
            case 1:{
                for (Line temp : lines){
                    Point begin = temp.getBegin();
                    Point end = temp.getEnd();
                    if (begin.getY() > end.getY()){
                        begin = temp.getEnd();
                        end = temp.getBegin();
                    }
                    if (begin.getY() < 0 && end.getY() >0){
                        double xRation = (end.getX() - begin.getX()) / (end.getY() - begin.getY());
                        double zRation = (end.getZ() - begin.getZ()) / (end.getY() - begin.getY());
                        Point newPoint = new Point(begin.getX() + xRation * (0 - begin.getY())
                                ,0
                                ,begin.getZ() + zRation * (0 - begin.getY()));
                        newPointList.add(begin);
                        newPointList.add(newPoint);
                        newPointList.add(end);
                        pointCount.put(begin,pointCount.getOrDefault(begin,0)+1);
                        pointCount.put(end,pointCount.getOrDefault(end,0)+1);
                    }
                }
                break;
            }
            case 2:{
                for (Line temp : lines){
                    Point begin = temp.getBegin();
                    Point end = temp.getEnd();
                    if (begin.getX() > end.getX()){
                        begin = temp.getEnd();
                        end = temp.getBegin();
                    }
                    if (begin.getX() < 0 && end.getX() >0){
                        double yRation = (end.getY() - begin.getY()) / (end.getX() - begin.getX());
                        double zRation = (end.getZ() - begin.getZ()) / (end.getX() - begin.getX());
                        Point newPoint = new Point(0
                                ,begin.getY() + yRation * (0 - begin.getX())
                                ,begin.getZ() + zRation * (0 - begin.getX()));
                        newPointList.add(begin);
                        newPointList.add(newPoint);
                        newPointList.add(end);
                        pointCount.put(begin,pointCount.getOrDefault(begin,0)+1);
                        pointCount.put(end,pointCount.getOrDefault(end,0)+1);
                    }
                }
                break;
            }
        }
        if (newPointList.size() == 0) {
            afterSplitTriangleList.add(triangle);
            List<Point> zeroPoints = new ArrayList<>();
            switch (type){
                case 0:{
                    for (Point point : pointList){
                        if (point.getZ() == 0)
                            zeroPoints.add(point);
                    }
                    break;
                }
                case 1:{
                    for (Point point : pointList){
                        if (point.getY() == 0)
                            zeroPoints.add(point);
                    }
                    break;
                }
                case 2:{
                    for (Point point : pointList){
                        if (point.getX() == 0)
                            zeroPoints.add(point);
                    }
                    break;
                }
            }
            if (zeroPoints.size() == 2){
                extraLines.add(new Line(zeroPoints.get(0),zeroPoints.get(1)));
            }
        }else if (newPointList.size() == 3){
            for (Point point : pointList){
                if (pointCount.getOrDefault(point,0) == 0){
                    for (Point point1 : pointList){
                        if (point != point1){
                           Triangle newTriangle = new Triangle(point, point1, newPointList.get(1),null);
                           switch (type){
                               case 0:{
                                   if (point1.getZ() < 0){
                                       afterSplitTriangleList.add(newTriangle);
                                   }else {
                                       unSplitTriangleList.add(newTriangle);
                                   }
                                   break;
                               }
                               case 1:{
                                   if (point1.getY() < 0){
                                       afterSplitTriangleList.add(newTriangle);
                                   }else {
                                       unSplitTriangleList.add(newTriangle);
                                   }
                                   break;
                               }
                               case 2:{
                                   if (point1.getX() < 0){
                                       afterSplitTriangleList.add(newTriangle);
                                   }else {
                                       unSplitTriangleList.add(newTriangle);
                                   }
                                   break;
                               }
                           }
                        }
                    }
                }
            }
            List<Point> zeroPoints = new ArrayList<>();
            switch (type){
                case 0:{
                    for (Point point : pointList){
                        if (point.getZ() == 0)
                            zeroPoints.add(point);
                    }
                    break;
                }
                case 1:{
                    for (Point point : pointList){
                        if (point.getY() == 0)
                            zeroPoints.add(point);
                    }
                    break;
                }
                case 2:{
                    for (Point point : pointList){
                        if (point.getX() == 0)
                            zeroPoints.add(point);
                    }
                    break;
                }
            }
            if (zeroPoints.size() == 1){
                extraLines.add(new Line(zeroPoints.get(0), newPointList.get(1)));
            }
        }else {
            int flag = 0;
            for (Point point : pointList){
                if (pointCount.get(point) == 2){
                    Triangle newTriangle = new Triangle(point,newPointList.get(1),newPointList.get(4),null);
                    switch (type) {
                        case 0: {
                            if (point.getZ() < 0) {
                                afterSplitTriangleList.add(newTriangle);
                            } else {
                                unSplitTriangleList.add(newTriangle);
                            }
                            break;
                        }
                        case 1: {
                            if (point.getY() < 0) {
                                afterSplitTriangleList.add(newTriangle);
                            } else {
                                unSplitTriangleList.add(newTriangle);
                            }
                            break;
                        }
                        case 2: {
                            if (point.getX() < 0) {
                                afterSplitTriangleList.add(newTriangle);
                            } else {
                                unSplitTriangleList.add(newTriangle);
                            }
                            break;
                        }
                    }
                }else {
                    Triangle newTriangle = null;
                    if (flag == 0) {
                        for (Point point1 : pointList){
                            if (pointCount.get(point1) == 1 && point != point1){
                                newTriangle = new Triangle(point, point1, newPointList.get(1), null);
                            }
                        }
                        flag++;
                    } else {
                        for (Point point1 : new Point[]{newPointList.get(3),newPointList.get(5)}){
                            if (pointCount.get(point1) == 1){
                                newTriangle = new Triangle(point1, newPointList.get(1),newPointList.get(4), null);
                            }
                        }
                    }
                    switch (type) {
                        case 0: {
                            if (point.getZ() < 0) {
                                afterSplitTriangleList.add(newTriangle);
                            } else {
                                unSplitTriangleList.add(newTriangle);
                            }
                            break;
                        }
                        case 1: {
                            if (point.getY() < 0) {
                                afterSplitTriangleList.add(newTriangle);
                            } else {
                                unSplitTriangleList.add(newTriangle);
                            }
                            break;
                        }
                        case 2: {
                            if (point.getX() < 0) {
                                afterSplitTriangleList.add(newTriangle);
                            } else {
                                unSplitTriangleList.add(newTriangle);
                            }
                            break;
                        }
                    }
                }
            }
            extraLines.add(new Line(newPointList.get(1), newPointList.get(4)));
        }
    }

    private void splitIntoTwoPart(Line line,List<Line> afterSplitLineList,List<Line> unSplitLineList,int type){
        Point begin = line.getBegin();
        Point end = line.getEnd();
        Point midPoint = null;
        switch (type){
            case 0:{
                if (begin.getZ()==0 || end.getZ()==0)
                    afterSplitLineList.add(line);
                else{
                    double xRatio = (end.getX() - begin.getX()) / (end.getZ() - begin.getZ());
                    double yRatio = (end.getY() - begin.getY()) / (end.getZ() - begin.getZ());
                    midPoint = new Point(begin.getX() + xRatio * (0 - begin.getZ()), begin.getY() + yRatio * (0 - begin.getZ()), 0);
                }
                break;
            }
            case 1:{
                if (begin.getY()==0 || end.getY()==0)
                    afterSplitLineList.add(line);
                else{
                    double xRatio = (end.getX() - begin.getX()) / (end.getY() - begin.getY());
                    double zRatio = (end.getZ() - begin.getZ()) / (end.getY() - begin.getY());
                    midPoint = new Point(begin.getX() + xRatio * (0 - begin.getY()),0 , begin.getZ() + zRatio * (0 - begin.getY()));
                }
                break;
            }
            case 2:{
                if (begin.getX()==0 || end.getX()==0)
                    afterSplitLineList.add(line);
                else{
                    double yRatio = (end.getY() - begin.getY()) / (end.getX() - begin.getX());
                    double zRatio = (end.getZ() - begin.getZ()) / (end.getX() - begin.getX());
                    midPoint = new Point(0, begin.getY() + yRatio * (0 - begin.getX()) , begin.getZ() + zRatio * (0 - begin.getX()));
                }
                break;
            }
        }
        if (null == midPoint)
            return;
        else {
            for (Point point : new Point[]{begin, end}){
                Line tempLine = new Line(point, midPoint);
                switch (type){
                    case 0:{
                        if (point.getZ() < 0)
                            afterSplitLineList.add(tempLine);
                        else
                            unSplitLineList.add(tempLine);
                        break;
                    }
                    case 1:{
                        if (point.getY() < 0)
                            afterSplitLineList.add(tempLine);
                        else
                            unSplitLineList.add(tempLine);
                        break;
                    }
                    case 2:{
                        if (point.getX() < 0)
                            afterSplitLineList.add(tempLine);
                        else
                            unSplitLineList.add(tempLine);
                        break;
                    }
                }
            }
        }

    }

    /**
     * 获得某个投影角度的图片，并展示在桌面上
     */
    public void getOnePicture(int index){
        Matrix matrix = matrixList.get(index);
        int[][] pictureData = getPictureWithFrameData(matrix);
        int[] data = new int[512 * 512];
        int index2 = 0;
        for (int i = 0; i < 512; i++) {
            for (int j = 0; j < 512; j++) {
                data[index2++] = pictureData[i][j];
            }
        }
        Mat picture = new Mat(512, 512, CvType.CV_32S);
        picture.put(0, 0, data);
        Imgcodecs.imwrite("D:\\Desktop\\1222.jpg", picture);
    }

    /**
     * 获得所有的角度的图片
     */
    public void getPicture(String filePath){
        for (int index =0 ; index < 62; index++) {
            Matrix matrix = matrixList.get(index);
            int[][] pictureData = getPictureWithFrameData(matrix);
            int[] data = new int[512 * 512];
            int index2 = 0;
            for (int i = 0; i < 512; i++) {
                for (int j = 0; j < 512; j++) {
                    data[index2++] = pictureData[i][j];
                }
            }
            Mat picture = new Mat(512, 512, CvType.CV_32S);
            picture.put(0, 0, data);
            String path = filePath + "\\" + index + ".jpg";
            System.out.println("生成第"+index+"张图片");
            Imgcodecs.imwrite(path, picture);
        }
    }

    /**
     * 获得含有线框的图片数据
     */
    public int[][] getPictureWithoutFrameData(Matrix viewMatrix){
        int[][] pictureData = new int[512][512];
        Map<String,Double> transformedMinMaxValue = new HashMap<>();
        List<Triangle> transformedTriangleList = new ArrayList<>();
        initialPictureData(pictureData, transformedMinMaxValue, transformedTriangleList, viewMatrix);
        for (Triangle triangle : transformedTriangleList){
            printToPicture(triangle, pictureData,transformedMinMaxValue);
        }
        return pictureData;
    }

    /**
     * 获得没有线框的图片数据
     */
    public int[][] getPictureWithFrameData(Matrix viewMatrix){
        int[][] pictureData = new int[512][512];
        Map<String,Double> transformedMinMaxValue = new HashMap<>();
        List<Triangle> transformedTriangleList = new ArrayList<>();
        initialPictureData(pictureData, transformedMinMaxValue, transformedTriangleList, viewMatrix);
        for (Triangle triangle : transformedTriangleList){
            printToPicture(triangle, pictureData,transformedMinMaxValue);
        }
        for (Line line : lineList){
            Point begin = viewMatrix.transform(line.getBegin());
            Point end = viewMatrix.transform(line.getEnd());
            Line transformedLine = new Line(begin, end);
            printToPicture(transformedLine, pictureData, transformedMinMaxValue,null);
        }
        return pictureData;
    }

    /**
     * 获得消隐后的纯线框的图片数据
     */
    public int[][] getLineFrameOfPictureDate(Matrix viewMatrix){
        int[][] pictureData = new int[512][512];
        Map<String,Double> transformedMinMaxValue = new HashMap<>();
        List<Triangle> transformedTriangleList = new ArrayList<>();
        initialPictureData(pictureData,transformedMinMaxValue,transformedTriangleList,viewMatrix);
        for (Triangle triangle : transformedTriangleList){
            printToPicture(triangle, pictureData,transformedMinMaxValue);
        }

        int[][] newPictureData = new int[512][512];
        for (int i = 0 ;i < 512; i++){
            for(int j = 0; j < 512; j++){
                newPictureData[i][j] = 0;
            }
        }
        for (Line line : lineList){
            Point begin = viewMatrix.transform(line.getBegin());
            Point end = viewMatrix.transform(line.getEnd());
            Line transformedLine = new Line(begin, end);
            printToPicture(transformedLine, pictureData, transformedMinMaxValue ,newPictureData);
        }
        return newPictureData;
    }

    /**
     * 获得没有消隐的纯线框的数据
     */
    public int[][] getAllLinePictureData(Matrix viewMatrix){
        int[][] pictureData = new int[512][512];
        Map<String,Double> transformedMinMaxValue = new HashMap<>();
        List<Triangle> transformedTriangleList = new ArrayList<>();
        initialPictureData(pictureData,transformedMinMaxValue,transformedTriangleList,viewMatrix);
        for (Line line : lineList){
            Point begin = viewMatrix.transform(line.getBegin());
            Point end = viewMatrix.transform(line.getEnd());
            Line transformedLine = new Line(begin, end);
            printToPicture(transformedLine, pictureData, transformedMinMaxValue);
        }
        return pictureData;
    }

    /**
     * 初始化图片数据，将所有的三角面片经过视口变换，得到新的三角形数据
     */
    public void initialPictureData(int[][] pictureData,  Map<String,Double> transformedMinMaxValue,List<Triangle> transformedTriangleList ,Matrix viewMatrix){
        for (int i = 0 ;i < 512; i++){
            for(int j = 0; j < 512; j++){
                pictureData[i][j] = 255;
            }
        }
        initialMinMaxValue(transformedMinMaxValue);
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
    }

    /**
     * 三角形面片，经过phong模型投影到图片中，利用z-buffer算法
     */
    private void printToPicture(Triangle triangle,int[][] pictureData,Map<String,Double> minMaxValue){
        Double minX = Double.min(triangle.getA().getX(),Double.min(triangle.getB().getX(),triangle.getC().getX()));
        Double maxX = Double.max(triangle.getA().getX(),Double.max(triangle.getB().getX(),triangle.getC().getX()));
        Double minY = Double.min(triangle.getA().getY(),Double.min(triangle.getB().getY(),triangle.getC().getY()));
        Double maxY = Double.max(triangle.getA().getY(),Double.max(triangle.getB().getY(),triangle.getC().getY()));
        Triangle triangleInScreen = new Triangle();
        int index = 0;
        for (Point point : new Point[]{triangle.getA(),triangle.getB(), triangle.getC()}){
            double x = 492 * (1 - minMaxValue.get("xScale")) / 2 + 10 + 492 * (point.getX() - minMaxValue.get("minX"))/minMaxValue.get("scale");
            double y = 492 * (1 - minMaxValue.get("yScale")) / 2 + 10 + 492 * (point.getY() - minMaxValue.get("minY"))/minMaxValue.get("scale");
            int z = (int)(10 + 200 * (point.getZ() - minMaxValue.get("minZ"))/(minMaxValue.get("maxZ")-minMaxValue.get("minZ")));
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
        int xMinIndex = (int)(492 * (1 - minMaxValue.get("xScale")) / 2 + 10 + 492 * (minX - minMaxValue.get("minX"))/minMaxValue.get("scale"));
        int xMaxIndex = (int)(492 * (1 - minMaxValue.get("xScale")) / 2 + 10 + 492 * (maxX - minMaxValue.get("minX"))/minMaxValue.get("scale"));
        int yMinIndex = (int)(492 * (1 - minMaxValue.get("yScale")) / 2 + 10 + 492 * (minY - minMaxValue.get("minY"))/minMaxValue.get("scale"));
        int yMaxIndex = (int)(492 * (1 - minMaxValue.get("yScale")) / 2 + 10 + 492 * (maxY - minMaxValue.get("minY"))/minMaxValue.get("scale"));
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

    /**
     * 将不消隐的线，加到图片的数据矩阵中
     */
    private void printToPicture(Line line,int[][] pictureData,Map<String,Double> minMaxValue){
        Line lineInScreen = new Line();
        int index = 0;
        for (Point point : new Point[]{line.getBegin(), line.getEnd()}) {
            double x = (int)(492 * (1 - minMaxValue.get("xScale")) / 2 + 10 + 492 * (point.getX() - minMaxValue.get("minX")) / minMaxValue.get("scale"));
            double y = (int)(492 * (1 - minMaxValue.get("yScale")) / 2 + 10 + 492 * (point.getY() - minMaxValue.get("minY")) / minMaxValue.get("scale"));
            int z = (int) (10 + 200 * (point.getZ() - minMaxValue.get("minZ")) / (minMaxValue.get("maxZ") - minMaxValue.get("minZ")));
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
            double Ratio = k;
            for (int i = (int)beginInScreen.getX(); i <= (int)endInScreen.getX(); i++){
                int xSteps = i - xBegin;
                int yValue = yBegin + (int)Math.round(xSteps * Ratio);
                pictureData[i][yValue] = 0;
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
            double Ratio = (endInScreen.getX() - beginInScreen.getX())/(endInScreen.getY() - beginInScreen.getY());
            for (int i = (int)beginInScreen.getY(); i <= (int)endInScreen.getY(); i++){
                int ySteps = i - yBegin;
                int xValue = xBegin + (int)Math.round(ySteps * Ratio);
                pictureData[xValue][i] = 0;
            }
        }
    }

    /**
     * 将消隐后的线，加到或者独立加到图片数据矩阵中，利用z-buffer
     */
    private void printToPicture(Line line, int[][] pictureData, Map<String,Double> minMaxValue ,int[][] newPictureData) {
        if (null == newPictureData)
            newPictureData = pictureData;

        Line lineInScreen = new Line();
        int index = 0;
        for (Point point : new Point[]{line.getBegin(), line.getEnd()}) {
            double x = (int)(492 * (1 - minMaxValue.get("xScale")) / 2 + 10 + 492 * (point.getX() - minMaxValue.get("minX")) / minMaxValue.get("scale"));
            double y = (int)(492 * (1 - minMaxValue.get("yScale")) / 2 + 10 + 492 * (point.getY() - minMaxValue.get("minY")) / minMaxValue.get("scale"));
            int z = (int) (10 + 200 * (point.getZ() - minMaxValue.get("minZ")) / (minMaxValue.get("maxZ") - minMaxValue.get("minZ")));
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
                    newPictureData[i][yValue] = 255;
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
                    newPictureData[xValue][i] = 255;
                }
            }
        }

    }

    /**
     * 典型的图形学的问题，判断像素的坐标是否在三角形的内部
     */
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

    /**
     * 通过三角形三个点的不同的z值，来按比例判断三角形中某个点的z值
     */
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

    /**
     * 初始化该数据结构
     */
    private void initialMinMaxValue(Map<String,Double> minMaxValue){
        minMaxValue.put("minX",Double.MAX_VALUE);
        minMaxValue.put("minY",Double.MAX_VALUE);
        minMaxValue.put("minZ",Double.MAX_VALUE);
        minMaxValue.put("maxX",Double.MIN_VALUE);
        minMaxValue.put("maxY",Double.MIN_VALUE);
        minMaxValue.put("maxZ",Double.MIN_VALUE);
    }

    /**
     * 更新数据结构的数据，保持其有效性
     */
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

    /**
     * 归一化模型数据，获得模型的中点，并移动到坐标系的原点，根据比例将最长的轴缩放到1，其余的按比例缩放
     */
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
