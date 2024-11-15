import java.util.Random;

public class LeitoresEscritoresMonitor {

    static class RecursoCompartilhado {
        private int leitoresAtivos = 0;
        private boolean escritorAtivo = false;

        public synchronized void iniciarLeitura(int leitorId) {
            while (escritorAtivo) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            leitoresAtivos++;
            System.out.println("Leitor " + leitorId + " começou a ler.");
        }

        public synchronized void finalizarLeitura(int leitorId) {
            leitoresAtivos--;
            System.out.println("Leitor " + leitorId + " terminou de ler.");
            if (leitoresAtivos == 0) {
                notifyAll();
            }
        }

        public synchronized void iniciarEscrita(int escritorId) {
            while (escritorAtivo || leitoresAtivos > 0) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            escritorAtivo = true;
            System.out.println("Escritor " + escritorId + " começou a escrever.");
        }

        public synchronized void finalizarEscrita(int escritorId) {
            escritorAtivo = false;
            System.out.println("Escritor " + escritorId + " terminou de escrever.");
            notifyAll();
        }
    }

    static class Leitor extends Thread {
        private final int id;
        private final RecursoCompartilhado recurso;

        public Leitor(int id, RecursoCompartilhado recurso) {
            this.id = id;
            this.recurso = recurso;
        }

        @Override
        public void run() {
            Random random = new Random();
            for (int i = 0; i < 10; i++) {
                recurso.iniciarLeitura(id);
                try {
                    Thread.sleep(random.nextInt(100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    recurso.finalizarLeitura(id);
                }
            }
        }
    }

    static class Escritor extends Thread {
        private final int id;
        private final RecursoCompartilhado recurso;

        public Escritor(int id, RecursoCompartilhado recurso) {
            this.id = id;
            this.recurso = recurso;
        }

        @Override
        public void run() {
            Random random = new Random();
            for (int i = 0; i < 10; i++) {
                recurso.iniciarEscrita(id);
                try {
                    Thread.sleep(random.nextInt(100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    recurso.finalizarEscrita(id);
                }
            }
        }
    }

    public static void main(String[] args) {
        RecursoCompartilhado recurso = new RecursoCompartilhado();

        int numLeitores = 5;
        int numEscritores = 3;

        for (int i = 0; i < numLeitores; i++) {
            new Leitor(i, recurso).start();
        }

        for (int i = 0; i < numEscritores; i++) {
            new Escritor(i, recurso).start();
        }
    }
}
