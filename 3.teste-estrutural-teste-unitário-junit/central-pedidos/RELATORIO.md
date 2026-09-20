# Relatorio do grupo

Integrantes: Wellington Amorim.

## Grafos e complexidade

### Grafo de chamadas de `PedidoService.fechar`

`PedidoService.fechar(pedido, cliente)` chama, nesta ordem:

1. `Objects.requireNonNull(pedido)` e `Objects.requireNonNull(cliente)`.
2. `cliente.bloqueado()`. Se verdadeiro, retorna `ResultadoPedido("BLOQUEADO", 0, 0, 0, 0)`.
3. `pedido.subtotalCentavos()`. Se o subtotal for zero, lanca `IllegalArgumentException`.
4. `pedido.estoqueSuficiente()`. Se falso, retorna `ResultadoPedido("SEM_ESTOQUE", 0, 0, 0, 0)`.
5. `PoliticaDesconto.calcular(cliente, subtotal, pedido.cupom())`.
6. `CalculadoraFrete.calcular(pedido, cliente, liquido)`, que tambem usa `pedido.uf()`, `pedido.pesoGramas()`, `pedido.expresso()` e `pedido.temFragil()`.
7. `AnaliseRisco.avaliar(cliente, total, pedido.expresso())`.
8. Se o risco nao for `APROVADO`, retorna `REVISAO` ou `RECUSADO` com valores calculados.
9. `PagamentoService.pagar(total, 3)`, que chama `ProcessadorPagamento.autorizar(total)`.
10. Retorna `PAGO` ou `PAGAMENTO_RECUSADO`.

### Modelo usado nos CFGs

- Cada metodo foi modelado como um grafo conectado com entrada unica e saida unificada.
- Retornos antecipados entram em um no final comum para permitir `V(G) = E - N + 2`.
- Condicoes com curto-circuito (`||` e `&&`) foram separadas quando o segundo operando pode deixar de ser avaliado. Exemplo: `cupom == null || cupom.isBlank()` e `liquido >= 30000 && !pedido.expresso()`.
- `switch` foi contado com uma aresta por alternativa real: `PR`, `SP/RJ` e `default` em frete; `BEMVINDO`, `EXTRA10` e `default` em desconto.
- Lacos (`for`, `while`, `do/while`) foram contados com arestas de repeticao e saida.
- Excecoes esperadas foram consideradas caminhos de saida anormal no projeto dos testes, mesmo quando o JaCoCo nao as conta como branch.

| Metodo | Nos | Arestas | V(G) | Caminhos independentes | Restricoes de viabilidade |
| --- | ---: | ---: | ---: | --- | --- |
| `Cliente` construtor | 3 | 3 | 2 | historico valido; historico negativo | Nenhuma. |
| `ItemPedido` construtor/metodos | 14 | 25 | 13 | SKU nulo/branco; preco invalido baixo/alto; quantidade invalida baixa/alta; estoque negativo; peso invalido baixo/alto; item valido; `disponivel` verdadeiro/falso; `totalCentavos` | Cada objeto invalido para no primeiro campo invalido encontrado. |
| `Pedido` construtor e auxiliares | 18 | 33 | 17 | lista nula; lista > 100; elemento nulo; UF nula/invalida/valida; subtotal com item inativo e ativo; peso; fragil ativo; fragil ausente/inativo; estoque todo suficiente; estoque insuficiente com `break` | Lista nula e lista grande param antes de `List.copyOf`; elemento nulo gera `NullPointerException` no `copyOf`. |
| `PoliticaDesconto.calcular` | 13 | 23 | 12 | subtotal negativo; VIP; comum >= 50000; comum abaixo; cupom nulo/branco; `BEMVINDO` elegivel/nao elegivel; `EXTRA10` elegivel/nao elegivel; cupom desconhecido; desconto acima/abaixo do teto | Cupom nao e avaliado quando nulo/branco; teto so e relevante depois de cupom conhecido. |
| `CalculadoraFrete.calcular` | 11 | 19 | 10 | liquido negativo; UF PR; UF SP/RJ; UF default; peso sem excedente; uma/varias iteracoes do `while`; gratuidade; VIP; expresso; fragil; combinacoes de adicionais | Frete gratis exige liquido >= 30000 e entrega normal; expresso impede gratuidade pelo curto-circuito. |
| `AnaliseRisco.avaliar` | 9 | 15 | 8 | total negativo; cliente bloqueado; sem historico com total > 100000; sem historico expresso; sem historico aprovado no limite; com historico nao VIP acima de 500000; VIP acima de 500000 aprovado; demais aprovados | Via `PedidoService`, cliente bloqueado nao chega em `AnaliseRisco`, pois o servico retorna antes. |
| `PagamentoService.pagar` | 7 | 10 | 5 | total invalido; limite baixo/alto; sucesso na primeira; retorno `false` sem repetir; `IllegalStateException` seguida de sucesso; esgotamento de tentativas; excecao diferente propagada | Apenas `IllegalStateException` permite repetir; qualquer retorno `true/false` encerra o `do/while`. |
| `PedidoService.fechar` | 10 | 14 | 6 | pedido/cliente nulos; bloqueado; subtotal zero; sem estoque; cupom invalido; risco `REVISAO`; pagamento aprovado; pagamento recusado; indisponibilidade temporaria seguida de sucesso | `RECUSADO` da analise de risco para cliente bloqueado e inviavel via servico, porque ha retorno `BLOQUEADO` antes. |

