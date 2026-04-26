import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PlateFactory {

    private static final Map<String, Supplier<Plate>> plateMap = new HashMap<>();

    static {
        plateMap.put("red", RedPlate::new);
        plateMap.put("blue", BluePlate::new);
        plateMap.put("green", GreenPlate::new);
        plateMap.put("orange", OrangePlate::new);
        plateMap.put("brown_grid", BrownGridPlate::new);
        plateMap.put("pink_speckled", PinkSpeckledPlate::new);
        plateMap.put("gold_pattern", GoldPatternPlate::new);
        //plateMap.put("white", WhitePlate::new);
    }

    public static Plate createPlate(String type) {
        Supplier<Plate> supplier = plateMap.get(type.toLowerCase());
        if (supplier == null) {
            throw new IllegalArgumentException("Invalid plate type");
        }
        return supplier.get();
    }
}