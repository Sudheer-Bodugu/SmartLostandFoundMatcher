import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

public class Main {

    static ArrayList<String> lostItems = new ArrayList<>();
    static ArrayList<String> foundItems = new ArrayList<>();

    static final String URL =
            "jdbc:mysql://localhost:3306/lost_found_db";
    static final String USER = "root";

    
    static final String PASSWORD =
            System.getenv("MYSQL_PASSWORD");

    public static void main(String[] args) {

        System.out.println("SMART LOST & FOUND MATCHER");
        System.out.println("Testing MySQL connection...");
        testConnection();
        loadItems();        

        Scanner sc = new Scanner(System.in);
        int choice;

        do {
            System.out.println("\n==============================");
            System.out.println(" SMART LOST & FOUND MATCHER");
            System.out.println("==============================");
            System.out.println("1. Report Lost Item");
            System.out.println("2. Report Found Item");
            System.out.println("3. View All Reports");
            System.out.println("4. Find Possible Matches");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");

            while (!sc.hasNextInt()) {
                System.out.print("Please enter a number: ");
                sc.next();
            }

            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {

                case 1:
                    System.out.print("Enter lost item name: ");
                    String lost = sc.nextLine().trim();

                    if (!lost.isEmpty()) {
                        System.out.print("Enter item description: ");
                        String description = sc.nextLine().trim();

                        System.out.print("Enter location: ");
                        String location = sc.nextLine().trim();

                        if (saveItem(lost, "LOST", description, location)) {
                            lostItems.add(lost);
                            System.out.println("Lost item reported successfully!");
                        }
                    } else {
                        System.out.println(
                                "Item name cannot be empty.");
                    }
                    break;

                case 2:
                    System.out.print("Enter found item name: ");
                    String found = sc.nextLine().trim();

                    if (!found.isEmpty()) {
                        
                        System.out.print("Enter item description: ");
                        String description = sc.nextLine().trim();

                        System.out.print("Enter location: ");
                        String location = sc.nextLine().trim();

                        if (saveItem(found, "FOUND", description, location)) {
                            foundItems.add(found);
                            System.out.println("Found item reported successfully!");
                        }
                    } else {
                        System.out.println(
                                "Item name cannot be empty.");
                    }
                    break;

                case 3:
                    System.out.println("\n--- LOST ITEMS ---");

                    if (lostItems.isEmpty()) {
                        System.out.println(
                                "No lost items reported.");
                    } else {
                    	LinkedHashSet<String> uniqueLostItems =
                    	        new LinkedHashSet<>();

                    	for (String item : lostItems) {
                    	    if (item != null && !item.trim().isEmpty()) {
                    	        boolean alreadyExists = uniqueLostItems.stream()
                    	                .anyMatch(existing ->
                    	                        existing.equalsIgnoreCase(item.trim()));

                    	        if (!alreadyExists) {
                    	            uniqueLostItems.add(item.trim());
                    	        }
                    	    }
                    	}

                    	int i = 1;
                    	for (String item : uniqueLostItems) {
                    	    System.out.println(i++ + ". " + item);
                    	}
                    }

                    System.out.println("\n--- FOUND ITEMS ---");

                    if (foundItems.isEmpty()) {
                        System.out.println(
                                "No found items reported.");
                    } else {
                    	LinkedHashSet<String> uniqueFoundItems =
                    	        new LinkedHashSet<>();

                    	for (String item : foundItems) {
                    	    if (item != null && !item.trim().isEmpty()) {
                    	        boolean alreadyExists = uniqueFoundItems.stream()
                    	                .anyMatch(existing ->
                    	                        existing.equalsIgnoreCase(item.trim()));

                    	        if (!alreadyExists) {
                    	            uniqueFoundItems.add(item.trim());
                    	        }
                    	    }
                    	}

                    	int i = 1;
                    	for (String item : uniqueFoundItems) {
                    	    System.out.println(i++ + ". " + item);
                    	}
                    }
                    break;

                case 4:
                    findMatchesFromDatabase();
                    break;
                    
                case 5:
                    System.out.println(
                            "Thank you for using the application!");
                    break;

                default:
                    System.out.println(
                            "Invalid choice. Select 1 to 5.");
            }

        } while (choice != 5);

        sc.close();
    }

