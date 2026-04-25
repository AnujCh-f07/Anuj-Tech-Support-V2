public class NormalPricing implements PricingStrategy {
    public double calculate(double total) {
        double serviceCharge = total * 0.10;
        return total + serviceCharge;
    }
}
