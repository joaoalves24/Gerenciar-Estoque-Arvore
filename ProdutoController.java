package com.faculdade.arvore.controller;

import com.faculdade.arvore.model.ItemFormula;
import com.faculdade.arvore.service.ProdutoService;
import com.faculdade.arvore.tree.ArvoreService;
import com.faculdade.arvore.tree.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/produtos")
@CrossOrigin(origins = "*")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private ArvoreService arvoreService;

    @PostMapping("/cadastrar")
    public ItemFormula cadastrar(@RequestBody ItemFormula item) {
        produtoService.cadastrarItem(item.getNome(), item.getPai(), item.getQuantidade(), item.getPreco());
        return item;
    }

    @GetMapping("/listar")
    public List<ItemFormula> listar() {
        return produtoService.listarTodos();
    }

    @DeleteMapping("/deletar/{nome}")
    public Map<String, Object> deletar(@PathVariable String nome) {
        boolean sucesso = produtoService.deletarItem(nome);
        Map<String, Object> resp = new HashMap<>();
        resp.put("sucesso", sucesso);
        resp.put("mensagem", sucesso ? "Item removido" : "Item não encontrado");
        return resp;
    }

    @PutMapping("/atualizar-preco")
    public Map<String, Object> atualizarPreco(@RequestBody Map<String, Object> req) {
        String nome = (String) req.get("nome");
        double novoPreco = Double.parseDouble(req.get("preco").toString());
        try {
            double novoTotal = arvoreService.implodir(nome, novoPreco);
            Map<String, Object> resp = new HashMap<>();
            resp.put("sucesso", true);
            resp.put("novoCustoTotal", novoTotal);
            return resp;
        } catch (Exception e) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("sucesso", false);
            resp.put("erro", e.getMessage());
            return resp;
        }
    }

    @GetMapping("/arvore")
    public Map<String, Object> getArvore() {
        TreeNode raiz = arvoreService.buildTree();
        Map<String, Object> resp = new HashMap<>();
        resp.put("raiz", treeToMap(raiz));
        resp.put("custoTotal", arvoreService.calcularCusto(raiz));
        return resp;
    }

    @GetMapping("/explodir")
    public Map<String, Object> explodir(@RequestParam int quantidade) {
        TreeNode raiz = arvoreService.buildTree();
        Map<String, Integer> comps = arvoreService.explodir(raiz, quantidade);
        double valorTotal = arvoreService.calcularValorAquisicao(raiz, quantidade);
        Map<String, Object> resp = new HashMap<>();
        resp.put("componentes", comps);
        resp.put("valorTotal", valorTotal);
        return resp;
    }

    @PostMapping("/carregar-teste")
    public Map<String, Object> carregarTeste(@RequestBody Map<String, String> req) {
        String teste = req.get("teste");
        produtoService.listarTodos().clear();
        List<ItemFormula> itens = new ArrayList<>();

        if ("1".equals(teste)) {
            itens.add(new ItemFormula("Caneta", null, 1, 2.50));
            itens.add(new ItemFormula("Tinta", "Caneta", 1, 0.80));
            itens.add(new ItemFormula("Pigmento", "Tinta", 1, 0.30));
            itens.add(new ItemFormula("Solvente", "Tinta", 1, 0.50));
            itens.add(new ItemFormula("Corpo", "Caneta", 1, 1.00));
            itens.add(new ItemFormula("Plástico", "Corpo", 1, 0.70));
            itens.add(new ItemFormula("Clip", "Corpo", 1, 0.30));
            itens.add(new ItemFormula("Tampa", "Caneta", 1, 0.70));
            itens.add(new ItemFormula("Plástico", "Tampa", 1, 0.50));
            itens.add(new ItemFormula("Vedação", "Tampa", 1, 0.20));
        } else if ("2".equals(teste)) {
            itens.add(new ItemFormula("Computador", null, 1, 2500));
            itens.add(new ItemFormula("Gabinete", "Computador", 1, 800));
            itens.add(new ItemFormula("Placa-Mãe", "Gabinete", 1, 500));
            itens.add(new ItemFormula("CPU", "Placa-Mãe", 1, 1000));
            itens.add(new ItemFormula("Memória RAM", "Placa-Mãe", 2, 200));
            itens.add(new ItemFormula("Chipset", "Placa-Mãe", 1, 150));
            itens.add(new ItemFormula("Fonte de Alimentação", "Gabinete", 1, 300));
            itens.add(new ItemFormula("Armazenamento", "Gabinete", 1, 0));
            itens.add(new ItemFormula("HDD", "Armazenamento", 1, 250));
            itens.add(new ItemFormula("SSD", "Armazenamento", 1, 400));
            itens.add(new ItemFormula("Monitor", "Computador", 1, 900));
            itens.add(new ItemFormula("Tela LCD", "Monitor", 1, 600));
            itens.add(new ItemFormula("Carcaça", "Monitor", 1, 100));
            itens.add(new ItemFormula("Periféricos", "Computador", 1, 0));
            itens.add(new ItemFormula("Teclado", "Periféricos", 1, 100));
            itens.add(new ItemFormula("Mouse", "Periféricos", 1, 80));
        }

        for (ItemFormula item : itens) {
            produtoService.cadastrarItem(item.getNome(), item.getPai(), item.getQuantidade(), item.getPreco());
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("sucesso", true);
        resp.put("itens", itens.size());
        return resp;
    }

    @PostMapping("/limpar")
    public Map<String, Object> limpar() {
        produtoService.listarTodos().clear();
        Map<String, Object> resp = new HashMap<>();
        resp.put("sucesso", true);
        return resp;
    }

    private Map<String, Object> treeToMap(TreeNode no) {
        if (no == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("nome", no.getItem().getNome());
        map.put("pai", no.getItem().getPai());
        map.put("quantidade", no.getItem().getQuantidade());
        map.put("preco", no.getItem().getPreco());
        map.put("custo", no.getItem().getQuantidade() * no.getItem().getPreco());
        List<Map<String, Object>> filhos = new ArrayList<>();
        for (TreeNode filho : no.getFilhos()) {
            filhos.add(treeToMap(filho));
        }
        map.put("filhos", filhos);
        return map;
    }
}