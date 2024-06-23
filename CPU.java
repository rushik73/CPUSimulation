import java.io.*;
import java.util.*;


public class CPU {

    //Constants for the simulated CPU's memory management and interrupt handling
    private static final int USER_MEMORY_SIZE = 1000;
    private static final int TIMER_INTERRUPT_ADDRESS = 1000;
    private static final int SYSTEM_CALL_ADDRESS = 1500;
    private static final int SYSTEM_MEMORY_SIZE = 2000;
    private int systemSP = SYSTEM_MEMORY_SIZE - 1;


    // Registers
    private int PC = 0;
    private int SP = USER_MEMORY_SIZE - 1;
    private int IR;
    private int AC;
    private int X;
    private int Y;
    private boolean interruptsEnabled = true;
    private boolean inSystemMode = false;
    private boolean running = true;


    private Memory memory;
    private int timerCountdown;
    private static int TIMER_INTERRUPT_PERIOD;

    private Process memoryProcess;
    private PrintWriter memoryInput;
    private BufferedReader memoryOutput;

    public CPU(Memory memoryProgramPath, int timerPeriod) {
        try {
            // Start the Memory process
            memoryProcess = Runtime.getRuntime().exec("java Memory " + memoryProgramPath);
            memoryInput = new PrintWriter(new OutputStreamWriter(memoryProcess.getOutputStream()), true);
            memoryOutput = new BufferedReader(new InputStreamReader(memoryProcess.getInputStream()));


            TIMER_INTERRUPT_PERIOD = timerPeriod; // Assuming TIMER_INTERRUPT_PERIOD is passed when CPU is created
            this.timerCountdown = TIMER_INTERRUPT_PERIOD;
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }



    public void run() {
        while (running) {
            // Timer interrupt check
            if (!inSystemMode && --timerCountdown <= 0) {
                triggerInterrupt(TIMER_INTERRUPT_ADDRESS);
                timerCountdown = TIMER_INTERRUPT_PERIOD;
            }


            // Fetch instruction
            IR = memory.read(PC++);
            if (IR == 50) { // End instruction
                running = false;
                break;
            }


            executeInstructions(IR);
        }
    }


    private void executeInstructions(int opcode) {
        // Check for illegal memory access if in user mode
        if (!inSystemMode) {
            checkMemoryAccess(opcode);
        }


        int operand = needsOperand(opcode) ? fetchOperand() : -1;


        switch (opcode) {
            case 1: // Load value
                AC = operand;
                break;
            case 2: // Load addr
                AC = memory.read(operand);
                break;
            case 3: // LoadInd addr
                AC = memory.read(memory.read(operand));
                break;
            case 4: // LoadIdxX addr
                AC = memory.read(operand + X);
                break;
            case 5: // LoadIdxY addr
                AC = memory.read(operand + Y);
                break;
            case 6: // LoadSpX
                AC = memory.read(SP + X);
                break;
            case 7: // Store addr
                memory.write(operand, AC);
                break;
            case 8: // Get random int
                AC = new Random().nextInt(100) + 1;
                break;
            case 9: // Put port
                if (operand == 1) {
                    System.out.print(AC);
                } else if (operand == 2) {
                    System.out.print((char) AC);
                }
                break;
            case 10: // AddX
                AC += X;
                break;
            case 11: // AddY
                AC += Y;
                break;
            case 12: // SubX
                AC -= X;
                break;
            case 13: // SubY
                AC -= Y;
                break;
            case 14: // CopyToX
                X = AC;
                break;
            case 15: // CopyFromX
                AC = X;
                break;
            case 16: // CopyToY
                Y = AC;
                break;
            case 17: // CopyFromY
                AC = Y;
                break;
            case 18: // CopyToSp
                SP = AC;
                break;
            case 19: // CopyFromSp
                AC = SP;
                break;
            case 20: // Jump addr
                PC = operand;
                break;
            case 21: // JumpIfEqual addr
                if (AC == 0) {
                    PC = operand;
                }
                break;
            case 22: // JumpIfNotEqual addr
                if (AC != 0) {
                    PC = operand;
                }
                break;
            case 23: // Call addr
                memory.write(--SP, PC);
                PC = operand;
                break;
            case 24: // Ret
                PC = memory.read(SP++);
                break;
            case 25: // IncX
                X++;
                break;
            case 26: // DecX
                X--;
                break;
            case 27: // Push
                memory.write(--SP, AC);
                break;
            case 28: // Pop
                AC = memory.read(SP++);
                break;
            case 29: // Int
                if (interruptsEnabled) {
                    triggerInterrupt(SYSTEM_CALL_ADDRESS);
                }
                break;
            case 30: // IRet
                if (inSystemMode) {
                    returnFromInterrupt();
                }
                break;
            case 50: // End
                running = false;
                break;
            default:
                throw new IllegalArgumentException("Unknown opcode: " + opcode);
        }


    }


    private boolean needsOperand(int instruction) {
        // These instructions need an operand
        return instruction == 1 || // Load value
                instruction == 2 || // Load addr
                instruction == 3 || // LoadInd addr
                instruction == 4 || // LoadIdxX addr
                instruction == 5 || // LoadIdxY addr
                instruction == 6 || // LoadSpX
                instruction == 7 || // Store addr
                instruction == 9 || // Put port
                instruction == 20 || // Jump addr
                instruction == 21 || // JumpIfEqual addr
                instruction == 22 || // JumpIfNotEqual addr
                instruction == 23;   // Call addr
    }


    // This is a new method to check for memory access violations
    private void checkMemoryAccess(int opcode) {
        if (isMemoryAccess(opcode)) {
            int address = memory.read(PC); // Assuming the address is the next value in memory
            if (address >= USER_MEMORY_SIZE && address < SYSTEM_MEMORY_SIZE) {
                throw new SecurityException("Memory access violation at address " + address);
            }
        }
    }


    private boolean isMemoryAccess(int opcode) {
        // Define which opcodes are considered as memory access opcodes
        return Arrays.asList(2, 3, 4, 5, 6, 7).contains(opcode);
    }


    private int fetchOperand() {
        // This method assumes that the next value in memory is the operand
        return memory.read(PC++);
    }




    private void triggerInterrupt(int interruptAddress) {
        // Save only the PC and user SP to the system stack
        memory.write(--systemSP, PC);
        memory.write(--systemSP, SP);


        // Switch to system mode
        SP = systemSP;
        PC = interruptAddress;


        inSystemMode = true;
        interruptsEnabled = false;
    }


    private void returnFromInterrupt() {
        // Restore the PC and user SP from the system stack
        SP = memory.read(systemSP++);
        PC = memory.read(systemSP++);


        // Reset the system stack pointer to the end of system memory
        systemSP = SYSTEM_MEMORY_SIZE - 1;


        inSystemMode = false;
        interruptsEnabled = true;
    }


    public static void main(String[] args) {
        // Check if exactly two arguments (file path and timer period) are provided
        if (args.length != 2) {
            System.err.println("Usage: java Project1 <path to input file> <timer period>");
            System.exit(1);
        }

        // Extract the file path from the first command line argument
        String filePath = args[0];
        // Extract the timer period from the second command line argument and convert it to an integer
        int timerPeriod = Integer.parseInt(args[1]);

        // Instantiate the Memory class with the file path to load the program into memory
        Memory memory = new Memory(filePath);


        // Create CPU instance, assuming CPU constructor takes filepath and timer period
        CPU cpu = new CPU(memory, timerPeriod); // Adjust the timer period as needed
        cpu.run();
    }


    }

// Defines a Memory class simulating the memory component of a computer system.
    class Memory {

