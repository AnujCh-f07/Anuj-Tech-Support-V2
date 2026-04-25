public class DiscountPricing implements PricingStrategy {
    public double calculate(double total) {
        if (total >= 500) {
            return total * 0.85;
        }
        return total;
    }
}