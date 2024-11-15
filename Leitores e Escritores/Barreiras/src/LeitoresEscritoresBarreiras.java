import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class LeitoresEscritoresBarreiras {

    static class RecursoCompartilhado {
        public void ler(int leitorId) {
            System.out.println("Leitor " + leitorId + " está lendo o recurso.");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("Leitor " + leitorId + " terminou de ler.");
        }

        public void escrever(int escritorId) {
            System.out.println("Escritor " + escritorId + " está escrevendo no recurso.");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("Escritor " + escritorId + " terminou de escrever.");
        }
    }

    static class Leitor extends Thread {
        private final int id;
        private final RecursoCompartilhado recurso;
        private final CyclicBarrier barreira;

        public Leitor(int id, RecursoCompartilhado recurso, CyclicBarrier barreira) {
            this.id = id;
            this.recurso = recurso;
            this.barreira = barreira;
        }

        @Override
        public void run() {
            for (int i = 0; i < 10; i++) {
                recurso.ler(id);
                aguardarBarreira();
            }
        }

        private void aguardarBarreira() {
            try {
                barreira.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }

    static class Escritor extends Thread {
        private final int id;
        private final RecursoCompartilhado recurso;
        private final CyclicBarrier barreira;

        public Escritor(int id, RecursoCompartilhado recurso, CyclicBarrier barreira) {
            this.id = id;
            this.recurso = recurso;
            this.barreira = barreira;
        }

        @Override
        public void run() {
            for (int i = 0; i < 10; i++) {
                recurso.escrever(id);
                aguardarBarreira();
            }
        }

        private void aguardarBarreira() {
            try {
                barreira.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        RecursoCompartilhado recurso = new RecursoCompartilhado();

        int numLeitores = 3;
        int numEscritores = 2;
        int totalThreads = numLeitores + numEscritores;

        CyclicBarrier barreira = new CyclicBarrier(totalThreads, () -> {
            System.out.println("\nTodos os leitores e escritores chegaram à barreira. Próximo ciclo!\n");
        });

        for (int i = 0; i < numLeitores; i++) {
            new Leitor(i, recurso, barreira).start();
        }

        for (int i = 0; i < numEscritores; i++) {
            new Escritor(i, recurso, barreira).start();
        }
    }
}
