import org.opencv.core.Mat;

/**
 * @author Liuhaifeng
 * @date 2021/9/8 - 19:37
 */
public class Moment {

    /**
     * 获取图像的Zernike矩，十五个系数
     */
    public static double[] getZernikeMoments(int[][] pictureData){
        int row = pictureData.length;
        int col = (row != 0) ? pictureData[0].length : 0;
        double[] ZernikeMoments = new double[15];
        for (int index = 0; index < 15; index++){
            for (int i = 0; i < row; i++)
                for (int j = 0;j < col; j++){
                    int n = -1;
                    if (index == 0)
                        n = 0;
                    else if (index > 0 && index < 3)
                        n = 1;
                    else if (index >= 3 && index < 6)
                        n = 2;
                    else if (index >= 6 && index < 10)
                        n = 3;
                    else
                        n = 4;
                    double[] polarPosition =  getPositionOfPolarCoordinates(i,j,row,col);
                    ZernikeMoments[index] += getZernikeMoment(index,polarPosition[0],polarPosition[1]) * pictureData[i][j] * (n + 1) / Math.PI;
                }
        }
        return ZernikeMoments;
    }

    /**
     * 获取单个像素的Zernike矩
     */
    public static double getZernikeMoment(int index,double r,double angle){
        double ans = 0;
        double arc = angle * Math.PI /180;
        switch (index){
            case 0:{
                ans = 1;
                break;
            }
            case 1:{
                ans =  2 * r * Math.sin(arc);
                break;
            }
            case 2:{
                ans = 2 * r * Math.cos(arc);
                break;
            }
            case 3:{
                ans = Math.sqrt(6) * Util.integerPow(r,2) * Math.sin(2 * arc);
                break;
            }
            case 4:{
                ans = Math.sqrt(3) * (2 * Util.integerPow(r,2) - 1);
                break;
            }
            case 5:{
                ans = Math.sqrt(6) * Util.integerPow(r,2) * Math.cos(2 * arc);
                break;
            }
            case 6:{
                ans = Math.sqrt(8) * Util.integerPow(r,3) * Math.sin(3 * arc);
                break;
            }
            case 7:{
                ans = Math.sqrt(8) * (3 * Util.integerPow(r,3) - 2 * Util.integerPow(r,2)) * Math.sin(arc);
                break;
            }
            case 8:{
                ans = Math.sqrt(8) * (3 * Util.integerPow(r,3) - 2 * Util.integerPow(r,2)) * Math.cos(arc);
                break;
            }
            case 9:{
                ans = Math.sqrt(8) * Util.integerPow(r,3) * Math.cos(3 * arc);
                break;
            }
            case 10:{
                ans = Math.sqrt(10) * Util.integerPow(r,4) * Math.sin(4 * arc);
                break;
            }
            case 11:{
                ans = Math.sqrt(10) * (4 * Util.integerPow(r,4) - 3 * Util.integerPow(r,2)) * Math.sin(2 * arc);
                break;
            }
            case 12:{
                ans = Math.sqrt(5) * (6 * Util.integerPow(r,4) - 6 * Util.integerPow(r,2) + 1);
                break;
            }
            case 13:{
                ans = Math.sqrt(10) * (4 * Util.integerPow(r,4) - 3 * Util.integerPow(r,3)) * Math.cos(2 * arc);
                break;
            }
            case 14:{
                ans = Math.sqrt(10) * Util.integerPow(r,4) * Math.cos(4 * arc);
                break;
            }
        }
        return Util.remainPoint4(ans);
    }

    /**
     * 将图像作为一个单位圆，返回某个像素的极坐标
     * 输入为像素的笛卡尔坐标
     * 输出为double[] 第一个参数是r，第二个是角度
     * @return
     */
    public static double[] getPositionOfPolarCoordinates(int x, int y, int row, int col){
        double ans[] = new double[2];
        int xPos = x - row/2;
        int yPos = y - col/2;
        double r = Math.sqrt(xPos * xPos + yPos * yPos)/Math.sqrt(row * row / 4 + col * col / 4);
        double arc = (yPos == 0) ? 0 : Math.atan(xPos*1.0/yPos);
        ans[0] = Util.remainPoint4(r);
        ans[1] = Util.remainPoint4(180 * arc/ Math.PI);
        return ans;
    }