## Matriz de testes

| ID / metodo JUnit | Unidade | Entrada e estado do stub | Resultado esperado | Caminho / aresta | Criterio atendido |
| --- | --- | --- | --- | --- | --- |
| `ClienteTest.deveCriarClienteComHistoricoZero` | `Cliente` | VIP, nao bloqueado, historico 0 | Objeto criado com campos corretos | Construtor valido | Linha/metodo/classe |
| `ClienteTest.deveRejeitarHistoricoNegativo` | `Cliente` | historico -1 | `IllegalArgumentException` | Validacao negativa | Branch/excecao |
| `ItemPedidoTest.deveCalcularTotalEDisponibilidade` | `ItemPedido` | preco 1999, quantidade 3, estoque 3 | total 5997 e disponivel | Produto e estoque suficiente | Linha/metodo |
| `ItemPedidoTest.deveIdentificarIndisponibilidadeQuandoQuantidadePassaDoEstoque` | `ItemPedido` | quantidade 4, estoque 3 | `disponivel=false` | Branch falso de disponibilidade | Branch |
| `ItemPedidoTest.deveAceitarValoresNosLimitesValidos` | `ItemPedido` | preco, quantidade e peso no limite maximo | objeto valido, total 100000000, indisponivel | Limites superiores validos | Limites |
| `ItemPedidoTest.deveRejeitarCamposInvalidos` | `ItemPedido` | SKU, preco, quantidade, estoque e peso invalidos | `IllegalArgumentException` | Validacoes do construtor | Branch/excecao |
| `PedidoTest.deveSomarSubtotalPesoEFragilidadeApenasQuandoAplicavel` | `Pedido` | item ativo fragil e item inativo | subtotal 4000, peso 1400, fragil verdadeiro | `for` com item ativo e `continue` no subtotal | Laco/branch |
| `PedidoTest.itemFragilInativoNaoTornaPedidoFragil` | `Pedido` | item fragil com quantidade 0 | `temFragil=false` | Fragilidade ignorada em item inativo | Branch |
| `PedidoTest.deveAvaliarEstoquePorLinhaEPararNaPrimeiraFalta` | `Pedido` | primeiro item sem estoque | `estoqueSuficiente=false` | `break` na primeira falta | Laco/break |
| `PedidoTest.deveCopiarListaDefensivamente` | `Pedido` | lista externa alterada depois | lista interna preservada e imutavel | Copia defensiva | Estado observavel |
| `PedidoTest.deveAceitarListaVaziaNaConstrucao` | `Pedido` | lista vazia, UF SC | pedido valido com subtotal 0 | Lista vazia valida na construcao | Limite |
| `PedidoTest.deveRejeitarListaUfOuElementoInvalidos` | `Pedido` | lista nula/grande, UF invalida, elemento nulo | excecoes esperadas | Validacoes e `List.copyOf` | Branch/excecao |
| `PoliticaDescontoTest.deveAplicarDezPorCentoParaVipComTruncamento` | `PoliticaDesconto` | VIP, subtotal 10005, sem cupom | desconto 1000 | Branch VIP e truncamento | Branch/limite |
| `PoliticaDescontoTest.deveAplicarCincoPorCentoParaClienteComumNoSubtotalMinimo` | `PoliticaDesconto` | comum, subtotal 50000, cupom branco | desconto 2500 | Branch comum >= minimo e cupom branco | Branch/curto-circuito |
| `PoliticaDescontoTest.deveRetornarZeroParaClienteComumAbaixoDoSubtotalMinimo` | `PoliticaDesconto` | comum, subtotal 49999 | desconto 0 | Branch sem desconto | Limite |
| `PoliticaDescontoTest.deveAplicarCupomBemVindoNormalizadoQuandoElegivel` | `PoliticaDesconto` | sem historico, subtotal 10000, cupom com espacos/minusculo | desconto 2000 | Normalizacao e cupom elegivel | Branch |
| `PoliticaDescontoTest.naoDeveAplicarBemVindoQuandoClienteTemHistoricoOuSubtotalInsuficiente` | `PoliticaDesconto` | historico > 0 ou subtotal 9999 | desconto 0 | `&&` com direito avaliado e nao avaliado | Curto-circuito |
| `PoliticaDescontoTest.deveAplicarExtra10QuandoElegivel` | `PoliticaDesconto` | subtotal 20000, `EXTRA10` | desconto 2000 | Case `EXTRA10` elegivel | Branch |
| `PoliticaDescontoTest.deveLimitarDescontoCombinadoAVintePorCentoDoSubtotal` | `PoliticaDesconto` | VIP sem historico, subtotal 10000, `BEMVINDO` | desconto limitado a 2000 | Teto de desconto | Branch |
| `PoliticaDescontoTest.deveRejeitarSubtotalNegativoECupomDesconhecido` | `PoliticaDesconto` | subtotal -1; cupom `NATAL` | `IllegalArgumentException` | Saidas por excecao | Excecao |
| `CalculadoraFreteTest.deveCalcularBaseDoParanaSemAdicionais` | `CalculadoraFrete` | UF PR, normal, 1 kg | frete 1200 | Case PR sem adicionais | Branch |
| `CalculadoraFreteTest.deveCalcularBaseDeSaoPauloERioDeJaneiro` | `CalculadoraFrete` | UF SP e RJ | frete 2000 | Cases agrupados SP/RJ | Switch |
| `CalculadoraFreteTest.deveCalcularBasePadraoParaOutrasUfs` | `CalculadoraFrete` | UF SC | frete 3000 | Default do switch | Branch |
| `CalculadoraFreteTest.deveAcrescentarPesoPorQuiloOuFracaoAcimaDeDoisQuilos` | `CalculadoraFrete` | 2001 g e 4001 g | fretes 1500 e 2100 | Uma e varias iteracoes do `while` | Laco |
| `CalculadoraFreteTest.deveZerarFreteEmEntregaNormalComLiquidoMinimo` | `CalculadoraFrete` | liquido 30000, normal | frete 0 | Gratuidade | Branch/limite |
| `CalculadoraFreteTest.deveAplicarMetadeParaVipAntesDosAdicionaisExpressosEFragil` | `CalculadoraFrete` | VIP, SC, expresso, fragil, 3 kg | frete 3650 | VIP + expresso + fragil | Combinacao |
| `CalculadoraFreteTest.deveManterAdicionaisQuandoBaseFoiZerada` | `CalculadoraFrete` | liquido gratis e item fragil | frete 500 | Adicional fragil apos zerar base | Combinacao |
| `CalculadoraFreteTest.deveRejeitarLiquidoNegativo` | `CalculadoraFrete` | liquido -1 | `IllegalArgumentException` | Validacao inicial | Excecao |
| `AnaliseRiscoTest.deveRecusarClienteBloqueadoAntesDasDemaisRegras` | `AnaliseRisco` | bloqueado, total alto, expresso | `RECUSADO` | Retorno antecipado | Branch |
| `AnaliseRiscoTest.deveEnviarClienteSemHistoricoParaRevisaoQuandoTotalPassaDeMilReais` | `AnaliseRisco` | sem historico, total 100001 | `REVISAO` | Primeira parte do `||` verdadeira | Branch/limite |
| `AnaliseRiscoTest.deveEnviarClienteSemHistoricoParaRevisaoQuandoEntregaExpressa` | `AnaliseRisco` | sem historico, expresso | `REVISAO` | Segunda parte do `||` avaliada | Curto-circuito |
| `AnaliseRiscoTest.deveAprovarClienteSemHistoricoNoLimiteComEntregaNormal` | `AnaliseRisco` | total 100000, normal | `APROVADO` | Limite nao revisa | Limite |
| `AnaliseRiscoTest.deveEnviarClienteComHistoricoNaoVipParaRevisaoQuandoTotalPassaDeCincoMilReais` | `AnaliseRisco` | historico, nao VIP, total 500001 | `REVISAO` | `&&` verdadeiro | Branch |
| `AnaliseRiscoTest.deveAprovarClienteVipComHistoricoMesmoAcimaDeCincoMilReais` | `AnaliseRisco` | historico, VIP, total 600000 | `APROVADO` | Segunda parte do `&&` falsa | Curto-circuito/branch |
| `AnaliseRiscoTest.deveRejeitarTotalNegativo` | `AnaliseRisco` | total -1 | `IllegalArgumentException` | Validacao inicial | Excecao |
| `PagamentoServiceTest.deveAprovarNaPrimeiraTentativa` | `PagamentoService` | stub retorna `true` | `true`, uma chamada | Retorno aprovado no `try` | Caminho basico |
| `PagamentoServiceTest.deveRecusarSemRepetirQuandoProcessadorRetornaFalse` | `PagamentoService` | stub retorna `false` | `false`, uma chamada | Recusa sem repetir | Caminho basico |
| `PagamentoServiceTest.deveRepetirIndisponibilidadeTemporariaAteSucesso` | `PagamentoService` | duas `IllegalStateException`, depois `true` | `true`, tres chamadas | `catch` e repeticao | Try/catch/laco |
| `PagamentoServiceTest.deveRetornarFalseQuandoEsgotaTentativasTemporarias` | `PagamentoService` | sempre `IllegalStateException`, limite 2 | `false`, duas chamadas | Esgotamento do `do/while` | Laco/excecao |
| `PagamentoServiceTest.devePropagarExcecoesDiferentesDeIndisponibilidade` | `PagamentoService` | stub lanca `IllegalArgumentException` | excecao propagada | Excecao nao capturada | Excecao |
| `PagamentoServiceTest.deveValidarDependenciaTotalELimiteDeTentativas` | `PagamentoService` | processador nulo, total 0, limites 0 e 4 | excecoes esperadas | Validacoes iniciais | Branch/excecao |
| `PedidoServiceTest.deveFecharPedidoDeClienteComumComFreteDoParanaEPagamentoAprovado` | `PedidoService` | comum, PR, normal, pagamento `true` | `PAGO`, total 11200, uma cobranca | Caminho feliz | Integracao |
| `PedidoServiceTest.deveRetornarBloqueadoAntesDeAvaliarItensECupom` | `PedidoService` | cliente bloqueado, item sem estoque, cupom invalido | `BLOQUEADO`, sem cobranca | Retorno antes de itens/cupom | Ordem/retorno antecipado |
| `PedidoServiceTest.deveRejeitarPedidoSemItensAtivos` | `PedidoService` | item quantidade 0 | `IllegalArgumentException` | Subtotal zero | Excecao |
| `PedidoServiceTest.deveRetornarSemEstoqueAntesDeAvaliarCupom` | `PedidoService` | falta estoque, cupom invalido | `SEM_ESTOQUE`, sem cobranca | Retorno antes do cupom | Ordem/retorno antecipado |
| `PedidoServiceTest.deveRetornarRevisaoSemCobrarQuandoRiscoNaoAprova` | `PedidoService` | sem historico, total alto | `REVISAO`, sem cobranca | Risco antes de pagamento | Integracao |
| `PedidoServiceTest.deveRetornarPagamentoRecusadoComValoresCalculados` | `PedidoService` | `EXTRA10`, stub retorna `false` | `PAGAMENTO_RECUSADO`, total 19200 | Pagamento recusado | Integracao |
| `PedidoServiceTest.deveRepetirPagamentoTemporariamenteIndisponivelNoFechamento` | `PedidoService` | duas indisponibilidades, depois sucesso | `PAGO`, tres cobrancas | Repeticao via servico | Integracao/laco |
| `PedidoServiceTest.devePropagarCupomDesconhecidoSemCobrar` | `PedidoService` | cupom desconhecido | `IllegalArgumentException`, sem cobranca | Excecao antes de frete/risco/pagamento | Ordem/excecao |
| `PedidoServiceTest.deveValidarReferenciasObrigatorias` | `PedidoService` | processador, pedido ou cliente nulos | `NullPointerException` | Validacoes obrigatorias | Excecao |

