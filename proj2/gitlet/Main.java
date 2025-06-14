package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        // Allow init to run even if .gitlet does not exist
        if (firstArg.equals("init")) {
            Repository repo = new Repository();
            repo.init();
            return;
        }

        // For all other commands, require a Gitlet repo
        if (!Repository.GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }

        Repository repo = new Repository();
        switch(firstArg) {
            case "add":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                repo.add(args[1]);
                break;
            case "commit":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                repo.commit(args[1]);
                break;
            case "rm":
                break;
            case "log":
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                repo.log();
                break;
            case "global-log":
                break;
            case "find":
                break;
            case "status":
                break;
            case "checkout":
                if (args.length == 3 && args[1].equals("--")) {
                    repo.checkout(args[2]);
                } else if (args.length == 4 && args[2].equals("--")) {
                    repo.checkout(args[1], args[3]);
                } else if (args.length == 2){
                    break;
                } else{
                    break;
                }
        }
    }
}
