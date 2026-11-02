import type { Page } from "@playwright/test";
import { expect } from "@playwright/test";

export const password = "e2e-long-enough-password";

/** registers a fresh kingdom through the UI and leaves the page on its overview */
export const foundKingdom = async (page: Page, prefix = "E2e"): Promise<{ kingdomName: string; email: string }> => {
  const suffix = `${Date.now() % 1_000_000}${Math.floor(Math.random() * 100)}`;
  const kingdomName = `${prefix} ${suffix}`;
  const email = `${prefix.toLowerCase()}-${suffix}@example.com`;
  await page.goto("/register");
  await page.getByLabel("Kingdom name").fill(kingdomName);
  await page.getByLabel("Email").fill(email);
  await page.getByLabel("Password, at least 8 characters").fill(password);
  await page.getByLabel("Password again").fill(password);
  await page.getByRole("button", { name: "Found the kingdom" }).click();
  await expect(page).toHaveURL(/\/kingdom\/overview/);
  await expect(page.getByRole("heading", { level: 1, name: kingdomName })).toBeVisible();
  return { kingdomName, email };
};

export const signIn = async (page: Page, email: string, secret: string): Promise<void> => {
  await page.goto("/sign-in");
  await page.getByLabel("Email").fill(email);
  await page.getByLabel("Password").fill(secret);
  await page.getByRole("button", { name: "Enter" }).click();
  await expect(page).toHaveURL(/\/kingdom\/overview/);
};

export const signOut = async (page: Page): Promise<void> => {
  await page.getByRole("button", { name: "Sign out" }).click();
  await expect(page).toHaveURL(/\/sign-in|\/$/);
};

export const goTo = async (page: Page, link: string): Promise<void> => {
  await page.getByRole("navigation", { name: "Kingdom" }).getByRole("link", { name: link, exact: false }).first().click();
};

export const toast = (page: Page, text: string | RegExp): ReturnType<Page["locator"]> => page.locator(".toast").filter({ hasText: text });

/** the server adds turns on its own clock, so the counter alone cannot tell that a turn was spent */
export const passTurn = async (page: Page): Promise<void> => {
  const passed = page.waitForResponse(response => response.url().endsWith("/kingdom/pass-turn") && response.ok());
  const reloaded = page.waitForResponse(response => response.url().includes("/kingdom/details") && response.ok());
  await page.getByRole("button", { name: /^Pass turn/ }).click();
  const result = (await (await passed).json()) as { success: boolean };
  expect(result.success).toBe(true);
  await reloaded;
};

export const passTurns = async (page: Page, count: number): Promise<void> => {
  for (let turn = 0; turn < count; turn++) {
    await passTurn(page);
  }
};
