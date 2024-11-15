import java.util.concurrent.Semaphore;

public class LeitoresEscritores {
    private static final Semaphore mutex = new Semaphore(1);
    private static final Semaphore db = new Semaphore(1);

    private static int leitores = 0;

    static class Leitor extends Thread {
        private final int id;

        public Leitor(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            try {
                mutex.acquire();
                leitores++;
                if (leitores == 1) {
                    db.acquire();
                }
                mutex.release();

                System.out.println("Leitor " + id + " está lendo o recurso compartilhado.");
                Thread.sleep((long) (Math.random() * 1000));

                mutex.acquire();
                leitores--;
                if (leitores == 0) {
                    db.release();
                }
                mutex.release();

                System.out.println("Leitor " + id + " terminou de ler.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static class Escritor extends Thread {
        private final int id;

        public Escritor(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            try {
                db.acquire();
                System.out.println("Escritor " + id + " está escrevendo no recurso compartilhado.");
                Thread.sleep((long) (Math.random() * 1000)); // Simula tempo de escrita
                System.out.println("Escritor " + id + " terminou de escrever.");
                db.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        int numLeitores = 5;
        int numEscritores = 3;

        for (int i = 0; i < numLeitores; i++) {
            new Leitor(i).start();
        }

        for (int i = 0; i < numEscritores; i++) {
            new Escritor(i).start();
        }
    }
}
