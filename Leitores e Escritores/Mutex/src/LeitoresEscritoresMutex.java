import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class LeitoresEscritoresMutex {
    static class RecursoCompartilhado {
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition podeEscrever = lock.newCondition();
        private final Condition podeLer = lock.newCondition();
        private int leitoresAtivos = 0;
        private boolean escritorAtivo = false;

        public void iniciarLeitura(int leitorId) {
            lock.lock();
            try {
                while (escritorAtivo) {
                    podeLer.await();
                }
                leitoresAtivos++;
                System.out.println("Leitor " + leitorId + " começou a ler.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }

        public void finalizarLeitura(int leitorId) {
            lock.lock();
            try {
                leitoresAtivos--;
                System.out.println("Leitor " + leitorId + " terminou de ler.");
                if (leitoresAtivos == 0) {
                    podeEscrever.signal();
                }
            } finally {
                lock.unlock();
            }
        }

        public void iniciarEscrita(int escritorId) {
            lock.lock();
            try {
                while (escritorAtivo || leitoresAtivos > 0) {
                    podeEscrever.await();
                }
                escritorAtivo = true;
                System.out.println("Escritor " + escritorId + " começou a escrever.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }

        public void finalizarEscrita(int escritorId) {
            lock.lock();
            try {
                escritorAtivo = false;
                System.out.println("Escritor " + escritorId + " terminou de escrever.");
                podeEscrever.signal();
                podeLer.signalAll();
            } finally {
                lock.unlock();
            }
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
            while (true) {
                recurso.iniciarLeitura(id);
                try {
                    Thread.sleep((long) (Math.random() * 1000)); // Simula tempo de leitura
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
            while (true) {
                recurso.iniciarEscrita(id);
                try {
                    Thread.sleep((long) (Math.random() * 1000));
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
        int numEscritores = 2;

        for (int i = 0; i < numLeitores; i++) {
            new Leitor(i, recurso).start();
        }

        for (int i = 0; i < numEscritores; i++) {
            new Escritor(i, recurso).start();
        }
    }
}
