import { expect, test } from "@playwright/test";
import { setupClerkTestingToken, clerk } from "@clerk/testing/playwright";
import "dotenv/config";
import path from "path";

/*
 * NOTE: this runs assuming hello@brown.edu is in the database. If not,
 * run Signup.spec.ts first.
 */

const url = "http://localhost:8000";

test.use({
  bypassCSP: true,
});

test.beforeEach(async ({ page }) => {
  await page.goto(url);

  // sign-in if needed
  const signInButton = page.getByRole("button", { name: "Sign in" });
  const signOutButton = page.getByRole("button", { name: "Sign out" });

  await Promise.race([
    signInButton.waitFor({ timeout: 5000 }),
    signOutButton.waitFor({ timeout: 5000 }),
  ]);

  const isSignedOut = await signInButton.isVisible().catch(() => false);

  if (isSignedOut) {
    await page.getByRole("button", { name: "Sign in" }).click();
    await page.getByLabel("Email address").click();
    await page.getByLabel("Email address").fill("hello@brown.edu");
    await page.getByRole("button", { name: "Continue", exact: true }).click();
    await page.getByLabel("Password", { exact: true }).click();
    await page.getByLabel("Password", { exact: true }).fill("HelloGuys123!!");
    await page.getByRole("button", { name: "Continue" }).click();
  }
});

/* Sign in */
test("Everything loads on sign in", async ({ page }) => {
  await expect(page.locator("a.navbar-brand")).toBeVisible();
  await expect(page.locator("div.homepage-listings")).toBeVisible();
  await expect(page.locator("button.create-listing")).toBeVisible();
  await expect(page.locator("div.user-profile-section")).toBeVisible();
});

/* Page switching */
test("Switching between pages loads correctly", async ({ page }) => {
  await page.locator("a.user-name").click();
  await expect(page.locator("div.profile")).toBeVisible();
  await expect(page.locator("div.listings-navigation")).toBeVisible();
  await expect(page.locator("button.edit-profile")).toBeVisible();
  await expect(page.locator("button.create-listing")).toBeVisible();
  await expect(page.locator("h2.name")).toContainText("Student");
  await expect(page.locator("p.school")).toHaveText("School: Brown");
  await expect(page.locator("p.email")).toHaveText("Email: hello@brown.edu");
  await expect(page.locator("p.phone")).toHaveText(
    "Phone Number: 216-222-2121"
  );
  await page.locator("a.back-link").click();
  await expect(page.locator("a.navbar-brand")).toBeVisible();
  await expect(page.locator("div.homepage-listings")).toBeVisible();
  await expect(page.locator("button.create-listing")).toBeVisible();
  await expect(page.locator("div.user-profile-section")).toBeVisible();
});

/* Creating, searching, and deleting a listing */
test("Creating, searching, and deleting a single listing works properly", async ({
  page,
}) => {
  await page.locator("button.create-listing").click();

  // this helps with the parallel workers by preventing issues where multiple
  // workers will create the same exact item
  const uniqueTitle = `Test Item ${Date.now()}`;
  await page.locator('input[name="title"]').fill(uniqueTitle);

  await page.locator('textarea[name="description"]').fill("Description");
  await page.locator('input[name="price"]').fill("4.00");
  await page
    .locator('select[name="category"]')
    .selectOption({ label: "Other" });
  await page.locator('select[name="condition"]').selectOption({ label: "New" });
  await page.locator('input[name="tags"]').fill("test tag1");
  await page.locator('input[name="tags"]').press("Enter");
  await page.locator('input[name="tags"]').fill("test tag2");
  await page.locator('input[name="tags"]').press("Enter");
  await page
    .locator('input[type="file"][accept="image/*"]')
    .setInputFiles("./dummy.png");
  await page.locator("button.btn.btn-submit.w-100").click();
  await page.waitForTimeout(2000);

  // Search for it and make sure its there!
  await page
    .getByPlaceholder("Search listings by title or tags...")
    .fill(uniqueTitle);
  await page.keyboard.press("Enter");
  await page.waitForTimeout(2000);
  await expect(page.getByText(uniqueTitle)).toBeVisible();

  // verify it has the correct information
  await page.getByText(uniqueTitle).click();
  await expect(page.getByText("test tag1")).toBeVisible();
  await expect(page.getByText("test tag2")).toBeVisible();
  await expect(page.getByText("$4")).toBeVisible();

  // Delete the item
  await page.getByText("Delete listing", { exact: true }).click();
  await expect(
    page.getByText(
      "Are you sure you want to delete this listing? This action cannot be undone."
    )
  ).toBeVisible();
  await page.locator("#confirm-delete-listing").click();
  await page.waitForTimeout(2000);

  // Search for it and make sure its not sure its not there!
  await page.goto(url);
  await page
    .getByPlaceholder("Search listings by title or tags...")
    .fill(uniqueTitle);
  await page.keyboard.press("Enter");
  await page.waitForTimeout(2000);

  await expect(page.getByText("No Items Found")).toBeVisible();
});