## Evolucao da cobertura

| Etapa | Testes executados | Linhas | Branches | Metodos | Classes | Lacunas e justificativas |
| --- | --- | --- | --- | --- | --- | --- |
| Inicial | 1 teste de exemplo em `PedidoServiceTest` | Nao medido no relatorio atual | Nao medido no relatorio atual | Nao medido no relatorio atual | Nao medido no relatorio atual | Exercitava apenas cliente comum, PR, sem desconto, entrega normal e pagamento aprovado. |
| Dominio | `ClienteTest`, `ItemPedidoTest`, `PedidoTest` | Cobriu construtores e auxiliares de dominio | Cobriu validacoes, disponibilidade, fragilidade, estoque e lacos | Todos os metodos de dominio executados | Classes de dominio executadas | Confirmou limites, copia defensiva, lista vazia e excecoes de entrada. |
| Regras isoladas | `PoliticaDescontoTest`, `CalculadoraFreteTest`, `AnaliseRiscoTest`, `PagamentoServiceTest` | Todas as linhas de negocio foram executadas | Cobriu a maior parte dos branches de desconto, frete, risco e pagamento | Todos os metodos de negocio executados | Classes de servico executadas | Incluiu limites, curto-circuito, repeticoes de laco, try/catch e excecoes. |
| Colaboracao | `PedidoServiceTest` completo | `PedidoService` com 22/22 linhas cobertas | `PedidoService` com 10/10 branches cobertos | `PedidoService` com 3/3 metodos cobertos | Integracao principal coberta | Validou ordem: bloqueado, subtotal zero, estoque, cupom, risco e pagamento. |
| Final JaCoCo | 50 testes: 7 risco, 8 frete, 2 cliente, 4 item, 6 pagamento, 9 servico, 6 pedido, 8 desconto | 108/108 = 100% | 114/116 = 98,3% | 21/21 = 100% | 9/9 = 100% | Restaram 2 branches: 1 em `CalculadoraFrete` e 1 em `PoliticaDesconto`. As linhas estao todas cobertas; faltam combinacoes especificas de decisao. |

