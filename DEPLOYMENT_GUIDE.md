# Deployment Guide

## Recommended Option

Use Render as a web service.

This project is a Java web server that:

- serves the dashboard UI
- serves API endpoints
- reads project data from local resources

So it should be deployed as a backend web app, not a static site.

## What Was Prepared

The project is now deployment-ready with:

- `PORT` support in the Java server
- `0.0.0.0` binding for cloud hosting
- `Dockerfile`
- `.dockerignore`

Main server:

- `src/main/java/com/retailproject/RetailDashboardServer.java`

## Option 1: Deploy On Render

Official docs:

- https://render.com/docs/web-services
- https://render.com/docs/your-first-deploy

### Steps

1. Push `RetailProject` to GitHub.
2. Go to Render.
3. Click `New` -> `Web Service`.
4. Connect your GitHub repository.
5. Choose the `RetailProject` repo or root if this project is the repo.
6. Render should detect the `Dockerfile`.
7. Set these values:
   - Environment: `Docker`
   - Branch: your main branch
   - Region: nearest to you
8. Deploy.

Render will provide a public URL like:

```text
https://your-app-name.onrender.com
```

## Option 2: Deploy On Fly.io

Official docs:

- https://fly.io/docs/launch/deploy/
- https://fly.io/docs/flyctl/deploy/

### Steps

1. Install `flyctl`.
2. Login:

```bash
fly auth login
```

3. From the `RetailProject` folder run:

```bash
fly launch
```

4. Accept the generated config.
5. Deploy:

```bash
fly deploy
```

## Before You Deploy

Make sure:

- your project is pushed to GitHub
- the repo includes:
  - `Dockerfile`
  - `src/`
  - `start-dashboard.ps1` is optional for local use only

## After Deployment

Test:

- homepage loads
- filters work
- AI assistant responds
- trend chart interaction works

## My Recommendation

If you want the easiest deployment:

- use Render

If you want, the next step I can help with is:

- preparing the GitHub push steps
- helping you deploy to Render step by step
- or helping you add a custom domain later