    public static double[] getKrawtchoukMoments(int[][] pictureData){
        double[] ans = new double[9];
        int[][] indexArrays = new int[9][2];
        // 1,0
        indexArrays[0][0] = 1;
        indexArrays[0][1] = 0;
        //0,1
        indexArrays[1][0] = 0;
        indexArrays[1][1] = 1;
        //1,1
        indexArrays[2][0] = 1;
        indexArrays[2][1] = 1;
        //2,0
        indexArrays[3][0] = 2;
        indexArrays[3][1] = 0;
        //0,2
        indexArrays[4][0] = 0;
        indexArrays[4][1] = 2;
        //3,0
        indexArrays[5][0] = 3;
        indexArrays[5][1] = 0;
        //1,2
        indexArrays[6][0] = 1;
        indexArrays[6][1] = 2;
        //2,1
        indexArrays[7][0] = 2;
        indexArrays[7][1] = 1;
        //3,0
        indexArrays[8][0] = 0;
        indexArrays[8][1] = 3;

        for (int index = 0; index < 9; index++){
            int[] indexArray = indexArrays[index];
            int n = indexArray[0];
            int m = indexArray[1];
            ans[index] = getKrawtchoukMoment(pictureData, n, m,pictureData.length);
        }
        return ans;
    }

    public static double getKrawtchoukMoment(int[][] pictureData, int n, int m,int N){
        double preNumber = Math.pow(getRho(n,0.5,N - 1) * getRho(m,0.5,N - 1),-0.5);
        double afterNumber = 0;
        for (int i = 0; i <= n; i++) {
            for (int j = 0; j <= m; j++) {
                afterNumber += getA(i, n, 0.5, N) * getA(j, m, 0.5, N) * getV(pictureData, i, j, N);
            }
        }
        double ans = preNumber * afterNumber;
        return ans;
    }

    public static double getV(int[][] pictureData,int i,int j,int N){
        double ans = 0;
        double m10 = getM(pictureData,1,0,N);
        double m00 = getM(pictureData,0,0,N);
        double m01 = getM(pictureData,0,1,N);
        double xBar = m10/m00;
        double yBar = m01/m00;
        double angle = getKrawchoukAngle(pictureData,N,xBar,yBar);
        for (int x = 0; x < N; x++){
            for (int y = 0; y < N; y++){
                ans += N * N * pictureData[x][y]
                        * Util.integerPow(((x - xBar) * Math.cos(angle) + (y - yBar) * Math.sin(angle)) * Math.sqrt(N * N/(2 * m00)) + N/2, i)
                        * Util.integerPow(((y - yBar) * Math.cos(angle) + (x - xBar) * Math.sin(angle)) * Math.sqrt(N * N/(2 * m00)) + N/2, j)
                        / (2 * m00);
            }
        }
        return ans;
    }

    /**
     * i+j阶几何矩 mij
     */
    public static double getM(int[][] pictureData, int i,int j,int N){
        double ans = 0;
        for (int x = 0; x < N; x++){
            for (int y = 0; y < N; y++){
                ans += pictureData[x][y] * Util.integerPow(x, i) * Util.integerPow(y, j);
            }
        }
        return ans;
    }

    public static double getKrawchoukAngle(int[][] pictureData,int N,double xBar,double yBar){
        double u11 = getU(pictureData,1,1,N,xBar,yBar);
        double u20 = getU(pictureData,2,0,N,xBar,yBar);
        double u02 = getU(pictureData,0,2,N,xBar,yBar);
        double ans = 0.5 * Math.atan(2 * u11 / (u20 - u02));
        return ans;
    }

    public static double getU(int[][] pictureData, int i,int j,int N,double xBar,double yBar){
        double ans = 0;
        for (int x = 0; x < N; x++){
            for (int y = 0; y < N; y++){
                ans += pictureData[x][y] * Util.integerPow(x - xBar, i) * Util.integerPow(y - yBar, j);
            }
        }
        return ans;
    }

    public static double getA(int k,int n,double p,int N){
        double ans = getPochhanmmer(-1 * n, k) * Util.integerPow(1 / p, k) / ((getPochhanmmer(-1 * N, k) * getFactorial(k)));
        return ans;
    }

    /**
     * ρ(n;p1,N-1)
     * = (-1)^n * ((1-p)/p)^n * n!/(-N)n
     */
    public static double getRho(int n, double p, int N){
        double ans = Util.integerPow(-1,n) * Util.integerPow((1 - p)/p,n) * getFactorial(n)/getPochhanmmer(-1 * N,n);
        return ans;
    }

    /**
     * 	(a)k为Pochhanmmer算子，(a)k=a(a+1)⋯(a+k+1) k>=0
     */
    public static double getPochhanmmer(double a, int k){
        double ans = 1;
        for (int index = 0; index <= k-1; index++) {
            ans *= (a + index);
        }
        return ans;
    }

    /**
     * n!,这里n比较小，用long也不会导致溢出
     */
    public static long getFactorial(int n){
        if (n < 0){
            System.out.println("Wrong input number of getFactorial:" + n);
            return 0;
        }
        if (n == 0) return 1;
        else return getFactorial(n-1) * n;
    }

}
