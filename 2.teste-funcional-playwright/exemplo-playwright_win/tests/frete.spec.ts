import { test, expect } from '@playwright/test';

const casosFrete = [
  { cep: '80000000', valor: '199.99', valido: true, msg: 'Frete: R$ 15,00', classe: 'cep 8 normal limite inferior frete pago' },
  { cep: '89999999', valor: '200.00', valido: true, msg: 'Frete grátis', classe: 'cep 8 frete grátis limite exato' },
  { cep: '01000000', valor: '199.99', valido: true, msg: 'Frete: R$ 25,00', classe: 'outros ceps normal limite inferior frete pago' },
  { cep: '12345678', valor: '200.01', valido: true, msg: 'Frete grátis', classe: 'outros ceps frete grátis acima do limite' },
  { cep: '99999999', valor: '50', valido: true, msg: 'Frete: R$ 25,00', classe: 'outros ceps valor padrão' },

  { cep: '1234567', valor: '100', valido: false, msg: 'Dados inválidos', classe: 'cep muito curto' },
  { cep: '123456789', valor: '100', valido: false, msg: 'Dados inválidos', classe: 'cep muito longo' },
  { cep: 'abcdefgh', valor: '100', valido: false, msg: 'Dados inválidos', classe: 'cep com letras' },
  { cep: '12345678', valor: '0', valido: false, msg: 'Dados inválidos', classe: 'valor zero' },
  { cep: '12345678', valor: '-10', valido: false, msg: 'Dados inválidos', classe: 'valor negativo' },
  { cep: '12345678', valor: 'abc', valido: false, msg: 'Dados inválidos', classe: 'valor com letras' },
  { cep: '', valor: '', valido: false, msg: 'Dados inválidos', classe: 'campos vazios' },
];

for (const caso of casosFrete) {
  test(`frete: cep ${caso.cep || '(vazio)'} e valor ${caso.valor || '(vazio)'} - ${caso.classe}`, async ({ page }) => {
    await page.goto('/frete');

    await page.getByLabel('CEP').fill(caso.cep);
    await page.getByLabel('Valor do pedido').fill(caso.valor);
    await page.getByRole('button', { name: 'Calcular frete' }).click();

    const resultado = page.locator('#resultado');
    await expect(resultado).toBeVisible();
    await expect(resultado).toHaveText(caso.msg);
    await expect(resultado).toHaveAttribute('role', caso.valido ? 'status' : 'alert');
  });
}