import java.util.ArrayList;
import java.util.List;

public class Bill {

    private List<Plate> plates = new ArrayList<>();
    private PricingStrategy pricingStrategy;
    private List<BillObserver> observers = new ArrayList<>();

    public Bill(PricingStrategy strategy) {
        this.pricingStrategy = strategy;
    }

    public void addPlate(Plate plate) {
        plates.add(plate);
        notifyObservers();
    }

    public double getTotal() {
        double total = 0;
        for (Plate p : plates) {
            total += p.getPrice();
        }
        return pricingStrategy.calculate(total);
    }

    public void setPricingStrategy(PricingStrategy strategy) {
        this.pricingStrategy = strategy;
        notifyObservers();
    }

    public void addObserver(BillObserver observer) {
        observers.add(observer);
    }

    private void notifyObservers() {
        double total = getTotal();
        for (BillObserver o : observers) {
            o.update(total);
        }
    }
}