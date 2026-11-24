package console;

import java.util.List;
import java.util.Scanner;
import java.util.function.Function;

public final class ConsoleUtils {
    private static final Scanner SCANNER = new Scanner(System.in);
    private static boolean eof = false;

    private ConsoleUtils() { }

    // ---------------------------------------------------------------- input

    public static String readLine(String prompt) {
        System.out.print(prompt);
        if (eof || !SCANNER.hasNextLine()) {
            eof = true;
            System.out.println("\n  [End of input. Exiting.]");
            System.exit(0);
        }
        return SCANNER.nextLine();
    }

    public static String readNonBlank(String prompt) {
        while (true) {
            String s = readLine(prompt).trim();
            if (!s.isEmpty()) return s;
            System.out.println("  [!] This field is required.");
        }
    }

    public static int readInt(String prompt, int min, int max) {
        while (true) {
            String s = readLine(prompt).trim();
            try {
                int v = Integer.parseInt(s);
                if (v >= min && v <= max) return v;
                System.out.println("  [!] Enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("  [!] Enter a whole number.");
            }
        }
    }

    public static double readDouble(String prompt) {
        while (true) {
            String s = readLine(prompt).trim();
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                System.out.println("  [!] Enter a valid number.");
            }
        }
    }

    public static java.time.LocalDate readDate(String prompt, boolean allowBlank) {
        while (true) {
            String s = readLine(prompt).trim();
            if (s.isEmpty()) {
                if (allowBlank) return null;
                System.out.println("  [!] Date is required (yyyy-mm-dd).");
                continue;
            }
            try {
                return java.time.LocalDate.parse(s);
            } catch (Exception e) {
                System.out.println("  [!] Invalid date. Use yyyy-mm-dd.");
            }
        }
    }

    public static boolean confirm(String prompt) {
        while (true) {
            String s = readLine(prompt + " [y/n]: ").trim().toLowerCase();
            if (s.equals("y") || s.equals("yes")) return true;
            if (s.equals("n") || s.equals("no")) return false;
            System.out.println("  [!] Please answer y or n.");
        }
    }

    // -------------------------------------------------------------- output

    public static void header(String title) {
        System.out.println();
        String line = "=".repeat(72);
        System.out.println(line);
        System.out.println("  " + title);
        System.out.println(line);
        System.out.println();
    }

    public static void subheader(String title) {
        System.out.println();
        System.out.println("  -- " + title + " " + "-".repeat(Math.max(0, 66 - title.length())));
    }

    public static void ok(String msg)    { System.out.println("\n  [OK] " + msg); }
    public static void error(String msg) { System.out.println("\n  [!!] " + msg); }
    public static void info(String msg)  { System.out.println("  " + msg); }

    public static void pause() {
        System.out.print("\n  Press Enter to continue...");
        if (eof || !SCANNER.hasNextLine()) { eof = true; return; }
        SCANNER.nextLine();
    }

    public static void table(String[] headers, List<String[]> rows) {
        int cols = headers.length;
        int[] w = new int[cols];
        for (int i = 0; i < cols; i++) w[i] = headers[i].length();
        for (String[] r : rows) {
            for (int i = 0; i < cols; i++) {
                String v = i < r.length && r[i] != null ? r[i] : "";
                if (v.length() > w[i]) w[i] = v.length();
            }
        }
        String sep = "  " + buildSep(w);
        System.out.println(sep);
        System.out.print("  ");
        for (int i = 0; i < cols; i++) System.out.print(pad(headers[i], w[i]) + "  ");
        System.out.println();
        System.out.println(sep);
        if (rows.isEmpty()) {
            System.out.println("  (no records)");
        } else {
            for (String[] r : rows) {
                System.out.print("  ");
                for (int i = 0; i < cols; i++) {
                    String v = i < r.length && r[i] != null ? r[i] : "";
                    System.out.print(pad(v, w[i]) + "  ");
                }
                System.out.println();
            }
        }
        System.out.println(sep);
        System.out.println("  " + rows.size() + " record(s)");
    }

    private static String buildSep(int[] w) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < w.length; i++) {
            if (i > 0) sb.append("  ");
            sb.append("-".repeat(w[i]));
        }
        return sb.toString();
    }

    private static String pad(String s, int width) {
        if (s == null) s = "";
        if (s.length() > width) s = s.substring(0, width);
        if (s.length() == width) return s;
        return s + " ".repeat(width - s.length());
    }

    // --------------------------------------------------- choose from list

    public static <T> T choose(List<T> items, Function<T, String> render, String title) {
        if (items.isEmpty()) {
            error("No records available.");
            return null;
        }
        subheader(title);
        for (int i = 0; i < items.size(); i++) {
            System.out.printf("  [%2d] %s%n", i + 1, render.apply(items.get(i)));
        }
        System.out.println("  [ 0] Cancel");
        System.out.println();
        int choice = readInt("  Choose: ", 0, items.size());
        if (choice == 0) return null;
        return items.get(choice - 1);
    }

    public static String money(double amount) {
        return String.format("PHP %,.2f", amount);
    }
}