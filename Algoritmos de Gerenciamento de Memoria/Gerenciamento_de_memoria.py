import random
from typing import Callable, List, Dict
from bitarray import bitarray

class Processo:
    def __init__(self, id: str, tamanho: int):
        self.id = id
        self.tamanho = tamanho
        self.alocado = False
        self.inicio = -1

    def alocar(self, inicio: int):
        self.alocado = True
        self.inicio = inicio

    def desalocar(self):
        self.alocado = False
        self.inicio = -1

#2
class GerenciadorMemoria:
    def __init__(self, tamanho_memoria: int, processos: List[Processo]):
        self.tamanho_memoria = tamanho_memoria
        self.memoria = bitarray(tamanho_memoria) 
        self.memoria.setall(0)  #3
        self.processos = processos
        self.ponteiro_next_fit = 0
        self.quick_fit_lists: Dict[int, List[int]] = {}
        self.inicializar_quick_fit()

    def exibir_memoria(self):
        print("Estado da memória:", ''.join(map(str, self.memoria)))

    def is_livre(self, inicio: int, tamanho: int) -> bool:
        """Verifica se o espaço de memória está livre usando mapa de bits."""
        if inicio + tamanho > self.tamanho_memoria:
            return False
        return all(self.memoria[i] == 0 for i in range(inicio, inicio + tamanho))

    def alocar(self, processo: Processo, inicio: int):
        """Aloca o processo no espaço de memória usando mapa de bits."""
        for i in range(inicio, inicio + processo.tamanho):
            self.memoria[i] = 1 
        processo.alocar(inicio)
        print(f"Processo {processo.id} alocado em {inicio}")
        self.exibir_memoria() #3

    def desalocar(self, processo: Processo):
        """Desaloca o processo usando mapa de bits."""
        for i in range(processo.inicio, processo.inicio + processo.tamanho):
            self.memoria[i] = 0  # Marca como livre
        self.atualizar_quick_fit(processo.inicio, processo.tamanho)
        print(f"Processo {processo.id} desalocado.")
        processo.desalocar()
        self.exibir_memoria() #3

    def simular(self, nome_algoritmo: str, algoritmo: Callable[[Processo], bool]):
        self.memoria.setall(0)  # Limpa a memória antes de cada simulação
        self.ponteiro_next_fit = 0
        self.inicializar_quick_fit()
        print(f"\n\n{nome_algoritmo}")

        menor_tamanho_processo = min(processo.tamanho for processo in self.processos)

        for _ in range(30):
            processo = random.choice(self.processos)
            if processo.alocado:
                self.desalocar(processo)
            else:
                if not algoritmo(processo):
                    print(f"Erro: Não foi possível alocar o {processo.id}")
            self.exibir_memoria()
            print("Fragmentação externa:", self.calcular_fragmentacao_externa(menor_tamanho_processo))

    #1
    def alocar_first_fit(self, processo: Processo) -> bool:
        for i in range(self.tamanho_memoria - processo.tamanho + 1):
            if self.is_livre(i, processo.tamanho):
                self.alocar(processo, i)
                return True
        return False

    #1
    def alocar_next_fit(self, processo: Processo) -> bool:
        start = self.ponteiro_next_fit
        while True:
            if self.is_livre(self.ponteiro_next_fit, processo.tamanho):
                self.alocar(processo, self.ponteiro_next_fit)
                return True
            self.ponteiro_next_fit = (self.ponteiro_next_fit + 1) % self.tamanho_memoria
            if self.ponteiro_next_fit == start:
                break
        return False

    #1
    def alocar_best_fit(self, processo: Processo) -> bool:
        melhor_indice = -1
        menor_tamanho = float('inf')

        for i in range(self.tamanho_memoria - processo.tamanho + 1):
            if self.is_livre(i, processo.tamanho):
                tamanho_livre = self.calcular_bloco_livre(i)
                if tamanho_livre < menor_tamanho:
                    melhor_indice = i
                    menor_tamanho = tamanho_livre

        if melhor_indice != -1:
            self.alocar(processo, melhor_indice)
            return True
        return False

    #1
    def alocar_worst_fit(self, processo: Processo) -> bool:
        pior_indice = -1
        maior_tamanho = -1

        for i in range(self.tamanho_memoria - processo.tamanho + 1):
            if self.is_livre(i, processo.tamanho):
                tamanho_livre = self.calcular_bloco_livre(i)
                if tamanho_livre > maior_tamanho:
                    pior_indice = i
                    maior_tamanho = tamanho_livre

        if pior_indice != -1:
            self.alocar(processo, pior_indice)
            return True
        return False

    #1
    def alocar_quick_fit(self, processo: Processo) -> bool:
        lista_tamanho = self.quick_fit_lists.get(processo.tamanho, [])
        if lista_tamanho:
            indice = lista_tamanho.pop(0)
            self.alocar(processo, indice)
            return True
        return self.alocar_first_fit(processo)

    def desalocar(self, processo: Processo):
        for i in range(processo.inicio, processo.inicio + processo.tamanho):
            self.memoria[i] = 0
        self.atualizar_quick_fit(processo.inicio, processo.tamanho)
        print(f"Processo {processo.id} desalocado.")
        processo.desalocar()

    def is_livre(self, inicio: int, tamanho: int) -> bool:
        if inicio + tamanho > self.tamanho_memoria:
            return False
        return all(self.memoria[i] == 0 for i in range(inicio, inicio + tamanho))

    def calcular_bloco_livre(self, inicio: int) -> int:
        tamanho = 0
        for i in range(inicio, self.tamanho_memoria):
            if self.memoria[i] == 0:
                tamanho += 1
            else:
                break
        return tamanho


    #4
    def calcular_fragmentacao_externa(self, menor_tamanho_processo: int) -> int:
        fragmentacao = 0
        tamanho_atual = 0

        for i in range(self.tamanho_memoria):
            if self.memoria[i] == 0:
                tamanho_atual += 1
            else:
                if 0 < tamanho_atual < menor_tamanho_processo:
                    fragmentacao += tamanho_atual
                tamanho_atual = 0

        if 0 < tamanho_atual < menor_tamanho_processo:
            fragmentacao += tamanho_atual

        return fragmentacao

    def alocar(self, processo: Processo, inicio: int):
        for i in range(inicio, inicio + processo.tamanho):
            self.memoria[i] = 1
        processo.alocar(inicio)
        print(f"Processo {processo.id} alocado em {inicio}")

    def exibir_memoria(self):
        print("Estado da memória:", self.memoria.to01())  # Exibe como uma string de '0' e '1'

    def inicializar_quick_fit(self):
        self.quick_fit_lists = {i: [] for i in range(1, self.tamanho_memoria + 1)}

    def atualizar_quick_fit(self, inicio: int, tamanho: int):
        self.quick_fit_lists[tamanho].append(inicio)

if __name__ == "__main__":
    processos = [
        Processo("P1", 5), Processo("P2", 4), Processo("P3", 2),
        Processo("P4", 5), Processo("P5", 8), Processo("P6", 3),
        Processo("P7", 5), Processo("P8", 8), Processo("P9", 2),
        Processo("P10", 6)
    ]

    gerenciamento = GerenciadorMemoria(32, processos)
    for algoritmo, nome in [
        (gerenciamento.alocar_first_fit, "First Fit"),
        (gerenciamento.alocar_next_fit, "Next Fit"),
        (gerenciamento.alocar_best_fit, "Best Fit"),
        (gerenciamento.alocar_quick_fit, "Quick Fit"),
        (gerenciamento.alocar_worst_fit, "Worst Fit")
    ]:
        gerenciamento.simular(nome, algoritmo)
