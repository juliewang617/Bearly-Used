import { expect, test } from "@playwright/test";
import { setupClerkTestingToken, clerk } from "@clerk/testing/playwright";
import "dotenv/config";

const url = "http://localhost:8000";

test.use({
  bypassCSP: true,
});

test.beforeEach(async ({ page }) => {
  await page.goto(url);
});

/* Signing up for the first time */
/* NOTE: only run this if hello@brown.edu is not in the database. */

test("Signing up for the first time works", async ({ page}) => {
  await expect(
    page.getByRole("heading", { name: "Bearly Used" })
  ).toBeVisible();

  // click sign in button 
  await page.getByRole("button", { name: "Sign In" }).click();

  // signing in 
  await page.getByLabel("Email address").click();
  await page.getByLabel("Email address").fill("hello@brown.edu");
  await page.getByRole("button", { name: "Continue", exact: true }).click();
  await page.getByLabel("Password", { exact: true }).click();
  await page.getByLabel("Password", { exact: true }).fill("HelloGuys123!!");
  await page.getByRole("button", { name: "Continue" }).click();

  // configuring info upon first log in
  await expect(
    page.getByRole("heading", { name: "Configure Your Information" })
  ).toBeVisible();
  await expect(page.getByText("Name")).toBeVisible();
  await page.getByPlaceholder("Enter your full name").click();
  await page.getByPlaceholder("Enter your full name").fill("Student");
  await expect(page.getByText("Phone Number")).toBeVisible();
  await page.getByPlaceholder("-456-7890").click();
  await page.getByPlaceholder("-456-7890").fill("216-222-2121");
  await expect(page.getByText("School:")).toBeVisible();
  await page.getByLabel("School:").selectOption("Brown");
  await page.getByLabel("School:").selectOption("RISD");
  await page.getByLabel("School:").selectOption("Brown");
  await page.getByRole("button", { name: "Save and Continue" }).click();

  // homepage
  await expect(
    page.getByRole("link", { name: "Bearly Used" })
  ).toBeVisible();
  await expect(page.getByRole("button", { name: "Price" })).toBeVisible();
  await expect(
    page.getByRole("button", { name: "Categories" })
  ).toBeVisible();
  await expect(page.getByPlaceholder("Search listings by title or tags...")).toBeVisible();
  await expect(page.getByRole("button", { name: "🔍" })).toBeVisible();
  await expect(
    page.getByRole("button", { name: "Create Listing" })
  ).toBeVisible();
  await expect(
    page.getByRole("button", { name: "Sign out" })
  ).toBeVisible();
})