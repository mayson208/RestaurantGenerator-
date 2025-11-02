import java.util.*;
import java.util.stream.Collectors;

public class Main {

    // -------- Restaurant Model --------
    static class Restaurant {
        final String name;
        final String cuisine;
        final int priceLevel; // 1 = $, 2 = $$, 3 = $$$
        final boolean takeout;

        Restaurant(String name, String cuisine, int priceLevel, boolean takeout) {
            this.name = name;
            this.cuisine = cuisine;
            this.priceLevel = priceLevel;
            this.takeout = takeout;
        }

        @Override
        public String toString() {
            String dollar = "$".repeat(Math.max(1, priceLevel));
            return String.format("%s (%s, %s, %s)",
                    name, cuisine, dollar, takeout ? "Takeout" : "Dine-in");
        }
    }

    // -------- Data --------
    private static final List<Restaurant> RESTAURANTS = List.of(
        new Restaurant("Taco Bell", "Mexican", 1, true),
        new Restaurant("Chipolte", "Mexican", 1, true),
        new Restaurant("Salsa's 3", "Mexican", 2, true),
        new Restaurant("Viron", "Italian", 2, true),
        new Restaurant("Bella G'as", "Italian", 3, false),
        new Restaurant("China Dragon", "Chinese", 1, true),
        new Restaurant("Asain Beastro", "Chinese", 2, true),
        new Restaurant("Wendy's", "American", 1, true),
        new Restaurant("Texas Roadhouse", "American", 1, true),
        new Restaurant("Sushi House", "Japanese", 3, false),
        new Restaurant("Funji Noodle", "Japanese", 2, true),
        new Restaurant("Curry House", "Indian", 2, true),
        new Restaurant("Spice Route", "Indian", 3, false),
        new Restaurant("Le Petit Bistro", "French", 3, false),
        new Restaurant("Olive Tree", "Mediterranean", 2, true),
        new Restaurant("K-BBQ", "Korean", 3, false),
        new Restaurant("Green Bowl", "Vegetarian", 1, true)
    );

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Random random = new Random();

        println("🍽  Random Restaurant Generator");
        println("--------------------------------");

        // --- Build cuisine map for random rolls ---
        Map<String, List<String>> restaurantsByCuisine = new HashMap<>();
        for (Restaurant r : RESTAURANTS) {
            restaurantsByCuisine.computeIfAbsent(r.cuisine, k -> new ArrayList<>()).add(r.name);
        }

        // --- Filter options ---
        String cuisine = ask(sc, "Filter by cuisine (press Enter to skip): ").trim();
        Integer priceLevel = askPrice(sc, "Max price level 1($) - 3($$$) (Enter to skip): ");
        Boolean wantsTakeout = askYesNoSkip(sc, "Require takeout? (y/n, Enter to skip): ");

        List<Restaurant> pool = RESTAURANTS.stream()
            .filter(r -> cuisine.isEmpty() || r.cuisine.equalsIgnoreCase(cuisine))
            .filter(r -> priceLevel == null || r.priceLevel <= priceLevel)
            .filter(r -> wantsTakeout == null || r.takeout == wantsTakeout)
            .collect(Collectors.toList());

        if (pool.isEmpty()) {
            println("\nNo matches with those filters. Showing all options instead.");
            pool = new ArrayList<>(RESTAURANTS);
        }

        List<Restaurant> shuffled = new ArrayList<>(pool);
        Collections.shuffle(shuffled, random);

        // --- Main loop ---
        int index = 0;
        while (true) {
            Restaurant pick = shuffled.get(index % shuffled.size());
            println("\n🎯 Suggestion: " + pick);
            String action = ask(sc, "(r)eroll, (l)ist all, (c)uisine roll, (q)uit: ").trim().toLowerCase();

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
                if (index % shuffled.size() == 0) Collections.shuffle(shuffled, random);
            } else if (action.equals("c")) {
                // --- NEW random cuisine & restaurant roll ---
                List<String> cuisines = new ArrayList<>(restaurantsByCuisine.keySet());
                String randomCuisine = cuisines.get(random.nextInt(cuisines.size()));
                List<String> options = restaurantsByCuisine.get(randomCuisine);
                String randomRestaurant = options.get(random.nextInt(options.size()));

                println("\n🎲 Cuisine Rolled: " + randomCuisine);
                println("🍴 Restaurant Picked: " + randomRestaurant);
            } else {
                println("Please type r, l, c, or q.");
            }
        }

        sc.close();
    }

    // -------- Helper Methods --------
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