/* Edit listing */
test("Creating, editing, and deleting a listing works properly", async ({
  page,
}) => {
  await page.locator("button.create-listing").click();

  const uniqueTitle = `Test Item ${Date.now()}`;
  await page.locator('input[name="title"]').fill(uniqueTitle);

  await page.locator('textarea[name="description"]').fill("Description");
  await page.locator('input[name="price"]').fill("4.00");
  await page
    .locator('select[name="category"]')
    .selectOption({ label: "Other" });
  await page.locator('select[name="condition"]').selectOption({ label: "New" });
  await page.locator('input[name="tags"]').fill("test tag1");
  await page.locator('input[name="tags"]').press("Enter");
  await page.locator('input[name="tags"]').fill("test tag2");
  await page.locator('input[name="tags"]').press("Enter");
  await page
    .locator('input[type="file"][accept="image/*"]')
    .setInputFiles("./dummy.png");
  await page.locator("button.btn.btn-submit.w-100").click();
  await page.waitForTimeout(2000);

  // Search for it and make sure its there!
  await page
    .getByPlaceholder("Search listings by title or tags...")
    .fill(uniqueTitle);
  await page.keyboard.press("Enter");
  await page.waitForTimeout(2000);
  await expect(page.getByText(uniqueTitle)).toBeVisible();

  // verify it has the correct information
  await page.getByText(uniqueTitle).click();
  await expect(page.getByText("test tag1")).toBeVisible();
  await expect(page.getByText("test tag2")).toBeVisible();
  await expect(page.getByText("$4")).toBeVisible();

  // Edit the item
  await page.getByText("Edit").click();
  await page.locator('input[name="price"]').fill("5.00");
  await page.locator('textarea[name="description"]').fill("Edited Description");
  await page.getByText("Save changes").click();
  await page.waitForTimeout(2000);

  // Search for it and make sure its there!
  await page
    .getByPlaceholder("Search listings by title or tags...")
    .fill(uniqueTitle);
  await page.keyboard.press("Enter");
  await page.waitForTimeout(2000);
  await expect(page.getByText(uniqueTitle)).toBeVisible();

  // verify it has the correct information
  await page.getByText(uniqueTitle).click();
  await expect(page.getByText("$5")).toBeVisible();
  await expect(page.getByText("Edited Description")).toBeVisible();

  // Delete the item
  await page.getByText("Delete listing", { exact: true }).click();
  await expect(
    page.getByText(
      "Are you sure you want to delete this listing? This action cannot be undone."
    )
  ).toBeVisible();
  await page.locator("#confirm-delete-listing").click();
  await page.waitForTimeout(2000);
});

