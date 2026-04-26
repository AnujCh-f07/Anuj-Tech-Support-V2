public class NormalPricing implements PricingStrategy {
    @Override
    public double calculate(double total) {
        return total;
    }
}