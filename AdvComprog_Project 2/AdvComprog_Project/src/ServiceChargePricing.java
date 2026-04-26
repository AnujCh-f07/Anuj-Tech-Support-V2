public class ServiceChargePricing implements PricingStrategy {
    @Override
    public double calculate(double total) {
        if (total >= 0) {
            return total * 1.1;
        }
        return total;
    }
}
// changes this to vat instead of discount