    // Memory array to simulate a simple computer's memory with 2000 integer slots.
    private static final int[] MEMORY = new int[2000];

    // Constructor that loads a program from a file into memory.
    public Memory(String file) {
        load(file);
    }


    public static void main(String[] args) {
        //Checks to see if reading and parsing the input from the sample text files and then displaying the loaded values from memory.
        if (args.length != 1) {
            System.err.println("Usage: java Memory <path to input file>");
            System.exit(1);
        }

        // Reads the file path from the command line argument.
        String file = args[0];
        load(file);

        // Prepares to read commands from standard input (stdin).
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        String line;

        try {
            // Continuously reads lines from stdin and processes commands until EOF or an exception occurs.
            while ((line = stdin.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length == 0) {
                    continue;
                }

                switch (parts[0].toUpperCase()) {
                    case "READ":// Handles the READ command to write a value to a specified memory address.
                        if (parts.length == 2) {
                            int address = Integer.parseInt(parts[1]);
                            System.out.println(read(address));
                        } else {
                            System.err.println("Error: Invalid READ command");
                        }
                        break;
                    case "WRITE": // Handles the WRITE command to write a value to a specified memory address.
                        if (parts.length == 3) {
                            int address = Integer.parseInt(parts[1]);
                            int value = Integer.parseInt(parts[2]);
                            write(address, value);
                            System.out.println("OK"); // Acknowledge the write operation
                        } else {
                            System.err.println("Error: Invalid WRITE command");
                        }
                        break;
                    default:
                        System.err.println("Error: Unknown command");
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading from stdin: " + e.getMessage());
            System.exit(1);
        }
    }

        // Loads a program from a file into memory.
    private static void load(String file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int address = 0;// Initializes the memory address where the program will be loaded.

            // Reads each line from the file.
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("//")) {
                    continue;
                }

                // If the line starts with '.', it sets a specific memory address.
                if (line.startsWith(".")) {
                    address = Integer.parseInt(line.substring(1).trim());
                } else {
                    // Otherwise, parses and stores the integer value at the current address in memory.
                    String[] parts = line.split("\\s+");
                    if (parts.length > 0 && parts[0].matches("\\d+")) {
                        MEMORY[address++] = Integer.parseInt(parts[0]);
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            // Catches and handles exceptions related to file reading and number parsing.
            System.err.println("Error loading program into memory: " + e.getMessage());
            System.exit(1);
        }
    }

        // Reads and returns the value at the specified memory address.
    public static int read(int address) {
        return MEMORY[address];
    }

        // Writes a value to the specified memory address.v
    public  static void write(int address, int data) {
        MEMORY[address] = data;
    }
}