    public static void testConnection() {
        try (Connection con =
                DriverManager.getConnection(URL, USER, PASSWORD)) {

            System.out.println("MySQL connected successfully!");

        } catch (SQLException e) {
            System.out.println("MySQL connection failed!");
            System.out.println(e.getMessage());
        }
    }
    public static void loadItems() {

        String sql = "SELECT item_name, item_type FROM items";

        try (Connection con =
                     DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            lostItems.clear();
            foundItems.clear();

            while (rs.next()) {
                String itemName = rs.getString("item_name");
                String itemType = rs.getString("item_type");

                if ("LOST".equalsIgnoreCase(itemType)) {
                    lostItems.add(itemName);
                } else if ("FOUND".equalsIgnoreCase(itemType)) {
                    foundItems.add(itemName);
                }
            }

            System.out.println("Saved items loaded from MySQL!");

        } catch (SQLException e) {
            System.out.println("Failed to load items from MySQL.");
            System.out.println(e.getMessage());
        }
    }

    public static boolean saveItem(
            String itemName,
            String itemType,
            String description,
            String location) {
    	if (itemName == null || itemName.trim().isEmpty()
    	        || description == null || description.trim().isEmpty()
    	        || location == null || location.trim().isEmpty()) {

    	    System.out.println(
    	            "Item name, description, and location are required.");

    	    return false;
    	}

    	String checkSql = "SELECT COUNT(*) FROM items "
    	        + "WHERE LOWER(TRIM(item_name)) = LOWER(TRIM(?)) "
    	        + "AND LOWER(TRIM(item_type)) = LOWER(TRIM(?)) "
    	        + "AND LOWER(TRIM(description)) <=> LOWER(TRIM(?)) "
    	        + "AND LOWER(TRIM(location)) <=> LOWER(TRIM(?))";

        String insertSql = "INSERT INTO items "
                + "(item_name, item_type, description, location) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection con =
                     DriverManager.getConnection(URL, USER, PASSWORD)) {

            try (PreparedStatement check =
                         con.prepareStatement(checkSql)) {

                check.setString(1, itemName);
                check.setString(2, itemType);
                check.setString(3, description);
                check.setString(4, location);

                try (java.sql.ResultSet rs = check.executeQuery()) {
                    rs.next();

                    if (rs.getInt(1) > 0) {
                        System.out.println(
                                "This report already exists. Not saved again.");
                        return false;
                    }
                }
            }

            try (PreparedStatement pst =
                         con.prepareStatement(insertSql)) {

                pst.setString(1, itemName);
                pst.setString(2, itemType);
                pst.setString(3, description);
                pst.setString(4, location);

                pst.executeUpdate();

                System.out.println("Item saved to MySQL successfully!");
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Failed to save item to MySQL.");
            System.out.println(e.getMessage());
            return false;
        }
    }

