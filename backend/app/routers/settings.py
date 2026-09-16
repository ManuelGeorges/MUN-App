from datetime import time

from fastapi import APIRouter, HTTPException

from app.schemas import MealType, MealWindowRequest
from app.store import MealWindow, STORE

router = APIRouter(prefix="/settings", tags=["settings"])


def _parse_time(value: str) -> time:
    try:
        hour, minute = value.split(":")
        return time(int(hour), int(minute))
    except ValueError as exc:
        raise HTTPException(status_code=400, detail="Invalid time format") from exc


@router.put("/meal-windows")
def update_meal_windows(payload: MealWindowRequest) -> dict[str, MealWindowRequest]:
    STORE.meal_windows[MealType.breakfast] = MealWindow(
        start=_parse_time(payload.breakfast_start),
        end=_parse_time(payload.breakfast_end),
    )
    STORE.meal_windows[MealType.lunch] = MealWindow(
        start=_parse_time(payload.lunch_start),
        end=_parse_time(payload.lunch_end),
    )
    return {"settings": payload}