/* Search by price, category, and tags */
test("Searching by price, category, and tags works properly", async ({
  page,
}) => {
  await page.locator("button.create-listing").click();

  const uniqueTitle = `Test Item ${Date.now()}`;
  await page.locator('input[name="title"]').fill(uniqueTitle);

  await page.locator('textarea[name="description"]').fill("Description");
  await page.locator('input[name="price"]').fill("0.00");
  await page
    .locator('select[name="category"]')
    .selectOption({ label: "Other" });
  await page.locator('select[name="condition"]').selectOption({ label: "New" });
  await page.locator('input[name="tags"]').fill("tag1");
  await page.locator('input[name="tags"]').press("Enter");
  await page.locator('input[name="tags"]').fill("tag2");
  await page.locator('input[name="tags"]').press("Enter");
  await page
    .locator('input[type="file"][accept="image/*"]')
    .setInputFiles("./dummy.png");
  await page.locator("button.btn.btn-submit.w-100").click();
  await page.waitForTimeout(2000);

  // Search for free items
  await page.locator("#DropdownPrice").click();
  await page.getByText("Free").click();
  await page.waitForTimeout(2000);
  await expect(page.getByText(uniqueTitle)).toBeVisible();

  // Sort by High to Low -- item should not be visible
  await page.goto(url);
  await page.locator("#DropdownPrice").click();
  await page.getByText("High to Low").click();
  await page.waitForTimeout(3000);
  await expect.soft(page.getByText(uniqueTitle)).not.toBeVisible();

  // Sort by Low to High -- item should be visible
  await page.goto(url);
  await page.locator("#DropdownPrice").click();
  await page.getByText("Low to High").click();
  await page.waitForTimeout(2000);
  await expect(page.getByText(uniqueTitle)).toBeVisible();

  // Search bar -- search the tags
  await page.goto(url);
  await page
    .getByPlaceholder("Search listings by title or tags...")
    .fill("tag1");
  await page.keyboard.press("Enter");
  await page.waitForTimeout(2000);
  await expect(page.getByText(uniqueTitle)).toBeVisible();

  // Search by category
  await page.getByText("Categories").click();
  await page.getByText("Other").first().click();
  await page.waitForTimeout(2000);
  await expect(page.getByText(uniqueTitle)).toBeVisible();

  // Clear all filters makes the item not visible anymore
  await page.getByText("Clear All Filters").click();

  // Delete the item
  await page
    .getByPlaceholder("Search listings by title or tags...")
    .fill(uniqueTitle);
  await page.keyboard.press("Enter");
  await page.waitForTimeout(2000);
  await page.getByText(uniqueTitle).first().click();
  await page.getByText("Delete listing", { exact: true }).click();
  await expect(
    page.getByText(
      "Are you sure you want to delete this listing? This action cannot be undone."
    )
  ).toBeVisible();
  await page.locator("#confirm-delete-listing").click();
  await page.waitForTimeout(2000);
});

/* Go to profile, see listings, edit information, create/delete listings */
test("Editing profile information and creating/editing listings from profile works properly", async ({
  page,
}) => {
  // Go to profile
  await page.locator("a.user-name").click();
  await page.waitForTimeout(2000);

  // Check info is correct
  await expect(page.getByText("hello@brown.edu")).toBeVisible();
  await expect(page.getByText("Student").first()).toBeVisible();
  await expect(page.getByText("216-222-2121")).toBeVisible();

  // Edit profile info and check it changed
  await page.getByText("Edit Profile").click();
  await page.locator('input[name="name"]').fill("Student1");
  await page.getByText("Save Changes").click();
  await page.waitForTimeout(2000);
  await page.reload();
  await page.waitForTimeout(2000);
  await expect(page.getByText("Student1").first()).toBeVisible();

  // Change it back
  await page.getByText("Edit Profile").click();
  await page.locator('input[name="name"]').fill("Student");
  await page.getByText("Save Changes").click();
  await page.waitForTimeout(2000);
  await page.reload();
  await page.waitForTimeout(2000);

  // Create a listing from profile and check its there
  await page.locator("button.create-listing").click();
  const uniqueTitle = `Test Item ${Date.now()}`;
  await page.locator('input[name="title"]').fill(uniqueTitle);

  await page.locator('textarea[name="description"]').fill("Description");
  await page.locator('input[name="price"]').fill("4.00");
  await page
    .locator('select[name="category"]')
    .selectOption({ label: "Other" });
  await page.locator('select[name="condition"]').selectOption({ label: "New" });
  await page.locator('input[name="tags"]').fill("test tag1");
  await page.locator('input[name="tags"]').press("Enter");
  await page.locator('input[name="tags"]').fill("test tag2");
  await page.locator('input[name="tags"]').press("Enter");
  await page
    .locator('input[type="file"][accept="image/*"]')
    .setInputFiles("./dummy.png");
  await page.locator("button.btn.btn-submit.w-100").click();
  await page.waitForTimeout(2000);
  await page.goto(url);
  await page.locator("a.user-name").click();
  await page.waitForTimeout(2000);
  await expect(page.getByText(uniqueTitle)).toBeVisible();

  // Edit the listing from profile and check its correct
  await page
    .locator(`text=${uniqueTitle}`)
    .locator("..")
    .locator('button[title="Edit"]')
    .click();
  await page.locator('input[name="price"]').first().fill("5.00");
  const uniqueTitle2 = `Test Item ${Date.now()}`;
  await page.locator('input[name="title"]').first().fill(uniqueTitle2);
  await page.getByText("Save changes").click();
  await page.waitForTimeout(2000);

  await page.goto(url);
  await page.locator("a.user-name").click();
  await page.waitForTimeout(2000);
  await expect(page.getByText("$5.00").first()).toBeVisible();
  await expect(page.getByText(uniqueTitle2)).toBeVisible();

  // Delete the listing from profile
  await page
    .locator(`text=${uniqueTitle2}`)
    .locator("..")
    .locator('button[title="Delete"]')
    .click();
  await expect(
    page.getByText(
      "Are you sure you want to delete this listing? This action cannot be undone."
    )
  ).toBeVisible();
  await page.locator("#confirm-delete-listing").click();
  await page.waitForTimeout(2000);
  await page.reload();
  await page.waitForTimeout(2000);
  await page.locator("a.user-name").click();
  await expect(page.getByText(uniqueTitle2)).not.toBeVisible();
});

