

/**
 * @author Liuhaifeng
 * @date 2021/8/29 - 15:08
 */
public class Matrix {
    private double[][] arrays = null;
    private int raw = -1;
    private int col = -1;

    public Matrix(int raw, int col){
        arrays = new double[raw][col];
        this.raw = raw;
        this.col = col;
    }

    public Point transform(Point another){
        double x = arrays[0][0] * another.getX() + arrays[0][1] * another.getY() + arrays[0][2] * another.getZ() + arrays[0][3];
        double y = arrays[1][0] * another.getX() + arrays[1][1] * another.getY() + arrays[1][2] * another.getZ() + arrays[1][3];
        double z = arrays[2][0] * another.getX() + arrays[2][1] * another.getY() + arrays[2][2] * another.getZ() + arrays[2][3];
        return new Point(x, y, z);
    }

    public void setData(int i, int j, double value){
        if (i < 0 || i >= this.raw || j < 0 || j > this.col)
            return;
        arrays[i][j] = value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append("\n");
        for (int i=0 ; i<raw; i++){
            sb.append("[");
            for (int j= 0; j<col; j++){
                sb.append(arrays[i][j]);
                sb.append(",");
            }
            sb.append("]");
            sb.append("\n");
        }
        sb.append("]");
        return sb.toString();
    }
}
