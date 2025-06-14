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
        if (firstArg.equals("add")) {
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
            repo.add(args[1]);

        } else if (firstArg.equals("commit")) {
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
            repo.commit(args[1]);

        } else if (firstArg.equals("rm")) {
            // stub for rm

        } else if (firstArg.equals("log")) {
            if (args.length != 1) {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
            repo.log();

        } else if (firstArg.equals("global-log")) {
            // stub for global-log

        } else if (firstArg.equals("find")) {
            // stub for find

        } else if (firstArg.equals("status")) {
            // stub for status

        } else if (firstArg.equals("checkout")) {
            if (args.length == 3 && args[1].equals("--")) {
                repo.checkout(args[2]);
            } else if (args.length == 4 && args[2].equals("--")) {
                repo.checkout(args[1], args[3]);
            } else if (args.length == 2) {
                // stub for branch checkout
            } else {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }

        } else {
            System.out.println("No command with that name exists.");
            System.exit(0);
        }
    }
}
