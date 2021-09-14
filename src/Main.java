import com.sun.deploy.util.StringUtils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Liuhaifeng
 * @date 2021/8/28 - 20:38
 */
public class Main {
    public static void main(String[] args) {
        try {
            Model model = new Model(
                    "D:\\Desktop\\文件\\任务\\毕业设计\\3D\\ESB零件库\\Rectangular-Cubic-Prism\\Rectangular-Cubic Prism\\Bearing Blocks\\advgr01.STL");
//            model.getPictures("D:\\Desktop\\2");
//            model.extractDescriptor("D:\\Desktop\\3");
            File file = new File("D:\\Desktop\\3\\advgr01.STL_DATA.txt");
            Scanner sc = new Scanner(file);
            String fileLine = null;
            Map<Integer,List<Integer>> dataMap = new HashMap<>();
            int index = 0;
            while (sc.hasNext()){
                fileLine = sc.nextLine();
                String[] numberList = fileLine.split("#");
                List<Integer> tempList = new ArrayList<>();
                for (int i = 0; i < numberList.length; i++){
                    tempList.add(Integer.parseInt(numberList[i]));
                }
                dataMap.put(index++,tempList);
            }
            model.calculateFrequentItem("D:\\Desktop\\3",dataMap);

        }catch (Exception e){
            e.printStackTrace();
        }

    }
}

