from pathlib import Path

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles

from app.db.bootstrap import ensure_schema, seed_demo_data
from app.db.database import Base, SessionLocal, engine
from app.routers import (
    access,
    analytics,
    auth,
    dashboard,
    meals,
    notifications,
    reports,
    settings,
    teams,
    transactions,
    users,
)

# Tables first, then the column top-ups create_all can't apply, then demo data.
Base.metadata.create_all(bind=engine)
ensure_schema()

with SessionLocal() as session:
    seed_demo_data(session)

app = FastAPI(title="NFC Payments & Meal Tracking API", version="0.2.0")

# The dashboard is served from this same origin, so CORS is only needed for a frontend opened
# straight off disk or run from a separate dev server. Prototype-wide open; tighten before deploy.
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=False,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(auth.router, prefix="/api/v1")
app.include_router(users.router, prefix="/api/v1")
app.include_router(meals.router, prefix="/api/v1")
app.include_router(transactions.router, prefix="/api/v1")
app.include_router(dashboard.router, prefix="/api/v1")
app.include_router(reports.router, prefix="/api/v1")
app.include_router(settings.router, prefix="/api/v1")
app.include_router(analytics.router, prefix="/api/v1")
app.include_router(access.router, prefix="/api/v1")
app.include_router(teams.router, prefix="/api/v1")
app.include_router(notifications.router, prefix="/api/v1")


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}


# Mounted last so it claims only the paths the API routes above didn't. html=True serves index.html
# at "/", which is the whole dashboard.
_FRONTEND = Path(__file__).resolve().parents[2] / "frontend"
if _FRONTEND.is_dir():
    app.mount("/", StaticFiles(directory=str(_FRONTEND), html=True), name="dashboard")
