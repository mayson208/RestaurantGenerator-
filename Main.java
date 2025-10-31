import java.util.*;
import java.util.stream.Collectors;

public class Main {

    // Simple model for a restaurant
    static class Restaurant {
        final String name;
        final String cuisine;     // e.g., "Mexican", "Italian", "American"
        final int priceLevel;     // 1 = $, 2 = $$, 3 = $$$
        final boolean takeout;    // offers takeout?

        Restaurant(String name, String cuisine, int priceLevel, boolean takeout) {
            this.name = name;
            this.cuisine = cuisine;
            this.priceLevel = priceLevel;
            this.takeout = takeout;
        }

        @Override
        public String toString() {
            String dollar = "$".repeat(Math.max(1, priceLevel));
            return String.format("%s (%s, %s, %s)", name, cuisine, dollar, takeout ? "Takeout" : "Dine-in");
        }
    }

    private static final List<Restaurant> RESTAURANTS = List.of(
        new Restaurant("Taco Loco", "Mexican", 1, true),
        new Restaurant("Bella Pasta", "Italian", 2, true),
        new Restaurant("Dragon Wok", "Chinese", 1, true),
        new Restaurant("Sushi Zen", "Japanese", 3, false),
        new Restaurant("Burger Barn", "American", 1, true),
        new Restaurant("Curry House", "Indian", 2, true),
        new Restaurant("Le Petit Bistro", "French", 3, false),
        new Restaurant("Mediterraneo", "Mediterranean", 2, true),
        new Restaurant("K-BBQ Grill", "Korean", 3, false),
        new Restaurant("Green Bowl", "Vegetarian", 1, true)
    );

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        println("🍽  Random Restaurant Generator");
        println("--------------------------------");

        // Gather optional filters
        String cuisine = ask(sc, "Filter by cuisine (press Enter to skip): ").trim();
        Integer priceLevel = askPrice(sc, "Max price level 1($) - 3($$$) (Enter to skip): ");
        Boolean wantsTakeout = askYesNoSkip(sc, "Require takeout? (y/n, Enter to skip): ");

        // Apply filters
        List<Restaurant> pool = RESTAURANTS.stream()
            .filter(r -> cuisine.isEmpty() || r.cuisine.equalsIgnoreCase(cuisine))
            .filter(r -> priceLevel == null || r.priceLevel <= priceLevel)
            .filter(r -> wantsTakeout == null || r.takeout == wantsTakeout)
            .collect(Collectors.toList());

        if (pool.isEmpty()) {
            println("\nNo matches with those filters. Showing all options instead.");
            pool = new ArrayList<>(RESTAURANTS);
        }

        // Shuffle for randomness
        List<Restaurant> shuffled = new ArrayList<>(pool);
        Collections.shuffle(shuffled, new Random());

        // Interactive reroll
        int index = 0;
        while (true) {
            Restaurant pick = shuffled.get(index % shuffled.size());
            println("\n🎯 Suggestion: " + pick);
            String action = ask(sc, "(r)eroll, (l)ist all, (q)uit: ").trim().toLowerCase();

            if (action.equals("q")) {
                println("\nEnjoy your meal! 👋");
                break;
            } else if (action.equals("l")) {
                println("\nAvailable options:");
                for (int i = 0; i < shuffled.size(); i++) {
                    println("  " + (i + 1) + ". " + shuffled.get(i));
                }
            } else if (action.equals("r")) {
                index++;
                // reshuffle occasionally for extra randomness
                if (index % shuffled.size() == 0) Collections.shuffle(shuffled, new Random());
            } else {
                println("Please type r, l, or q.");
            }
        }

        sc.close();
    }

    // -------- Helpers --------
    private static String ask(Scanner sc, String prompt) {
        System.out.print(prompt);
        return sc.nextLine();
    }

    private static Integer askPrice(Scanner sc, String prompt) {
        System.out.print(prompt);
        String s = sc.nextLine().trim();
        if (s.isEmpty()) return null;
        try {
            int p = Integer.parseInt(s);
            if (p < 1 || p > 3) {
                println("Please enter 1, 2, or 3. Skipping price filter.");
                return null;
            }
            return p;
        } catch (NumberFormatException e) {
            println("Not a number. Skipping price filter.");
            return null;
        }
    }

    private static Boolean askYesNoSkip(Scanner sc, String prompt) {
        System.out.print(prompt);
        String s = sc.nextLine().trim().toLowerCase();
        if (s.isEmpty()) return null;
        if (s.startsWith("y")) return true;
        if (s.startsWith("n")) return false;
        println("Invalid response. Skipping takeout filter.");
        return null;
    }

    private static void println(String s) {
        System.out.println(s);
    }
}
