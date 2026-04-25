public class EqualSplit implements SplitStrategy {
    public double[] split(double total, int people) {
        double each = total / people;
        double[] result = new double[people];
        for (int i = 0; i < people; i++) {
            result[i] = each;
        }
        return result;
    }
}