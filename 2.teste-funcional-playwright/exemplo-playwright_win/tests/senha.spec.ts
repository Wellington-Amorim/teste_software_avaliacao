import { test, expect } from '@playwright/test';

const casosSenha = [
  { senha: 'Senha1!', confirmacao: 'Senha1!', aceito: false, msg: 'Senha fora do padrão', classe: 'abaixo do mínimo (7 chars)' },
  { senha: 'Senha1!a', confirmacao: 'Senha1!a', aceito: true, msg: 'Senha cadastrada', classe: 'limite mínimo exato (8 chars)' },
  { senha: 'SenhaSegura123456789', confirmacao: 'SenhaSegura123456789', aceito: true, msg: 'Senha cadastrada', classe: 'limite máximo exato (20 chars)' },
  { senha: 'SenhaSegura1234567890', confirmacao: 'SenhaSegura1234567890', aceito: false, msg: 'Senha fora do padrão', classe: 'acima do máximo (21 chars)' },

  { senha: 'senhasegura123', confirmacao: 'senhasegura123', aceito: false, msg: 'Senha fora do padrão', classe: 'sem letra maiúscula' },
  { senha: 'SENHASEGURA123', confirmacao: 'SENHASEGURA123', aceito: false, msg: 'Senha fora do padrão', classe: 'sem letra minúscula' },
  { senha: 'SenhaSegura!', confirmacao: 'SenhaSegura!', aceito: false, msg: 'Senha fora do padrão', classe: 'sem números' },
  { senha: 'Senha 123!', confirmacao: 'Senha 123!', aceito: false, msg: 'Senha fora do padrão', classe: 'contém espaço' },
  { senha: '', confirmacao: '', aceito: false, msg: 'Senha fora do padrão', classe: 'vazio' },

  { senha: 'SenhaSegura123', confirmacao: 'SenhaDiferente123', aceito: false, msg: 'As senhas não coincidem', classe: 'confirmação divergente' },
];

test.describe('Validação de cadastro de senha', () => {
  for (const caso of casosSenha) {
    test(`senha: ${caso.classe}`, async ({ page }) => {
      await page.goto('/senha');

      await page.getByLabel('Nova senha').fill(caso.senha);
      await page.getByLabel('Confirmar senha').fill(caso.confirmacao);
      await page.getByRole('button', { name: 'Cadastrar senha' }).click();

      const resultado = page.locator('#resultado');
      await expect(resultado).toBeVisible();
      await expect(resultado).toHaveText(caso.msg);
      await expect(resultado).toHaveAttribute('role', caso.aceito ? 'status' : 'alert');
    });
  }

  test('limpa o formulário após cadastro de senha válido', async ({ page }) => {
    await page.goto('/senha');

    const inputSenha = page.getByLabel('Nova senha');
    const inputConfirmacao = page.getByLabel('Confirmar senha');

    await inputSenha.fill('SenhaValida123');
    await inputConfirmacao.fill('SenhaValida123');
    await page.getByRole('button', { name: 'Cadastrar senha' }).click();

    await expect(page.locator('#resultado')).toHaveText('Senha cadastrada');

    await expect(inputSenha).toHaveValue('');
    await expect(inputConfirmacao).toHaveValue('');
  });
});