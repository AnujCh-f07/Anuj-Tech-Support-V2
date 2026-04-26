import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        Bill bill = new Bill(new NormalPricing());

        System.out.print("Enter number of RED plates: ");
        int red = scanner.nextInt();

        for (int i = 0; i < red; i++) {
            bill.addPlate(PlateFactory.createPlate("red"));
        }

        double total = bill.getTotal();
        System.out.println("Total: " + total);

        bill.setPricingStrategy(new ServiceChargePricing());
        System.out.println("Discounted: " + bill.getTotal());

        SplitStrategy splitStrategy = new EqualSplit();

        System.out.print("People: ");
        int people = scanner.nextInt();

        double[] split = splitStrategy.split(bill.getTotal(), people);

        System.out.println("Each pays: " + split[0]);

        scanner.close();
    }
}