    public static boolean isPossibleMatch(
            String lostItem, String foundItem) {

        if (lostItem == null || foundItem == null) {
            return false;
        }

        lostItem = normalizeText(lostItem);
        foundItem = normalizeText(foundItem);

        if (lostItem.isEmpty() || foundItem.isEmpty()) {
            return false;
        }

        // Exact match
        if (lostItem.equals(foundItem)) {
            return true;
        }

        // Split descriptions into words
        String[] lostWords = lostItem.split("\\s+");
        String[] foundWords = foundItem.split("\\s+");

        java.util.Set<String> ignoredWords = java.util.Set.of(
                "the", "and", "with", "for", "near",
                "from", "was", "has", "have", "containing",
                "found", "lost", "item", "this", "that",
                "its", "nearby", "inside"
        );

        java.util.Set<String> lostMeaningfulWords =
                new java.util.HashSet<>();

        java.util.Set<String> foundMeaningfulWords =
                new java.util.HashSet<>();

        for (String word : lostWords) {
            if (word.length() >= 3
                    && !word.equals("test")
                    && !ignoredWords.contains(word)) {
                lostMeaningfulWords.add(word);
            }
        }

        for (String word : foundWords) {
            if (word.length() >= 3
                    && !word.equals("test")
                    && !ignoredWords.contains(word)) {
                foundMeaningfulWords.add(word);
            }
        }

        int matchingWords = 0;

        for (String word : lostMeaningfulWords) {
            if (foundMeaningfulWords.contains(word)) {
                matchingWords++;
            }
        }

        // Require at least two meaningful shared words,
        // or one shared word when one side has only one meaningful word.
        return matchingWords >= 2
                || (matchingWords == 1
                && (lostMeaningfulWords.size() == 1
                || foundMeaningfulWords.size() == 1));
    }
    public static void findMatchesFromDatabase() {

        ArrayList<ItemDetails> lostReports = new ArrayList<>();
        ArrayList<ItemDetails> foundReports = new ArrayList<>();

        java.util.Set<String> displayedMatches =
                new java.util.HashSet<>();

        ArrayList<String[]> matches = new ArrayList<>();

        String sql = "SELECT id, item_name, item_type, description, location "
                + "FROM items";

        try (Connection con =
                     DriverManager.getConnection(URL, USER, PASSWORD);
             java.sql.Statement stmt = con.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("item_name");
                String type = rs.getString("item_type");
                String description = rs.getString("description");
                String location = rs.getString("location");

                ItemDetails item =
                        new ItemDetails(id, name, description, location);

                if ("LOST".equalsIgnoreCase(type)) {
                    lostReports.add(item);
                } else if ("FOUND".equalsIgnoreCase(type)) {
                    foundReports.add(item);
                }
            }

            for (ItemDetails lost : lostReports) {
                for (ItemDetails found : foundReports) {
                	if (lost.name != null
                	        && lost.name.toLowerCase().contains("test")) {
                	    continue;
                	}

                	if (found.name != null
                	        && found.name.toLowerCase().contains("test")) {
                	    continue;
                	}

                    boolean nameMatch =
                            lost.name != null
                            && found.name != null
                            && !lost.name.trim().isEmpty()
                            && !found.name.trim().isEmpty()
                            && isPossibleMatch(lost.name, found.name);

                    boolean descriptionMatch =
                            lost.description != null
                            && found.description != null
                            && isPossibleMatch(
                                    lost.description, found.description);

                    if (nameMatch
                            && descriptionMatch
                            && lost.name != null
                            && found.name != null) {

                    	int score = calculateMatchScore(lost, found);

                    	// Display exact matches or strong supported matches only.
                    	boolean exactDescription =
                    	        lost.description != null
                    	        && found.description != null
                    	        && normalizeText(lost.description)
                    	                .equals(normalizeText(found.description));

                    	boolean similarNames =
                    	        isPossibleMatch(lost.name, found.name);

                    	boolean similarDescriptions =
                    	        isPossibleMatch(lost.description, found.description);

                    	if (score < 50 || !exactDescription) {
                    	    continue;
                    	}
                    	String matchKey =
                    	        normalizeText(lost.name) + "|"
                    	        + normalizeText(found.name);

                    	if (!displayedMatches.add(matchKey)) {
                    	    continue;
                    	}

                        matches.add(new String[] {
                                String.valueOf(score),
                                lost.name,
                                found.name,
                                lost.description,
                                found.description,
                                lost.location,
                                found.location
                        });
                    }
                }
            }

            // Sort matches from highest score to lowest score.
            matches.sort((a, b) ->
                    Integer.compare(
                            Integer.parseInt(b[0]),
                            Integer.parseInt(a[0])));

            System.out.println("\n--- POSSIBLE MATCHES ---");

            if (matches.isEmpty()) {
                System.out.println("No possible matches found yet.");
            } else {
                for (String[] match : matches) {

                    System.out.println(
                            "Possible match: " + match[1]
                            + " <-> " + match[2]);

                    int score = Integer.parseInt(match[0]);

                    System.out.println("Match Score: " + score + "/100");

                    if (score >= 80) {
                        System.out.println("Match Category: HIGH");
                    } else if (score >= 50) {
                        System.out.println("Match Category: MEDIUM");
                    } else {
                        System.out.println("Match Category: LOW");
                    }
                    System.out.println(
                            "Lost description: " + match[3]);

                    System.out.println(
                            "Found description: " + match[4]);

                    System.out.println(
                            "Lost location: " + match[5]);

                    System.out.println(
                            "Found location: " + match[6]);

                    System.out.println();
                }
            }

        } catch (SQLException e) {
            System.out.println("Failed to find database matches.");
            System.out.println(e.getMessage());
        }
    }
    public static String normalizeText(String value) {
        if (value == null) {
            return "";
        }

        return value.trim().toLowerCase();
    }
    public static int calculateMatchScore(
            ItemDetails lost, ItemDetails found) {

        if (lost == null || found == null) {
            return 0;
        }

        String lostName = normalizeText(lost.name);
        String foundName = normalizeText(found.name);

        String lostDescription = normalizeText(lost.description);
        String foundDescription = normalizeText(found.description);

        String lostLocation = normalizeText(lost.location);
        String foundLocation = normalizeText(found.location);

        int score = 0;

        // Exact item name
        if (!lostName.isEmpty() && lostName.equals(foundName)) {
            score += 40;
        } else if (isPossibleMatch(lost.name, found.name)) {
            score += 25;
        }

        // Exact description
        if (!lostDescription.isEmpty()
                && lostDescription.equals(foundDescription)) {
            score += 40;
        } else if (isPossibleMatch(
                lost.description, found.description)) {
            score += 25;
        }

        // Exact location
        if (!lostLocation.isEmpty()
                && lostLocation.equals(foundLocation)) {
            score += 20;
        }

        return score;
    }
    static class ItemDetails {
    	int id;
        String name;
        String description;
        String location;

        ItemDetails(int id, String name, String description, String location) {
        	this.id = id;
            this.name = name;
            this.description = description;
            this.location = location;
        }
    }
}