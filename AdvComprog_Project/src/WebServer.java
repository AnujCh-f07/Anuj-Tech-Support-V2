import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class WebServer {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        
        server.createContext("/", (exchange) -> {
            String response = getForm();
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
        });

       
        server.createContext("/calculate", (exchange) -> {
            try {
                if ("POST".equals(exchange.getRequestMethod())) {
                    String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    HashMap<String, String> params = parse(body);

                    // Logic: Bill and Strategy setup
                    Bill bill = new Bill(new NormalPricing());

                    for (String key : params.keySet()) {
                        if (key.startsWith("plate_")) {
                            int qty = Integer.parseInt(params.getOrDefault(key, "0"));
                            String type = key.replace("plate_", "");
                            for (int i = 0; i < qty; i++) {
                                bill.addPlate(PlateFactory.createPlate(type));
                            }
                        }
                    }

                    int people = Integer.parseInt(params.getOrDefault("people", "1"));
                    if (people < 1) people = 1;

                    double total = bill.getTotal();
                    bill.setPricingStrategy(new DiscountPricing());
                    double discounted = bill.getTotal();

                    SplitStrategy splitStrategy = new EqualSplit();
                    double[] split = splitStrategy.split(discounted, people);

                    // Response with Aesthetic Receipt
                    String response = getReceipt(total, discounted, split[0], people);
                    byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);

                    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                    exchange.sendResponseHeaders(200, responseBytes.length);

                    OutputStream os = exchange.getResponseBody();
                    os.write(responseBytes);
                    os.close();
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            } catch (Exception e) {
                e.printStackTrace();
                String error = "<html><body style='font-family:sans-serif; text-align:center;'><h2>Oops!</h2><p>" + e.getMessage() + "</p><a href='/'>Go Back</a></body></html>";
                exchange.sendResponseHeaders(500, error.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(error.getBytes());
                os.close();
            }
        });

        server.start();
        System.out.println("🚀 Server active: http://localhost:8000");
    }

    
    static String getForm() {
        return "<html><head><meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<style>" +
                "  :root { --gold: #ba8c5a; --dark: #1a1a1a; --bg: #fdfcfb; --card: #ffffff; }" +
                "  body { font-family: 'Segoe UI', system-ui, sans-serif; background: var(--bg); color: var(--dark); margin: 0; padding: 20px; }" +
                "  .container { max-width: 450px; margin: 40px auto; background: var(--card); padding: 40px; border-radius: 20px; box-shadow: 0 10px 40px rgba(0,0,0,0.06); }" +
                "  h1 { font-weight: 300; letter-spacing: -1px; margin-bottom: 30px; border-bottom: 2px solid var(--gold); display: inline-block; padding-bottom: 5px; }" +
                "  .plate-grid { display: grid; gap: 12px; margin-bottom: 30px; }" +
                "  .plate-item { display: flex; justify-content: space-between; align-items: center; padding: 12px 18px; background: #f9f9f9; border-radius: 12px; transition: 0.2s; }" +
                "  .plate-item:hover { background: #f2f2f2; transform: scale(1.02); }" +
                "  .plate-name { font-weight: 500; display: flex; align-items: center; }" +
                "  .dot { width: 10px; height: 10px; border-radius: 50%; margin-right: 12px; }" +
                "  input[type='number'] { width: 55px; padding: 8px; border: 1px solid #ddd; border-radius: 8px; outline: none; text-align: center; font-weight: bold; font-family: inherit; }" +
                "  .people-row { margin-top: 25px; padding-top: 20px; border-top: 1px solid #eee; display: flex; justify-content: center; align-items: center; gap: 15px; font-weight: 500; }" +
                "  button { width: 100%; padding: 16px; background: var(--dark); color: white; border: none; border-radius: 12px; font-size: 1rem; font-weight: 600; cursor: pointer; transition: 0.3s; margin-top: 25px; }" +
                "  button:hover { background: #333; box-shadow: 0 4px 12px rgba(0,0,0,0.2); }" +
                "</style></head><body>" +
                "<div class='container'>" +
                "  <h1>🍣 Sushi Sensei</h1>" +
                "  <form method='POST' action='/calculate'>" +
                "    <div class='plate-grid'>" +
                        plateRow("Red", "plate_red", "#e74c3c") +
                        plateRow("Blue", "plate_blue", "#3498db") +
                        plateRow("Green", "plate_green", "#2ecc71") +
                        plateRow("Orange", "plate_orange", "#f39c12") +
                        plateRow("Brown", "plate_brown_grid", "#795548") +
                        plateRow("Pink", "plate_pink_speckled", "#ff9ff3") +
                        plateRow("Pattern", "plate_green_pattern", "#16a085") +
                "    </div>" +
                "    <div class='people-row'>" +
                "       <span>👥 Split between</span>" +
                "       <input type='number' name='people' value='1' min='1'>" +
                "       <span>people</span>" +
                "    </div>" +
                "    <button type='submit'>Generate Receipt</button>" +
                "  </form>" +
                "</div>" +
                "</body></html>";
    }

    static String plateRow(String name, String key, String color) {
        return "<div class='plate-item'>" +
                "  <span class='plate-name'><span class='dot' style='background:"+color+"'></span>" + name + "</span>" +
                "  <input type='number' name='" + key + "' value='0' min='0'>" +
                "</div>";
    }

    
    static String getReceipt(double total, double discounted, double split, int people) {
        return "<html><head><meta charset='UTF-8'>" +
                "<style>" +
                "  body { font-family: 'Segoe UI', sans-serif; background: #121212; color: #333; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; }" +
                "  .receipt { background: white; padding: 40px; border-radius: 2px; width: 300px; box-shadow: 0 20px 50px rgba(0,0,0,0.5); position: relative; }" +
                "  .receipt::after { content: ''; position: absolute; bottom: -15px; left: 0; width: 100%; height: 15px; background: linear-gradient(-45deg, white 8px, transparent 0), linear-gradient(45deg, white 8px, transparent 0); background-size: 16px 16px; }" +
                "  h2 { text-align: center; text-transform: uppercase; letter-spacing: 3px; border-bottom: 1px dashed #aaa; padding-bottom: 10px; margin-top: 0; }" +
                "  .line { display: flex; justify-content: space-between; margin: 12px 0; font-size: 0.9rem; }" +
                "  .total { font-size: 1.5rem; font-weight: 800; border-top: 2px solid #333; padding-top: 15px; margin-top: 15px; }" +
                "  .promo { color: #27ae60; font-weight: bold; }" +
                "  .back-link { display: block; text-align: center; margin-top: 35px; text-decoration: none; color: #ba8c5a; font-size: 0.75rem; font-weight: bold; letter-spacing: 1px; }" +
                "</style></head><body>" +
                "  <div class='receipt'>" +
                "    <h2>Receipt</h2>" +
                "    <div class='line'><span>Subtotal</span><span>฿" + String.format("%.2f", total) + "</span></div>" +
                "    <div class='line'><span class='promo'>Discount</span><span class='promo'>-฿" + String.format("%.2f", total - discounted) + "</span></div>" +
                "    <div class='line'><span>Party Size</span><span>" + people + "</span></div>" +
                "    <div class='line total'><span>Total PP</span><span>฿" + String.format("%.2f", split) + "</span></div>" +
                "    <a href='/' class='back-link'>NEW ORDER</a>" +
                "  </div>" +
                "</body></html>";
    }

    
    static HashMap<String, String> parse(String query) {
        HashMap<String, String> map = new HashMap<>();
        if (query == null) return map;
        String[] pairs = query.split("&");
        for (String p : pairs) {
            String[] kv = p.split("=");
            try {
                if (kv.length == 2) {
                    map.put(URLDecoder.decode(kv[0], "UTF-8"), URLDecoder.decode(kv[1], "UTF-8"));
                }
            } catch (Exception ignored) {}
        }
        return map;
    }
}