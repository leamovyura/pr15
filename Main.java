import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Main {

    static final int MAX_USERS = 15;
    static String[] usernames = new String[MAX_USERS];
    static String[] passwords = new String[MAX_USERS];
    static int userCount = 0;

    static String[] forbiddenPasswords = {"admin", "pass", "password", "qwerty", "ytrewq"};
    static int forbiddenCount = forbiddenPasswords.length;

    static final String CONFIG_FILE = "config.txt";
    static final String STATS_FILE = "stats.txt";

    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        loadConfig();

        boolean exit = false;
        while (!exit) {
            printMenu();
            String choice = scanner.nextLine();

            try {
                switch (choice) {
                    case "1":
                        addUser();
                        break;
                    case "2":
                        deleteUser();
                        break;
                    case "3":
                        performUserAction();
                        break;
                    case "4":
                        showForbiddenPasswords();
                        break;
                    case "5":
                        addForbiddenPassword();
                        break;
                    case "6":
                        saveConfig();
                        exit = true;
                        System.out.println("Вихід/");
                        break;
                    default:
                        System.out.println("Невірний вибір.");
                }
            } catch (UserAuthException e) {
                System.out.println("Помилкп: " + e.getMessage());
            }
        }
        scanner.close();
    }

    static void printMenu() {
        System.out.println("\nМеню:");
        System.out.println("1 Додати користувача");
        System.out.println("2 Видалити користувача");
        System.out.println("3 Виконати дію від користувача");
        System.out.println("4 Показати заборонені паролі");
        System.out.println("5 Додати заборонений пароль");
        System.out.println("6 Вийти");
        System.out.print("Ваш вибір: ");
    }

    static void addUser() throws UserAuthException {
        if (userCount >= MAX_USERS) {
            throw new UserAuthException("Максимальна кількість користувачів досягнута");
        }

        System.out.print("Введіть ім'я: ");
        String username = scanner.nextLine();

        validateUsername(username);

        if (findUserIndex(username) != -1) {
            throw new UserAuthException("Користувач з таким ім'ям вже існує.");
        }

        System.out.print("Введіть пароль: ");
        String password = scanner.nextLine();

        validatePassword(password);

        usernames[userCount] = username;
        passwords[userCount] = password;
        userCount++;

        System.out.println("Користувача додано");
    }

    static void deleteUser() throws UserAuthException {
        System.out.print("Введіть ім'я користувача для видалення: ");
        String username = scanner.nextLine();

        int idx = findUserIndex(username);
        if (idx == -1) {
            throw new UserAuthException("Користувача з таким ім'ям не знайдено");
        }

        for (int i = idx; i < userCount - 1; i++) {
            usernames[i] = usernames[i + 1];
            passwords[i] = passwords[i + 1];
        }
        usernames[userCount - 1] = null;
        passwords[userCount - 1] = null;
        userCount--;

        System.out.println("Користувача видалено");
    }

    static void performUserAction() throws UserAuthException {
        System.out.print("Введіть ім'я: ");
        String username = scanner.nextLine();
        System.out.print("Введіть пароль: ");
        String password = scanner.nextLine();

        int idx = findUserIndex(username);
        if (idx == -1 || !passwords[idx].equals(password)) {
            throw new UserAuthException("Невірне ім'я або пароль");
        }

        System.out.println("Залогінило");

        saveStats(username);
    }

    static void validateUsername(String username) throws UserAuthException {
        if (username.length() < 5) {
            throw new UserAuthException("Ім'я має бути не менше 5 символів");
        }
        for (int i = 0; i < username.length(); i++) {
            if (username.charAt(i) == ' ') {
                throw new UserAuthException("Ім'я не має містити пробілів.");
            }
        }
    }

    static void validatePassword(String password) throws UserAuthException {
        if (password.length() < 10) {
            throw new UserAuthException("Пароль має бути не менше 10 символів");
        }
        int digitCount = 0;
        int specialCount = 0;
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (c == ' ') {
                throw new UserAuthException("Пароль не має містити пробілів");
            }
            if (c >= '0' && c <= '9') digitCount++;
            else if (!((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z'))) specialCount++;
        }
        if (digitCount < 3) {
            throw new UserAuthException("Пароль має містити принаймні 3 цифри");
        }
        if (specialCount < 1) {
            throw new UserAuthException("Пароль має містити принаймні 1 спеціальний символ");
        }
        for (int i = 0; i < forbiddenCount; i++) {
            if (containsIgnoreCase(password, forbiddenPasswords[i])) {
                throw new UserAuthException("Пароль містить заборонене слово: " + forbiddenPasswords[i]);
            }
        }
    }

    static boolean containsIgnoreCase(String str, String substr) {
        String lowerStr = toLowerCase(str);
        String lowerSubstr = toLowerCase(substr);
        return indexOf(lowerStr, lowerSubstr) != -1;
    }

    static String toLowerCase(String s) {
        char[] chars = new char[s.length()];
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                chars[i] = (char)(c + 32);
            } else {
                chars[i] = c;
            }
        }
        return new String(chars);
    }

    static int indexOf(String text, String pattern) {
        int n = text.length();
        int m = pattern.length();
        for (int i = 0; i <= n - m; i++) {
            boolean found = true;
            for (int j = 0; j < m; j++) {
                if (text.charAt(i + j) != pattern.charAt(j)) {
                    found = false;
                    break;
                }
            }
            if (found) return i;
        }
        return -1;
    }

    static int findUserIndex(String username) {
        for (int i = 0; i < userCount; i++) {
            if (usernames[i].equals(username)) return i;
        }
        return -1;
    }

    static void saveStats(String username) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(STATS_FILE, true))) {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String record = now.format(dtf) + " - Користувач " + username + " успішно залогіниний";
            bw.write(record);
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Помилка збереження статистики: " + e.getMessage());
        }
    }

    static void loadConfig() {
        try (BufferedReader br = new BufferedReader(new FileReader(CONFIG_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("forbiddenPassword=")) {
                    String pass = line.substring("forbiddenPassword=".length());
                    addForbiddenPasswordInternal(pass);
                }
            }
        } catch (IOException e) {
        }
    }

    static void saveConfig() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(CONFIG_FILE))) {
            for (int i = 0; i < forbiddenCount; i++) {
                bw.write("forbiddenPassword=" + forbiddenPasswords[i]);
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Помилка збереження конфігурації: " + e.getMessage());
        }
    }

    static void showForbiddenPasswords() {
        System.out.println("Заборонені паролі:");
        for (int i = 0; i < forbiddenCount; i++) {
            System.out.println("- " + forbiddenPasswords[i]);
        }
    }

    static void addForbiddenPassword() {
        System.out.print("Введіть пароль для заборони: ");
        String pass = scanner.nextLine().trim();
        if (pass.isEmpty()) {
            System.out.println("Порожній пароль не може бути доданий");
            return;
        }
        addForbiddenPasswordInternal(pass);
        System.out.println("Пароль додано до заборонених");
    }

    static void addForbiddenPasswordInternal(String pass) {
        String[] newArray = new String[forbiddenCount + 1];
        for (int i = 0; i < forbiddenCount; i++) {
            newArray[i] = forbiddenPasswords[i];
        }
        newArray[forbiddenCount] = pass;
        forbiddenPasswords = newArray;
        forbiddenCount++;
    }

    static class UserAuthException extends Exception {
        public UserAuthException(String message) {
            super(message);
        }
    }
}