## Analise critica

- Mesmo com quase todos os ramos cobertos, ainda faltavam combinacoes completas. Em frete, por exemplo, gratuidade, VIP, expresso e fragilidade sao decisoes independentes; cobrir cada uma como verdadeiro/falso nao prova todas as combinacoes possiveis, como VIP com frete zerado e depois adicional expresso.
- Condicoes nao avaliadas por curto-circuito foram observadas em `cupom == null || cupom.isBlank()`, `cliente.comprasAnteriores() == 0 && subtotal >= 10000`, `liquido >= 30000 && !pedido.expresso()` e `total > 500000 && !cliente.vip()`. Quando o primeiro operando define o resultado, o segundo pode nao ser executado.
- Um caminho viavel na unidade mas inviavel no servico e `AnaliseRisco.avaliar` retornar `RECUSADO` para cliente bloqueado. No `PedidoService`, cliente bloqueado retorna `BLOQUEADO` antes de chamar a analise de risco.
- Excecoes foram testadas com `assertThrows`: entradas invalidas em construtores, subtotal/liquido/total negativos, cupom desconhecido, referencias nulas e excecao definitiva do processador. O tratamento de `IllegalStateException` em pagamento foi testado com uma, varias e esgotamento de tentativas.
- Iteracoes foram testadas com itens inativos/ativos em `Pedido`, `break` na falta de estoque, uma e varias iteracoes no `while` do frete, e uma/duas/tres chamadas no `do/while` de pagamento.
- A alteracao proposital sugerida para mutacao foi mudar o limite de desconto comum de `subtotal >= 50000` para `subtotal > 50000`. O teste `PoliticaDescontoTest.deveAplicarCincoPorCentoParaClienteComumNoSubtotalMinimo` detecta a falha, pois espera R$ 25,00 de desconto exatamente em R$ 500,00. A alteracao deve ser desfeita antes da entrega.
