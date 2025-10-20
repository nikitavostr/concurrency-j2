package ru.nsu.nvostrikov;

import java.util.*;

public class  Runner {

    private volatile long operationCount = 0;

    private final CustomLinkedList<String> customStorage;
    private final List<String> defaultStorage;

    private final int pause;
    private final int threadCount;
    private final boolean customMode;

    private final List<Worker> workers;

    public Runner(int pause, int threadCount, boolean customMode) {
        this.pause = pause;
        this.threadCount = threadCount;
        this.customMode = customMode;

        if (customMode) {
            customStorage = new CustomLinkedList<>();
            defaultStorage = null;
        } else {
            defaultStorage = Collections.synchronizedList(new ArrayList<>());
            customStorage = null;
        }

        this.workers = new ArrayList<>();
    }

    public static void main(String[] args) {
        int threads = Integer.parseInt(args[0]);
        int delay = Integer.parseInt(args[1]);
        boolean useCustom = Boolean.parseBoolean(args[2]);

        new Runner(delay, threads, useCustom).launch();
    }

    private void launch() {
        for (int i = 0; i < threadCount; i++) {
            Worker w = new Worker(i + 1);
            workers.add(w);
            w.start();
        }

        try (Scanner input = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                String line = input.nextLine();
                if (line.isEmpty()) {
                    printState();
                } else {
                    handleInput(line);
                }
            }
        }
    }

    private void handleInput(String text) {
        if (text.length() <= 80) {
            store(text);
        } else {
            for (int i = 0; i < text.length(); i += 80) {
                int end = Math.min(i + 80, text.length());
                store(text.substring(i, end));
            }
        }
    }

    private void store(String s) {
        if (customMode) {
            customStorage.push(s);
        } else {
            defaultStorage.add(0, s);
        }
    }

    private class Worker extends Thread {
        private final int number;
        private volatile boolean active = true;

        Worker(int number) {
            this.number = number;
        }

        @Override
        public void run() {
            System.out.println("Worker #" + number + " started");
            while (active && !isInterrupted()) {
                try {
                    if (customMode) {
                        performCustomStep();
                    } else {
                        performStandardStep();
                    }
                    if (pause > 0) {
                        Thread.sleep(pause);
                    }
                } catch (InterruptedException e) {
                    interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Worker " + number + " crashed: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        private void performCustomStep() throws InterruptedException {
            synchronized (customStorage) {
                if (customStorage.size() < 2) {
                    return;
                }

                boolean changed = false;
                for (int i = 0; i < customStorage.size() - 1 && active; i++) {
                    operationCount++;
                    String first = customStorage.get(i);
                    String second = customStorage.get(i + 1);
                    if (first.compareTo(second) > 0) {
                        customStorage.swap(i);
                        changed = true;
                        if (pause > 0) {
                            Thread.sleep(pause);
                        }
                    }
                }
                if (!changed && active) {
                    Thread.sleep(pause);
                }
            }
        }

        private void performStandardStep() throws InterruptedException {
            synchronized (defaultStorage) {
                if (defaultStorage.size() < 2) {
                    return;
                }

                boolean changed = false;
                for (int i = 0; i < defaultStorage.size() - 1 && active; i++) {
                    operationCount++;
                    String a = defaultStorage.get(i);
                    String b = defaultStorage.get(i + 1);
                    if (a.compareTo(b) > 0) {
                        defaultStorage.set(i, b);
                        defaultStorage.set(i + 1, a);
                        changed = true;
                        if (pause > 0) {
                            Thread.sleep(pause);
                        }
                    }
                }
                if (!changed && active) {
                    Thread.sleep(pause);
                }
            }
        }
    }

    private void printState() {
        if (customMode) {
            for (String entry : customStorage) {
                System.out.println(entry);
            }
        } else {
            synchronized (defaultStorage) {
                for (String entry : defaultStorage) {
                    System.out.println(entry);
                }
            }
        }
        System.out.println("Total operations: " + operationCount);
    }
}