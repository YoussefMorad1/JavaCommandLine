import java.io.File;
import java.nio.file.*;
import java.nio.file.*;
import java.util.*;
import java.io.IOException;

public class Terminal {
    Parser parser;
    Path currentDirectory;
    private ArrayList<String> commandHistory;

    public Terminal() {
        parser = new Parser();
        currentDirectory = Path.of(System.getProperty("user.dir"));
        commandHistory = new ArrayList<>();
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
                String parsedCommand = parser.getCommandName();
                if (isCommandAvailable(parsedCommand)) {
                    commandHistory.add(command); // Add the command to the history
                    chooseCommandAction();
                } else {
                    System.out.println(parsedCommand + ": command not found");
                }
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
        } else if (commandName.equals("rmdir")) {
                rmdir(commandArgs);
        } else if (commandName.equals("mkdir")) {
                mkdir(commandArgs);
        } else if (commandName.equals("cat")) {
            cat(commandArgs);
        } else if (commandName.equals("pwd")) {
            pwd();
        } else if (commandName.equals("touch")) {
            touch(commandArgs);
        } else if (commandName.equals("cd")) {
            cd(commandArgs);
        } else if (commandName.equals("history")) {
            history();
        }else if(commandName.equals("ls")) {
            ls(commandArgs);
        }else if(commandName.equals("help")){
            help();
        }else if (commandName.equals("exit")) {
            System.exit(0);
        }
    }

    public void help(){
        System.out.println("1.help     -> prints the list of supported commands");
        System.out.println("2.echo     -> prints the arguments passed to it");
        System.out.println("3.pwd      -> prints the current working directory");
        System.out.println("4.cd       -> changes the current working directory");
        System.out.println("5.ls       -> lists the contents of the current directory");
        System.out.println("6.ls -r    -> lists the contents of the current directory in reverse order");
        System.out.println("7.cp       -> copies a file to a new location");
        System.out.println("8.cp -r    -> copies a directory to a new location");
        System.out.println("9.history  -> prints the last 5 commands");
        System.out.println("10.mkdir   -> creates a new directory");
        System.out.println("11.rmdir   -> removes an empty directory");
        System.out.println("12.touch   -> creates a new file");
        System.out.println("13.rm      -> removes a file");
        System.out.println("14.cat     -> prints the contents of a file");
        System.out.println("15.exit    -> exits the terminal");
    }



    /**
     * Checks if a command is available in the list of supported commands.
     *
     * @param command The command to check for availability
     * @return true if the command is available, false otherwise
     */
    private boolean isCommandAvailable(String command) {
        List<String> availableCommands = Arrays.asList("echo", "pwd", "cd", "ls",
                                                      "mkdir", "rmdir", "touch", "cp", "rm",
                                                       "cat", "exit", "history");
        return availableCommands.contains(command);
    }

    // Command methods (called by chooseCommandAction)

    /**
     * echo command: prints the arguments passed to it
     *
     * @param args The array of arguments to print
     */
    public void echo(String[] args) {
        for (String arg : args)
            System.out.print(arg + " ");
        System.out.println();
    }

    public void ls(String []r){
                File[] contents = currentDirectory.toFile().listFiles();
        try {
            if (r.length > 0 && r[0].equals("-r")) {
                Arrays.sort(contents, Comparator.reverseOrder());
            }
            else if(r.length>1){
                System.out.println("ls: too many arguments (currently only supports one argument)");
                return;
            }
            else if(r.length>0 && !r[0].equals("-r")){
                System.out.println("ls: invalid argument");
                return;
            }
            for (File file : contents) {
                System.out.println(file.getName());
            }
        }
        catch (NullPointerException e){
            System.out.println("No such directory");
        }
        catch (SecurityException e){
            System.out.println("Permission denied");
        }
    }

    public boolean isEmptyDir(File dir){
        String[] contents = dir.list();
        return contents.length == 0 || contents == null;
    }

    public void rmdir(String[] args){
        if(args.length > 1)
        {
            System.out.println("invalid number of arguments");
            return;
        }

        String dir = args[0];
        if(dir.equals("*")){
            File[] contents = currentDirectory.toFile().listFiles();
            for(File directory : contents){
                if(directory.isDirectory() && isEmptyDir(directory)){
                    try {
                        Files.delete(directory.toPath());
                    } catch (IOException e) {
                        System.out.println("rmdir: failed to remove '" + directory.getName() + "': Permission denied");
                    }
                }
            }
        }
        else{
            try {
                Path dirPath = currentDirectory.resolve(dir);
                if(!Files.isDirectory(dirPath)){
                    System.out.println("rmdir: failed to remove '" + dir + "': Not a directory");
                }
                else if(!isEmptyDir(dirPath.toFile())){
                    System.out.println("rmdir: failed to remove '" + dir + "': Directory not empty");
                }
                else
                    Files.delete(dirPath);
            } catch (NoSuchFileException e) {
                System.out.println("rmdir: failed to remove '" + dir + "': No such file or directory");
            } catch (IOException e) {
                System.out.println("rmdir: failed to remove '" + dir + "': Permission denied");
            }catch (InvalidPathException e){
                System.out.println("rmdir: failed to remove '" + dir + "': Invalid Path");
            }
        }
    }

    public void mkdir(String[] args){
        if(args.length < 1)
        {
            System.out.println("mkkdir: needs at least one argument");
            return;
        }
        try{
            for(String dir : args){
                Path DirPath = currentDirectory.resolve(dir);
                File directory = new File(DirPath.toString());
                if(directory.exists())
                            System.out.println("\"Directory already exists at: \"" + directory.toPath());
                else{
                    directory.mkdir();
                }
            }
        } catch (InvalidPathException e){
            System.out.println("mkdir: failed to create directory '" + args[0] + "': Invalid path");
        } catch (SecurityException e){
            System.out.println("mkdir: failed to create directory '" + args[0] + "': Permission denied");
        }
    }

    /**
     * rm command: removes a file
     *
     * @param args The array of file paths to be removed (currently only supports one file)
     */
    public void rm(String[] args) {
        if (args.length == 1) {
            String file = args[0];
            try {
                Path filePath = currentDirectory.resolve(file);
                // Check if the path points to a regular file before deleting
                if (Files.isRegularFile(filePath)) {
                    Files.delete(filePath);
                } else {
                    System.out.println("rm: cannot remove '" + file + "': Not a regular file");
                }
            } catch (NoSuchFileException e) {
                System.out.println("rm: cannot remove '" + file + "': No such file or directory");
            } catch (IOException e) {
                System.out.println("rm: cannot remove '" + file + "': Permission denied");
            } catch (InvalidPathException e) {
                System.out.println("rm: failed to remove '" + args[0] + "': Invalid path");
            }
        } else {
            System.out.println("rm: Invalid number of arguments");
        }
    }

    /**
     * cat command: prints the content of a file or concatenates the content of two files and prints it
     *
     * @param args The array of file paths to be printed or concatenated (one or two files)
     */
    public void cat(String[] args) {
        if(args.length == 1 || args.length == 2){
            // Iterate over each argument to process
            for (String arg : args) {
                try {
                    Path filePath = currentDirectory.resolve(arg);
                    System.out.println(Files.readString(filePath));  // Read and print the content of the file
                } catch (NoSuchFileException e) {
                    System.out.println("cat: " + arg + ": No such file or directory");
                } catch (IOException e) {
                    System.out.println("cat: " + arg + ": Error reading the file");
                } catch (InvalidPathException e) {
                    System.out.println("cat: " + arg + ": Invalid path");
                }
            }
        }else{
            System.out.println("cat: Invalid number of arguments");
        }
    }


    /**
     * history command: displays an enumerated list of past commands
     */
    public void history(){
        if (commandHistory.isEmpty()) {
            System.out.println("No commands in history");
        } else {
            for (int i = 0; i < commandHistory.size(); i++) {
                System.out.println((i + 1) + " " + commandHistory.get(i));
            }
        }
    }

    /**
     * pwd command: prints the current directory
     */
    public void pwd() {
        currentDirectory = currentDirectory.normalize();
        System.out.println(currentDirectory);
    }

    /**
     * touch command: creates a file
     *
     * @param args The array of paths of files to create (currently only supports one file)
     */
    public void touch(String[] args) {
        // Make sure only one argument is passed
        if (args.length == 0) {
            System.out.println("touch: missing file operand");
            return;
        } else if (args.length > 1) {
            System.out.println("touch: too many arguments (currently only supports one file)");
            return;
        }
        // If one argument is passed
        String file = args[0];
        try {
            // Get the path of the file and create it
            Path filePath = currentDirectory.resolve(file);
            Files.createFile(filePath);
        } catch (FileAlreadyExistsException e) {
            // If the file already exists, do nothing (real touch simulation)
        } catch (IOException e) {
            System.out.println("touch: cannot create file '" + file + "': Permission denied or invalid path/file name");
        }
    }

    /**
     * cd command: changes the current directory, or goes to the home directory if no arguments are passed
     *
     * @param args The arguments array, which holds the path of the directory to change to (must have 0 or 1 arguments)
     */
    public void cd(String[] args) {
        // Make sure 0 or 1 arguments are passed
        if (args.length > 1) {
            System.out.println("cd: too many arguments");
            return;
        }

        // If no arguments are passed
        if (args.length == 0) {
            // Go to the home directory
            Path homeDir = Path.of(System.getProperty("user.home"));
            if (!Files.isDirectory(homeDir)) { // if home directory doesn't exist
                // print error message
                System.out.println("cd: cannot change directory to home directory: No such directory");
            } else {
                currentDirectory = homeDir;
            }
            return;
        }

        // If one argument is passed
        String dir = args[0];
        Path dirPath = currentDirectory.resolve(dir);
        if (Files.isDirectory(dirPath)) { // if the directory exists
            currentDirectory = dirPath; // change the current directory
        } else {
            // print error message
            System.out.println("cd: cannot change directory '" + dir + "': No such directory");
        }
    }

    /**
     * Entry point of the program
     * @param args The arguments passed to the program (not used)
     */
    public static void main(String[] args) {
        Terminal terminal = new Terminal();
        terminal.runInterface();
    }
}
