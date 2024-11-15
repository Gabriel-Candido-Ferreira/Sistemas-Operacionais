import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class LeitoresEscritoresTrocaDeMensagens {
    static class Operacao {
        enum Tipo {
            LEITURA, ESCRITA
        }

        private final Tipo tipo;
        private final int id;

        public Operacao(Tipo tipo, int id) {
            this.tipo = tipo;
            this.id = id;
        }

        public Tipo getTipo() {
            return tipo;
        }

        public int getId() {
            return id;
        }
    }

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
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("Escritor " + escritorId + " terminou de escrever.");
        }
    }

    static class ProcessadorOperacoes implements Runnable {
        private final BlockingQueue<Operacao> filaOperacoes;
        private final RecursoCompartilhado recurso;

        public ProcessadorOperacoes(BlockingQueue<Operacao> filaOperacoes, RecursoCompartilhado recurso) {
            this.filaOperacoes = filaOperacoes;
            this.recurso = recurso;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    Operacao operacao = filaOperacoes.take();
                    if (operacao.getTipo() == Operacao.Tipo.LEITURA) {
                        recurso.ler(operacao.getId());
                    } else {
                        recurso.escrever(operacao.getId());
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Processador de Operações interrompido.");
            }
        }
    }

    public static void main(String[] args) {
        RecursoCompartilhado recurso = new RecursoCompartilhado();

        BlockingQueue<Operacao> filaOperacoes = new ArrayBlockingQueue<>(50);

        Thread processador = new Thread(new ProcessadorOperacoes(filaOperacoes, recurso));
        processador.start();

        Runnable gerarLeitores = () -> {
            Random random = new Random();
            for (int i = 0; i < 10; i++) {
                try {
                    filaOperacoes.put(new Operacao(Operacao.Tipo.LEITURA, i));
                    Thread.sleep(random.nextInt(200));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        };

        Runnable gerarEscritores = () -> {
            Random random = new Random();
            for (int i = 0; i < 5; i++) {
                try {
                    filaOperacoes.put(new Operacao(Operacao.Tipo.ESCRITA, i));
                    Thread.sleep(random.nextInt(300));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        };

        new Thread(gerarLeitores, "Gerador de Leitores").start();
        new Thread(gerarEscritores, "Gerador de Escritores").start();
    }
}
