import random
import time

class Processo:
    def __init__(self, pid, tempo_total_execucao):
        self.pid = pid
        self.tempo_total_execucao = tempo_total_execucao
        self.tp = 0 
        self.cp = 1  
        self.estado = "PRONTO"
        self.nes = 0  
        self.n_cpu = 0 

    def __str__(self):
        return (f"PID: {self.pid}, TP: {self.tp}, CP: {self.cp}, "
                f"EP: {self.estado}, NES: {self.nes}, N_CPU: {self.n_cpu}")

    def atualizar_estado(self, novo_estado):
        print(f"{self.pid} {self.estado} >>> {novo_estado}")
        time.sleep(2)
        self.estado = novo_estado
        if novo_estado == "EXECUTANDO":
            self.n_cpu += 1 

    def realizar_operacao_es(self):
        self.nes += 1
        self.atualizar_estado("BLOQUEADO")

    def restaurar_quantum(self):
        return 1000  

def salvar_tabela_processos(processos):
    with open("tabela_processos.txt", "w") as file:
        for processo in processos:
            file.write(str(processo) + "\n")

def simular():
    processos = [Processo(pid, tempo_exec) for pid, tempo_exec in [
        (0, 10000), (1, 5000), (2, 7000), (3, 3000), (4, 3000),
        (5, 8000), (6, 2000), (7, 5000), (8, 4000), (9, 10000)
    ]]
    processos_finalizados = 0
    quantum = 1000  

    while processos_finalizados < len(processos):
        for processo in processos:
            if processo.estado == "PRONTO":
                processo.atualizar_estado("EXECUTANDO")
                salvar_tabela_processos(processos)
                ciclos_restantes = quantum

                while ciclos_restantes > 0 and processo.tp < processo.tempo_total_execucao:
                    if random.random() < 0.01:
                        processo.realizar_operacao_es()
                        salvar_tabela_processos(processos)
                        break

                    processo.tp += 1
                    processo.cp = processo.tp + 1
                    ciclos_restantes -= 1

                if processo.tp >= processo.tempo_total_execucao:
                    print(f"Processo {processo.pid} terminou. Dados finais: {processo}")
                    time.sleep(2)
                    processo.estado = "FINALIZADO"
                    processos_finalizados += 1
                elif processo.estado == "EXECUTANDO":
                    processo.atualizar_estado("PRONTO")
                    salvar_tabela_processos(processos)
                
            elif processo.estado == "BLOQUEADO":
                if random.random() < 0.3:
                    processo.atualizar_estado("PRONTO")
                    salvar_tabela_processos(processos)

if __name__ == "__main__":
    simular()