/* Mark as sold hides the item */
test("Marking a listing as sold hides it", async ({ page }) => {
  // Create a new listing
  await page.locator("button.create-listing").click();
  const uniqueTitle3 = `Test Item ${Date.now()}`;
  await page.locator('input[name="title"]').fill(uniqueTitle3);
  await page.locator('textarea[name="description"]').fill("Description");
  await page.locator('input[name="price"]').fill("10.00");
  await page
    .locator('select[name="category"]')
    .selectOption({ label: "Other" });
  await page
    .locator('select[name="condition"]')
    .selectOption({ label: "Like New" });
  await page.locator('input[name="tags"]').fill("test tag");
  await page.locator('input[name="tags"]').press("Enter");
  await page
    .locator('input[type="file"][accept="image/*"]')
    .setInputFiles("./dummy.png");
  await page.locator("button.btn.btn-submit.w-100").click();
  await page.waitForTimeout(2000);

  // Search for the listing and ensure it's there
  await page
    .getByPlaceholder("Search listings by title or tags...")
    .fill(uniqueTitle3);
  await page.keyboard.press("Enter");
  await page.waitForTimeout(3000);
  await expect(page.getByText(uniqueTitle3)).toBeVisible();

  // Mark the listing as sold
  await page.getByText(uniqueTitle3).click();
  await page.getByText("Mark as sold").click();
  await page.waitForTimeout(2000);

  // Verify the listing is no longer visible
  await page.goto(url);
  await page
    .getByPlaceholder("Search listings by title or tags...")
    .fill(uniqueTitle3);
  await page.keyboard.press("Enter");
  await page.waitForTimeout(2000);
  await expect(page.getByText("No Items Found")).toBeVisible();

  // Delete the listing
  await page.goto(url);
  await page.waitForTimeout(2000);
  await page.locator("a.user-name").click();
  await page.waitForTimeout(3000);
  await page
    .locator(`text=${uniqueTitle3}`)
    .locator("..")
    .locator('button[title="Delete"]')
    .click();
  await expect(
    page.getByText(
      "Are you sure you want to delete this listing? This action cannot be undone."
    )
  ).toBeVisible();
  await page.locator("#confirm-delete-listing").click();
  await page.waitForTimeout(2000);
});

