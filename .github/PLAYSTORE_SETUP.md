# GitHub Actions CI/CD & Google Play Deployment Guide

This repository includes two GitHub Actions workflows:
1. **Android CI & Test (`android-ci.yml`)**: Automatically triggers on pushes and pull requests to run unit tests, Android Lint, and build a debug APK.
2. **Publish to Google Play Closed Testing (`publish-closed-testing.yml`)**: Automatically triggers when you push a version tag (e.g. `v1.0.0`) or when triggered manually via the **Run workflow** button in GitHub Actions.

---

## 1. Required GitHub Repository Secrets

Go to your GitHub repository: **Settings > Secrets and variables > Actions > New repository secret** and add the following 4 secrets:

| Secret Name | Description | Example / How to generate |
|---|---|---|
| `RELEASE_KEYSTORE_BASE64` | Base64 string of your upload keystore file (`.jks`) | `base64 -w 0 my-upload-key.jks` |
| `STORE_PASSWORD` | Password for your keystore | The password set when creating the keystore |
| `KEY_PASSWORD` | Password for the key alias | The key password set when creating the alias |
| `KEY_ALIAS` *(Optional)* | Key alias in the keystore (defaults to `upload`) | `upload` or your custom alias name |
| `PLAY_STORE_JSON_KEY` | Full JSON content of the Google Play Developer API Service Account private key | Paste the full contents of your `.json` key file |

---

## 2. Generating your Release Keystore (One-time)

If you do not have an existing upload keystore, generate one locally:

```bash
keytool -genkey -v -keystore my-upload-key.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000
```

Then encode it as Base64 to add as `RELEASE_KEYSTORE_BASE64`:
- **macOS / Linux**:
  ```bash
  base64 -i my-upload-key.jks | tr -d '\n'
  ```
- **Windows PowerShell**:
  ```powershell
  [Convert]::ToBase64String([IO.File]::ReadAllBytes("my-upload-key.jks"))
  ```

---

## 3. Creating Google Play Service Account (for `PLAY_STORE_JSON_KEY`)

1. Open the [Google Cloud Console](https://console.cloud.google.com/) linked with your Google Play Console account.
2. Navigate to **IAM & Admin > Service Accounts > Create Service Account**.
3. Name it `play-store-publisher` and create it.
4. Under the newly created service account, go to the **Keys** tab > **Add Key > Create new key > JSON**.
5. Save the downloaded JSON file. Its entire text content is what you paste into `PLAY_STORE_JSON_KEY`.
6. Open the [Google Play Console](https://play.google.com/console):
   - Go to **API Access** (or **Users & Permissions**).
   - Find or invite the service account email (e.g. `play-store-publisher@<project>.iam.gserviceaccount.com`).
   - Grant permissions:
     - **Releases**: Manage release tracks, create and edit releases, upload APKs/AABs.
   - Click **Save**.

*Note: Google Play requires you to upload your very first AAB manually once through the Play Console web UI to create the initial release draft. Subsequent releases can then be fully automated via GitHub Actions.*

---

## 4. Running the Publish Workflow

You can trigger deployment in two ways:

### Option A: Manual Trigger (Recommended for testing)
1. Go to **Actions** tab in GitHub.
2. Select **Publish to Google Play Closed Testing** in the left sidebar.
3. Click **Run workflow**:
   - Choose Track: `closed` (or `internal`, `alpha`, `beta`)
   - Choose Status: `completed` (or `draft`)
4. The workflow will build, sign, and upload the `.aab` straight to your closed testing track.

### Option B: Push a Git Tag
Push any tag matching `v*`:
```bash
git tag v1.0.0
git push origin v1.0.0
```
This triggers the automated release pipeline to Closed Testing.
