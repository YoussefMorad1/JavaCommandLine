import java.nio.file.Path;
import java.util.Scanner;
import java.nio.file.Files;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

public class Terminal {
    Parser parser;
    Path currentDirectory;

    public Terminal() {
        parser = new Parser();
        currentDirectory = Path.of(System.getProperty("user.dir"));
    }

    public static void showPrompt() {
        System.out.print("> ");
    }

    // Main loop of the program interface
    public void runInterface() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            showPrompt();
            String command = scanner.nextLine();
            if (parser.parse(command)) {
                chooseCommandAction();
            } // else do nothing (empty command)
        }
    }

    // Method that chooses which command to run
    public void chooseCommandAction() {
        String commandName = parser.getCommandName();
        String[] commandArgs = parser.getArgs();
        if (commandName.equals("echo")) {
            echo(commandArgs);
        } else if (commandName.equals("rm")) {
            rm(commandArgs);
        } else if (commandName.equals("exit")) {
            System.exit(0);
        }
    }


    // Command methods (called by chooseCommandAction)

    public void echo(String[] args) {
        for (String arg : args)
            System.out.print(arg + " ");
        System.out.println();
    }

    public void rm(String[] args) {
        String file = args[0];
        try {
            Path filePath = currentDirectory.resolve(file);
            Files.delete(filePath);
        } catch (NoSuchFileException e) {
            System.out.println("rm: cannot remove '" + file + "': No such file or directory");
        } catch (IOException e) {
            System.out.println("rm: cannot remove '" + file + "': Permission denied");
        }
    }

    // Entry point of the program
    public static void main(String[] args) {
        Terminal terminal = new Terminal();
        terminal.runInterface();
    }
}