/* Viewing seller profile */
test("Viewing seller profile works properly", async ({ page }) => {
  // Create a new listing
  await page.locator("button.create-listing").click();
  const uniqueTitle = `Test Item ${Date.now()}`;
  await page.locator('input[name="title"]').fill(uniqueTitle);
  await page.locator('textarea[name="description"]').fill("Description");
  await page.locator('input[name="price"]').fill("15.00");
  await page
    .locator('select[name="category"]')
    .selectOption({ label: "Electronics" });
  await page.locator('select[name="condition"]').selectOption({ label: "New" });
  await page.locator('input[name="tags"]').fill("seller test tag");
  await page.locator('input[name="tags"]').press("Enter");
  await page
    .locator('input[type="file"][accept="image/*"]')
    .setInputFiles("./dummy.png");
  await page.locator("button.btn.btn-submit.w-100").click();
  await page.waitForTimeout(2000);

  // Search for the listing and go to its page
  await page
    .getByPlaceholder("Search listings by title or tags...")
    .fill(uniqueTitle);
  await page.keyboard.press("Enter");
  await page.waitForTimeout(2000);
  await expect(page.getByText(uniqueTitle)).toBeVisible();
  await page.getByText(uniqueTitle).click();

  // Click on the profile link
  await page.getByText("View Full Profile").click();
  await page.waitForTimeout(2000);

  // Verify seller profile details
  await expect(page.getByText("hello@brown.edu").first()).toBeVisible();
  await expect(page.getByText("Student").first()).toBeVisible();
  await expect(page.getByText("Brown").first()).toBeVisible();
  await expect(page.getByText("216-222-2121")).toBeVisible();

  // Delete the listing 
  await page.locator("a.back-link").click();
  await expect(page.locator("div.homepage-listings")).toBeVisible();
  await page
  .getByPlaceholder("Search listings by title or tags...")
  .fill(uniqueTitle);
  await page.keyboard.press("Enter");
  await page.waitForTimeout(2000);
  await page.getByText(uniqueTitle).first().click();
  await page.getByText("Delete listing", { exact: true }).click();
  await expect(
    page.getByText(
      "Are you sure you want to delete this listing? This action cannot be undone."
    )
  ).toBeVisible();
  await page.locator("#confirm-delete-listing").click();
  await page.waitForTimeout(2000);

});

test("Copying seller's email works properly", async ({ page }) => {
  // Create a new listing
  await page.locator("button.create-listing").click();
  const uniqueTitle = `Test Item ${Date.now()}`;
  await page.locator('input[name="title"]').fill(uniqueTitle);
  await page.locator('textarea[name="description"]').fill("Test Description");
  await page.locator('input[name="price"]').fill("20.00");
  await page
    .locator('select[name="category"]')
    .selectOption({ label: "Other" });
  await page.locator('select[name="condition"]').selectOption({ label: "New" });
  await page.locator('input[name="tags"]').fill("email test");
  await page.locator('input[name="tags"]').press("Enter");
  await page
    .locator('input[type="file"][accept="image/*"]')
    .setInputFiles("./dummy.png");
  await page.locator("button.btn.btn-submit.w-100").click();
  await page.waitForTimeout(2000);

  // Search for the listing and go to its page
  await page
    .getByPlaceholder("Search listings by title or tags...")
    .fill(uniqueTitle);
  await page.keyboard.press("Enter");
  await page.waitForTimeout(2000);
  await expect(page.getByText(uniqueTitle)).toBeVisible();
  await page.getByText(uniqueTitle).click();

  // Copy seller's email
  const email = await page
    .locator(".seller-detail:has-text('Email') span")
    .innerText();
  await page.locator('button[title="Copy email address"]').click();

  // Verify clipboard content
  const clipboardText = await page.evaluate(() =>
    navigator.clipboard.readText()
  );
  expect(clipboardText).toBe(email);
});

/* Sign Out Redirect */
test("Sign out redirects to the sign-up screen", async ({ page }) => {
  // Ensure the user is signed in
  const signInButton = page.getByRole("button", { name: "Sign in" });
  const signOutButton = page.getByRole("button", { name: "Sign out" });

  await Promise.race([
    signInButton.waitFor({ timeout: 10000 }),
    signOutButton.waitFor({ timeout: 10000 }),
  ]);

  const isSignedOut = await signInButton.isVisible().catch(() => false);

  if (isSignedOut) {
    // If signed out, sign in
    await page.getByRole("button", { name: "Sign in" }).click();
    await page.getByLabel("Email address").fill("hello@brown.edu");
    await page.getByRole("button", { name: "Continue" }).click();
    await page.getByLabel("Password").fill("HelloGuys123!!");
    await page.getByRole("button", { name: "Continue" }).click();
  }

  // Sign out
  await page.getByRole("button", { name: "Sign out" }).click();

  // Verify the sign-up screen
  await expect(page.locator(".sign-up-container")).toBeVisible();
  await expect(page.locator("h2")).toHaveText("Bearly Used");
  await expect(page.locator("p")).toHaveText(
    "Sign in with your Brown/RISD email to access Bearly Used!"
  );
  await expect(page.getByRole("button", { name: "Sign In" })).toBeVisible();
});
